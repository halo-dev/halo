package run.halo.app.security;

import com.google.common.net.HttpHeaders;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.properties.SecurityProperties;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
@Order(0)
public class CorsConfigurer implements SecurityConfigurer {

    private final SecurityProperties.CorsOptions corsOptions;

    public CorsConfigurer(HaloProperties haloProperties) {
        corsOptions = haloProperties.getSecurity().getCorsOptions();
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        http.cors(spec -> {
            if (corsOptions.isDisabled()) {
                spec.disable();
                return;
            }
            spec.configurationSource(apiCorsConfigSource());
        });
    }

    CorsConfigurationSource apiCorsConfigSource() {
        var source = new UrlBasedCorsConfigurationSource();
        // additional CORS configuration
        this.corsOptions.getConfigs().forEach(corsConfig -> source.registerCorsConfiguration(
            corsConfig.getPathPattern(), corsConfig.getConfig().toCorsConfiguration()
        ));

        // default CORS configuration
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(
            List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                "X-XSRF-TOKEN", HttpHeaders.COOKIE));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/apis/**", configuration);
        return source;
    }
}
