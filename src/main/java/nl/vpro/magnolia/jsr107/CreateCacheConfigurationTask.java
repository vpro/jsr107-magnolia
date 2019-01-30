package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.cache.ehcache3.configuration.EhCache3ConfigurationBuilder;
import info.magnolia.module.cache.ehcache3.configuration.EhCache3Expiry;
import info.magnolia.module.cache.ehcache3.configuration.Ehcache3ResourcePoolBuilder;
import info.magnolia.module.cache.ehcache3.configuration.Ehcache3ResourcePoolsBuilder;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.repository.RepositoryConstants;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.ehcache.config.ResourceType;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@Slf4j
public class CreateCacheConfigurationTask extends AbstractRepositoryTask {
    @Getter
    private final String nodeName;
    @Getter
    private final Method method;

    private final CacheSettings[] cacheSettings;
    private final boolean overrideOnUpdate;


    @lombok.Builder(builderClassName = "Builder")
    protected CreateCacheConfigurationTask(
        @Nonnull String name,
        Method method,
        @Singular("cacheSettings") List<CacheSettings> cacheSettings,
        boolean overrideOnUpdate
    ) {
        super("Cache configuration for " + name, "Installs cache configuration for " + name);
        this.method = method;
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Cache name cannot be empty");
        }
        this.nodeName = Text.escapeIllegalJcrChars(name);
        if (cacheSettings.size() < 1) {
            throw new IllegalArgumentException();
        }
        this.cacheSettings = cacheSettings.toArray(new CacheSettings[0]);
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
    protected void doExecute(InstallContext installContext) throws RepositoryException {
        final Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);

        createCacheConfigurationNode(session);

        session.save();

    }

    private void createCacheConfigurationNode(Session session) throws RepositoryException {
        createAndFill(session, nodeName, (node) -> {
            try {
                node.setProperty("class", EhCache3ConfigurationBuilder.class.getName());
                node.setProperty("keyType", Serializable.class.getName());
                node.setProperty("valueType", Serializable.class.getName());

                // expiry
                Node expiry = NodeUtil.createPath(node, "expiry", NodeTypes.ContentNode.NAME);
                expiry.setProperty("class", EhCache3Expiry.class.getName());
                for (CacheSettings settings : cacheSettings) {
                    if (!settings.isEternal() && settings.getTimeToLiveSeconds() != null) {
                        expiry.setProperty("create", Long.valueOf(settings.getTimeToLiveSeconds()));
                    }
                }

                // resourcePoolsBuilder
                Node resourcePoolsBuilder = NodeUtil.createPath(node, "resourcePoolsBuilder", NodeTypes.ContentNode.NAME);
                resourcePoolsBuilder.setProperty("class", Ehcache3ResourcePoolsBuilder.class.getName());

                // resourcePoolsBuilder/pools
                Node resourcePools = NodeUtil.createPath(resourcePoolsBuilder, "pools", NodeTypes.ContentNode.NAME);

                for (CacheSettings settings : cacheSettings) {
                    // resourcePoolsBuilder/pools/heap
                    final Node heap = NodeUtil.createPath(resourcePools, "heap", NodeTypes.ContentNode.NAME);
                    heap.setProperty("class", Ehcache3ResourcePoolBuilder.class.getName());
                    heap.setProperty("resourceType", ResourceType.Core.HEAP.name());
                    heap.setProperty("resourceUnit", EntryUnit.ENTRIES.name());
                    heap.setProperty("size", (long) settings.getMaxElementsInMemory());

                    // resourcePoolsBuilder/pools/disk
                    if (settings.isOverflowToDisk() && (settings.getMaxSizeOnDiskMB() > 0 || settings.getMaxElementsOnDisk() > 0)) {
                        final Node disk = resourcePools.addNode("disk", NodeTypes.ContentNode.NAME);
                        disk.setProperty("class", Ehcache3ResourcePoolBuilder.class.getName());
                        disk.setProperty("persistent", Boolean.TRUE);
                        disk.setProperty("resourceType", ResourceType.Core.DISK.name());
                        disk.setProperty("resourceUnit", MemoryUnit.MB.name());
                        long size = 1000L;
                        if (settings.getMaxSizeOnDiskMB() > 0) {
                            size = (long) settings.getMaxSizeOnDiskMB();
                        } else {
                            if (settings.getMaxElementsOnDisk() > 0) {
                                // Size estimate taken from : info.magnolia.module.cache.ehcache3.setup.MigrateEhCache2ConfigurationTask.java:137
                                size = settings.getMaxElementsOnDisk() / 10000L;
                            }
                        }
                        disk.setProperty("size", size);
                    }
                }

            } catch (RepositoryException e) {
                log.error("Unable to create/update settings of {} : {}", node, e.getMessage());
            }
        });
    }

    private void createAndFill(Session session, String path, Consumer<Node> consume) throws RepositoryException {

        String configPath  = CreateConfigurationTasks.getPath(session).getPath();
        Node node;
        try {
            node = getOrCreatePath(session, configPath).getNode(path);
        } catch (PathNotFoundException pnf) {
            node = null;
        }
        if (node == null) {
            node = session.getNode(configPath).addNode(path, NodeTypes.ContentNode.NAME);
            consume.accept(node);
            log.info("Created {}", node);
        } else {

            if (overrideOnUpdate) {
                log.info("Already existed {}. Will override settings with values defined by annotation", node);
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
}
