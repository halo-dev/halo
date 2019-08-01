package run.halo.app.cache;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * InMemoryCacheStoreTest.
 *
 * @author johnniang
 * @date 3/28/19
 */
public class InMemoryCacheStoreTest {

    private InMemoryCacheStore cacheStore;

    @Before
    public void setUp() {
        cacheStore = new InMemoryCacheStore();
    }

    @Test(expected = IllegalArgumentException.class)
    public void putNullValueTest() {
        String key = "test_key";

        cacheStore.put(key, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putNullKeyTest() {
        String value = "test_value";

        cacheStore.put(null, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getByNullKeyTest() {
        cacheStore.get(null);
    }


    @Test
    public void getNullTest() {
        String key = "test_key";

        Optional<String> valueOptional = cacheStore.get(key);

        assertFalse(valueOptional.isPresent());
    }

    @Test
    public void expirationTest() throws InterruptedException {
        String key = "test_key";
        String value = "test_value";
        cacheStore.put(key, value, 500, TimeUnit.MILLISECONDS);

        Optional<String> valueOptional = cacheStore.get(key);

        assertTrue(valueOptional.isPresent());
        assertThat(valueOptional.get(), equalTo(value));

        TimeUnit.SECONDS.sleep(1L);

        valueOptional = cacheStore.get(key);

        assertFalse(valueOptional.isPresent());
    }

    @Test
    public void deleteTest() {
        String key = "test_key";
        String value = "test_value";

        // Put the cache
        cacheStore.put(key, value);

        // Get the caceh
        Optional<String> valueOptional = cacheStore.get(key);

        // Assert
        assertTrue(valueOptional.isPresent());
        assertThat(valueOptional.get(), equalTo(value));

        // Delete the cache
        cacheStore.delete(key);

        // Get the cache again
        valueOptional = cacheStore.get(key);

        // Assertion
        assertFalse(valueOptional.isPresent());
    }
}
