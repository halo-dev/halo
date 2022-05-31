package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

class HaloUtilsTest {

    @Test
    void checkRequestIsAjaxRequest() {
        var request = MockServerHttpRequest
            .method(HttpMethod.POST, "")
            .build();
        assertTrue(HaloUtils.isAjaxRequest(request));

        request.getHeaders().set("x-requested-with", "fake-request");
        assertFalse(HaloUtils.isAjaxRequest(request));

        request.getHeaders().set("x-requested-with", "XMLHttpRequest");
        assertTrue(HaloUtils.isAjaxRequest(request));
    }

}