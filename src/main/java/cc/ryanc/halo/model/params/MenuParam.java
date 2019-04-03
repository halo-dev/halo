package cc.ryanc.halo.model.params;

import cc.ryanc.halo.model.dto.base.InputConverter;
import cc.ryanc.halo.model.entity.Menu;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Menu param.
 *
 * @author johnniang
 * @date 4/3/19
 */
@Data
@ToString
public class MenuParam implements InputConverter<Menu> {

    @NotBlank(message = "Menu name must not be blank")
    @Size(max = 50, message = "Length of menu name must not be more than {max}")
    private String name;

    @NotBlank(message = "Menu url must not be blank")
    @Size(max = 1023, message = "Length of menu url must not be more than {max}")
    @URL(message = "Menu url has incorrect url format")
    private String url;

    @Min(value = 0, message = "Menu sort must not be less than {value}")
    private Integer sort;

    @Size(max = 50, message = "Length of menu target must not be more than {max}")
    private String target;

    @Size(max = 50, message = "Length of menu icon must not be more than {max}")
    private String icon;
}
