package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Gallery;
import cc.ryanc.halo.repository.GalleryRepository;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

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
}
