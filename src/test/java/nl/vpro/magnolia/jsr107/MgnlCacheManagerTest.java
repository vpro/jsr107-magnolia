package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.inject.CacheFactoryProvider;
import info.magnolia.module.cache.mbean.CacheMonitor;

import java.time.Duration;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheResult;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.vpro.magnolia.jsr107.mock.MockCacheFactory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
public class MgnlCacheManagerTest {


    public static class TestClass {
        int count = 0;

        int constant = 0;

        @CacheResult(cacheName = "counts")
        public Integer getCachedCount(String key) {
            return count++;
        }

    }

    MgnlCacheManager cacheManager;
    MockCacheFactory f;
    CacheConfigurerTest.TestClass instance;

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

        cacheManager = (MgnlCacheManager) injector.getInstance(CacheManager.class);
        instance = injector.getInstance(CacheConfigurerTest.TestClass.class);

    }

    @Test
    @Ignore
    public void getValue() {
        assertThat(instance.getCachedCount("a")).isEqualTo(0);
        long start = System.nanoTime();
        for (int i= 0; i < 10000;i++) {
            assertThat(cacheManager.getValue(TestClass.class, instance, "getCachedCount", "a")).isEqualTo(0);
        }
        System.out.print(Duration.ofNanos(System.nanoTime() - start));
    }

}
