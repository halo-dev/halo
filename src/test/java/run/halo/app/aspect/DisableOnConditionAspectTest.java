package run.halo.app.aspect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.service.OptionService;

import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author guqing
 * @date 2020-02-14 17:06
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "halo.auth-enabled=false")
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class DisableOnConditionAspectTest {

    static final String REQUEST_URI = "/api/admin/test/disableOnCondition";

    @Autowired
    MockMvc mvc;

    @SpyBean
    OptionService optionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Assertions.assertNotNull(optionService);
        Mockito.doReturn(true).when(optionService).getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
    }

    @Test()
    void blockAccessTest() throws Exception {
        Throwable t = null;
        try {
            mvc.perform(get(REQUEST_URI + "/no"))
                    .andDo(print())
                    .andReturn();
        } catch (NestedServletException nse) {
            t = nse;
        }
        Assertions.assertNotNull(t);
        final Throwable rootException = t;
        Assertions.assertThrows(ForbiddenException.class, () -> {
            throw rootException.getCause();
        });
    }

    @Test
    void ableAccessTest() throws Exception {
        mvc.perform(get(REQUEST_URI + "/yes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())));
    }
}
