package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <pre>
 *     菜单
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/24
 */
@Data
@Entity
@Table(name = "halo_menu")
public class Menu implements Serializable {

    private static final long serialVersionUID = -7726233157376388786L;

    /**
     * 编号 自增
     */
    @Id
    @GeneratedValue
    private Long menuId;

    /**
     * 菜单名称
     */
    @NotEmpty(message = "菜单名称不能为空！")
    private String menuName;

    /**
     * 菜单路径
     */
    @NotEmpty(message = "菜单路径不能为空！")
    private String menuUrl;

    /**
     * 排序编号
     */
    @NotNull(message = "排序编号不能为空！")
    private Integer menuSort;

    /**
     * 图标，可选，部分主题可显示
     */
    private String menuIcon;

    /**
     * 打开方式
     */
    private String menuTarget;
}
