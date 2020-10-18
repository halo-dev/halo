package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Email;

import javax.persistence.Column;
import javax.persistence.Lob;

/**
 * Email with post count output dto.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EmailWithPostCountDTO extends PostEmailDTO {

    private Long postCount;

    private String emailFullPath;
}
