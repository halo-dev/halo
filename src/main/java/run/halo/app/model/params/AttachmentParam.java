package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Attachment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Attachment params.
 *
 * @author ryanwang
 * @date : 2019/04/20
 */
@Data
public class AttachmentParam implements InputConverter<Attachment> {

    @NotBlank(message = "Attachment name must not be blank")
    @Size(max = 255, message = "Length of attachment name must not be more than {max}")
    private String name;

}
