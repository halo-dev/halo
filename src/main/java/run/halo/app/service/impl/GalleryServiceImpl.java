package run.halo.app.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.model.dto.GalleryDTO;
import run.halo.app.model.entity.Gallery;
import run.halo.app.model.vo.GalleryTeamVO;
import run.halo.app.repository.GalleryRepository;
import run.halo.app.service.GalleryService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GalleryService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class GalleryServiceImpl extends AbstractCrudService<Gallery, Integer> implements GalleryService {

    private final GalleryRepository galleryRepository;

    public GalleryServiceImpl(GalleryRepository galleryRepository) {
        super(galleryRepository);
        this.galleryRepository = galleryRepository;
    }

    /**
     * List gallery dtos.
     *
     * @param sort sort
     * @return all galleries
     */
    @Override
    public List<GalleryDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return listAll(sort).stream().map(gallery -> (GalleryDTO) new GalleryDTO().convertFrom(gallery)).collect(Collectors.toList());
    }

    /**
     * Lists gallery team vos.
     *
     * @param sort must not be null
     * @return a list of gallery team vo
     */
    @Override
    public List<GalleryTeamVO> listTeamVos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all galleries
        List<GalleryDTO> galleries = listDtos(sort);

        // Get teams
        Set<String> teams = ServiceUtils.fetchProperty(galleries, GalleryDTO::getTeam);

        Map<String, List<GalleryDTO>> teamGalleryListMap = ServiceUtils.convertToListMap(teams, galleries, GalleryDTO::getTeam);

        List<GalleryTeamVO> result = new LinkedList<>();

        // Wrap gallery team vo list
        teamGalleryListMap.forEach((team, galleryList) -> {
            // Build gallery team vo
            GalleryTeamVO galleryTeamVO = new GalleryTeamVO();
            galleryTeamVO.setTeam(team);
            galleryTeamVO.setGalleries(galleryList);

            // Add it to result
            result.add(galleryTeamVO);
        });

        return result;
    }

    /**
     * List galleries by team.
     *
     * @param team team
     * @param sort sort
     * @return list of galleries
     */
    @Override
    public List<GalleryDTO> listByTeam(String team, Sort sort) {
        List<Gallery> galleries = galleryRepository.findByTeam(team, sort);
        return galleries.stream().map(gallery -> (GalleryDTO) new GalleryDTO().convertFrom(gallery)).collect(Collectors.toList());
    }
}
