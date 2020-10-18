package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Email;

/**
 * Email output dto.
 *
 * @author ryanwang
 * @date 2019/3/21
 */
@Data
@EqualsAndHashCode
public class EmailDTO implements OutputConverter<EmailDTO, Email> {

    private Integer id;

    private String name;

    private String value;

    private String description;

    private String fullPath;

    public Long postCount;
}
