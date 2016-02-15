package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.mbean.CacheMonitor;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheResult;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
        public int getCachedCount() {
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


}
