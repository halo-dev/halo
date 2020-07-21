package run.halo.app.handler.read;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: HeHui
 * @date: 2020-07-20 11:51
 * @description: smooth reading interface
 */
public interface Read<T extends Number, ID> {


    /**
     * reading
     *
     * @param key      reading info unique identifier
     * @param n        increase
     * @param clientID If client restrictions need to be increased (not required)
     */
    void read(ID key, T n, String clientID);


    /**
     * find info increase
     *
     * @param key info unique identifier
     * @return {@link Optional<T>}
     */
    Optional<T> getRead(ID key);


    /**
     * find more info increase
     *
     * @param keys info unique identifier
     * @return {@link Optional<Map<ID,T>>}
     */
    Optional<Map<ID, T>> getReads(List<ID> keys);
}
