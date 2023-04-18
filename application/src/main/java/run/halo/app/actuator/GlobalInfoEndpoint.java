package run.halo.app.actuator;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.Basic;
import run.halo.app.infra.SystemSetting.Comment;
import run.halo.app.infra.SystemSetting.User;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.security.AuthProviderService;

@WebEndpoint(id = "globalinfo")
@Component
@RequiredArgsConstructor
public class GlobalInfoEndpoint {

    private final ObjectProvider<SystemConfigurableEnvironmentFetcher> systemConfigFetcher;

    private final HaloProperties haloProperties;

    private final AuthProviderService authProviderService;

    @ReadOperation
    public GlobalInfo globalInfo() {
        final var info = new GlobalInfo();
        info.setExternalUrl(haloProperties.getExternalUrl());
        info.setUseAbsolutePermalink(haloProperties.isUseAbsolutePermalink());
        info.setLocale(Locale.getDefault());
        info.setTimeZone(TimeZone.getDefault());
        handleSocialAuthProvider(info);
        systemConfigFetcher.ifAvailable(fetcher -> fetcher.getConfigMapBlocking()
            .ifPresent(configMap -> {
                handleCommentSetting(info, configMap);
                handleUserSetting(info, configMap);
                handleBasicSetting(info, configMap);
            }));

        return info;
    }

    @Data
    public static class GlobalInfo {

        private URL externalUrl;

        private boolean useAbsolutePermalink;

        private TimeZone timeZone;

        private Locale locale;

        private boolean allowComments;

        private boolean allowAnonymousComments;

        private boolean allowRegistration;

        private String favicon;

        private List<SocialAuthProvider> socialAuthProviders;
    }

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

    private void handleCommentSetting(GlobalInfo info, ConfigMap configMap) {
        var comment = SystemSetting.get(configMap, Comment.GROUP, Comment.class);
        if (comment == null) {
            info.setAllowComments(true);
            info.setAllowAnonymousComments(true);
        } else {
            info.setAllowComments(comment.getEnable() != null && comment.getEnable());
            info.setAllowAnonymousComments(
                comment.getSystemUserOnly() == null || !comment.getSystemUserOnly());
        }
    }

    private void handleUserSetting(GlobalInfo info, ConfigMap configMap) {
        var user = SystemSetting.get(configMap, User.GROUP, User.class);
        if (user == null) {
            info.setAllowRegistration(false);
        } else {
            info.setAllowRegistration(
                user.getAllowRegistration() != null && user.getAllowRegistration());
        }
    }

    private void handleBasicSetting(GlobalInfo info, ConfigMap configMap) {
        var basic = SystemSetting.get(configMap, Basic.GROUP, Basic.class);
        if (basic != null) {
            info.setFavicon(basic.getFavicon());
        }
    }

    private void handleSocialAuthProvider(GlobalInfo info) {
        List<SocialAuthProvider> providers = authProviderService.listAll()
            .map(listedAuthProviders -> listedAuthProviders.stream()
                .filter(provider -> isTrue(provider.getEnabled()))
                .filter(provider -> StringUtils.isNotBlank(provider.getBindingUrl()))
                .map(provider -> {
                    SocialAuthProvider socialAuthProvider = new SocialAuthProvider();
                    socialAuthProvider.setName(provider.getName());
                    socialAuthProvider.setDisplayName(provider.getDisplayName());
                    socialAuthProvider.setDescription(provider.getDescription());
                    socialAuthProvider.setLogo(provider.getLogo());
                    socialAuthProvider.setWebsite(provider.getWebsite());
                    socialAuthProvider.setAuthenticationUrl(provider.getAuthenticationUrl());
                    socialAuthProvider.setBindingUrl(provider.getBindingUrl());
                    return socialAuthProvider;
                })
                .toList()
            )
            .block();

        info.setSocialAuthProviders(providers);
    }

}
