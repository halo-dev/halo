package run.halo.app.model.support;

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
     * Theme key,is theme folder name.
     */
    private String key;

    /**
     * Is support setting options
     */
    private boolean hasOptions;

    /**
     * Theme properties
     */
    private ThemeProperties properties;
}
