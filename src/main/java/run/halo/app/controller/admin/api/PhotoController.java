package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.PhotoDTO;
import run.halo.app.model.entity.Photo;
import run.halo.app.model.params.PhotoParam;
import run.halo.app.model.params.PhotoQuery;
import run.halo.app.service.PhotoService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Photo controller
 *
 * @author ryanwang
 * @date : 2019/3/21
 */
@RestController
@RequestMapping("/api/admin/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * List all photos
     *
     * @param sort sort
     * @return all of photos
     */
    @GetMapping(value = "latest")
    public List<PhotoDTO> listPhotos(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return photoService.listDtos(sort);
    }

    @GetMapping
    public Page<PhotoDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                 PhotoQuery photoQuery) {
        return photoService.pageDtosBy(pageable, photoQuery);
    }

    /**
     * Get photo by id.
     *
     * @param photoId photo id
     * @return PhotoDTO
     */
    @GetMapping("{photoId:\\d+}")
    @ApiOperation("Get photo detail by id")
    public PhotoDTO getBy(@PathVariable("photoId") Integer photoId) {
        return new PhotoDTO().convertFrom(photoService.getById(photoId));
    }

    /**
     * Delete photo by id.
     *
     * @param photoId photo id
     */
    @DeleteMapping("{photoId:\\d+}")
    @ApiOperation("Delete photo by id")
    public void deletePermanently(@PathVariable("photoId") Integer photoId) {
        photoService.removeById(photoId);
    }

    @PostMapping
    public PhotoDTO createBy(@Valid @RequestBody PhotoParam photoParam) {
        return new PhotoDTO().convertFrom(photoService.createBy(photoParam));
    }

    @PutMapping("{photoId:\\d+}")
    @ApiOperation("Updates a photo")
    public PhotoDTO updateBy(@PathVariable("photoId") Integer photoId,
                             @RequestBody @Valid PhotoParam photoParam) {
        // Get the photo
        Photo photo = photoService.getById(photoId);

        // Update changed properties of the photo
        photoParam.update(photo);

        // Update menu in database
        return new PhotoDTO().convertFrom(photoService.update(photo));
    }
}
