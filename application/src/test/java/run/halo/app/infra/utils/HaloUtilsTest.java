package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import run.halo.app.theme.router.ModelConst;

class HaloUtilsTest {

    @Test
    void checkNoCache() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        var request = MockServerRequest.builder()
            .exchange(exchange)
            .build();
        var applied = HaloUtils.noCache().apply(request);
        assertEquals(applied, request);
        assertTrue(() -> exchange.getRequiredAttribute(ModelConst.NO_CACHE));
    }

}