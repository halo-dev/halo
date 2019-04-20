package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Attachment;

import javax.validation.constraints.NotBlank;

/**
 * Attachment params.
 *
 * @author : RYAN0UP
 * @date : 2019/04/20
 */
@Data
public class AttachmentParam implements InputConverter<Attachment> {

    @NotBlank(message = "Attachment name must not be blank")
    private String name;
}
