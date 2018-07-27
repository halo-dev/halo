package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     图库业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
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
     * @return Gallery
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
     * @return Page
     */
    Page<Gallery> findAllGalleries(Pageable pageable);

    /**
     * 查询所有图片 不分页
     *
     * @return List
     */
    List<Gallery> findAllGalleries();

    /**
     * 根据编号查询图片信息
     *
     * @param galleryId galleryId
     * @return Optional
     */
    Optional<Gallery> findByGalleryId(Long galleryId);
}
