package run.halo.app.infra.actuator;

import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;
import lombok.Data;

/**
 * Global info.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Data
public class GlobalInfo {

    private URL externalUrl;

    private TimeZone timeZone;

    private Locale locale;

    private boolean allowAnonymousComments;

    private boolean allowRegistration;

    private String favicon;

    private String postSlugGenerationStrategy;

    private Boolean mustVerifyEmailOnRegistration;

    private String siteTitle;

}
