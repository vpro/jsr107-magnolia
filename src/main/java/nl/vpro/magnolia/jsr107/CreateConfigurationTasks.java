package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheResult;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;

/**
 * Use this in your {@link info.magnolia.module.ModuleVersionHandler}
 * to create default configurations for your caches.
 * <pre>{@code
   setInstallOrUpdateTasks(CreateConfigurationTasks.createConfigurationTasks(<classes with @CacheResult annotations>...));
}</pre>
 * @author Michiel Meeuwissen
 * @since 1.4
 */

@Slf4j
public class CreateConfigurationTasks {

    static final String[] PATHS = {
        "/modules/cache/config/cacheFactory/delegateFactories/ehcache3/caches", // version >= 5.5.5
        "/modules/cache/config/cacheFactory/caches"                             // version < 5.5.5
    };


    /**
     * Returns the path which contains the cache configuration
     * @since 1.15
     */
    public static Node getPath(Session session) throws RepositoryException {
        RepositoryException first = null;
        for(String proposal : PATHS) {
            try {
                return session.getNode(proposal);
            } catch (RepositoryException re) {
                if (first == null) {
                    first = re;
                }
                log.warn("{}: {}, falling back to backwards compatibility", proposal, re.getClass().getName());
            }

        }
        if (first == null) {
            throw new IllegalStateException();
        }
        throw first;
    }

    /**
     * Generates tasks to create (default) configuration for the caches provided by a list of beans.
     * The values used are the ones in the annotation {@link DefaultCacheSettings}. You can set this annotation on your methods to provide different defaults.
     * If the annotation is missing altogether the defaults of it are still used.
     */
    public static List<CreateCacheConfigurationTask> createConfigurationTasks(Class<?>... beans) {
        List<CreateCacheConfigurationTask> result = new ArrayList<>();
        for (Class<?> bean : beans) {
            Class<?> c = bean;
            while (c != null) {
                for (Method m : c.getDeclaredMethods()) {
                    CacheResult cr = m.getDeclaredAnnotation(CacheResult.class);
                    if (cr != null) {
                        DefaultCacheSettings cacheSettings = m.getDeclaredAnnotation(DefaultCacheSettings.class);
                        Defaults defaultsWrapper = m.getDeclaredAnnotation(Defaults.class);
                        DefaultCacheSettings exceptionCacheSettings = null;
                        boolean overrideOnUpdate = false;
                        if (defaultsWrapper != null) {
                            if (cacheSettings != null) {
                                throw new IllegalArgumentException("Can't use both @DefaultCacheSettings @Defaults on same method " + m);
                            }
                            overrideOnUpdate = defaultsWrapper.overrideOnUpdate();
                            cacheSettings = defaultsWrapper.cacheSettings();
                            exceptionCacheSettings = defaultsWrapper.exceptionCacheSettings();
                        }
                        CacheSettings settings = CacheSettings.of(cacheSettings);
                        result.add(CreateCacheConfigurationTask.builder()
                            .name(getCacheName(m , cr))
                            .method(m)
                            .overrideOnUpdate(overrideOnUpdate)
                            .cacheSettings(settings)
                            .build());
                        if (StringUtils.isNotBlank(cr.exceptionCacheName())) {
                            result.add(CreateCacheConfigurationTask.builder()
                                .name(cr.exceptionCacheName())
                                .method(m)
                                .overrideOnUpdate(overrideOnUpdate)
                                .cacheSettings(settings)
                                .cacheSettings(CacheSettings.of(exceptionCacheSettings))
                                .build());
                        }

                    }

                }
                c = c.getSuperclass();
            }
        }
        result.sort(Comparator.comparing(CreateCacheConfigurationTask::getNodeName));
        return result;
    }

    protected static String getCacheName(Method m, CacheResult cr) {
        String providedByAnotation = cr.cacheName();
        if (StringUtils.isBlank(providedByAnotation)) {
            log.info("No explicit cache name on {}", cr);
            // If not specified defaults first to {@link CacheDefaults#cacheName()} and if
            //that is not set it defaults to:
            //  package.name.ClassName.methodName(package.ParameterType,package.ParameterType)
            CacheDefaults annotation = m.getDeclaringClass().getAnnotation(CacheDefaults.class);
            if (annotation != null) {
                String providedByCacheDefaults = annotation.cacheName();
                if (StringUtils.isNotEmpty(providedByCacheDefaults)) {
                    return providedByCacheDefaults;
                }
            }
            return m.getDeclaringClass().getName() + "." + m.getName() + "(" + Arrays.stream(m.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")";

        } else {
            return providedByAnotation;
        }

    }

}
