package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.cache.annotation.CacheResult;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author Michiel Meeuwissen
 * @since 1.4
 */

@Slf4j
public class CreateConfigurationTasks {

    private static final String PATH = "/modules/cache/config/cacheFactory/caches";

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
                        result.add(new CreateCacheConfigurationTask(cr.cacheName(), cacheSettings));
                    }

                }
                c = c.getSuperclass();
            }
        }
        return result;
    }

    public static class CreateCacheConfigurationTask extends AbstractRepositoryTask {
        private final String nodeName;
        private final DefaultCacheSettings cacheSettings;
        public CreateCacheConfigurationTask(String name, DefaultCacheSettings cacheSettings) {
            super("Cache configuration for " + name, "Installs cache configuration for " + name);
            this.nodeName = name;
            this.cacheSettings = cacheSettings;
        }

        @Override
        protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
            final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
            Node node;
            try {
                node = session.getNode(PATH).getNode(nodeName);
            } catch (PathNotFoundException pnf) {
                node = null;
            }
            if (node == null) {
                node = session.getNode(PATH).addNode(nodeName, NodeTypes.Content.NAME);
                for (Method m : DefaultCacheSettings.class.getDeclaredMethods()) {
                    setPropertyOrDefault(node, cacheSettings, m);
                }
                CreateConfigurationTasks.log.info("Created {}", node);
            } else {
                CreateConfigurationTasks.log.info("Already existed {}", node);
            }
            session.save();

        }

        protected void setPropertyOrDefault(Node node, DefaultCacheSettings cacheSettings, Method property) {
            Object o;
            try {
                if (cacheSettings != null) {
                    o = property.invoke(cacheSettings);
                } else {
                    o = property.getDefaultValue();
                }
                if (o != null) {
                    PropertyUtil.setProperty(node, property.getName(), o);
                }
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | RepositoryException e) {
                CreateConfigurationTasks.log.error("For " + property + " of " + cacheSettings + " to set on " + node + " :" + e.getMessage(), e);
            }

        }
    }
}
