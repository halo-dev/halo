package run.halo.app.localization;

import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Explicit language-context opt-in for a public resource router, including plugin-owned APIs.
 *
 * <pre>{@code
 * return RouterFunctions.route(GET("/products/{id}"), this::getProduct)
 *     .filter(publicApiLocalizationFilter);
 * }</pre>
 *
 * <p>Apply only to public display endpoints that read through presentation clients. This filter does not grant access,
 * expose an endpoint anonymously or replace its existing security policy. Console and internal routers must retain
 * their normal source reads. An existing subscriber context takes precedence; otherwise an explicit {@code locale}
 * query parameter is resolved by the enabled localization provider. Missing or unavailable configuration retains source
 * values. It never registers or rewrites routes.
 *
 * @since 2.28.0
 */
public interface PublicApiLocalizationFilter extends HandlerFilterFunction<ServerResponse, ServerResponse> {}
