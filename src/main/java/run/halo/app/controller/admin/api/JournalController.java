package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.JournalDTO;
import run.halo.app.model.dto.JournalWithCmtCountDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.params.JournalParam;
import run.halo.app.model.params.JournalQuery;
import run.halo.app.service.JournalService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Journal controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-25
 */
@RestController
@RequestMapping("/api/admin/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    @ApiOperation("Lists journals")
    public Page<JournalWithCmtCountDTO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
            JournalQuery journalQuery) {
        Page<Journal> journalPage = journalService.pageBy(journalQuery, pageable);
        return journalService.convertToCmtCountDto(journalPage);
    }

    @GetMapping("latest")
    @ApiOperation("Gets latest journals")
    public List<JournalWithCmtCountDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        List<Journal> journals = journalService.pageLatest(top).getContent();
        return journalService.convertToCmtCountDto(journals);
    }

    @PostMapping
    @ApiOperation("Creates a journal")
    public JournalDTO createBy(@RequestBody @Valid JournalParam journalParam) {
        Journal createdJournal = journalService.createBy(journalParam);
        return journalService.convertTo(createdJournal);
    }

    @PutMapping("{id:\\d+}")
    @ApiOperation("Updates a Journal")
    public JournalDTO updateBy(@PathVariable("id") Integer id,
            @RequestBody @Valid JournalParam journalParam) {
        Journal journal = journalService.getById(id);
        journalParam.update(journal);
        Journal updatedJournal = journalService.updateBy(journal);
        return journalService.convertTo(updatedJournal);
    }

    @DeleteMapping("{journalId:\\d+}")
    @ApiOperation("Delete journal")
    public JournalDTO deleteBy(@PathVariable("journalId") Integer journalId) {
        Journal deletedJournal = journalService.removeById(journalId);
        return journalService.convertTo(deletedJournal);
    }
}
