package cc.ryanc.halo.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/3
 * description : 主题数据
 */
@Data
public class Theme implements Serializable {

    /**
     * 主题名称
     */
    private String themeName;

    /**
     * 是否支持设置
     */
    private boolean hasOptions;
}
