package run.halo.app.controller.admin.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.cache.lock.CacheParam;
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
@Validated
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

    @DeleteMapping("/batch")
    @ApiOperation("Deletes photos permanently in batch by id array")
    public List<PhotoDTO> deletePermanentlyInBatch(@RequestBody List<Integer> ids) {
        return ids.stream().map(photoService::removeById)
            .map(photo -> (PhotoDTO) new PhotoDTO().convertFrom(photo))
            .collect(Collectors.toList());
    }

    @PostMapping
    @ApiOperation("Creates a photo")
    public PhotoDTO createBy(@Valid @RequestBody PhotoParam photoParam) {
        return new PhotoDTO().convertFrom(photoService.createBy(photoParam));
    }

    @PostMapping("/batch")
    @ApiOperation("Batch creation photos")
    public List<PhotoDTO> createBatchBy(@RequestBody List<@Valid PhotoParam> photoParams) {
        return photoParams.stream()
            .map(photoParam -> {
                PhotoDTO photoDto = new PhotoDTO();
                photoDto.convertFrom(photoService.createBy(photoParam));
                return photoDto;
            })
            .collect(Collectors.toList());
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

    @PutMapping("/batch")
    @ApiOperation("Updates photo in batch")
    public List<PhotoDTO> updateBatchBy(@RequestBody List<@Valid PhotoParam> photoParams) {
        List<Photo> photosToUpdate = photoParams.stream()
            .filter(photoParam -> Objects.nonNull(photoParam.getId()))
            .map(photoParam -> {
                Photo photoToUpdate = photoService.getById(photoParam.getId());
                photoParam.update(photoToUpdate);
                return photoToUpdate;
            })
            .collect(Collectors.toList());
        return photoService.updateInBatch(photosToUpdate).stream()
            .map(photo -> (PhotoDTO) new PhotoDTO().convertFrom(photo))
            .collect(Collectors.toList());
    }

    @PutMapping("{photoId:\\d+}/likes")
    @ApiOperation("Likes a photo")
    @CacheLock(autoDelete = false, traceRequest = true)
    public void likes(@PathVariable @CacheParam Integer photoId) {
        photoService.increaseLike(photoId);
    }

    @GetMapping("teams")
    @ApiOperation("Lists all of photo teams")
    public List<String> listTeams() {
        return photoService.listAllTeams();
    }
}
