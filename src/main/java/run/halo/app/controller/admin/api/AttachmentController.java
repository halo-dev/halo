package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.dto.AttachmentDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
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
 * @date 2019-03-21
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
     * @return Page<AttachmentDTO>
     */
    @GetMapping
    public Page<AttachmentDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                      AttachmentQuery attachmentQuery) {
        return attachmentService.pageDtosBy(pageable, attachmentQuery);
    }

    /**
     * Get attachment by id.
     *
     * @param id attachment id
     * @return AttachmentDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get attachment detail by id")
    public AttachmentDTO getBy(@PathVariable("id") Integer id) {
        Attachment attachment = attachmentService.getById(id);
        return attachmentService.convertToDto(attachment);
    }

    @PutMapping("{attachmentId:\\d+}")
    @ApiOperation("Updates a attachment")
    public AttachmentDTO updateBy(@PathVariable("attachmentId") Integer attachmentId,
                                  @RequestBody @Valid AttachmentParam attachmentParam) {
        Attachment attachment = attachmentService.getById(attachmentId);
        attachmentParam.update(attachment);
        return new AttachmentDTO().convertFrom(attachmentService.update(attachment));
    }

    /**
     * Delete attachment by id
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete attachment permanently by id")
    public AttachmentDTO deletePermanently(@PathVariable("id") Integer id) {
        return attachmentService.convertToDto(attachmentService.removePermanently(id));
    }

    @DeleteMapping
    @ApiOperation("Delete attachments permanently in batch by id array")
    public List<Attachment> deletePermanentlyInBatch(@RequestBody List<Integer> ids) {
        return attachmentService.removePermanently(ids);
    }

    @PostMapping("upload")
    @ApiOperation("Uploads single file")
    public AttachmentDTO uploadAttachment(@RequestPart("file") MultipartFile file) {
        return attachmentService.convertToDto(attachmentService.upload(file));
    }

    @PostMapping(value = "uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Uploads multi files (Invalid in Swagger UI)")
    public List<AttachmentDTO> uploadAttachments(@RequestPart("files") MultipartFile[] files) {
        List<AttachmentDTO> result = new LinkedList<>();

        for (MultipartFile file : files) {
            // Upload single file
            Attachment attachment = attachmentService.upload(file);
            // Convert and add
            result.add(attachmentService.convertToDto(attachment));
        }

        return result;
    }

    @GetMapping("media_types")
    @ApiOperation("Lists all of media types")
    public List<String> listMediaTypes() {
        return attachmentService.listAllMediaType();
    }

    @GetMapping("types")
    @ApiOperation("Lists all of types.")
    public List<AttachmentType> listTypes() {
        return attachmentService.listAllType();
    }
}
