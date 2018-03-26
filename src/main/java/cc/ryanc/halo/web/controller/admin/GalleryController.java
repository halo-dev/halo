package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.service.GalleryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : RYAN0UP
 * @date : 2018/2/26
 * @version : 1.0
 * description :
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @GetMapping
    public String gallery(){
        return "";
    }
}
