package run.halo.app.handler.read;

import java.util.Map;

/**
 * @author: HeHui
 * @date: 2020-07-20 14:57
 * @description: info read storage
 */
public interface ReadStorage<T extends Number,ID>  {

    /**
     *  increase
     *
     * @param key reading info unique identifier
     * @param n   increase num
     */
    void increase(ID key,T n);


    /**
     * increase all
     *
     * @param data  info increase  key -> info unique identifier, value-> increase num
     */
    void increase(Map<ID,T> data);
}
