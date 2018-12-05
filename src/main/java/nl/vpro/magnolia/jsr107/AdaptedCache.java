package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.BlockingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.event.*;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import static nl.vpro.magnolia.jsr107.CacheValue.of;

/**
 * Implements a {@link javax.cache.Cache} backed by a {@link info.magnolia.module.cache.Cache}
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

    private final List<CacheEntryListenerConfiguration<K, V>> cacheEntryListenerConfigurations = new CopyOnWriteArrayList<>();
    private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryCreatedListener<K, V>> createdListenerMap = new ConcurrentHashMap<>();
    private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryUpdatedListener<K, V>> updatedListenerMap = new ConcurrentHashMap<>();
    private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryRemovedListener<K, V>> removedListenerMap = new ConcurrentHashMap<>();




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
        V result = cacheValue.orNull();
        if (Objects.equals(result, EXCEPTION)) {
            return null;
        }
        return result;
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
        boolean result = mgnlCache.hasElement(key);
        unlock(key);
        return result;
    }
    public V getUnblocking(K key) {
        V value = get(key);
        unlock(key);
        return value;
    }

    public void unlock(K key) {
        if (mgnlCache instanceof BlockingCache) {
            ((BlockingCache) mgnlCache).unlock(key);
        }
    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        log.debug("loading ", keys);
        throw new UnsupportedOperationException();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(K key, V value) {
        final CacheValue<V> oldValue = ((CacheValue<V>) mgnlCache.getQuiet(key));
        final CacheValue<V> newValue = of(value);
        mgnlCache.put(key, newValue);
        handleEvents(new CacheEntryEvent<K, V>(this, oldValue == null ? EventType.CREATED : EventType.UPDATED) {

            @Override
            public K getKey() {
                return key;
            }
            @Override
            public V getValue() {
                return newValue.orNull();

            }

            @Override
            public Object unwrap(Class clazz) {
                return null;

            }

            @Override
            public V getOldValue() {
                return oldValue.orNull();

            }

            @Override
            public boolean isOldValueAvailable() {
                return oldValue != null;

            }
        });
    }

    @Override
    public V getAndPut(K key, V value) {
        V previousValue = get(key);
        put(key, value);
        return previousValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if (! containsKey(key)) {
            put(key, value);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean remove(K key) {
        boolean result = containsKey(key);
        if (result) {
            final CacheValue<V> oldValue = ((CacheValue<V>) mgnlCache.getQuiet(key));
            mgnlCache.remove(key);
            handleEvents(new CacheEntryEvent<K, V>(this, EventType.REMOVED) {
                @Override
                public V getOldValue() {
                    return oldValue.orNull();

                }

                @Override
                public boolean isOldValueAvailable() {
                    return oldValue != null;

                }

                @Override
                public K getKey() {
                    return key;

                }

                @Override
                public V getValue() {
                    return null;

                }

                @Override
                public <T> T unwrap(Class<T> clazz) {
                    return null;

                }
            });
        }
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
            put(key, newValue);
            return true;
        }
        return false;

    }

    @Override
    public boolean replace(K key, V value) {
        boolean result = mgnlCache.hasElement(key);
        if (result) {
            put(key, value);
            return true;
        }
        return false;
    }

    @Override
    public V getAndReplace(K key, V value) {
        if (containsKey(key)) {
            V oldValue = get(key);
            put(key, value);
            return oldValue;
        }
        return null;

    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        for (K key : keys) {
            remove(key);
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
        cacheEntryListenerConfigurations.add(cacheEntryListenerConfiguration);
        CacheEntryListener<? super K, ? super V> cacheEntryListener = cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create();
        if (cacheEntryListener instanceof CacheEntryCreatedListener) {
            createdListenerMap.put(cacheEntryListenerConfiguration, (CacheEntryCreatedListener<K, V>) cacheEntryListener);
        }

        if (cacheEntryListener instanceof CacheEntryUpdatedListener) {
            updatedListenerMap.put(cacheEntryListenerConfiguration, (CacheEntryUpdatedListener<K, V>) cacheEntryListener);
        }
        if (cacheEntryListener instanceof CacheEntryRemovedListener) {
            removedListenerMap.put(cacheEntryListenerConfiguration, (CacheEntryRemovedListener<K, V>) cacheEntryListener);
        }
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        cacheEntryListenerConfigurations.remove(cacheEntryListenerConfiguration);
        createdListenerMap.remove(cacheEntryListenerConfiguration);
    }

    @Override
    public String toString() {
        return "Adapted mgnl cache " + mgnlCache.getClass().getName() + " " + mgnlCache.getName();
    }

    @SafeVarargs
    protected final void handleEvents(CacheEntryEvent<? extends K, ? extends V>... events) {

        List<CacheEntryEvent<? extends K, ? extends V>> created = new ArrayList<>();
        List<CacheEntryEvent<? extends K, ? extends V>> updated  = new ArrayList<>();
        List<CacheEntryEvent<? extends K, ? extends V>> removed  = new ArrayList<>();
        for (CacheEntryEvent<? extends K, ? extends V> event : events) {
            switch(event.getEventType()) {
                case CREATED:
                    created.add(event);
                    break;
                case UPDATED:
                    updated.add(event);
                    break;
                case REMOVED:
                    removed.add(event);
                    break;
                default:
                case EXPIRED:
                    log.warn("Not supported");
            }
        }
        if (! created.isEmpty()) {
            for (CacheEntryCreatedListener<K, V> listener : createdListenerMap.values()) {
                listener.onCreated(created);
            }
        }
        if (! updated.isEmpty()) {
            for (CacheEntryUpdatedListener<K, V> listener : updatedListenerMap.values()) {
                listener.onUpdated(updated);
            }
        }
        if (! removed.isEmpty()) {
            for (CacheEntryRemovedListener<K, V> listener : removedListenerMap.values()) {
                listener.onRemoved(removed);
            }
        }


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
