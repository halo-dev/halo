package run.halo.app.web.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.dto.AttachmentOutputDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.params.AttachmentParam;
import run.halo.app.model.params.AttachmentQuery;
import run.halo.app.service.AttachmentService;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Attachment controller.
 *
 * @author johnniang
 * @date 3/21/19
 */
@RestController
@RequestMapping("/api/admin/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * List of attachment.
     *
     * @param pageable pageable
     * @return Page<AttachmentOutputDTO>
     */
    @GetMapping
    public Page<AttachmentOutputDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                            AttachmentQuery attachmentQuery) {
        return attachmentService.pageDtosBy(pageable, attachmentQuery);
    }

    /**
     * Get attachment by id.
     *
     * @param id attachment id
     * @return AttachmentOutputDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get attachment detail by id")
    public AttachmentOutputDTO getBy(@PathVariable("id") Integer id) {
        Attachment attachment = attachmentService.getById(id);
        return attachmentService.convertToDto(attachment);
    }

    @PutMapping("{attachmentId:\\d+}")
    @ApiOperation("Updates a attachment")
    public AttachmentOutputDTO updateBy(@PathVariable("attachmentId") Integer attachmentId,
                                        @RequestBody @Valid AttachmentParam attachmentParam) {
        Attachment attachment = attachmentService.getById(attachmentId);
        attachmentParam.update(attachment);
        return new AttachmentOutputDTO().convertFrom(attachmentService.update(attachment));
    }

    /**
     * Delete attachment by id
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete attachment by id")
    public AttachmentOutputDTO deletePermanently(@PathVariable("id") Integer id) {
        return attachmentService.convertToDto(attachmentService.removePermanently(id));
    }

    @PostMapping("upload")
    @ApiOperation("Uploads single file")
    public AttachmentOutputDTO uploadAttachment(@RequestPart("file") MultipartFile file) {
        return attachmentService.convertToDto(attachmentService.upload(file));
    }

    @PostMapping(value = "uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Uploads multi files (Invalid in Swagger UI)")
    public List<AttachmentOutputDTO> uploadAttachments(@RequestPart("files") MultipartFile[] files) {
        List<AttachmentOutputDTO> result = new LinkedList<>();

        for (MultipartFile file : files) {
            // Upload single file
            Attachment attachment = attachmentService.upload(file);
            // Convert and add
            result.add(attachmentService.convertToDto(attachment));
        }

        return result;
    }

    @GetMapping("mediaTypes")
    @ApiOperation("List all of media types")
    public List<String> mediaTypes(){
        return attachmentService.listAllMediaType();
    }
}
