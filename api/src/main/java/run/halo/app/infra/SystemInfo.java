package run.halo.app.infra;

import com.github.zafarkhaja.semver.Version;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SystemInfo {
    private String title;

    private String subtitle;

    private String logo;

    private String favicon;

    private URL url;

    private Version version;

    private SeoProp seo;

    private Locale locale;

    private TimeZone timeZone;

    private String activatedThemeName;

    @Data
    @Accessors(chain = true)
    public static class SeoProp {
        private boolean blockSpiders;
        private String keywords;
        private String description;
    }
}
