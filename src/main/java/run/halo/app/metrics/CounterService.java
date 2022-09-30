package run.halo.app.metrics;

import run.halo.app.core.extension.Counter;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface CounterService {

    Counter getByName(String counterName);
}
