package cc.ryanc.halo.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Authentication failure handler.
 *
 * @author johnniang
 */
public class AdminAuthenticationFailureHandler extends DefaultAuthenticationFailureHandler {

    public AdminAuthenticationFailureHandler(boolean productionEnv, ObjectMapper objectMapper) {
        super(productionEnv, objectMapper);
    }

//    @Override
//    public void onFailure(HttpServletRequest request, HttpServletResponse response, HaloException exception) throws IOException, ServletException {
//        // TODO handler the admin authentication failure.
//    }
}
