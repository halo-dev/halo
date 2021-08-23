package run.halo.app.service;

/**
 * User client option service.
 *
 * @author LIlGG
 * @date 2021/8/2
 */
public interface ClientOptionService extends OptionProvideService {
    /**
     * Flushes all pending changes to the database.
     */
    void flush();
}
