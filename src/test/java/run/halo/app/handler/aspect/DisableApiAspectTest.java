package run.halo.app.handler.aspect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import run.halo.app.security.service.OneTimeTokenService;

import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author guqing
 * @date 2020-02-14 17:06
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class DisableApiAspectTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    OneTimeTokenService oneTimeTokenService;

    static final String REQUEST_URI = "/api/admin/options/1";

    @Test
    void deleteOptionTest() throws Exception {
        String ott = oneTimeTokenService.create(REQUEST_URI);
        mvc.perform(delete(REQUEST_URI + "?ott={ott}", ott))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())));
    }
}
