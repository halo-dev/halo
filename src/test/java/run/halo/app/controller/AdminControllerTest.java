package run.halo.app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import run.halo.app.controller.admin.api.AdminController;
import run.halo.app.core.ControllerExceptionHandler;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.LoginPreCheckDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.ResetPasswordParam;
import run.halo.app.model.params.ResetPasswordSendCodeParam;
import run.halo.app.security.token.AuthToken;
import run.halo.app.service.AdminService;
import run.halo.app.utils.JsonUtils;

/**
 * Admin controller test.
 *
 * @author guqing
 * @date 2022-02-11
 */
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private static final String FIELD_ERROR_MESSAGE = "字段验证错误，请完善后重试！";

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        AdminController adminController =
            new AdminController(new MockAdminService(), null);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
            .setControllerAdvice(ControllerExceptionHandler.class)
            .addFilter((request, response, chain) -> {
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                chain.doFilter(request, response);
            })
            .build();
    }

    @Test
    public void sendResetPasswordCodeShouldOk() throws Exception {
        ResetPasswordSendCodeParam param = new ResetPasswordSendCodeParam();
        param.setEmail("example@example.com");
        param.setUsername("admin");
        sendResetPasswordCodePerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void sendResetPasswordCodeShould4xxError() throws Exception {
        ResetPasswordSendCodeParam param = new ResetPasswordSendCodeParam();
        param.setEmail("example@example.com");
        sendResetPasswordCodePerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value(FIELD_ERROR_MESSAGE));
    }

    @Test
    public void sendResetPasswordCodeShould4xxErrorToo() throws Exception {
        ResetPasswordSendCodeParam param = new ResetPasswordSendCodeParam();
        param.setUsername("admin");
        sendResetPasswordCodePerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value(FIELD_ERROR_MESSAGE));
    }

    @Test
    public void resetPasswordShouldOk() throws Exception {
        ResetPasswordParam param = new ResetPasswordParam();
        param.setUsername("admin");
        param.setEmail("example@example.com");
        param.setPassword("a password");
        param.setCode("a code");
        param.setPassword("12345678");
        resetPasswordPerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void resetPasswordAndEmailFieldAbsentShouldReturn4xxError() throws Exception {
        ResetPasswordParam param = new ResetPasswordParam();
        // The email field value in the parent class is missing，verification will also be triggered
        param.setUsername("admin");
        param.setCode("a code");
        param.setPassword("a password");
        resetPasswordPerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value(FIELD_ERROR_MESSAGE));
    }

    @Test
    public void resetPasswordAndCodeFieldAbsentShouldReturn4xxError() throws Exception {
        ResetPasswordParam param = new ResetPasswordParam();
        // The code field value in the param class is missing，verification will also be triggered
        param.setUsername("admin");
        param.setEmail("example@example.com");
        param.setPassword("a password");
        resetPasswordPerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value(FIELD_ERROR_MESSAGE));
    }

    @Test
    public void resetPasswordAndInsufficientPasswordLengthShouldReturn4xxError() throws Exception {
        ResetPasswordParam param = new ResetPasswordParam();
        // The code field value in the param class is missing，verification will also be triggered
        param.setUsername("admin");
        param.setEmail("example@example.com");
        param.setCode("a code");
        param.setPassword("123");
        resetPasswordPerform(JsonUtils.objectToJson(param))
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value(FIELD_ERROR_MESSAGE));
    }

    @NotNull
    private ResultActions resetPasswordPerform(String param)
        throws Exception {
        return this.mockMvc.perform(put("/api/admin/password/reset")
            .content(param)
            .contentType(MediaType.APPLICATION_JSON));
    }

    @NotNull
    private ResultActions sendResetPasswordCodePerform(String param)
        throws Exception {
        return this.mockMvc.perform(post("/api/admin/password/code")
            .content(param)
            .contentType(MediaType.APPLICATION_JSON));
    }

    public static class MockAdminService implements AdminService {

        @Override
        public User authenticate(LoginParam loginParam) {
            return null;
        }

        @Override
        public AuthToken authCodeCheck(LoginParam loginParam) {
            return null;
        }

        @Override
        public void clearToken() {

        }

        @Override
        public void sendResetPasswordCode(ResetPasswordSendCodeParam param) {

        }

        @Override
        public void resetPasswordByCode(ResetPasswordParam param) {

        }

        @Override
        public EnvironmentDTO getEnvironments() {
            return null;
        }

        @Override
        public AuthToken refreshToken(String refreshToken) {
            return null;
        }

        @Override
        public String getLogFiles(Long lines) {
            return null;
        }

        @Override
        public LoginPreCheckDTO getUserEnv(String username) {
            return null;
        }
    }
}
