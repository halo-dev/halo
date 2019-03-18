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
     * Theme name
     */
    private String themeName;

    /**
     * Is support setting options
     */
    private boolean hasOptions;

    /**
     * Is support update
     */
    private boolean hasUpdate;

    /**
     * Is internal theme
     */
    private boolean isInternal;
}
