package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import static nl.vpro.magnolia.jsr107.CacheValue.of;

/**
 * Implements a {@link Cache} backed by a {@link info.magnolia.module.cache.Cache}
 *
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
class AdaptedCache<K, V> implements Cache<K, V> {

    protected static final Object NULL = AdaptedCache.class.getName() + ".NULL";
    protected static final Object EXCEPTION = AdaptedCache.class.getName() + ".EXCEPTION";
    private final info.magnolia.module.cache.Cache mgnlCache;
    private final CacheManager cacheManager;
    private final Configuration<?, ?> configuration;

    public AdaptedCache(
        info.magnolia.module.cache.Cache mgnlCache,
        CacheManager manager,
        Configuration<?, ?> configuration
        ) {
        this.mgnlCache = mgnlCache;
        this.cacheManager = manager;
        this.configuration = configuration;

    }

    @Override
    public V get(K key) {
        CacheValue<V> cacheValue = ((CacheValue<V>) mgnlCache.get(key));
        if (cacheValue == null) {
            // Not present in cache
            return null;
        }
        return cacheValue.orNull();
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        for (K k : keys) {
            if (containsKey(k)) {
                result.put(k, get(k));
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(K key) {
        return mgnlCache.hasElement(key);
    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        log.debug("loading ", keys);
        throw new UnsupportedOperationException();

    }

    @Override
    public void put(K key, V value) {
        mgnlCache.put(key, of(value));
    }

    @Override
    public V getAndPut(K key, V value) {
        V previousValue = get(key);
        mgnlCache.put(key, of(value));
        return previousValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            mgnlCache.put(e.getKey(), of(e.getValue()));
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if (! mgnlCache.hasElement(key)) {
            mgnlCache.put(key, of(value));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean remove(K key) {
        boolean result = mgnlCache.hasElement(key);
        mgnlCache.remove(key);
        return result;

    }

    @Override
    public boolean remove(K key, V oldValue) {
        V compare = get(key);
        if (compare != null && compare.equals(oldValue)) {
            mgnlCache.remove(key);
            return true;
        }
        return false;

    }

    @Override
    public V getAndRemove(K key) {
        V result = get(key);
        remove(key);
        return result;

    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        V compare = get(key);
        if (compare != null && compare.equals(oldValue)) {
            mgnlCache.put(key, of(newValue));
            return true;
        }
        return false;

    }

    @Override
    public boolean replace(K key, V value) {
        boolean result = mgnlCache.hasElement(key);
        if (result) {
            mgnlCache.put(key, of(value));
            return true;
        }
        return false;
    }

    @Override
    public V getAndReplace(K key, V value) {
        if (mgnlCache.hasElement(key)) {
            V oldValue = get(key);
            put(key, value);
            return oldValue;
        }
        return null;

    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        for (K key : keys) {
            mgnlCache.remove(key);
        }
    }

    @Override
    public void removeAll() {
        mgnlCache.clear();
    }

    @Override
    public void clear() {
        mgnlCache.clear();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        return (C) configuration;
    }

    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getName() {
        return mgnlCache.getName();
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;

    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(mgnlCache.getClass())) {
            return (T) mgnlCache;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        final Iterator keys = mgnlCache.getKeys().iterator();
        return new Iterator<Entry<K, V>>() {
            @Override
            public boolean hasNext() {
                return keys.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                final Object key = keys.next();
                return new Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return (K) key;

                    }

                    @Override
                    public V getValue() {
                        return AdaptedCache.this.get((K) key);

                    }

                    @Override
                    public <T> T unwrap(Class<T> clazz) {
                        throw new UnsupportedOperationException();

                    }
                };
            }
        };
    }



}
