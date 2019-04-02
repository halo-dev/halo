package cc.ryanc.halo.web.controller.portal.api;

import cc.ryanc.halo.model.vo.ArchiveMonthVO;
import cc.ryanc.halo.model.vo.ArchiveYearVO;
import cc.ryanc.halo.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Archive portal controller.
 *
 * @author johnniang
 * @date 4/2/19
 */
@RestController("PortalArchiveController")
@RequestMapping("/api/archives")
public class ArchiveController {

    private final PostService postService;

    public ArchiveController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("years")
    public List<ArchiveYearVO> listYearArchives() {
        return postService.listYearArchives();
    }

    @GetMapping("months")
    public List<ArchiveMonthVO> listMonthArchives() {
        return postService.listMonthArchives();
    }
}
