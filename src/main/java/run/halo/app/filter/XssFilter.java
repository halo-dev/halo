package run.halo.app.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.filter.GenericFilterBean;
import run.halo.app.wrapper.XssHttpServletRequestWrapper;

/**
 * 防止XSS攻击的过滤器
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class XssFilter extends GenericFilterBean {
    private List<String> excludes = new ArrayList<>();
    private boolean enabled = true;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        // for (String key : req.getParameterMap().keySet()) {
        //     System.out.println(key + ": " + Arrays.toString(req.getParameterMap().get(key)));
        // }
        XssHttpServletRequestWrapper xssRequest =
            new XssHttpServletRequestWrapper(req);
        // for (String key : req.getParameterMap().keySet()) {
        //     System.out.println(key + ": " + Arrays.toString(req.getParameterMap().get(key)));
        // }
        chain.doFilter(xssRequest, response);
    }

    /**
     * 判断当前路径是否需要过滤
     */
    private boolean handleExcludeUrl(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        if (excludes == null || excludes.isEmpty()) {
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : excludes) {
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }
}
