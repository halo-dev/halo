package cc.ryanc.halo.model.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/3
 * description :
 */
@Data
public class Theme implements Serializable {
    private Integer themeId;
    private String themeName;
    private String themeScreenShot;
}
