package run.halo.app.infra;

import reactor.core.publisher.Mono;

/**
 * <p>A interface that get system initialization state.</p>
 *
 * @author guqing
 * @since 2.9.0
 */
public interface InitializationStateGetter {

    /**
     * Check if system user is initialized.
     *
     * @return <code>true</code> if system user is initialized, <code>false</code> otherwise.
     */
    Mono<Boolean> userInitialized();

    /**
     * Check if system basic data is initialized.
     *
     * @return <code>true</code> if system basic data is initialized, <code>false</code> otherwise.
     */
    Mono<Boolean> dataInitialized();
}
