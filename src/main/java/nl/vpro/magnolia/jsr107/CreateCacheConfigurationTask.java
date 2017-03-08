package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.jcr.*;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@Getter
@Slf4j
public class CreateCacheConfigurationTask extends AbstractRepositoryTask {
    private final String nodeName;
    private final CacheSettings cacheSettings;
    private final String exceptionCacheName;
    private final CacheSettings exceptionCacheSettings;
    private final boolean overrideOnUpdate;


    @lombok.Builder(builderClassName = "Builder")
    public CreateCacheConfigurationTask(
        String name, CacheSettings cacheSettings,
        String exceptionCacheName, CacheSettings exceptionCacheSettings,
        boolean overrideOnUpdate) {
        super("Cache configuration for " + name, "Installs cache configuration for " + name);
        this.nodeName = name;
        this.cacheSettings = cacheSettings;
        this.exceptionCacheName = exceptionCacheName;
        this.exceptionCacheSettings = exceptionCacheSettings;
        this.overrideOnUpdate = overrideOnUpdate;
    }

    public static class Builder {
        public Builder settings(CacheSettings.Builder builder) {
            return cacheSettings(builder.build());
        }
        public Builder exceptionSettings(CacheSettings.Builder builder) {
            return exceptionCacheSettings(builder.build());
        }
    }
    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);

        createCacheConfigurationNode(session);
        if (StringUtils.isNotBlank(this.exceptionCacheName)) {
            createExceptionCacheConfigurationNode(session);
        }
        session.save();

    }

    private void createCacheConfigurationNode(Session session) throws RepositoryException {
        createAndFill(session, nodeName, (node) -> {
            for (Field f : CacheSettings.class.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    setProperty(node, f, cacheSettings);
                }
            }
        });
    }

    private void createExceptionCacheConfigurationNode(Session session) throws RepositoryException {
        createAndFill(session, exceptionCacheName, (node) -> {
            for (Field f : CacheSettings.class.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    setProperty(node, f,
                        Stream.of(exceptionCacheSettings, cacheSettings).filter(Objects::nonNull).findFirst().orElse(null)
                    );
                }
            }
        });
    }

    private void createAndFill(Session session, String path, Consumer<Node> consume) throws RepositoryException {
        Node node;
        try {
            node = getOrCreatePath(session, CreateConfigurationTasks.PATH).getNode(path);
        } catch (PathNotFoundException pnf) {
            node = null;
        }
        if (node == null) {
            node = session.getNode(CreateConfigurationTasks.PATH).addNode(path, NodeTypes.ContentNode.NAME);
            consume.accept(node);
            log.info("Created {}", node);
        } else {

            if (overrideOnUpdate) {
                log.info("Already existed {}. Will override settings with values defined by annotation");
                consume.accept(node);
            } else {
                log.info("Already existed {}", node);
            }
        }
    }

    private Node getOrCreatePath(Session session, String path) throws RepositoryException {
        try {
            return session.getNode(path);
        } catch (RepositoryException re) {
            throw re;
        }
    }

    protected void setProperty(Node node, Field property, CacheSettings cacheSettings) {
        try {
            Object o = property.get(cacheSettings);
            String name = property.getName();
            PropertyUtil.setProperty(node, name, o);
            log.info("Set {}/@{}={}", node.getPath(), name, o);
        } catch (IllegalArgumentException | IllegalAccessException | RepositoryException e) {
            log.error("For " + property + " of " + cacheSettings + " to set on " + node + " :" + e.getMessage(), e);
        }

    }
}
