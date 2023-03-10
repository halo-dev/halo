package run.halo.app.actuator;

import java.net.URI;
import java.util.Locale;
import java.util.TimeZone;
import lombok.Data;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.Basic;
import run.halo.app.infra.SystemSetting.Comment;
import run.halo.app.infra.SystemSetting.User;

@WebEndpoint(id = "globalinfo")
@Component
public class GlobalInfoEndpoint {

    private final ObjectProvider<SystemConfigurableEnvironmentFetcher> systemConfigFetcher;

    private final ExternalUrlSupplier externalUrl;

    public GlobalInfoEndpoint(
        ObjectProvider<SystemConfigurableEnvironmentFetcher> systemConfigFetcher,
        ExternalUrlSupplier externalUrl) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.externalUrl = externalUrl;
    }

    @ReadOperation
    public GlobalInfo globalInfo() {
        final var info = new GlobalInfo();
        info.setExternalUrl(externalUrl.get());
        info.setLocale(Locale.getDefault());
        info.setTimeZone(TimeZone.getDefault());
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

        private URI externalUrl;

        private TimeZone timeZone;

        private Locale locale;

        private boolean allowComments;

        private boolean allowAnonymousComments;

        private boolean allowRegistration;

        private String favicon;

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

}
