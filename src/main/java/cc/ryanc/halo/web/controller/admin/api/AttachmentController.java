package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.AttachmentOutputDTO;
import cc.ryanc.halo.model.entity.Attachment;
import cc.ryanc.halo.service.AttachmentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("/admin/api/attachments")
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
    public Page<AttachmentOutputDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable) {
        return attachmentService.pageDtosBy(pageable);
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
        return new AttachmentOutputDTO().convertFrom(attachmentService.getById(id));
    }

    /**
     * Delete attachment by id
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete attachment by id")
    public AttachmentOutputDTO deletePermanently(@PathVariable("id") Integer id) {
        return new AttachmentOutputDTO().convertFrom(attachmentService.removePermanently(id));
    }

    @PostMapping("upload")
    @ApiOperation("Uploads single file")
    public AttachmentOutputDTO uploadAttachment(@RequestPart("file") MultipartFile file) {
        return new AttachmentOutputDTO().convertFrom(attachmentService.upload(file));
    }

    @PostMapping(value = "uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Uploads multi files (Invalid in Swagger UI)")
    public List<AttachmentOutputDTO> uploadAttachments(@RequestPart("files") MultipartFile[] files) {
        List<AttachmentOutputDTO> result = new LinkedList<>();

        for (MultipartFile file : files) {
            // Upload single file
            Attachment attachment = attachmentService.upload(file);
            // Convert and add
            result.add(new AttachmentOutputDTO().convertFrom(attachment));
        }

        return result;
    }
}
