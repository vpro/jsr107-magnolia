package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import javax.jcr.Node;

/**
 * A {@link CacheKeyGenerator} that maps every {@link Node} argument to its path, which makes it {@link java.io.Serializable}
 * @author Michiel Meeuwissen
 * @since 1.13
 */
@Slf4j
public class NodeAwareCacheKeyGenerator implements CacheKeyGenerator {

    private static final Map<Class<?>, Method> KNOWNMETHODS = new HashMap<>();

    static {
        for (String[] classAndMethod : new String[][] {
            // Let's support some non core magnolia classes to, for which we know a logical serializable key
            new String[]{"info.magnolia.module.site.Site", "getName"},
            new String[]{"info.magnolia.dam.api.Item", "getName"},
        }) {
            Class<?> c;
            try {
                c = Class.forName(classAndMethod[0]);
            } catch (ClassNotFoundException e) {
                log.info(e.getMessage());
                continue;
            }
            Method m;
            try {
                m = c.getMethod(classAndMethod[1]);
            } catch (NoSuchMethodException e) {
                log.error(e.getMessage(), e);
                continue;
            }
            KNOWNMETHODS.put(c, m);
        }

    }

    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        List<Serializable> result = new ArrayList<>();
        for (CacheInvocationParameter cacheInvocationParameter : cacheKeyInvocationContext.getKeyParameters()) {
            Object value = cacheInvocationParameter.getValue();
            if (value instanceof Node) {
                Node node = (Node) value;
                result.add(NodeUtil.getPathIfPossible(node));
                continue;
            } else if (value instanceof Serializable) {
                result.add((Serializable) value);
                continue;
            } else {
                Method m = KNOWNMETHODS.get(value.getClass());
                if (m != null) {
                    try {
                        result.add((String) m.invoke(value));
                        continue;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throw new IllegalArgumentException("Not serializable " + value);
        }

        return new SerializableGeneratedCacheKey(result.toArray(new Serializable[result.size()]));
    }


}

