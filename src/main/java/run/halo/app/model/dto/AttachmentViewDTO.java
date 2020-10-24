package run.halo.app.model.dto;


import lombok.Data;

import java.util.List;

/**
 * Attachment view dto.
 *
 * @author guqing
 * @date 2020-10-24
 */
@Data
public class AttachmentViewDTO {

    private List<AttachmentDTO> attachments;

    private List<AttachmentGroupDTO> groups;
}
