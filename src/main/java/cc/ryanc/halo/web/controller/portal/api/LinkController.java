package cc.ryanc.halo.web.controller.portal.api;

import cc.ryanc.halo.model.vo.LinkTeamVO;
import cc.ryanc.halo.service.LinkService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Portal link controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController("PortalLinkController")
@RequestMapping("/api/links")
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
