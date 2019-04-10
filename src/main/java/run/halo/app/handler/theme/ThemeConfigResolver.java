package run.halo.app.handler.theme;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * Theme config resolver interface.
 *
 * @author johnniang
 * @date 4/10/19
 */
public interface ThemeConfigResolver {

    /**
     * Resolves content as group list.
     *
     * @param content content must not be blank
     * @return a list of group
     * @throws IOException throws when content conversion fails
     */
    @NonNull
    List<Group> resolve(@NonNull String content) throws IOException;
}
