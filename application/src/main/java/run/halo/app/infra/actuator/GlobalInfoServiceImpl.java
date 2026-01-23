package run.halo.app.infra.actuator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * Global info service implementation.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Service
@RequiredArgsConstructor
public class GlobalInfoServiceImpl implements GlobalInfoService {

    private final ObjectProvider<SystemConfigFetcher> systemConfigFetcher;

    private final ExternalUrlSupplier externalUrl;

    @Override
    public Mono<GlobalInfo> getGlobalInfo() {
        final var info = new GlobalInfo();
        info.setExternalUrl(externalUrl.getRaw());
        info.setLocale(Locale.getDefault());
        info.setTimeZone(TimeZone.getDefault());

        var publishers = new ArrayList<Publisher<?>>(1);
        publishers.add(handleSettings(info));
        return Mono.when(publishers).then(Mono.just(info));
    }

    private Mono<Void> handleSettings(GlobalInfo info) {
        return Optional.ofNullable(systemConfigFetcher.getIfUnique())
            .map(fetcher -> fetcher.getConfig()
                .doOnNext(config -> {
                    handleCommentSetting(info, config);
                    handleUserSetting(info, config);
                    handleBasicSetting(info, config);
                    handlePostSlugGenerationStrategy(info, config);
                })
                .then()
            )
            .orElseGet(Mono::empty);
    }

    private void handleCommentSetting(GlobalInfo info, Map<String, String> config) {
        var comment =
            SystemSetting.get(config, SystemSetting.Comment.GROUP, SystemSetting.Comment.class);
        if (comment == null) {
            info.setAllowAnonymousComments(true);
        } else {
            info.setAllowAnonymousComments(
                comment.getSystemUserOnly() == null || !comment.getSystemUserOnly());
        }
    }

    private void handleUserSetting(GlobalInfo info, Map<String, String> config) {
        var userSetting =
            SystemSetting.get(config, SystemSetting.User.GROUP, SystemSetting.User.class);
        if (userSetting == null) {
            info.setAllowRegistration(false);
            info.setMustVerifyEmailOnRegistration(false);
        } else {
            info.setAllowRegistration(userSetting.isAllowRegistration());
            info.setMustVerifyEmailOnRegistration(userSetting.isMustVerifyEmailOnRegistration());
        }
    }

    private void handlePostSlugGenerationStrategy(GlobalInfo info, Map<String, String> config) {
        var post = SystemSetting.get(config, SystemSetting.Post.GROUP, SystemSetting.Post.class);
        if (post != null) {
            info.setPostSlugGenerationStrategy(post.getSlugGenerationStrategy());
        }
    }

    private void handleBasicSetting(GlobalInfo info, Map<String, String> config) {
        var basic = SystemSetting.get(config, SystemSetting.Basic.GROUP, SystemSetting.Basic.class);
        if (basic != null) {
            info.setFavicon(basic.getFavicon());
            info.setSiteTitle(basic.getTitle());
            basic.useSystemLocale().ifPresent(info::setLocale);
        }
    }

}
