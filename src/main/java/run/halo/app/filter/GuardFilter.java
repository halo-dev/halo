package run.halo.app.filter;

import org.springframework.web.filter.GenericFilterBean;
import run.halo.app.security.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author johnniang
 * @date 19-4-30
 */
public class GuardFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // Do filter
        chain.doFilter(request, response);

        // Clear security context
        SecurityContextHolder.clearContext();
    }
}
