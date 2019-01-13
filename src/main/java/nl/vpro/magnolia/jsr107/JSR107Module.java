package nl.vpro.magnolia.jsr107;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * The magnolia module implementation. Doesn't do much, besides some logging.
 *
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@Slf4j
public class JSR107Module implements ModuleLifecycle {
    private final MgnlCacheManager mgnlCacheManager;

    @Inject
    public JSR107Module(MgnlCacheManager mgnlCacheManager) {
        this.mgnlCacheManager = mgnlCacheManager;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("javax.cache.CacheManager: {}. Version {}. See https://github.com/vpro/jsr107-magnolia",
            mgnlCacheManager,  moduleLifecycleContext.getCurrentModuleDefinition().getVersion());

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {


    }
}
