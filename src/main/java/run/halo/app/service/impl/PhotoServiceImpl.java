package run.halo.app.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.PhotoDTO;
import run.halo.app.model.entity.Photo;
import run.halo.app.model.params.PhotoParam;
import run.halo.app.model.params.PhotoQuery;
import run.halo.app.model.vo.PhotoTeamVO;
import run.halo.app.repository.PhotoRepository;
import run.halo.app.service.PhotoService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

/**
 * PhotoService implementation class.
 *
 * @author ryanwang
 * @author guqing
 * @date 2019-03-14
 */
@Slf4j
@Service
public class PhotoServiceImpl extends AbstractCrudService<Photo, Integer> implements PhotoService {

    private final PhotoRepository photoRepository;

    public PhotoServiceImpl(PhotoRepository photoRepository) {
        super(photoRepository);
        this.photoRepository = photoRepository;
    }

    @Override
    public List<PhotoDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return listAll(sort).stream().map(photo -> (PhotoDTO) new PhotoDTO().convertFrom(photo))
            .collect(Collectors.toList());
    }

    @Override
    public List<PhotoTeamVO> listTeamVos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all photos
        List<PhotoDTO> photos = listDtos(sort);

        // Get teams
        Set<String> teams = ServiceUtils.fetchProperty(photos, PhotoDTO::getTeam);

        Map<String, List<PhotoDTO>> teamPhotoListMap =
            ServiceUtils.convertToListMap(teams, photos, PhotoDTO::getTeam);

        List<PhotoTeamVO> result = new LinkedList<>();

        // Wrap photo team vo list
        teamPhotoListMap.forEach((team, photoList) -> {
            // Build photo team vo
            PhotoTeamVO photoTeamVO = new PhotoTeamVO();
            photoTeamVO.setTeam(team);
            photoTeamVO.setPhotos(photoList);

            // Add it to result
            result.add(photoTeamVO);
        });

        return result;
    }

    @Override
    public List<PhotoDTO> listByTeam(String team, Sort sort) {
        List<Photo> photos = photoRepository.findByTeam(team, sort);
        return photos.stream().map(photo -> (PhotoDTO) new PhotoDTO().convertFrom(photo))
            .collect(Collectors.toList());
    }

    @Override
    public Page<PhotoDTO> pageBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        Page<Photo> photos = photoRepository.findAll(pageable);

        return photos.map(photo -> new PhotoDTO().convertFrom(photo));
    }

    @Override
    public Page<PhotoDTO> pageDtosBy(Pageable pageable, PhotoQuery photoQuery) {
        Assert.notNull(pageable, "Page info must not be null");

        // List all
        Page<Photo> photoPage = photoRepository.findAll(buildSpecByQuery(photoQuery), pageable);

        // Convert and return
        return photoPage.map(photo -> new PhotoDTO().convertFrom(photo));
    }

    @Override
    public Photo createBy(PhotoParam photoParam) {
        Assert.notNull(photoParam, "Photo param must not be null");

        return create(photoParam.convertTo());
    }

    @Override
    public List<String> listAllTeams() {
        return photoRepository.findAllTeams();
    }

    @Override
    @Transactional
    public void increaseLike(Integer photoId) {
        Assert.notNull(photoId, "Photo id must not be null");

        int affectedRows = photoRepository.updateLikes(1L, photoId);

        if (affectedRows != 1) {
            log.error("Photo with id: [{}] may not be found", photoId);
            throw new BadRequestException(
                "Failed to increase likes 1 for photo with id " + photoId);
        }
    }

    @NonNull
    private Specification<Photo> buildSpecByQuery(@NonNull PhotoQuery photoQuery) {
        Assert.notNull(photoQuery, "Photo query must not be null");

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (photoQuery.getTeam() != null) {
                predicates.add(criteriaBuilder.equal(root.get("team"), photoQuery.getTeam()));
            }

            if (photoQuery.getKeyword() != null) {

                String likeCondition =
                    String.format("%%%s%%", StringUtils.strip(photoQuery.getKeyword()));

                Predicate nameLike = criteriaBuilder.like(root.get("name"), likeCondition);
                Predicate descriptionLike =
                    criteriaBuilder.like(root.get("description"), likeCondition);
                Predicate locationLike = criteriaBuilder.like(root.get("location"), likeCondition);

                predicates.add(criteriaBuilder.or(nameLike, descriptionLike, locationLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }
}
