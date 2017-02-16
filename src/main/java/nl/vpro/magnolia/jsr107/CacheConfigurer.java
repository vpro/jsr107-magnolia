package nl.vpro.magnolia.jsr107;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentConfigurer;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.cache.CacheManager;
import javax.cache.annotation.*;

import org.aopalliance.intercept.MethodInvocation;
import org.jsr107.ri.annotations.CacheContextSource;
import org.jsr107.ri.annotations.DefaultCacheKeyGenerator;
import org.jsr107.ri.annotations.guice.*;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public class CacheConfigurer extends AbstractModule implements ComponentConfigurer {


    @Override
    protected void configure() {
        bind(CacheKeyGenerator.class).to(DefaultCacheKeyGenerator.class);
        requireBinding(MgnlCacheResolver.class);
        bind(CacheResolverFactory.class).to(MgnlCacheResolverFactory.class);
        bind(CacheManager.class).to(MgnlCacheManager.class);

        bind(new TypeLiteral<CacheContextSource<MethodInvocation>>() {
        }).to(CacheLookupUtil.class);


        {
            ParameterCacheValueInterceptor cacheValueInterceptor = new ParameterCacheValueInterceptor();
            requestInjection(cacheValueInterceptor);
            CachePutInterceptor cachePutInterceptor = new CachePutInterceptor();
            requestInjection(cachePutInterceptor);
            bindInterceptor(Matchers.annotatedWith(CachePut.class), Matchers.any(), cacheValueInterceptor, cachePutInterceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(CachePut.class), cacheValueInterceptor, cachePutInterceptor);
        }

        {
            ReturnCacheValueInterceptor cacheValueInterceptor = new ReturnCacheValueInterceptor();
            ReturnCacheValueUnInterceptor cacheValueUnInterceptor = new ReturnCacheValueUnInterceptor();
            requestInjection(cacheValueInterceptor);
            CacheResultInterceptor cacheResultInterceptor = new CacheResultInterceptor();
            requestInjection(cacheResultInterceptor);
            bindInterceptor(Matchers.annotatedWith(CacheResult.class), Matchers.any(), cacheValueUnInterceptor, cacheResultInterceptor, cacheValueInterceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheResult.class), cacheValueUnInterceptor, cacheResultInterceptor, cacheValueInterceptor);
        }

        {
            CacheRemoveEntryInterceptor cacheRemoveEntryInterceptor = new CacheRemoveEntryInterceptor();
            requestInjection(cacheRemoveEntryInterceptor);
            bindInterceptor(Matchers.annotatedWith(CacheRemove.class), Matchers.any(), cacheRemoveEntryInterceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheRemove.class), cacheRemoveEntryInterceptor);
        }
        {
            CacheRemoveAllInterceptor cacheRemoveAllInterceptor = new CacheRemoveAllInterceptor();
            requestInjection(cacheRemoveAllInterceptor);
            bindInterceptor(Matchers.annotatedWith(CacheRemoveAll.class), Matchers.any(), cacheRemoveAllInterceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheRemoveAll.class), cacheRemoveAllInterceptor);
        }

    }

    @Override
    public void doWithConfiguration(ComponentProvider parentComponentProvider, ComponentProviderConfiguration configuration) {
        log.info("Installing JSR 107 caching by annotation");
    }


}
