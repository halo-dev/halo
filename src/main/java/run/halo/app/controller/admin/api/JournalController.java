package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.params.JournalParam;
import run.halo.app.service.JournalService;

import java.util.List;

/**
 * Journal controller.
 *
 * @author johnniang
 * @date 19-4-25
 */
@RestController
@RequestMapping("/api/admin/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("latest")
    @ApiOperation("Gets latest journals")
    public List<BaseCommentDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        List<Journal> journals = journalService.pageLatest(top).getContent();
        return journalService.convertTo(journals);
    }

    @PostMapping
    @ApiOperation("Creates a journal")
    public BaseCommentDTO createBy(@RequestBody JournalParam journalParam) {
        Journal createdJournal = journalService.createBy(journalParam);
        return journalService.convertTo(createdJournal);
    }

}
