package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import lombok.Data;

/**
 * @author : RYAN0UP
 * @date : 2019-03-10
 */
@Data
public class AttachmentAdminOutputDTO extends AbstractOutputConverter<AttachmentAdminOutputDTO, Attachment> {

    private Long attachId;

    private String attachPath;

    private String attachSmallPath;
}
