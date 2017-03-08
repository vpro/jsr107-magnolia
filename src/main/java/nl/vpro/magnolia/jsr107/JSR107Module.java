package nl.vpro.magnolia.jsr107;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@Slf4j
public class JSR107Module implements ModuleLifecycle {
    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("Version " + moduleLifecycleContext.getCurrentModuleDefinition().getVersion());

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {


    }
}
