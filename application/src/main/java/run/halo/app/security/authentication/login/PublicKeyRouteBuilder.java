package run.halo.app.security.authentication.login;

import java.util.Base64;
import lombok.Data;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public class PublicKeyRouteBuilder {

    private final CryptoService cryptoService;

    public PublicKeyRouteBuilder(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * Builds public key router function.
     *
     * @return public key router function.
     */
    public RouterFunction<ServerResponse> build() {
        return SpringdocRouteBuilder.route()
            .GET("/login/public-key", request -> cryptoService.readPublicKey()
                    .flatMap(publicKey -> {
                        var base64Format = Base64.getEncoder().encodeToString(publicKey);
                        var response = new PublicKeyResponse();
                        response.setBase64Format(base64Format);
                        return ServerResponse.ok()
                            .bodyValue(response);
                    }),
                builder -> builder.operationId("GetPublicKey")
                    .description("Read public key for encrypting password.")
                    .tag("Login")
                    .response(Builder.responseBuilder()
                        .implementation(PublicKeyResponse.class))).build();
    }

    @Data
    public static class PublicKeyResponse {

        private String base64Format;

    }
}
