package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@Slf4j
public class CreateCacheConfigurationTask extends AbstractRepositoryTask {
    private final String nodeName;
    private final CacheSettings[] cacheSettings;
    private final boolean overrideOnUpdate;


    @lombok.Builder(builderClassName = "Builder")
    public CreateCacheConfigurationTask(
        String name,
        @Singular("cacheSettings")
        List<CacheSettings> cacheSettings,
        boolean overrideOnUpdate
    ) {
        super("Cache configuration for " + name, "Installs cache configuration for " + name);
        this.nodeName = name;
        if (cacheSettings.size() < 1) {
            throw new IllegalArgumentException();
        }
        this.cacheSettings = cacheSettings.toArray(new CacheSettings[cacheSettings.size()]);
        this.overrideOnUpdate = overrideOnUpdate;
    }

    public static class Builder {
        public Builder settings(CacheSettings.Builder builder) {
            return cacheSettings(builder.build());
        }
    }

    public CacheSettings getCacheSettings() {
        return cacheSettings[0];

    }
    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);

        createCacheConfigurationNode(session);

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

    protected void setProperty(Node node, Field property, CacheSettings... cacheSettingss) {
        try {
            Object o = null;
            for (CacheSettings cacheSettings : cacheSettingss) {
                o = property.get(cacheSettings);
                if (o instanceof Enum) {
                    o = ((Enum) o).name();
                }
                if (o != null) {
                    break;
                }
            }
            String name = property.getName();
            if (o != null) {
                log.info("Set {}/@{}={}", node.getPath(), name, o);
                PropertyUtil.setProperty(node, name, o);
            } else {
                if (PropertyUtil.getPropertyOrNull(node,  name) != null) {
                    log.info("Unset {}/@{}", node.getPath(), name);
                    PropertyUtil.setProperty(node, name, null);
                }
            }

        } catch (IllegalArgumentException | IllegalAccessException | RepositoryException e) {
            log.error("For " + property + " of " +
                Arrays.stream(cacheSettings).map(Object::toString)
                .collect(Collectors.joining(", ")) +
                " to set on " + node + " :" + e.getMessage(), e);
        }

    }
}
