package run.halo.app.controller.admin.api;

import run.halo.app.model.dto.GalleryDTO;
import run.halo.app.service.GalleryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gallery controller
 *
 * @author : RYAN0UP
 * @date : 2019/3/21
 */
@RestController
@RequestMapping("/api/admin/galleries")
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
    public List<GalleryDTO> listGalleries(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return galleryService.listDtos(sort);
    }

    /**
     * Get gallery by id.
     *
     * @param galleryId gallery id
     * @return GalleryDTO
     */
    @GetMapping("{galleryId:\\d+}")
    @ApiOperation("Get gallery detail by id")
    public GalleryDTO getBy(@PathVariable("galleryId") Integer galleryId) {
        return new GalleryDTO().convertFrom(galleryService.getById(galleryId));
    }

    /**
     * Delete gallery by id.
     *
     * @param galleryId gallery id
     */
    @DeleteMapping("{galleryId:\\d+}")
    @ApiOperation("Delete gallery by id")
    public void deletePermanently(@PathVariable("galleryId") Integer galleryId) {
        galleryService.removeById(galleryId);
    }
}
