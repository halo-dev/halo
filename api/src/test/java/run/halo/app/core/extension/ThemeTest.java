package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

class ThemeTest {

    @Test
    void shouldSerializeStatusScreenshot() {
        var theme = new Theme();
        var status = new Theme.ThemeStatus();
        status.setScreenshot("/themes/test-theme/screenshot.png");
        theme.setStatus(status);

        assertThat(JsonUtils.objectToJson(theme)).contains("\"screenshot\":\"/themes/test-theme/screenshot.png\"");
    }
}
