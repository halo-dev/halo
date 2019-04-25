package run.halo.app.service.impl;

import cn.hutool.core.lang.Assert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.JournalParam;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.repository.JournalRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ServiceUtils;
import run.halo.app.utils.ValidationUtils;

/**
 * Journal service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class JournalServiceImpl extends BaseCommentServiceImpl<Journal> implements JournalService {

    private final JournalRepository journalRepository;

    public JournalServiceImpl(JournalRepository journalRepository,
                              PostRepository postRepository,
                              OptionService optionService,
                              ApplicationEventPublisher eventPublisher) {
        super(journalRepository, postRepository, optionService, eventPublisher);
        this.journalRepository = journalRepository;
    }

    @Override
    public Journal createBy(JournalParam journalParam) {
        Assert.notNull(journalParam, "Journal param must not be null");

        // Check user login status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Get user detail
            User user = authentication.getDetail().getUser();
            // Set some default value
            journalParam.setAuthor(StringUtils.isBlank(user.getNickname()) ? user.getUsername() : user.getNickname());
            journalParam.setAuthorUrl(optionService.getByPropertyOfNullable(BlogProperties.BLOG_URL));
            journalParam.setEmail(user.getEmail());
        } else {
            // Guest comment
            if (ServiceUtils.isEmptyId(journalParam.getParentId())) {
                throw new ForbiddenException("You have no right to create a journal");
            }
        }

        // Validate the journal param
        ValidationUtils.validate(journalParam);

        // Convert, create and return
        return createBy(journalParam.convertTo());
    }

    @Override
    public Page<Journal> pageBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        return journalRepository.findAllByParentId(0L, pageable);
    }

    @Override
    public Page<Journal> pageLatest(int top) {
        return pageBy(buildLatestPageable(top));
    }

}
