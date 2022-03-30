package run.halo.app.service.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.JournalWithCmtCountDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.JournalType;
import run.halo.app.model.params.JournalParam;
import run.halo.app.model.params.JournalQuery;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.JournalCommentService;
import run.halo.app.service.JournalService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

/**
 * Journal service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
@Slf4j
@Service
public class JournalServiceImpl extends AbstractCrudService<Journal, Integer>
    implements JournalService {

    private final JournalRepository journalRepository;

    private final JournalCommentService journalCommentService;

    public JournalServiceImpl(JournalRepository journalRepository,
        JournalCommentService journalCommentService) {
        super(journalRepository);
        this.journalRepository = journalRepository;
        this.journalCommentService = journalCommentService;
    }

    @Override
    public Journal createBy(JournalParam journalParam) {
        Assert.notNull(journalParam, "Journal param must not be null");

        Journal journal = journalParam.convertTo();

        return create(journal);
    }

    @Override
    public Journal updateBy(Journal journal) {
        Assert.notNull(journal, "Journal must not be null");
        return update(journal);
    }

    @Override
    public Page<Journal> pageLatest(int top) {
        return listAll(ServiceUtils.buildLatestPageable(top));
    }

    @Override
    public Page<Journal> pageBy(JournalQuery journalQuery, Pageable pageable) {
        Assert.notNull(journalQuery, "Journal query must not be null");
        Assert.notNull(pageable, "Page info must not be null");
        return journalRepository.findAll(buildSpecByQuery(journalQuery), pageable);
    }

    @Override
    public Page<Journal> pageBy(JournalType type, Pageable pageable) {
        Assert.notNull(type, "Journal type must not be null");
        Assert.notNull(pageable, "Page info must not be null");
        return journalRepository.findAllByType(type, pageable);
    }

    @Override
    public Journal removeById(Integer id) {
        Assert.notNull(id, "Journal id must not be null");

        // Remove journal comments
        List<JournalComment> journalComments = journalCommentService.removeByPostId(id);
        log.debug("Removed journal comments: [{}]", journalComments);

        return super.removeById(id);
    }

    @Override
    public JournalWithCmtCountDTO convertTo(Journal journal) {
        Assert.notNull(journal, "Journal must not be null");

        JournalWithCmtCountDTO journalWithCmtCountDto = new JournalWithCmtCountDTO()
            .convertFrom(journal);

        journalWithCmtCountDto.setCommentCount(journalCommentService.countByStatusAndPostId(
            CommentStatus.PUBLISHED, journal.getId()));

        return journalWithCmtCountDto;
    }

    @Override
    public List<JournalWithCmtCountDTO> convertToCmtCountDto(List<Journal> journals) {
        if (CollectionUtils.isEmpty(journals)) {
            return Collections.emptyList();
        }

        // Get journal ids
        Set<Integer> journalIds = ServiceUtils.fetchProperty(journals, Journal::getId);

        // Get comment count map
        Map<Integer, Long> journalCommentCountMap =
            journalCommentService.countByStatusAndPostIds(CommentStatus.PUBLISHED, journalIds);

        return journals.stream()
            .map(journal -> {
                JournalWithCmtCountDTO journalWithCmtCountDTO =
                    new JournalWithCmtCountDTO().convertFrom(journal);
                // Set comment count
                journalWithCmtCountDTO
                    .setCommentCount(journalCommentCountMap.getOrDefault(journal.getId(), 0L));
                return journalWithCmtCountDTO;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Page<JournalWithCmtCountDTO> convertToCmtCountDto(Page<Journal> journalPage) {
        Assert.notNull(journalPage, "Journal page must not be null");

        // Convert
        List<JournalWithCmtCountDTO> journalWithCmtCountDTOS =
            convertToCmtCountDto(journalPage.getContent());

        // Build and return
        return new PageImpl<>(journalWithCmtCountDTOS, journalPage.getPageable(),
            journalPage.getTotalElements());
    }

    @Override
    @Transactional
    public void increaseLike(Integer id) {
        increaseLike(1L, id);
    }


    @Override
    @Transactional
    public void increaseLike(long likes, Integer id) {
        Assert.isTrue(likes > 0, "Likes to increase must not be less than 1");
        Assert.notNull(id, "Journal id must not be null");

        long affectedRows = journalRepository.updateLikes(likes, id);

        if (affectedRows != 1) {
            log.error("Journal with id: [{}] may not be found", id);
            throw new BadRequestException(
                "Failed to increase likes " + likes + " for journal with id " + id);
        }
    }

    /**
     * Build specification by journal query.
     *
     * @param journalQuery query query must not be null
     * @return a query specification
     */
    @NonNull
    private Specification<Journal> buildSpecByQuery(@NonNull JournalQuery journalQuery) {
        Assert.notNull(journalQuery, "Journal query must not be null");

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (journalQuery.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), journalQuery.getType()));
            }

            if (journalQuery.getKeyword() != null) {
                // Format like condition
                String likeCondition =
                    String.format("%%%s%%", StringUtils.strip(journalQuery.getKeyword()));

                // Build like predicate
                Predicate contentLike = criteriaBuilder.like(root.get("content"), likeCondition);

                predicates.add(criteriaBuilder.or(contentLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }
}
