package nl.vpro.magnolia.jsr107;

import info.magnolia.cms.util.MBeanUtil;
import info.magnolia.module.cache.BlockingCache;
import info.magnolia.module.cache.ehcache3.EhCache3Wrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.event.EventType;
import javax.cache.event.*;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.*;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.core.Ehcache;
import org.ehcache.event.*;

import static nl.vpro.magnolia.jsr107.CacheValue.of;

/**
 * Implements a {@link javax.cache.Cache} backed by a {@link info.magnolia.module.cache.Cache}
 *
 * This class is basically stateless, and only wraps stuff in the magnolia cache system.
 *
 * Because there are no cache events in magnolia, the listeners are stored statically by cache name in this class see {@link #LISTENERS}.
 *
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
class AdaptedCache<K, V> implements Cache<K, V>, AdaptedCacheMBean {

    protected static final Object NULL = AdaptedCache.class.getName() + ".NULL";
    protected static final Object EXCEPTION = AdaptedCache.class.getName() + ".EXCEPTION";

    private static final Map<String, Listeners<?, ?>> LISTENERS = new ConcurrentHashMap<>();

    private final CacheManager cacheManager;

    /**
     * The magnolia cache this is cache is backed by.
     */
    private final info.magnolia.module.cache.Cache mgnlCache;

    private final Configuration<?, ?> configuration;

    private final Listeners<K, V> listeners;


    @SuppressWarnings("unchecked")
    public AdaptedCache(
        info.magnolia.module.cache.Cache mgnlCache,
        CacheManager manager,
        Configuration<?, ?> configuration
        ) {
        this.mgnlCache = mgnlCache;
        this.cacheManager = manager;
        this.configuration = configuration;
        listeners = (Listeners<K, V>) LISTENERS.computeIfAbsent(this.mgnlCache.getName(), Listeners::new);
        MBeanUtil.registerMBean("JSR107AdaptedCache,name=" + mgnlCache.getName(), this);
    }


    @SuppressWarnings("unchecked")
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

    /**
     * Gets value without locking the key (in case the cache is a {@link BlockingCache}
     */
    public V getUnblocking(K key) {
        V value = get(key);
        unlock(key);
        return value;
    }

    /**
     * Unlocks given key (in case the cache is a {@link BlockingCache}
     */
    public void unlock(K key) {
        if (mgnlCache instanceof BlockingCache) {
            ((BlockingCache) mgnlCache).unlock(key);
        }
    }

    @Override
    public void loadAll(
        Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        log.debug("loading {}", keys);
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(K key, V value) {
        final CacheValue<V> oldValue = listeners.has() ? ((CacheValue<V>) mgnlCache.getQuiet(key)) : null;
        final CacheValue<V> newValue = of(value);
        mgnlCache.put(key, newValue);
        if (listeners.has()) {
            handleEvent(new AdapterCacheEntry<>(this,
                oldValue == null ? EventType.CREATED : EventType.UPDATED, key, oldValue, newValue));
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(K key) {
        boolean result = containsKey(key);
        if (result) {
            final CacheValue<V> oldValue = ((CacheValue<V>) mgnlCache.getQuiet(key));
            mgnlCache.remove(key);
            if (listeners.has()) {
                handleEvent(removeEvent(key, oldValue));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(K key, V oldValue) {
        CacheValue<V> compare = ((CacheValue<V>) mgnlCache.getQuiet(key));

        if (compare != null && Objects.equals(compare.orNull(), oldValue)) {
            mgnlCache.remove(key);
            if (listeners.has()) {
                handleEvent(removeEvent(key, compare));
            }
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
        List<CacheEntryEvent<? extends K, ? extends V>> events = new ArrayList<>();
        for (K key : keys) {
            @SuppressWarnings("unchecked")
            final CacheValue<V> oldValue = ((CacheValue<V>) mgnlCache.getQuiet(key));
            mgnlCache.remove(key);
            if (listeners.has()) {
                events.add(removeEvent(key, oldValue));
            }
        }
        handleEvents(events);
    }

    @Override
    public void removeAll() {
        removeAllEvent();
        mgnlCache.clear();
    }

    @Override
    public void clear() {
        removeAllEvent();
        mgnlCache.clear();
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        if ( ! listeners.cacheEntryListenerConfigurations.contains(cacheEntryListenerConfiguration)) {
            listeners.cacheEntryListenerConfigurations.add(cacheEntryListenerConfiguration);
            boolean viaEhcache = false;
            if (mgnlCache instanceof EhCache3Wrapper) {
                try {
                    EhCache3Wrapper ehcacheWrapper = (EhCache3Wrapper) mgnlCache;
                    Ehcache ehCache = (Ehcache) ehcacheWrapper.getWrappedEhCache();
                    ehCache.getRuntimeConfiguration().registerCacheEventListener(
                        new EhcacheEventLister(this, cacheEntryListenerConfiguration),
                        EventOrdering.ORDERED,
                        EventFiring.ASYNCHRONOUS,
                        new HashSet<>(Arrays.asList(org.ehcache.event.EventType.values())));
                    viaEhcache = true;
                } catch (Exception e) {
                    log.warn("{}", e);
                }
            }
            if (! viaEhcache) {

                CacheEntryListener<? super K, ? super V> cacheEntryListener = cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create();
                if (cacheEntryListener instanceof CacheEntryCreatedListener) {
                    listeners.createdListenerMap.put(cacheEntryListenerConfiguration, (CacheEntryCreatedListener<K, V>) cacheEntryListener);
                }

                if (cacheEntryListener instanceof CacheEntryUpdatedListener) {
                    listeners.updatedListenerMap.put(cacheEntryListenerConfiguration, (CacheEntryUpdatedListener<K, V>) cacheEntryListener);
                }
                if (cacheEntryListener instanceof CacheEntryRemovedListener) {
                    listeners.removedListenerMap.put(cacheEntryListenerConfiguration, (CacheEntryRemovedListener<K, V>) cacheEntryListener);
                }
                listeners.cacheEntryEventFilter = cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory().create();
            }
        }
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        if (listeners.remove(cacheEntryListenerConfiguration)) {
            if (mgnlCache instanceof EhCache3Wrapper) {
                try {
                    EhCache3Wrapper ehcacheWrapper = (EhCache3Wrapper) mgnlCache;
                    Ehcache ehCache = (Ehcache) ehcacheWrapper.getWrappedEhCache();
                    ehCache.getRuntimeConfiguration().deregisterCacheEventListener(
                        new EhcacheEventLister(this, cacheEntryListenerConfiguration));
                } catch (Exception e) {
                    log.warn("{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Adapted mgnl cache " + mgnlCache.getClass().getName() + " " + mgnlCache.getName();
    }

    @Override
    public int getSize() {
        return mgnlCache.getSize();
    }

    @Override
    public int getBlockingTimeout() {
        if (mgnlCache instanceof BlockingCache) {
            return ((BlockingCache) mgnlCache).getBlockingTimeout();
        } else {
            return -1;
        }
    }

    @Override
    public String getConfiguration() {
        StringBuilder builder = new StringBuilder();
        builder.append("class=").append(mgnlCache.getClass().getSimpleName()).append("\n");
        builder.append("keytype=").append(configuration.getKeyType()).append("\n");
        builder.append("valuetype=").append(configuration.getValueType()).append("\n");
        if (mgnlCache instanceof EhCache3Wrapper) {

            CacheRuntimeConfiguration runtimeConfiguration = ((EhCache3Wrapper) mgnlCache).getWrappedEhCache().getRuntimeConfiguration();
            builder.append(ReflectionToStringBuilder.toString(runtimeConfiguration));
        }
        return builder.toString();
    }


    protected final void handleEvent(CacheEntryEvent<? extends K, ? extends V> event) {
        handleEvents(Collections.singleton(event));
    }

    protected final void handleEvents(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
        List<CacheEntryEvent<? extends K, ? extends V>> created = new ArrayList<>();
        List<CacheEntryEvent<? extends K, ? extends V>> updated  = new ArrayList<>();
        List<CacheEntryEvent<? extends K, ? extends V>> removed  = new ArrayList<>();
        for (CacheEntryEvent<? extends K, ? extends V> event : events) {
            if (listeners.cacheEntryEventFilter != null && ! listeners.cacheEntryEventFilter.evaluate(event)) {
                continue;
            }
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
            for (CacheEntryCreatedListener<K, V> listener : listeners.createdListenerMap.values()) {
                listener.onCreated(created);
            }
        }
        if (! updated.isEmpty()) {
            for (CacheEntryUpdatedListener<K, V> listener : listeners.updatedListenerMap.values()) {
                listener.onUpdated(updated);
            }
        }
        if (! removed.isEmpty()) {
            for (CacheEntryRemovedListener<K, V> listener : listeners.removedListenerMap.values()) {
                listener.onRemoved(removed);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected final void removeAllEvent() {
        List<CacheEntryEvent<? extends K, ? extends V>> events = new ArrayList<>();
        for (Object  key : mgnlCache.getKeys()) {
            CacheValue<V> value = ((CacheValue<V>) mgnlCache.getQuiet(key));
            events.add(removeEvent((K) key, value));

        }
        handleEvents(events);
    }

    protected final CacheEntryEvent<K, V> removeEvent(K key, CacheValue<V> oldValue) {
        return new AdapterCacheEntry<>(this, EventType.REMOVED, key, oldValue, null);
    }


    @Override
    @NonNull
    public Iterator<Entry<K, V>> iterator() {
        final Iterator<Object> keys = mgnlCache.getKeys().iterator();
        return new Iterator<Entry<K, V>>() {
            @Override
            public boolean hasNext() {
                return keys.hasNext();
            }

            @Override
            @SuppressWarnings("unchecked")
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

    /**
     *
     * @since 1.16
     */
    protected static class AdapterCacheEntry<KK, VV> extends CacheEntryEvent<KK, VV> {

        final String cache;
        final KK key;
        final CacheValue<VV> oldValue;
        final CacheValue<VV> newValue;


        protected AdapterCacheEntry(
            Cache source,
            EventType eventType,
            KK key,
            CacheValue<VV> oldValue,
            CacheValue<VV> newValue) {
            super(source, eventType);
            this.cache = source.getName();
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        @Override
        public KK getKey() {
            return key;
        }

         @Override
         public VV getValue() {
             return newValue.orNull();
         }
        @Override
        public VV getOldValue() {
            return oldValue == null ? null : oldValue.orNull();
        }
        @Override
        public boolean isOldValueAvailable() {
            return oldValue != null;
        }
        @Override
        public <T> T unwrap(Class<T> clazz) {
            if (clazz.isAssignableFrom(CacheValue.class)) {
                return (T) newValue;
            }
            throw new IllegalArgumentException();
        }
        @Override
        public String toString() {
            return getEventType() + " " + cache + "#" + key;
        }
    }

    static final class Listeners<KK, VV> {
        final String name;
        final List<CacheEntryListenerConfiguration<KK, VV>> cacheEntryListenerConfigurations = new CopyOnWriteArrayList<>();
        final Map<CacheEntryListenerConfiguration<KK, VV>, CacheEntryCreatedListener<KK, VV>> createdListenerMap = new ConcurrentHashMap<>();
        final Map<CacheEntryListenerConfiguration<KK, VV>, CacheEntryUpdatedListener<KK, VV>> updatedListenerMap = new ConcurrentHashMap<>();
        final Map<CacheEntryListenerConfiguration<KK, VV>, CacheEntryRemovedListener<KK, VV>> removedListenerMap = new ConcurrentHashMap<>();
        CacheEntryEventFilter<? super KK, ? super VV> cacheEntryEventFilter;

        Listeners(String name) {
            this.name = name;
        }

        public boolean has() {
            return ! createdListenerMap.isEmpty() || ! updatedListenerMap.isEmpty() || ! removedListenerMap.isEmpty();
        }

        public boolean remove(CacheEntryListenerConfiguration<KK, VV> cacheEntryListenerConfiguration) {
            boolean remove = cacheEntryListenerConfigurations.remove(cacheEntryListenerConfiguration);
            createdListenerMap.remove(cacheEntryListenerConfiguration);
            updatedListenerMap.remove(cacheEntryListenerConfiguration);
            removedListenerMap.remove(cacheEntryListenerConfiguration);
            return remove;
        }
    }

    static final class EhcacheEventLister<KK, VV> implements  org.ehcache.event.CacheEventListener {
        final CacheEntryListenerConfiguration<KK, VV> cacheEntryListenerConfiguration;
        final CacheEntryEventFilter<? super KK, ? super VV> cacheEntryEventFilter;
        final CacheEntryListener<? super KK, ? super VV> cacheEntryListener;
        final Cache cache;



        public EhcacheEventLister(Cache cache, CacheEntryListenerConfiguration<KK, VV> cacheEntryListenerConfiguration) {
            this.cacheEntryListenerConfiguration = cacheEntryListenerConfiguration;
            this.cacheEntryEventFilter = cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory().create();
            this.cacheEntryListener = cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create();
            this.cache = cache;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EhcacheEventLister<?, ?> that = (EhcacheEventLister<?, ?>) o;

            return cacheEntryListenerConfiguration != null ? cacheEntryListenerConfiguration.equals(that.cacheEntryListenerConfiguration) : that.cacheEntryListenerConfiguration == null;
        }

        @Override
        public int hashCode() {
            return cacheEntryListenerConfiguration != null ? cacheEntryListenerConfiguration.hashCode() : 0;
        }

        @Override
        public void onEvent(CacheEvent cacheEvent) {

            Iterable<CacheEntryEvent<? extends KK, ? extends VV>> events = Arrays.asList(new AdapterCacheEntry<>(
                cache,
                EventType.valueOf(cacheEvent.getType().name()),
                (KK) cacheEvent.getKey(),
                (CacheValue<VV>) cacheEvent.getOldValue(),
                (CacheValue<VV>) cacheEvent.getNewValue()));

            switch(cacheEvent.getType()) {

                case EXPIRED:
                     if (cacheEntryListener instanceof CacheEntryExpiredListener) {
                        ((CacheEntryExpiredListener<KK, VV>) cacheEntryListener).onExpired(events);
                    }
                    break;
                case EVICTED:
                case REMOVED:
                    if (cacheEntryListener instanceof CacheEntryRemovedListener) {
                        ((CacheEntryRemovedListener<KK, VV>) cacheEntryListener).onRemoved(events);
                    }
                    break;
                case CREATED:
                     if (cacheEntryListener instanceof CacheEntryCreatedListener) {
                        ((CacheEntryCreatedListener<KK, VV>) cacheEntryListener).onCreated(events);
                    }
                    break;

                case UPDATED:
                     if (cacheEntryListener instanceof CacheEntryUpdatedListener) {
                        ((CacheEntryUpdatedListener<KK, VV>) cacheEntryListener).onUpdated(events);
                    }
                    break;
                default:
                    log.warn("Unrecognized event {}", cacheEvent);
            }
        }
    }

}
