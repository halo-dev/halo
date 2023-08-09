package run.halo.app.security;

import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Mono;

/**
 * Super admin initializer.
 *
 * @author guqing
 * @since 2.9.0
 */
public interface SuperAdminInitializer {

    /**
     * Initialize super admin.
     *
     * @param param super admin initialization param
     */
    Mono<Void> initialize(InitializationParam param);

    @Data
    @Builder
    class InitializationParam {
        private String username;
        private String password;
        private String email;
    }
}
