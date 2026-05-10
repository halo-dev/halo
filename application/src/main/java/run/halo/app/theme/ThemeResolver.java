package run.halo.app.theme;

import static run.halo.app.security.authorization.AuthorityUtils.authoritiesToRoles;

import java.util.Collection;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.security.authorization.AuthorityUtils;

/**
 * @author johnniang
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ThemeResolver {

    private final SystemConfigFetcher environmentFetcher;

    private final ThemeRootGetter themeRoot;
    private final RoleService roleService;
    private final ReactiveExtensionClient client;

    public Mono<ThemeContext> getThemeContext(String themeName) {
        Assert.hasText(themeName, "Theme name cannot be empty");
        var path = themeRoot.get().resolve(themeName);
        return Mono.just(ThemeContext.builder().name(themeName).path(path))
                .flatMap(builder -> {
                    var activeMono = environmentFetcher
                            .fetch(SystemSetting.Theme.GROUP, SystemSetting.Theme.class)
                            .mapNotNull(SystemSetting.Theme::getActive)
                            .map(activatedTheme -> StringUtils.equals(activatedTheme, themeName))
                            .defaultIfEmpty(false);
                    var versionMono = client.fetch(Theme.class, themeName)
                            .mapNotNull(theme ->
                                    theme.getSpec() != null ? theme.getSpec().getVersion() : null)
                            .defaultIfEmpty("");
                    return Mono.zip(activeMono, versionMono)
                            .map(tuple -> builder.active(tuple.getT1()).version(tuple.getT2()))
                            .defaultIfEmpty(builder.active(false));
                })
                .map(ThemeContext.ThemeContextBuilder::build);
    }

    public Mono<ThemeContext> getTheme(ServerWebExchange exchange) {
        return fetchThemeFromExchange(exchange)
                .switchIfEmpty(Mono.defer(() -> fetchActivationState()
                        .flatMap(themeState -> {
                            var activatedTheme = themeState.activatedTheme();
                            var builder = ThemeContext.builder();
                            var themeName = exchange.getRequest()
                                    .getQueryParams()
                                    .getFirst(ThemeContext.THEME_PREVIEW_PARAM_NAME);

                            if (StringUtils.isBlank(themeName) || !themeState.supportsPreviewTheme()) {
                                themeName = activatedTheme;
                            }

                            boolean active = StringUtils.equals(activatedTheme, themeName);
                            var path = themeRoot.get().resolve(themeName);
                            builder.name(themeName).path(path).active(active);

                            return client.fetch(Theme.class, themeName)
                                    .map(theme -> builder.version(
                                                    theme.getSpec() != null
                                                            ? theme.getSpec().getVersion()
                                                            : null)
                                            .build())
                                    .defaultIfEmpty(builder.build());
                        })
                        .doOnNext(themeContext ->
                                exchange.getAttributes().put(ThemeContext.class.getName(), themeContext))));
    }

    public Mono<ThemeContext> fetchThemeFromExchange(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange)
                .map(ServerWebExchange::getAttributes)
                .filter(attrs -> attrs.containsKey(ThemeContext.class.getName()))
                .map(attrs -> attrs.get(ThemeContext.class.getName()))
                .cast(ThemeContext.class);
    }

    private Mono<ThemeActivationState> fetchActivationState() {
        var builder = ThemeActivationState.builder();

        var activatedMono = environmentFetcher
                .fetch(SystemSetting.Theme.GROUP, SystemSetting.Theme.class)
                .map(SystemSetting.Theme::getActive)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No theme activated")))
                .doOnNext(builder::activatedTheme);

        var preivewDisabledMono = environmentFetcher
                .fetch(ThemeRouteRules.GROUP, ThemeRouteRules.class)
                .map(ThemeRouteRules::isDisableThemePreview)
                .defaultIfEmpty(false)
                .doOnNext(builder::previewDisabled);

        var themeManageMono = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(au -> !AnonymousUserConst.isAnonymousUser(au.getName()))
                .flatMap(au -> supportsPreviewTheme(authoritiesToRoles(au.getAuthorities())))
                .doOnNext(builder::hasThemeManagementRole);

        return Mono.when(activatedMono, preivewDisabledMono, themeManageMono).then(Mono.fromSupplier(builder::build));
    }

    private Mono<Boolean> supportsPreviewTheme(Collection<String> authorities) {
        return roleService
                .contains(authorities, Set.of(AuthorityUtils.THEME_MANAGEMENT_ROLE_NAME))
                .defaultIfEmpty(false);
    }

    @Builder
    record ThemeActivationState(String activatedTheme, boolean previewDisabled, boolean hasThemeManagementRole) {

        private boolean supportsPreviewTheme() {
            if (hasThemeManagementRole) {
                return true;
            }
            return !previewDisabled;
        }
    }
}
