package nl.vpro.magnolia.jsr107;

import lombok.ToString;

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

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@ToString
class UnblockingCache<K, V> implements Cache<K, V> {

    private final AdaptedCache<K, V> cache;

    public UnblockingCache(AdaptedCache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public V get(K key) {
        return cache.getUnblocking(key);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return cache.getAll(keys);
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);

    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        cache.loadAll(keys, replaceExistingValues, completionListener);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V getAndPut(K key, V value) {
        return cache.getAndPut(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);

    }

    @Override
    public boolean remove(K key) {
        return cache.remove(key);

    }

    @Override
    public boolean remove(K key, V oldValue) {
        return cache.remove(key, oldValue);

    }

    @Override
    public V getAndRemove(K key) {
        return cache.getAndRemove(key);

    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return cache.replace(key, oldValue, newValue);

    }

    @Override
    public boolean replace(K key, V value) {
        return cache.replace(key, value);

    }

    @Override
    public V getAndReplace(K key, V value) {
        return cache.getAndReplace(key, value);

    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        cache.removeAll(keys);

    }

    @Override
    public void removeAll() {
        cache.removeAll();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        return cache.getConfiguration(clazz);

    }

    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
        return cache.invoke(key, entryProcessor, arguments);

    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
        return cache.invokeAll(keys, entryProcessor, arguments);

    }

    @Override
    public String getName() {
        return cache.getName();

    }

    @Override
    public CacheManager getCacheManager() {
        return cache.getCacheManager();

    }

    @Override
    public void close() {
        cache.close();
    }

    @Override
    public boolean isClosed() {
        return cache.isClosed();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (AdaptedCache.class.isAssignableFrom(clazz)) {
            return (T) cache;
        } else {
            return cache.unwrap(clazz);
        }

    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        cache.registerCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        cache.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return cache.iterator();
    }
}
