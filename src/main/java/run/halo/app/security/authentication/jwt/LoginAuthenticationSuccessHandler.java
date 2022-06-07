package run.halo.app.security.authentication.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.JwtProperties;

public class LoginAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProp;

    private final ServerResponse.Context context;

    public LoginAuthenticationSuccessHandler(JwtEncoder jwtEncoder, JwtProperties jwtProp,
        ServerResponse.Context context) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProp = jwtProp;
        this.context = context;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
        Authentication authentication) {
        var issuedAt = Instant.now();
        // TODO Make the expiresAt configurable
        var expiresAt = issuedAt.plus(24, ChronoUnit.HOURS);
        var headers = JwsHeader.with(jwtProp.getJwsAlgorithm()).build();
        var claims = JwtClaimsSet.builder()
            .issuer("Halo Owner")
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            // the principal is the username
            .subject(authentication.getName())
            .claim("scope", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
            .build();

        var jwt = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims));

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("token", jwt.getTokenValue()))
            .flatMap(serverResponse -> serverResponse.writeTo(webFilterExchange.getExchange(),
                this.context));
    }

}
