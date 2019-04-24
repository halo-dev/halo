package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.GalleryDTO;
import run.halo.app.model.entity.Gallery;
import run.halo.app.model.params.GalleryParam;
import run.halo.app.model.params.GalleryQuery;
import run.halo.app.service.GalleryService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

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
    @GetMapping(value = "latest")
    public List<GalleryDTO> listGalleries(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return galleryService.listDtos(sort);
    }

    @GetMapping
    public Page<GalleryDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                   GalleryQuery galleryQuery) {
        return galleryService.pageDtosBy(pageable, galleryQuery);
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

    @PostMapping
    public Gallery createBy(@Valid @RequestBody GalleryParam galleryParam) {
        return galleryService.createBy(galleryParam);
    }
}
