package cc.ryanc.halo.filter;

import cc.ryanc.halo.logging.Logger;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for logging.
 *
 * @author johnniang
 */
public class LogFilter extends OncePerRequestFilter {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String remoteAddr = ServletUtil.getClientIP(request);

        logger.debug("");
        logger.debug("Starting url: [{}], method: [{}], ip: [{}]", request.getRequestURL(), request.getMethod(), remoteAddr);

        // Set start time
        long startTime = System.currentTimeMillis();

        // Do filter
        filterChain.doFilter(request, response);

        logger.debug("Ending   url: [{}], method: [{}], ip: [{}], status: [{}], usage: [{}] ms", request.getRequestURL(), request.getMethod(), remoteAddr, response.getStatus(), (System.currentTimeMillis() - startTime));
        logger.debug("");
    }
}
