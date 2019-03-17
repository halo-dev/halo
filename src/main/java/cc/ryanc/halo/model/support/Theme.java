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
     * theme name
     */
    private String themeName;

    /**
     * is support setting options
     */
    private boolean hasOptions;

    /**
     * is support update
     */
    private boolean hasUpdate;

    /**
     * is internal theme
     */
    private boolean isInternal;
}
