package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.repository.GalleryRepository;
import cc.ryanc.halo.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     图库业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/2/26
 */
@Service
public class GalleryServiceImpl implements GalleryService {

    private static final String GALLERIES_CACHE_NAME = "galleries";

    @Autowired
    private GalleryRepository galleryRepository;

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return Gallery
     */
    @Override
    @CacheEvict(value = GALLERIES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Gallery saveByGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    /**
     * 根据编号删除图片
     *
     * @param galleryId galleryId
     * @return Gallery
     */
    @Override
    @CacheEvict(value = GALLERIES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Gallery removeByGalleryId(Long galleryId) {
        Optional<Gallery> gallery = this.findByGalleryId(galleryId);
        galleryRepository.delete(gallery.get());
        return gallery.get();
    }

    /**
     * 修改图片信息
     *
     * @param gallery gallery
     * @return Gallery
     */
    @Override
    @CacheEvict(value = GALLERIES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Gallery updateByGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    /**
     * 查询所有图片 分页
     *
     * @param pageable pageable
     * @return Page
     */
    @Override
    public Page<Gallery> findAllGalleries(Pageable pageable) {
        return galleryRepository.findAll(pageable);
    }

    /**
     * 查询所有图片 不分页
     *
     * @return List
     */
    @Override
    @Cacheable(value = GALLERIES_CACHE_NAME, key = "'gallery'")
    public List<Gallery> findAllGalleries() {
        return galleryRepository.findAll();
    }

    /**
     * 根据编号查询图片信息
     *
     * @param galleryId galleryId
     * @return Optional
     */
    @Override
    public Optional<Gallery> findByGalleryId(Long galleryId) {
        return galleryRepository.findById(galleryId);
    }
}
