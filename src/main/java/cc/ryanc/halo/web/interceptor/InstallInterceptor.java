package cc.ryanc.halo.web.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/28
 * description : 安装拦截器
 */
@Component
public class InstallInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
        File installFile = new File(basePath.getAbsolutePath(), "install.lock");
        if(installFile.exists()){
            return true;
        }
        response.sendRedirect("/install");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception { }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception { }
}
