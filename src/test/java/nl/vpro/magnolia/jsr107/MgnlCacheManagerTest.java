package nl.vpro.magnolia.jsr107;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.Iterator;

import javax.cache.annotation.CacheResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@Log4j2
public class MgnlCacheManagerTest extends AbstractJSR107Test {


    public static class TestClass {
        int count = 0;
        @CacheResult(cacheName = "counts")
        public Integer getCachedCount(String key) {
            return count++;
        }

        @CacheResult(cacheName = "counts2")
        public Integer getCachedCount(String key, String key2) {
            return count++;
        }

    }

    MgnlCacheManagerTest.TestClass instance;

    @BeforeEach
    public void setup() {
        instance = injector.getInstance(MgnlCacheManagerTest.TestClass.class);


    }

    @Test
    public void getValue() {
        assertThat(instance.getCachedCount("a")).isEqualTo(0);
        long start = System.nanoTime();
        for (int i= 0; i < 10000;i++) {
            assertThat(cacheManager.getValue(TestClass.class, instance, "getCachedCount", "a")).isEqualTo(0);
        }
        log.info("duration: {}", () -> Duration.ofNanos(System.nanoTime() - start));
    }

    @Test
    public void getKeys() {
        instance.getCachedCount("a", "b");
        instance.getCachedCount("c", "d");

        Iterator<Object[]> keys = cacheManager.getKeys(TestClass.class, instance, "getCachedCount", String.class, String.class);
        assertThat(keys.hasNext()).isTrue();
        assertThat(keys.next()).containsExactly("a", "b");
        assertThat(keys.next()).containsExactly("c", "d");
        assertThat(keys.hasNext()).isFalse();

    }

}
