package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.LinkDTO;
import run.halo.app.model.entity.Link;
import run.halo.app.model.params.LinkParam;
import run.halo.app.service.LinkService;

import javax.validation.Valid;
import java.util.List;

/**
 * Link Controller
 *
 * @author ryanwang
 * @date : 2019/3/21
 */
@RestController
@RequestMapping("/api/admin/links")
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
    public List<LinkDTO> listLinks(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return linkService.listDtos(sort);
    }

    /**
     * Get link by id.
     *
     * @param id id
     * @return LinkDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get link detail by id")
    public LinkDTO getBy(@PathVariable("id") Integer id) {
        return new LinkDTO().convertFrom(linkService.getById(id));
    }

    @PostMapping
    public LinkDTO createBy(@RequestBody @Valid LinkParam linkParam) {
        Link link = linkService.createBy(linkParam);
        return new LinkDTO().convertFrom(link);
    }

    @PutMapping("{id:\\d+}")
    @ApiOperation("Updates a link")
    public LinkDTO updateBy(@PathVariable("id") Integer id,
                            @RequestBody @Valid LinkParam linkParam) {
        Link link = linkService.getById(id);
        linkParam.update(link);
        return new LinkDTO().convertFrom(linkService.update(link));
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
