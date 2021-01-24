package run.halo.app.model.support;

import java.util.Comparator;
import java.util.List;
import lombok.Data;

/**
 * Static page dto.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
@Data
public class StaticPageFile implements Comparator<StaticPageFile> {

    private String id;

    private String name;

    private Boolean isFile;

    private List<StaticPageFile> children;

    @Override
    public int compare(StaticPageFile leftFile, StaticPageFile rightFile) {
        if (leftFile.isFile && !rightFile.isFile) {
            return 1;
        }

        if (!leftFile.isFile && rightFile.isFile) {
            return -1;
        }

        return leftFile.getName().compareTo(rightFile.getName());
    }
}
