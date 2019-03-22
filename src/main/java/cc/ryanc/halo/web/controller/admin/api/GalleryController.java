package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.GalleryOutputDTO;
import cc.ryanc.halo.service.GalleryService;
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

    /**
     * Get gallery by id.
     *
     * @param id gallery id
     * @return GalleryOutputDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get gallery detail by id")
    public GalleryOutputDTO getBy(@PathVariable("id") Integer id) {
        return new GalleryOutputDTO().convertFrom(galleryService.getById(id));
    }

    /**
     * Delete gallery by id.
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete gallery by id")
    public void deletePermanently(@PathVariable("id") Integer id) {
        galleryService.removeById(id);
    }
}
