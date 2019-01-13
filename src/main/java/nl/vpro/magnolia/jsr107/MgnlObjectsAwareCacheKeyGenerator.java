package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import javax.cache.annotation.*;
import javax.jcr.Node;

/**
 * A {@link CacheKeyGenerator} that maps every {@link Node} argument to its path, which makes it {@link java.io.Serializable}
 *
 * It also recognizes some other magnolia objects, which can trivially be serialized but are not {@link Serializable} themselves.
 *
 * Currently these are info.magnolia.module.site.Site and  info.magnolia.dam.api.Item. These are not magnolia core classes by the way.
 *
 * This class can be used as an argument for {@link CacheResult#cacheKeyGenerator()} for methods which have these kind of JCR or other magnolia objects as arguments.
 *
 * @author Michiel Meeuwissen
 * @since 1.13
 */
@Slf4j
public class MgnlObjectsAwareCacheKeyGenerator implements CacheKeyGenerator {

    private static final Map<Class<?>, Function<Object, Serializable>> REGISTERED_VALUE_TO_SERIALIZABLE = new LinkedHashMap<>();

    
    public static <T> void register(Class<T> clazz, Function<T, Serializable> function) {
        REGISTERED_VALUE_TO_SERIALIZABLE.put(clazz, (Function<Object, Serializable>) function);
        log.info("Registered {} -> {}", clazz, function);
    }
    static {
        register(Node.class, NodeUtil::getPathIfPossible);
        // Let's support some non core magnolia classes to, for which we know a logical serializable key
        register("info.magnolia.module.site.Site", "getName");
        register("info.magnolia.dam.api.Item", "getName");
        register(Serializable.class, o -> o);
    }
    protected static void register(String clazz, String method) {
        Class<?> c;
        try {
            c = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            log.info(e.getClass().getName() + " " + e.getMessage());
            return;
        }
        final Method m;
        try {
            m = c.getMethod(method);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            return;
        }
        register(c, o -> {
            try {
                return (Serializable) m.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                log.error(e.getMessage(), e);
            }
            return String.valueOf(o);
        });
    } 
    
    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        List<Serializable> result = new ArrayList<>();
        OUTER:
        for (CacheInvocationParameter cacheInvocationParameter : cacheKeyInvocationContext.getKeyParameters()) {
            Object value = cacheInvocationParameter.getValue();
            for (Map.Entry<Class<?>, Function<Object, Serializable>> e : REGISTERED_VALUE_TO_SERIALIZABLE.entrySet()) {
                if (e.getKey().isInstance(value)) {
                    result.add(e.getValue().apply(value));
                    continue OUTER;
                }
            }
            throw new IllegalArgumentException("Not serializable " + value);
        }

        return new SerializableGeneratedCacheKey(result.toArray(new Serializable[result.size()]));
    }


}

