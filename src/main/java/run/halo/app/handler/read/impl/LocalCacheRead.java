package run.halo.app.handler.read.impl;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.handler.read.ReadStorage;
import run.halo.app.model.entity.BasePost;
import run.halo.app.service.base.BasePostService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: HeHui
 * @date: 2020-07-21 16:28
 * @description: local cache impl read
 * @see AbstractStringCacheStore
 */
@Slf4j
public class LocalCacheRead<POST extends BasePost> extends ReadAbstract<Long, Integer> {

    private final AbstractStringCacheStore stringCacheStore;

    private final String cacheKey;

    /**
     * build
     *
     * @param maxRead          maximum number of readings
     * @param jobSeconds       timing seconds
     * @param stringCacheStore local cache impl
     * @param basePostService  used to build {@link VisitReadStorage}
     * @param cacheKey
     */
    public LocalCacheRead(long maxRead, int jobSeconds, AbstractStringCacheStore stringCacheStore, BasePostService basePostService, String cacheKey) {
        super(maxRead, jobSeconds, new VisitReadStorage(basePostService, cacheKey));
        this.stringCacheStore = stringCacheStore;
        this.cacheKey = cacheKey;
    }


    /**
     * increase
     *
     * @param key reading info unique identifier
     * @param n   increase num
     * @return {@link Long} result read after num
     */
    @Override
    protected Optional<Long> increase(Integer key, Long n) {
        Map<Integer, Long> cache = getCache();
        if (cache == null) {
            cache = new HashMap<>(128);
        }
        Long oldNum = cache.getOrDefault(key, 0L);
        cache.put(key, oldNum + n);

        stringCacheStore.putAny(cacheKey, cache);

        return Optional.ofNullable(oldNum + n);
    }


    /**
     * reduce
     *
     * @param key reading info unique identifier
     * @param n   num
     */
    @Override
    protected void reduce(Integer key, Long n) {
        Map<Integer, Long> cache = getCache();
        if (MapUtil.isNotEmpty(cache)) {
            Long num = cache.get(key);
            if (num != null) {
                if (num <= n) {
                    cache.remove(key);
                } else {
                    cache.put(key, num - n);
                }
                stringCacheStore.putAny(cacheKey, cache);
            }
        }
    }

    /**
     * get all info increase
     *
     * @return {@link Map<Integer, Long>}
     */
    @Override
    protected Optional<Map<Integer, Long>> getAll() {
        return Optional.ofNullable(getCache());
    }

    /**
     * clear all
     */
    @Override
    protected void clear() {
        stringCacheStore.delete(cacheKey);
    }

    /**
     * find info increase
     *
     * @param key info unique identifier
     * @return {@link Optional<Long>}
     */
    @Override
    public Optional<Long> getRead(Integer key) {
        return Optional.empty();
    }


    /**
     * find more info increase
     *
     * @param keys info unique identifier
     * @return {@link Optional<Map<Integer,Long>>}
     */
    @Override
    public Optional<Map<Integer, Long>> getReads(List<Integer> keys) {
        Map<Integer, Long> map = getCache();
        if (MapUtil.isNotEmpty(map)) {
            Optional.ofNullable(map.entrySet().stream().filter(v -> keys.contains(v.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return Optional.empty();
    }


    /**
     * get cache
     *
     * @return
     */
    private Map<Integer, Long> getCache() {
        Optional<Map> optional = stringCacheStore.getAny(cacheKey, Map.class);
        if (optional.isPresent()) {
            Map<String, Integer> map = optional.get();
            return map.entrySet().stream().collect(Collectors.toMap(v -> Integer.valueOf(v.getKey()), i -> Long.valueOf(i.getValue())));
        }
        return null;
    }


    /**
     * visit read update
     */
    static class VisitReadStorage implements ReadStorage<Long, Integer> {
        private BasePostService postService;
        private String name;

        VisitReadStorage(BasePostService basePostService, String name) {
            this.postService = basePostService;
            this.name = name;
        }


        @Override
        public void increase(Integer posId, Long n) {
            postService.increaseVisit(n.longValue(), posId);
            log.info("{} 新增阅读数:{},postId:{}", name, n, posId);

        }

        @Override
        public void increase(Map<Integer, Long> map) {
            log.info("{}: 定时清除阅读数", name);
            try {
                postService.increaseListVisit(map);
            } catch (Exception e) {
                log.error("{} 定时清除阅读数异常:", name, e);
            }
        }
    }
}
