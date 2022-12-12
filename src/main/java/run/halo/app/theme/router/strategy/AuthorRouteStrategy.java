package run.halo.app.theme.router.strategy;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.vo.UserVo;

/**
 * Author route strategy.
 *
 * @author guqing
 * @since 2.0.1
 */
@Component
@AllArgsConstructor
public class AuthorRouteStrategy implements DetailsPageRouteHandlerStrategy {

    private final ReactiveExtensionClient client;

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.AUTHOR.getValue(),
                Map.of("name", name,
                    "author", getByName(name),
                    ModelConst.TEMPLATE_ID, DefaultTemplateEnum.AUTHOR.getValue()
                )
            );
    }

    private Mono<UserVo> getByName(String name) {
        return client.fetch(User.class, name)
            .map(UserVo::from);
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return GroupVersionKind.fromExtension(User.class).equals(gvk);
    }
}
