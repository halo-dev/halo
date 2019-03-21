package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.GalleryOutputDTO;
import cc.ryanc.halo.model.entity.Gallery;
import cc.ryanc.halo.repository.GalleryRepository;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
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
    public List<GalleryOutputDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return listAll(sort).stream().map(gallery -> (GalleryOutputDTO) new GalleryOutputDTO().convertFrom(gallery)).collect(Collectors.toList());
    }
}
