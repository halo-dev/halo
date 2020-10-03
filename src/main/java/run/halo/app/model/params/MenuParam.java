package run.halo.app.model.params;

import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Menu;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Menu param.
 *
 * @author johnniang
 * @author ryanwang
 * @date 4/3/19
 */
@Data
@ToString
public class MenuParam implements InputConverter<Menu> {

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称的字符长度不能超过 {max}")
    private String name;

    @NotBlank(message = "菜单地址不能为空")
    @Size(max = 1023, message = "菜单地址的字符长度不能超过 {max}")
    private String url;

    @Min(value = 0, message = "排序编号不能低于 {value}")
    private Integer priority;

    @Size(max = 50, message = "Length of menu target must not be more than {max}")
    private String target;

    @Size(max = 50, message = "菜单图标的字符长度不能超过 {max}")
    private String icon;

    private Integer parentId;

    @Size(max = 255, message = "菜单分组的字符长度不能超过 {max}")
    private String team;
}
