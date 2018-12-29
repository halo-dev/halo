package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatusEnum;
import cc.ryanc.halo.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     图库API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/galleries")
public class ApiGalleryController {

    @Autowired
    private GalleryService galleryService;

    /**
     * 获取所有图片
     *
     * <p>
     *     result json:
     *     <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "galleryId": ,
     *             "galleryName": "",
     *             "galleryDesc": "",
     *             "galleryDate": "",
     *             "galleryLocation": "",
     *             "galleryThumbnailUrl": "",
     *             "galleryUrl": ""
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult galleries() {
        final List<Gallery> galleries = galleryService.findAll();
        if (null != galleries && galleries.size() > 0) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), galleries);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }

    /**
     * 获取单张图片的信息
     *
     * <p>
     *     result json:
     *     <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "galleryId": ,
     *             "galleryName": "",
     *             "galleryDesc": "",
     *             "galleryDate": "",
     *             "galleryLocation": "",
     *             "galleryThumbnailUrl": "",
     *             "galleryUrl": ""
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @param id id
     * @return JsonResult
     */
    @GetMapping(value = "/{id}")
    public JsonResult galleries(@PathVariable("id") Long id) {
        final Optional<Gallery> gallery = galleryService.findByGalleryId(id);
        if (gallery.isPresent()) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), gallery.get());
        } else {
            return new JsonResult(ResponseStatusEnum.NOTFOUND.getCode(), ResponseStatusEnum.NOTFOUND.getMsg());
        }
    }
}
