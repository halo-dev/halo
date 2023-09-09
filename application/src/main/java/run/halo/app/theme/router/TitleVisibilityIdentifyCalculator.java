package run.halo.app.theme.router;

import java.util.Locale;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Post;

@Component
@AllArgsConstructor
public class TitleVisibilityIdentifyCalculator {

    private final MessageSource messageSource;

    /**
     * Calculate title with visibility identification.
     *
     * @param title title must not be null
     * @param visibleEnum visibility enum
     */
    public String calculateTitle(String title, Post.VisibleEnum visibleEnum, Locale locale) {
        Assert.notNull(title, "Title must not be null");
        if (Post.VisibleEnum.PRIVATE.equals(visibleEnum)) {
            String identify = messageSource.getMessage(
                "title.visibility.identification.private",
                null,
                "",
                locale);
            return title + identify;
        }
        return title;
    }
}
