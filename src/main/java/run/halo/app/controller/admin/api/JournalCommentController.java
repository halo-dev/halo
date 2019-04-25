package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.params.JournalCommentParam;
import run.halo.app.service.JournalCommentService;

/**
 * Journal comment controller.
 *
 * @author johnniang
 * @date 19-4-25
 */
@RestController
@RequestMapping("/api/admin/journals/comments")
public class JournalCommentController {

    private final JournalCommentService journalCommentService;

    public JournalCommentController(JournalCommentService journalCommentService) {
        this.journalCommentService = journalCommentService;
    }

    @PostMapping
    @ApiOperation("Creates a journal comment")
    public BaseCommentDTO createCommentBy(@RequestBody JournalCommentParam journalCommentParam) {
        JournalComment journalComment = journalCommentService.createBy(journalCommentParam);
        return journalCommentService.convertTo(journalComment);
    }

}
