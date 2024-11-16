package run.halo.app.infra.actuator;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
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

    private final ObjectProvider<SystemConfigurableEnvironmentFetcher>
        systemConfigFetcher;

    public GlobalInfoServiceImpl(HaloProperties haloProperties,
        AuthProviderService authProviderService,
        InitializationStateGetter initializationStateGetter,
        ObjectProvider<SystemConfigurableEnvironmentFetcher> systemConfigFetcher) {
        this.haloProperties = haloProperties;
        this.authProviderService = authProviderService;
        this.initializationStateGetter = initializationStateGetter;
        this.systemConfigFetcher = systemConfigFetcher;
    }

    @Override
    public Mono<GlobalInfo> getGlobalInfo() {
        final var info = new GlobalInfo();
        info.setExternalUrl(haloProperties.getExternalUrl());
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
            .map(fetcher -> fetcher.getConfigMap()
                .doOnNext(configMap -> {
                    handleCommentSetting(info, configMap);
                    handleUserSetting(info, configMap);
                    handleBasicSetting(info, configMap);
                    handlePostSlugGenerationStrategy(info, configMap);
                })
                .then()
            )
            .orElseGet(Mono::empty);
    }

    private void handleCommentSetting(GlobalInfo info, ConfigMap configMap) {
        var comment =
            SystemSetting.get(configMap, SystemSetting.Comment.GROUP, SystemSetting.Comment.class);
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
        var userSetting =
            SystemSetting.get(configMap, SystemSetting.User.GROUP, SystemSetting.User.class);
        if (userSetting == null) {
            info.setAllowRegistration(false);
            info.setMustVerifyEmailOnRegistration(false);
        } else {
            info.setAllowRegistration(userSetting.isAllowRegistration());
            info.setMustVerifyEmailOnRegistration(userSetting.isMustVerifyEmailOnRegistration());
        }
    }

    private void handlePostSlugGenerationStrategy(GlobalInfo info,
        ConfigMap configMap) {
        var post = SystemSetting.get(configMap, SystemSetting.Post.GROUP, SystemSetting.Post.class);
        if (post != null) {
            info.setPostSlugGenerationStrategy(post.getSlugGenerationStrategy());
        }
    }

    private void handleBasicSetting(GlobalInfo info, ConfigMap configMap) {
        var basic =
            SystemSetting.get(configMap, SystemSetting.Basic.GROUP, SystemSetting.Basic.class);
        if (basic != null) {
            info.setFavicon(basic.getFavicon());
            info.setSiteTitle(basic.getTitle());
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
