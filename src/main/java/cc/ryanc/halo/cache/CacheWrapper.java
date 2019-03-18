package cc.ryanc.halo.cache;

import lombok.*;

import java.util.Date;

/**
 * Cache wrapper.
 *
 * @author johnniang
 */
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CacheWrapper<T> {

    /**
     * Cache key.
     */
    private String key;

    /**
     * Cache data
     */
    private T data;

    /**
     * Expired time.
     */
    private Date expireAt;

    /**
     * Create time.
     */
    private Date createAt;
}
