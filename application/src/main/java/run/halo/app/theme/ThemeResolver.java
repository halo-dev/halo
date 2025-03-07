package run.halo.app.theme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.Theme;
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

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final ThemeRootGetter themeRoot;

    public Mono<ThemeContext> getThemeContext(String themeName) {
        Assert.hasText(themeName, "Theme name cannot be empty");
        var path = themeRoot.get().resolve(themeName);
        return Mono.just(ThemeContext.builder().name(themeName).path(path))
            .flatMap(builder -> environmentFetcher.fetch(Theme.GROUP, Theme.class)
                .mapNotNull(Theme::getActive)
                .map(activatedTheme -> {
                    boolean active = StringUtils.equals(activatedTheme, themeName);
                    return builder.active(active);
                })
                .defaultIfEmpty(builder.active(false))
            )
            .map(ThemeContext.ThemeContextBuilder::build);
    }

    public Mono<ThemeContext> getTheme(ServerWebExchange exchange) {
        return fetchThemeFromExchange(exchange)
            .switchIfEmpty(Mono.defer(() -> fetchActivationState()
                .map(themeState -> {
                    var activatedTheme = themeState.activatedTheme();
                    var builder = ThemeContext.builder();
                    var themeName = exchange.getRequest().getQueryParams()
                        .getFirst(ThemeContext.THEME_PREVIEW_PARAM_NAME);

                    if (StringUtils.isBlank(themeName) || !themeState.supportsPreviewTheme()) {
                        themeName = activatedTheme;
                    }

                    boolean active = StringUtils.equals(activatedTheme, themeName);
                    var path = themeRoot.get().resolve(themeName);
                    return builder.name(themeName)
                        .path(path)
                        .active(active)
                        .build();
                })
                .doOnNext(themeContext ->
                    exchange.getAttributes().put(ThemeContext.class.getName(), themeContext))
            ));
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

        var activatedMono = environmentFetcher.fetch(Theme.GROUP, Theme.class)
            .map(Theme::getActive)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No theme activated")))
            .doOnNext(builder::activatedTheme);

        var preivewDisabledMono = environmentFetcher.fetch(ThemeRouteRules.GROUP,
                ThemeRouteRules.class)
            .map(ThemeRouteRules::isDisableThemePreview)
            .defaultIfEmpty(false)
            .doOnNext(builder::previewDisabled);

        var authenticatedUser = ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .doOnNext(builder::authenticatedUser);

        return Mono.when(activatedMono, preivewDisabledMono, authenticatedUser)
            .then(Mono.fromSupplier(builder::build));
    }

    @Builder
    record ThemeActivationState(String activatedTheme, boolean previewDisabled,
                                Authentication authenticatedUser) {

        private boolean supportsPreviewTheme() {
            if (!previewDisabled) {
                return true;
            }
            return isSuperAdminUser();
        }

        private boolean isSuperAdminUser() {
            if (authenticatedUser == null) {
                return false;
            }
            var authorities = AuthorityUtils.authoritiesToRoles(authenticatedUser.getAuthorities());
            return AuthorityUtils.containsSuperRole(authorities);
        }
    }
}
