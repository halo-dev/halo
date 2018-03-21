package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.repository.GalleryRepository;
import cc.ryanc.halo.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/2/26
 * description :
 */
@Service
public class GalleryServiceImpl implements GalleryService{

    @Autowired
    private GalleryRepository galleryRepository;

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return Gallery
     */
    @Override
    public Gallery saveByGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    /**
     * 根据编号删除图片
     *
     * @param galleryId galleryId
     */
    @Override
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
    public Gallery updateByGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    /**
     * 查询所有图片 分页
     *
     * @param pageable pageable
     * @return page
     */
    @Override
    public Page<Gallery> findAllGalleries(Pageable pageable) {
        return galleryRepository.findAll(pageable);
    }

    /**
     * 查询所有图片 不分页
     *
     * @return list
     */
    @Override
    public List<Gallery> findAllGalleries() {
        return galleryRepository.findAll();
    }

    /**
     * 根据编号查询图片信息
     *
     * @param galleryId galleryId
     * @return gallery
     */
    @Override
    public Optional<Gallery> findByGalleryId(Long galleryId) {
        return galleryRepository.findById(galleryId);
    }
}
