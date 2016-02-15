package nl.vpro.jsr107magnolia;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentConfigurer;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import org.aopalliance.intercept.MethodInvocation;
import org.jsr107.ri.annotations.CacheContextSource;
import org.jsr107.ri.annotations.DefaultCacheKeyGenerator;
import org.jsr107.ri.annotations.guice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.annotation.*;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class CacheConfigurer extends AbstractModule implements ComponentConfigurer {

    private static Logger LOG = LoggerFactory.getLogger(CacheConfigurer.class);

    @Override
    protected void configure() {
        bind(CacheKeyGenerator.class).to(DefaultCacheKeyGenerator.class);
        bind(CacheResolverFactory.class).to(MgnlCacheResolverFactory.class);
        bind(new TypeLiteral<CacheContextSource<MethodInvocation>>() {
        }).to(CacheLookupUtil.class);

        CachePutInterceptor cachePutInterceptor = new CachePutInterceptor();
        requestInjection(cachePutInterceptor);
        bindInterceptor(Matchers.annotatedWith(CachePut.class), Matchers.any(), cachePutInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(CachePut.class), cachePutInterceptor);

        CacheResultInterceptor cacheResultInterceptor = new CacheResultInterceptor();
        requestInjection(cacheResultInterceptor);
        bindInterceptor(Matchers.annotatedWith(CacheResult.class), Matchers.any(), cacheResultInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheResult.class), cacheResultInterceptor);

        CacheRemoveEntryInterceptor cacheRemoveEntryInterceptor = new CacheRemoveEntryInterceptor();
        requestInjection(cacheRemoveEntryInterceptor);
        bindInterceptor(Matchers.annotatedWith(CacheRemove.class), Matchers.any(), cacheRemoveEntryInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheRemove.class), cacheRemoveEntryInterceptor);

        CacheRemoveAllInterceptor cacheRemoveAllInterceptor = new CacheRemoveAllInterceptor();
        requestInjection(cacheRemoveAllInterceptor);
        bindInterceptor(Matchers.annotatedWith(CacheRemoveAll.class), Matchers.any(), cacheRemoveAllInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheRemoveAll.class), cacheRemoveAllInterceptor);
    }

    @Override
    public void doWithConfiguration(ComponentProvider parentComponentProvider, ComponentProviderConfiguration configuration) {
        LOG.info("Installing JSR 107 caching by annotation");
    }


}
