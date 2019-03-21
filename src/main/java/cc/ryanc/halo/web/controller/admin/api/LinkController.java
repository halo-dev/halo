package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.LinkOutputDTO;
import cc.ryanc.halo.service.LinkService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Link Controller
 *
 * @author : RYAN0UP
 * @date : 2019/3/21
 */
@RestController
@RequestMapping(value = "/admin/api/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    /**
     * List all links
     *
     * @param sort sort
     * @return List
     */
    @GetMapping
    public List<LinkOutputDTO> listLinks(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return linkService.listDtos(sort);
    }
}
