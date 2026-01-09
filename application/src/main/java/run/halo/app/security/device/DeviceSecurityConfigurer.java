package run.halo.app.security.device;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.stereotype.Component;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
@RequiredArgsConstructor
class DeviceSecurityConfigurer implements SecurityConfigurer {

    private final DeviceService deviceService;

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter = new DeviceSessionFilter(deviceService);
        http.addFilterAfter(filter, SecurityWebFiltersOrder.REACTOR_CONTEXT);
    }
}
