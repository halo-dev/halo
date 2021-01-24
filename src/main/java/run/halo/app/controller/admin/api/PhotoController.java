package run.halo.app.controller.admin.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.PhotoDTO;
import run.halo.app.model.entity.Photo;
import run.halo.app.model.params.PhotoParam;
import run.halo.app.model.params.PhotoQuery;
import run.halo.app.service.PhotoService;

/**
 * Photo controller
 *
 * @author ryanwang
 * @date 2019-03-21
 */
@RestController
@RequestMapping("/api/admin/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping(value = "latest")
    @ApiOperation("Lists latest photos")
    public List<PhotoDTO> listPhotos(
        @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort) {
        return photoService.listDtos(sort);
    }

    @GetMapping
    @ApiOperation("Lists photos")
    public Page<PhotoDTO> pageBy(
        @PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
        PhotoQuery photoQuery) {
        return photoService.pageDtosBy(pageable, photoQuery);
    }

    @GetMapping("{photoId:\\d+}")
    @ApiOperation("Gets photo detail by id")
    public PhotoDTO getBy(@PathVariable("photoId") Integer photoId) {
        return new PhotoDTO().convertFrom(photoService.getById(photoId));
    }

    @DeleteMapping("{photoId:\\d+}")
    @ApiOperation("Deletes photo by id")
    public void deletePermanently(@PathVariable("photoId") Integer photoId) {
        photoService.removeById(photoId);
    }

    @PostMapping
    @ApiOperation("Creates a photo")
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

    @GetMapping("teams")
    @ApiOperation("Lists all of photo teams")
    public List<String> listTeams() {
        return photoService.listAllTeams();
    }
}
