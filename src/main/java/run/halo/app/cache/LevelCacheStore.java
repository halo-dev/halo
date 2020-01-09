package run.halo.app.cache;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import run.halo.app.utils.JsonUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * level-db cache store
 * Create by Pencilso on 2020/1/8 9:58 下午
 */
@Slf4j
public class LevelCacheStore extends StringCacheStore {

    @Value("${leveldb.storefolder:~/.halo/leveldb/}")
    private String folderPath;

    /**
     * Cleaner schedule period. (ms)
     */
    private final static long PERIOD = 60 * 1000;

    private DB leveldb;

    private Timer timer;


    @PostConstruct
    public void init() throws IOException {
        // 标准化路径
        folderPath = FileUtil.normalize(folderPath);
        folderPath = StrUtil.removePrefix(folderPath, StrUtil.SLASH);

        File folder = new File(this.folderPath);
        folder.mkdirs();
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        options.createIfMissing(true);
        //folder 是db存储目录
        leveldb = factory.open(folder, options);
        timer = new Timer();
        timer.scheduleAtFixedRate(new CacheExpiryCleaner(), 0, PERIOD);
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void preDestroy() {
        try {
            leveldb.close();
            timer.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        Assert.hasText(key, "Cache key must not be blank");
        byte[] bytes = leveldb.get(stringToBytes(key));
        if (bytes != null) {
            String valueJson = bytesToString(bytes);
            return StringUtils.isEmpty(valueJson) ? Optional.empty() : jsonToCacheWrapper(valueJson);
        }
        return Optional.empty();
    }

    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        putInternalIfAbsent(key, cacheWrapper);
    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");
        try {
            leveldb.put(
                    stringToBytes(key),
                    stringToBytes(JsonUtils.objectToJson(cacheWrapper))
            );
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.debug("cache key: [{}], original cache wrapper: [{}]", key, cacheWrapper);
        return false;
    }

    @Override
    public void delete(String key) {
        leveldb.delete(stringToBytes(key));
    }


    private byte[] stringToBytes(String str) {
        return str.getBytes(Charset.defaultCharset());
    }

    private String bytesToString(byte[] bytes) {
        return new String(bytes, Charset.defaultCharset());
    }

    private Optional<CacheWrapper<String>> jsonToCacheWrapper(String json) {
        Assert.hasText(json, "json value must not be null");
        CacheWrapper<String> cacheWrapper = null;
        try {
            cacheWrapper = JsonUtils.jsonToObject(json, CacheWrapper.class);
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("cache value bytes: [{}]", json);
        }
        return Optional.ofNullable(cacheWrapper);
    }

    private class CacheExpiryCleaner extends TimerTask {

        @Override
        public void run() {
            //batch
            WriteBatch writeBatch = leveldb.createWriteBatch();

            DBIterator iterator = leveldb.iterator();
            long currentTimeMillis = System.currentTimeMillis();
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> next = iterator.next();
                if (next.getKey() == null || next.getValue() == null) continue;
                String valueJson = bytesToString(next.getValue());
                Optional<CacheWrapper<String>> stringCacheWrapper = StringUtils.isEmpty(valueJson) ? Optional.empty() : jsonToCacheWrapper(valueJson);
                if (stringCacheWrapper.isPresent()) {
                    Long expireAtTime = stringCacheWrapper
                            .map(CacheWrapper::getExpireAt)
                            .map(Date::getTime)
                            .orElse(0L);
                    if (expireAtTime != 0 && currentTimeMillis > expireAtTime) {
                        writeBatch.delete(next.getKey());
                        log.debug("Deleted the cache: [{}] for expiration", bytesToString(next.getKey()));
                    }
                }

            }

            leveldb.write(writeBatch);
        }
    }
}
