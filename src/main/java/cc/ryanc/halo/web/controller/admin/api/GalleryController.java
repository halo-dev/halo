package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.GalleryOutputDTO;
import cc.ryanc.halo.service.GalleryService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gallery controller
 *
 * @author : RYAN0UP
 * @date : 2019/3/21
 */
@RestController
@RequestMapping("/admin/api/galleries")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    /**
     * List all galleries
     *
     * @param sort sort
     * @return all of galleries
     */
    @GetMapping
    public List<GalleryOutputDTO> listGalleries(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return galleryService.listDtos(sort);
    }
}
