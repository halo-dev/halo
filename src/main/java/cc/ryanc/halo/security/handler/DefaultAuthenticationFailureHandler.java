package cc.ryanc.halo.security.handler;

import cc.ryanc.halo.exception.HaloException;
import cc.ryanc.halo.model.support.BaseResponse;
import cc.ryanc.halo.utils.ExceptionUtils;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default AuthenticationFailureHandler.
 *
 * @author johnniang
 * @date 12/12/18
 */
@Slf4j
public class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final boolean productionEnv;

    private final ObjectMapper objectMapper;

    public DefaultAuthenticationFailureHandler(boolean productionEnv,
                                               ObjectMapper objectMapper) {
        this.productionEnv = productionEnv;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onFailure(HttpServletRequest request, HttpServletResponse response, HaloException exception) throws IOException, ServletException {
        log.warn("Handle unsuccessful authentication, ip: [{}]", ServletUtil.getClientIP(request));
        log.error("Authentication failure", exception);

        BaseResponse<Object> errorDetail = new BaseResponse<>();

        errorDetail.setStatus(exception.getStatus().value());
        errorDetail.setMessage(exception.getMessage());
        errorDetail.setData(exception.getErrorData());

        if (!productionEnv) {
            errorDetail.setDevMessage(ExceptionUtils.getStackTrace(exception));
        }

        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(exception.getStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(errorDetail));
    }

}
