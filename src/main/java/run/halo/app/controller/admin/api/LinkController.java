package run.halo.app.controller.admin.api;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.LinkDTO;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Link;
import run.halo.app.model.params.LinkParam;
import run.halo.app.service.LinkService;

/**
 * Link Controller
 *
 * @author ryanwang
 * @date 2019-03-21
 */
@RestController
@RequestMapping("/api/admin/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    @ApiOperation("Lists links")
    public List<LinkDTO> listLinks(@SortDefault(sort = "team", direction = DESC) Sort sort) {
        return linkService.listDtos(sort.and(Sort.by(ASC, "priority")));
    }

    @GetMapping("{id:\\d+}")
    @ApiOperation("Gets link detail by id")
    public LinkDTO getBy(@PathVariable("id") Integer id) {
        return new LinkDTO().convertFrom(linkService.getById(id));
    }

    @PostMapping
    @ApiOperation("Creates a link")
    public LinkDTO createBy(@RequestBody @Valid LinkParam linkParam) {
        Link link = linkService.createBy(linkParam);
        return new LinkDTO().convertFrom(link);
    }

    @PutMapping("{id:\\d+}")
    @ApiOperation("Updates a link")
    public LinkDTO updateBy(@PathVariable("id") Integer id,
        @RequestBody @Valid LinkParam linkParam) {
        Link link = linkService.updateBy(id, linkParam);
        return new LinkDTO().convertFrom(link);
    }

    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete link by id")
    public void deletePermanently(@PathVariable("id") Integer id) {
        linkService.removeById(id);
    }

    @GetMapping("teams")
    @ApiOperation("Lists all link teams")
    public List<String> teams() {
        return linkService.listAllTeams();
    }

    /**
     * Update the links in batch.
     *
     * <p>To realize the draggable sort approach for link priority,
     * a links batch update API is in demand.
     *
     * @param linkParams the modified links params.
     * @return the links after updated.
     */
    @PutMapping("/batch")
    @ApiOperation("Updates links in batch")
    public List<LinkDTO> updateBatchBy(@RequestBody List<@Valid LinkParam> linkParams) {
        List<Link> links = linkParams
            .stream()
            .filter(linkParam -> Objects.nonNull(linkParam.getId()))
            .map(InputConverter::convertTo)
            .collect(Collectors.toList());
        return linkService.updateInBatch(links).stream()
            .map(link -> (LinkDTO) new LinkDTO().convertFrom(link))
            .collect(Collectors.toList());
    }
}
