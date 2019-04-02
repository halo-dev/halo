package cc.ryanc.halo.model.support;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author RYAN0UP
 * @date 2019/04/02
 */
@Data
@ToString
public class ThemeFile {

    private String name;

    private Boolean isFile;

    private List<ThemeFile> node;
}
