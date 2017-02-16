package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import info.magnolia.module.cache.mbean.CacheMonitor;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheResult;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.vpro.magnolia.jsr107.mock.MockCacheFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class CacheConfigurerTest {


    public static class TestClass {
        int count = 0;

        int constant = 0;

        @CacheResult(cacheName = "counts")
        public  int getCachedCount(String key) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
            return count++;
        }
        @CachePut(cacheName = "counts")
        public void setCachedCount(
            @CacheKey String key,
            @javax.cache.annotation.CacheValue  int count) {

        }
        @CacheResult(cacheName = "constants", cacheKeyGenerator = MethodKey.class)
        public int constant() {
            return constant++;
        }

        @CacheResult(cacheName = "exception", cacheKeyGenerator = MethodKey.class)
        public int exception() {
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
    CacheManager cacheManager;
    MockCacheFactory f;

    @Before
    public void setup() {

        Injector injector = Guice.createInjector(new CacheConfigurer(), new AbstractModule() {
            @Override
            protected void configure() {
                f = new MockCacheFactory(true);
                CacheFactoryProvider fp = mock(CacheFactoryProvider.class);
                when(fp.get()).thenReturn(f);
                binder().bind(CacheFactoryProvider.class).toInstance(fp);
                binder().bind(CacheMonitor.class).toInstance(mock(CacheMonitor.class));
            }
        });

        instance = injector.getInstance(TestClass.class);
        cacheManager = injector.getInstance(CacheManager.class);
    }

    @Test
    public void testCache() {
        assertEquals(0, instance.getCachedCount("a"));
        assertEquals(0, instance.getCachedCount("a"));
        cacheManager.getCache("counts").clear();
        assertEquals(1, instance.getCachedCount("a"));
        assertEquals(2, instance.getCachedCount("b"));
    }


    @Test
    @Ignore("Failing.")
    public void testCachePut() {
        instance.setCachedCount("a", 10);
        assertEquals(10, instance.getCachedCount("a"));
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
        cacheManager.getCache("exception").clear();

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
        int[] results = new int[max];
        for (int i = 0; i < max; i++) {
            final int j = i;
            threads[i] =
                new Thread(() -> {
                    results[j] = instance.getCachedCount("a");
                    System.out.println(j + ":" + results[j]);
                });
            threads[i].start();
            Thread.sleep(2);
        }
        for (int i = 0; i < max; i++) {
            threads[i].join();
            assertEquals(expected, results[i]);
        }
    }

}
