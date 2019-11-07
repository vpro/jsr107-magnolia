package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;

import java.util.HashMap;
import java.util.Map;

import javax.cache.annotation.*;

import org.jsr107.ri.annotations.DefaultGeneratedCacheKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.vpro.magnolia.jsr107.mock.MockCacheFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class CacheConfigurerTest extends AbstractJSR107Test {


    public static class TestClass {
        int count = 0;

        int constant = 0;

        @CacheResult(cacheName = "counts")
        public  Integer getCachedCount(String key) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
            return count++;
        }
        @CachePut(cacheName = "counts")
        public void setCachedCount(
            @CacheKey String key,
            @javax.cache.annotation.CacheValue  Integer count) {

        }
        public void multiPut(Map<String, Integer> values) {
            for (Map.Entry<String, Integer> e : values.entrySet()) {
                setCachedCount(e.getKey(), e.getValue());
            }
        }
        public Integer getCachedCountIndirect(String key) {
            return getCachedCount(key);
        }
        @CacheResult(cacheName = "constants", cacheKeyGenerator = MethodKey.class)
        public int constant() {
            return constant++;
        }

        @CacheResult(
            cacheName = "exception",
            exceptionCacheName = "exception.exception",
            cacheKeyGenerator = MethodKey.class)
        public int exception() {
            if (count++ % 2 == 0) {
                throw new RuntimeException("bla" + count);
            } else {
                return count;
            }
        }


        @CacheResult(
            cacheName = "exceptionWithoutCache")
        public int exceptionWithoutExceptionCache() {
            if (count++ % 2 == 0) {
                throw new RuntimeException("bla" + count);
            } else {
                return count;
            }
        }
        @CacheResult(cacheName = "null", cacheKeyGenerator = MethodKey.class)
        public String nulls() {
            return count++ % 2 == 0 ? null : "string";

        }
    }
    TestClass instance;

    @BeforeEach
    public void setup() {
        instance = injector.getInstance(TestClass.class);
    }

    @Test
    public void testCache() {
        assertEquals(Integer.valueOf(0), instance.getCachedCount("a"));
        assertEquals(Integer.valueOf(0), instance.getCachedCount("a"));

        cacheManager.getCache("counts").clear();
        assertEquals(Integer.valueOf(1), instance.getCachedCount("a"));
        assertEquals(Integer.valueOf(2), instance.getCachedCount("b"));
    }


    @Test
    public void testCacheIndirect() {
        instance.setCachedCount("x", 11);
        assertEquals(Integer.valueOf(11), instance.getCachedCountIndirect("x"));
        cacheManager.getCache("counts").clear();
        assertEquals(Integer.valueOf(0), instance.getCachedCountIndirect("x"));
    }

    @Test
    public void testCachePut() {
        instance.setCachedCount("a", 10);
        assertEquals(Integer.valueOf(10), cacheManager.getValue("counts", new DefaultGeneratedCacheKey(new Object[]{"a"})));
        assertEquals(Integer.valueOf(10), cacheManager.getValue(TestClass.class, instance, "getCachedCount", "a"));

        assertEquals(Integer.valueOf(10), instance.getCachedCount("a"));
    }


    @Test
    public void testMultiPut() {
        Map<String, Integer> map = new HashMap<>();
        map.put("y", 5);
        map.put("z", 6);
        instance.multiPut(map);
        assertEquals(Integer.valueOf(5), instance.getCachedCount("y"));
        assertEquals(Integer.valueOf(6), instance.getCachedCount("z"));
    }

    @Test
    public void testCachePutNull() {
        instance.setCachedCount("a", null);
        //
        assertNull(instance.getCachedCount("a"));
    }

    @Test
    public void testNulls() {
        assertNull(instance.nulls());
        assertNull(instance.nulls());
        cacheManager.getCache("null").clear();
        assertEquals("string", instance.nulls());
        cacheManager.getCache("null").clear();
        assertNull(instance.nulls());
    }


    @Test
    public void testExceptions() {
        try {
            instance.exception();
            fail();
        } catch (RuntimeException rt) {
            assertEquals("bla1", rt.getMessage());
        }
        try {
            instance.exception();
            fail();
        } catch (RuntimeException rt) {
            assertEquals("bla1", rt.getMessage());
        }
        cacheManager.getCache("exception.exception").clear();

        assertEquals(2, instance.exception());
        assertEquals(2, instance.exception());

        cacheManager.getCache("exception").clear();

        try {
            instance.exception();
            fail();
        } catch (RuntimeException rt) {
            assertEquals("bla3", rt.getMessage());
        }

    }

    @Test
    public void testExceptionsWithoutCaching() {
        try {
            instance.exceptionWithoutExceptionCache();
            fail();
        } catch (RuntimeException rt) {
            assertEquals("bla1", rt.getMessage());
        }
        // exceptions are not cached, so the next call will evaluate again
        assertEquals(2, instance.exceptionWithoutExceptionCache());
        // but that _is_ cached_
        assertEquals(2, instance.exceptionWithoutExceptionCache());

        cacheManager.getCache("exceptionWithoutCache").clear();


        try {
            instance.exceptionWithoutExceptionCache();
            fail();
        } catch (RuntimeException rt) {
            assertEquals("bla3", rt.getMessage());
        }

    }
    @Test
    public void testUnwrap() {
        assertEquals(MockCacheFactory.class, cacheManager.unwrap(CacheFactory.class).getClass());

    }

    @Test
    public void testConstantCacheKeyGenerator() {
        assertEquals(0, instance.constant());
        assertEquals(0, instance.constant());
        System.out.println(f.caches.get("constants").getKeys().iterator().next());
    }

    // Checking whether the cache is 'blocking'
    // See for similar spring issue: https://jira.spring.io/browse/SPR-9254
    // https://groups.google.com/forum/m/?fromgroups#!topic/spring-framework-contrib/bVjdVHhZci8
    @Test
    public void testConcurrency() throws InterruptedException {
        int expected = 0;
        int max = 10;
        Thread[] threads = new Thread[max];
        Thread[] threads2 = new Thread[max];

        cacheManager.getCache("counts").clear();
        int[] results = new int[max];
        for (int i = 0; i < max; i++) {
            final int j = i;
            threads[i] =
                new Thread(() -> {
                    try {
                        Thread.sleep(6);
                        results[j] = instance.getCachedCount("a");
                        System.out.println(j + " value:" + results[j]);
                    } catch (InterruptedException ignored) {
                    }
                });
            threads[i].start();
            threads2[i] =
                new Thread(() -> {
                    Integer value = (Integer) cacheManager.getValue("counts", new DefaultGeneratedCacheKey(new Object[]{"a"}));
                    Integer value2 = (Integer) cacheManager.getValue(TestClass.class, instance, "getCachedCount", "a");

                    System.out.println(j + " found:" + value + " " + value2);

                });
            threads2[i].start();
            Thread.sleep(2);
        }
        for (int i = 0; i < max; i++) {
            threads[i].join();
            threads2[i].join();
            assertEquals(expected, results[i]);
        }
    }

}
