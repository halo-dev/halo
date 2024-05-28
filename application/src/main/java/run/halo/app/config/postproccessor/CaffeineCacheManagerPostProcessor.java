package run.halo.app.config.postproccessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Post-processes of {@link CaffeineCacheManager} beans to enabled async mode for them after
 * initialization.
 *
 * @author sergei
 * @see CaffeineCacheManager#setAsyncCacheMode(boolean)
 */
@Component
public class CaffeineCacheManagerPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
        throws BeansException {
        if (bean instanceof CaffeineCacheManager cacheManager) {
            configure(cacheManager);
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private void configure(CaffeineCacheManager caffeineCacheManager) {
        caffeineCacheManager.setAsyncCacheMode(true);
    }
}
