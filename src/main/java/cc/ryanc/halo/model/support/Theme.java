package cc.ryanc.halo.model.support;

import lombok.Data;

import java.io.Serializable;

/**
 * Theme DTO
 *
 * @author : RYAN0UP
 * @date : 2018/1/3
 */
@Data
public class Theme implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Theme dir
     */
    private String themeDir;

    /**
     * Is support setting options
     */
    private boolean hasOptions;

    /**
     * Is internal theme
     */
    private boolean isInternal;

    /**
     * Theme properties
     */
    private ThemeProperties properties;
}
