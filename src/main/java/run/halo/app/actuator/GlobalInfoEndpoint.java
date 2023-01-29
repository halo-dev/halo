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
        systemConfigFetcher.getIfAvailable()
            .getConfigMapBlocking()
            .ifPresent(configMap -> {
                info.setAllowComments(allowComments(configMap));
                info.setAllowRegistration(allowRegistration(configMap));
            });

        return info;
    }

    @Data
    public static class GlobalInfo {

        private URI externalUrl;

        private TimeZone timeZone;

        private Locale locale;

        private boolean allowComments;

        private boolean allowRegistration;

    }

    private boolean allowComments(ConfigMap configMap) {
        var comment = SystemSetting.get(configMap, Comment.GROUP, Comment.class);
        if (comment == null || comment.getEnable() == null) {
            return false;
        }
        return comment.getEnable();
    }

    private boolean allowRegistration(ConfigMap configMap) {
        var user = SystemSetting.get(configMap, User.GROUP, User.class);
        if (user == null || user.getAllowRegistration() == null) {
            return false;
        }
        return user.getAllowRegistration();
    }
}
