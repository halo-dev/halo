package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.AttachmentGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Attachment group params.
 *
 * @author guqing
 * @date 2020-10-24
 */
@Data
public class AttachmentGroupParam implements InputConverter<AttachmentGroup> {
    private Integer parentId;

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 100, message = "附件名称的字符长度不能超过 {max}")
    private String name;
}
