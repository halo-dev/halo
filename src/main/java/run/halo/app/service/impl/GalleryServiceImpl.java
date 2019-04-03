package run.halo.app.service.impl;

import run.halo.app.model.dto.GalleryOutputDTO;
import run.halo.app.model.entity.Gallery;
import run.halo.app.repository.GalleryRepository;
import run.halo.app.service.GalleryService;
import run.halo.app.service.base.AbstractCrudService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.repository.GalleryRepository;
import run.halo.app.service.base.AbstractCrudService;

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
