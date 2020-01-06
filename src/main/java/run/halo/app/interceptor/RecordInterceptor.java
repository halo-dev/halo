package run.halo.app.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import run.halo.app.service.impl.RequestRecordServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RecordInterceptor implements HandlerInterceptor {

    private final RequestRecordServiceImpl recordService;

    public RecordInterceptor(RequestRecordServiceImpl recordService) {
        this.recordService = recordService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        recordService.save(request);
        return true;
    }
}
