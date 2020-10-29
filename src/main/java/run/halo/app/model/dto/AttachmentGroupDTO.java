package run.halo.app.model.dto;

import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.AttachmentGroup;

/**
 * Attachment group output dto.
 *
 * @author guqing
 * @date 2020-10-24
 */
@Data
public class AttachmentGroupDTO implements OutputConverter<AttachmentGroupDTO, AttachmentGroup> {

    private Integer id;

    private String name;

    private Integer parentId;
}
