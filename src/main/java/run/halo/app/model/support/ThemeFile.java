package run.halo.app.model.support;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Theme file.
 *
 * @author RYAN0UP
 * @date 2019/04/02
 */
@Data
@ToString
public class ThemeFile {

    private String name;

    private String path;

    private Boolean isFile;

    private Boolean editable;

    private List<ThemeFile> node;
}
