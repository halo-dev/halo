package run.halo.app.theme;

import static run.halo.app.core.extension.Theme.PageLayoutState.INVALID;
import static run.halo.app.core.extension.Theme.PageLayoutState.MISSING;
import static run.halo.app.core.extension.Theme.PageLayoutState.SUPPORTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.Theme.ThemeStatus.PageLayout;

@Component
public class ThemeLayoutCompatibilityChecker {

    private static final Pattern LAYOUT_FRAGMENT_PATTERN =
            Pattern.compile("th:fragment\\s*=\\s*([\"'])\\s*html\\s*\\(\\s*head\\s*,\\s*content\\s*\\)\\s*\\1");

    public PageLayout check(Path themePath) {
        var layout = themePath.resolve(PageLayoutContract.TEMPLATE_FILE);
        if (Files.notExists(layout)) {
            return layout(MISSING, "MissingLayoutTemplate", "templates/layout.html was not found.");
        }
        if (!Files.isRegularFile(layout) || !Files.isReadable(layout)) {
            return layout(INVALID, "UnreadableLayoutTemplate", "templates/layout.html is not readable.");
        }
        try {
            if (!hasContractFragment(layout)) {
                return layout(
                        INVALID,
                        "UnsupportedLayoutFragment",
                        "templates/layout.html must declare th:fragment=\"html (head, content)\".");
            }
            return layout(SUPPORTED, "LayoutTemplateSupported", "templates/layout.html declares html(head, content).");
        } catch (IOException e) {
            return layout(INVALID, "UnreadableLayoutTemplate", diagnosticMessage(e));
        }
    }

    public boolean isSupported(Path themePath) {
        return SUPPORTED.equals(check(themePath).getState());
    }

    private static boolean hasContractFragment(Path layout) throws IOException {
        return LAYOUT_FRAGMENT_PATTERN.matcher(Files.readString(layout)).find();
    }

    private static PageLayout layout(Theme.PageLayoutState state, String reason, String message) {
        var layout = new PageLayout();
        layout.setState(state);
        layout.setTemplate(PageLayoutContract.TEMPLATE_FILE);
        layout.setReason(reason);
        layout.setMessage(message);
        return layout;
    }

    private static String diagnosticMessage(Exception e) {
        var message = e.getMessage();
        if (message == null && e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        if (message == null) {
            return e.getClass().getSimpleName();
        }
        return message.length() > 240 ? message.substring(0, 240) : message;
    }
}
