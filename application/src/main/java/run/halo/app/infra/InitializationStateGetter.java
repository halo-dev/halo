package run.halo.app.infra;

import reactor.core.publisher.Mono;

/**
 * A interface that get system initialization state.
 *
 * @author guqing
 * @since 2.9.0
 */
public interface InitializationStateGetter {

    /**
     * Check if system setup is required.
     *
     * <p>System setup is required if the system has never been set up, or if a previous setup was interrupted before it
     * was completed.
     *
     * @return <code>true</code> if system setup is required, <code>false</code> otherwise.
     */
    Mono<Boolean> setupRequired();
}
