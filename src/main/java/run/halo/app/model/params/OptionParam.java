package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Option;
import run.halo.app.model.enums.OptionType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Optional param.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-20
 */
@Data
public class OptionParam implements InputConverter<Option> {

    @NotBlank(message = "Option key must not be blank")
    @Size(max = 100, message = "Length of option key must not be more than {max}")
    private String key;


    @Size(max = 1023, message = "Length of option value must not be more than {max}")
    private String value;

    private OptionType type;
}
