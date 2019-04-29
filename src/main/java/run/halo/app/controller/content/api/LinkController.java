package run.halo.app.controller.content.api;

import run.halo.app.model.vo.LinkTeamVO;
import run.halo.app.service.LinkService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.vo.LinkTeamVO;
import run.halo.app.service.LinkService;

import java.util.List;

/**
 * Portal link controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController("ApiContentLinkController")
@RequestMapping("/api/content/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("team_view")
    public List<LinkTeamVO> listTeamVos(Sort sort) {
        return linkService.listTeamVos(sort);
    }
}
