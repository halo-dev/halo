package run.halo.app.security;

import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Mono;
import run.halo.app.security.authorization.AuthorityUtils;

/**
 * Super admin initializer.
 *
 * @author guqing
 * @since 2.9.0
 */
public interface SuperAdminInitializer {

    String SUPER_ROLE_NAME = AuthorityUtils.SUPER_ROLE_NAME;

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
