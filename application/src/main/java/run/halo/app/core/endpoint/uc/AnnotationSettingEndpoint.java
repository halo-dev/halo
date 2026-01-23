package run.halo.app.core.endpoint.uc;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.ExtensionUtil.defaultSort;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginService;
import run.halo.app.theme.service.ThemeService;

/**
 * The endpoint for managing AnnotationSettings.
 *
 * @author johnniang
 * @since 2.22.3
 */
@Component
@RequiredArgsConstructor
class AnnotationSettingEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final ThemeService themeService;

    private final PluginService pluginService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "AnnotationSettingV1AlphaUc";
        return SpringdocRouteBuilder.route()
            .GET(
                "/annotationsettings",
                this::listAvailableAnnotationSettings,
                builder -> builder
                    .operationId("listAvailableAnnotationSettings")
                    .description("""
                        List available AnnotationSettings for the given targetRef. \
                        The available AnnotationSettings are determined by \
                        the currently activated theme and started plugins.""")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("targetRef")
                        .in(ParameterIn.QUERY)
                        .description(
                            "The targetRef of the AnnotationSetting. e.g.: 'content.halo.run/Post"
                        )
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(Builder.responseBuilder()
                        .implementationArray(AnnotationSetting.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> listAvailableAnnotationSettings(ServerRequest serverRequest) {
        var targetRef = serverRequest.queryParam("targetRef")
            .filter(StringUtils::hasText)
            .orElse(null);
        if (targetRef == null) {
            return Mono.error(new ServerWebInputException("Query param 'targetRef' is required"));
        }
        var getActivatedTheme = themeService.fetchActivatedThemeName()
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty());
        var getStartedPlugins = pluginService.getStartedPluginNames().collectList();
        var annotationSettings = Mono.zip(getActivatedTheme, getStartedPlugins,
                (themeName, pluginNames) -> {
                    Condition labelConditions = null;
                    if (themeName.isPresent()) {
                        labelConditions = Queries.labelEqual(Theme.THEME_NAME_LABEL,
                            themeName.get());
                    }
                    if (!CollectionUtils.isEmpty(pluginNames)) {
                        var pluginLabelCondition =
                            Queries.labelIn(PluginConst.PLUGIN_NAME_LABEL_NAME, pluginNames);
                        if (labelConditions == null) {
                            labelConditions = pluginLabelCondition;
                        } else {
                            labelConditions = labelConditions.or(pluginLabelCondition);
                        }
                    }
                    if (labelConditions == null) {
                        labelConditions = Queries.empty();
                    }
                    var builder = ListOptions.builder()
                        .andQuery(labelConditions)
                        .andQuery(Queries.equal("spec.targetRef", targetRef));
                    return builder.build();
                })
            .flatMapMany(
                listOptions -> client.listAll(AnnotationSetting.class, listOptions, defaultSort())
            );
        return ServerResponse.ok().body(annotationSettings, AnnotationSetting.class);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.halo.run/v1alpha1");
    }

}
