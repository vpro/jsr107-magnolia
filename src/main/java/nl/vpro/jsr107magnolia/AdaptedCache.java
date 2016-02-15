package nl.vpro.jsr107magnolia;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
class AdaptedCache<K, V> implements Cache<K, V> {

	private final info.magnolia.module.cache.Cache mgnlCache;

	public AdaptedCache(info.magnolia.module.cache.Cache mgnlCache) {
		this.mgnlCache = mgnlCache;
	}

	@Override
	public V get(K key) {
		return (V) mgnlCache.get(key);
	}

	@Override
	public Map<K, V> getAll(Set<? extends K> keys) {
		Map<K, V> result = new HashMap<>();
		for (K k : keys) {
			result.put(k, (V) mgnlCache.get(k));
		}
		return result;
	}

	@Override
	public boolean containsKey(K key) {
		return mgnlCache.hasElement(key);
	}

	@Override
	public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {


	}

	@Override
	public void put(K key, V value) {
		mgnlCache.put(key, value);
	}

	@Override
	public V getAndPut(K key, V value) {
		mgnlCache.put(key, value);
		return value;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
			mgnlCache.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public boolean putIfAbsent(K key, V value) {
		return false;

	}

	@Override
	public boolean remove(K key) {
		return false;

	}

	@Override
	public boolean remove(K key, V oldValue) {
		return false;

	}

	@Override
	public V getAndRemove(K key) {
		return null;

	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return false;

	}

	@Override
	public boolean replace(K key, V value) {
		return false;

	}

	@Override
	public V getAndReplace(K key, V value) {
		return null;

	}

	@Override
	public void removeAll(Set<? extends K> keys) {
		System.out.println("--");

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
		return (C) MgnlCacheConfiguration.instance;
	}

	@Override
	public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
		return null;

	}

	@Override
	public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
		return null;

	}

	@Override
	public String getName() {
		return mgnlCache.getName();
	}

	@Override
	public CacheManager getCacheManager() {
		return null;

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
		throw new UnsupportedOperationException();
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
						return (V) mgnlCache.get(key);

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
