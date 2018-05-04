package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/2/26
 */
public interface GalleryService {

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return Gallery
     */
    Gallery saveByGallery(Gallery gallery);

    /**
     * 根据编号删除图片
     *
     * @param galleryId galleryId
     */
    Gallery removeByGalleryId(Long galleryId);

    /**
     * 修改图片信息
     *
     * @param gallery gallery
     * @return Gallery
     */
    Gallery updateByGallery(Gallery gallery);

    /**
     * 查询所有图片 分页
     *
     * @param pageable pageable
     * @return page
     */
    Page<Gallery> findAllGalleries(Pageable pageable);

    /**
     * 查询所有图片 不分页
     *
     * @return list
     */
    List<Gallery> findAllGalleries();

    /**
     * 根据编号查询图片信息
     *
     * @param galleryId galleryId
     * @return gallery
     */
    Optional<Gallery> findByGalleryId(Long galleryId);
}
