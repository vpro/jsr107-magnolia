package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.inject.CacheFactoryProvider;
import info.magnolia.module.cache.mbean.CacheMonitor;

import javax.cache.CacheManager;

import org.junit.jupiter.api.BeforeEach;

import com.google.inject.*;

import nl.vpro.magnolia.jsr107.mock.MockCacheFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michiel Meeuwissen
 * @since 1.13
 */
public abstract class AbstractJSR107Test {


    MgnlCacheManager cacheManager;
    MockCacheFactory f;
    Injector injector;

    @BeforeEach
    public void setupCacheManager() {

        injector = Guice.createInjector(new CacheConfigurer(), new AbstractModule() {
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
    }
}
