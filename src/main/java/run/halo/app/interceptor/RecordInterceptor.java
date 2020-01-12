package run.halo.app.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import run.halo.app.service.RequestRecordService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RecordInterceptor implements HandlerInterceptor {

    private final RequestRecordService recordService;

    public RecordInterceptor(RequestRecordService recordService) {
        this.recordService = recordService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        recordService.save(request);
        return true;
    }
}
