package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.mbean.CacheMonitor;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheResult;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.vpro.magnolia.jsr107.mock.MgnlCacheFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class CacheConfigurerTest {


    public static class TestClass {
        int count = 0;

        @CacheResult(cacheName = "counts")
        public  int getCachedCount() {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
            return count++;
        }
    }
    TestClass instance;
    CacheManager cacheManager;

    @Before
    public void setup() {

        Injector injector = Guice.createInjector(new CacheConfigurer(), new AbstractModule() {
            @Override
            protected void configure() {
                binder().bind(CacheFactory.class).toInstance(new MgnlCacheFactory());
                binder().bind(CacheMonitor.class).toInstance(mock(CacheMonitor.class));

            }
        });

        instance = injector.getInstance(TestClass.class);
        cacheManager = injector.getInstance(CacheManager.class);
    }

    @Test
    public void testCache() {
        assertEquals(0, instance.getCachedCount());
        assertEquals(0, instance.getCachedCount());
        cacheManager.getCache("counts").clear();
        assertEquals(1, instance.getCachedCount());
    }
    
    @Test
    public void testUnwrap() {
        assertEquals(MgnlCacheFactory.class, cacheManager.unwrap(MgnlCacheFactory.class).getClass());
        
    }

    // Checking whether the cache is 'blocking'
    // -> it isn't.
    // See for similar spring issue: https://jira.spring.io/browse/SPR-9254
    // https://groups.google.com/forum/m/?fromgroups#!topic/spring-framework-contrib/bVjdVHhZci8
    @Test
    @Ignore("Fails!")
    public void testConcurrency() throws InterruptedException {
        int max = 10;
        Thread[] threads = new Thread[max];
        int[] results = new int[max];
        for (int i = 0; i < max; i++) {
            final int j = i;
            threads[i] = 
            new Thread(() -> {
                results[j] = instance.getCachedCount();
                System.out.println(j + ":" + results[j]);
            });
            threads[i].start();
        }
        for (int i = 0; i < max; i++) {
            threads[i].join();
        }
        for (int i = 0; i < max; i++) {
            assertEquals(0, results[i]); // Fails
        }
    }

}
