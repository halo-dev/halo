package run.halo.app.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import run.halo.app.wrapper.XssHttpServletRequestWrapper;

/**
 * @author: Ljfanny, Yhcrown
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class XssFilter extends GenericFilterBean {

    @Value("${xss.enabled}")
    private String tempEnabled;

    @Value("${xss.excludes}")
    private String tempExcludes;

    private boolean enabled;

    private List<String> excludes = new ArrayList<>();

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        enabled = Boolean.parseBoolean(tempEnabled);
        String[] urls = tempExcludes.split(",");
        Collections.addAll(excludes, urls);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!checkXss(req)) {
            XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(req);
            chain.doFilter(xssRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * check if there is need to do filter.
     */
    private boolean checkXss(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        if (excludes.isEmpty() || excludes.get(0).length() == 0) {
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
