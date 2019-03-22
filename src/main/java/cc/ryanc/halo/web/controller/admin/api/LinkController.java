package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.LinkOutputDTO;
import cc.ryanc.halo.service.LinkService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Get link by id.
     *
     * @param id id
     * @return LinkOutputDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get link detail by id")
    public LinkOutputDTO getBy(@PathVariable("id") Integer id) {
        return new LinkOutputDTO().convertFrom(linkService.getById(id));
    }

    /**
     * Delete link by id.
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete link by id")
    public void deletePermanently(@PathVariable("id") Integer id) {
        linkService.removeById(id);
    }
}
