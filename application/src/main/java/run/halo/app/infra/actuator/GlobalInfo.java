package run.halo.app.infra.actuator;

import java.net.URL;
import java.util.List;
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

    private boolean useAbsolutePermalink;

    private TimeZone timeZone;

    private Locale locale;

    private boolean allowComments;

    private boolean allowAnonymousComments;

    private boolean allowRegistration;

    private String favicon;

    private boolean userInitialized;

    private boolean dataInitialized;

    private String postSlugGenerationStrategy;

    private List<SocialAuthProvider> socialAuthProviders;

    private Boolean mustVerifyEmailOnRegistration;

    private String siteTitle;

    @Data
    public static class SocialAuthProvider {
        private String name;

        private String displayName;

        private String description;

        private String logo;

        private String website;

        private String authenticationUrl;

        private String bindingUrl;
    }
}
