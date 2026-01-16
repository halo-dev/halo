package run.halo.app.infra.actuator;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.security.AuthProviderService;

/**
 * Global info service implementation.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Service
public class GlobalInfoServiceImpl implements GlobalInfoService {

    private final HaloProperties haloProperties;

    private final AuthProviderService authProviderService;

    private final InitializationStateGetter initializationStateGetter;

    private final ObjectProvider<SystemConfigFetcher> systemConfigFetcher;

    private final ExternalUrlSupplier externalUrl;

    public GlobalInfoServiceImpl(HaloProperties haloProperties,
        AuthProviderService authProviderService,
        InitializationStateGetter initializationStateGetter,
        ObjectProvider<SystemConfigFetcher> systemConfigFetcher,
        ExternalUrlSupplier externalUrl) {
        this.haloProperties = haloProperties;
        this.authProviderService = authProviderService;
        this.initializationStateGetter = initializationStateGetter;
        this.systemConfigFetcher = systemConfigFetcher;
        this.externalUrl = externalUrl;
    }

    @Override
    public Mono<GlobalInfo> getGlobalInfo() {
        final var info = new GlobalInfo();
        info.setExternalUrl(externalUrl.getRaw());
        info.setUseAbsolutePermalink(haloProperties.isUseAbsolutePermalink());
        info.setLocale(Locale.getDefault());
        info.setTimeZone(TimeZone.getDefault());

        var publishers = new ArrayList<Publisher<?>>(4);
        publishers.add(
            initializationStateGetter.userInitialized().doOnNext(info::setUserInitialized)
        );
        publishers.add(
            initializationStateGetter.dataInitialized().doOnNext(info::setDataInitialized)
        );
        publishers.add(handleSocialAuthProvider(info));
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
            info.setAllowComments(true);
            info.setAllowAnonymousComments(true);
        } else {
            info.setAllowComments(comment.getEnable() != null && comment.getEnable());
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

    private Mono<Void> handleSocialAuthProvider(GlobalInfo info) {
        return authProviderService.listAll()
            .map(listedAuthProviders -> listedAuthProviders.stream()
                .filter(provider -> isTrue(provider.getEnabled()))
                .filter(provider -> StringUtils.isNotBlank(provider.getBindingUrl()))
                .map(provider -> {
                    GlobalInfo.SocialAuthProvider socialAuthProvider =
                        new GlobalInfo.SocialAuthProvider();
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
            .doOnNext(info::setSocialAuthProviders)
            .then();
    }
}
