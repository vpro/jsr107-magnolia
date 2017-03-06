package nl.vpro.magnolia.jsr107;

import info.magnolia.module.delta.Task;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.cache.annotation.CacheResult;

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

    static final String PATH = "/modules/cache/config/cacheFactory/caches";

    /**
     * Generates tasks to create (default) configuration for the caches provided by a list of beans.
     * The values used are the ones in the annotation {@link DefaultCacheSettings}. You can set this annotation on your methods to provide different defaults.
     * If the annotation is missing altogether the defaults of it are still used.
     */
    public static List<Task> createConfigurationTasks(Class<?>... beans) {
        List<Task> result = new ArrayList<>();
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
                        result.add(new CreateCacheConfigurationTask(
                            cr.cacheName(), CacheSettings.of(cacheSettings),
                            cr.exceptionCacheName(), CacheSettings.of(exceptionCacheSettings),
                            overrideOnUpdate

                        ));
                    }

                }
                c = c.getSuperclass();
            }
        }
        return result;
    }

}
