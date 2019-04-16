package run.halo.app.security.filter;

import org.springframework.util.AntPathMatcher;
import run.halo.app.config.properties.HaloProperties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Api authentication Filter
 *
 * @author johnniang
 */
public class ApiAuthenticationFilter extends AbstractAuthenticationFilter {

    private final AntPathMatcher antPathMatcher;

    public ApiAuthenticationFilter(HaloProperties haloProperties) {
        super(haloProperties);
        antPathMatcher = new AntPathMatcher();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO Handle authentication
        filterChain.doFilter(request, response);
    }
}
