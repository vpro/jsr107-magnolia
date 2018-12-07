package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.ehcache3.EhCache3Wrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.*;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.magnolia.jsr107.mock.MockCache;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
public class AdaptedCacheTest {

	private AdaptedCache<String, String> mockCache;
    private AdaptedCache<String, String> ehCache;

    private AdaptedCache<String, Optional<String>> cacheWithOptional;


    @Before
	public void init() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .withCache("test",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CacheValue.class, ResourcePoolsBuilder.heap(10)))
            .build();
        cacheManager.init();


        mockCache = new AdaptedCache<>(new MockCache("test"), null, null);
        cacheWithOptional = new AdaptedCache<>(new MockCache("test"), null, null);
        ehCache = new AdaptedCache<>(new EhCache3Wrapper("test", null, 100, cacheManager.getCache("test", String.class, CacheValue.class)),null, null);
	}
	@Test
	public void get() throws Exception {
		assertThat(mockCache.get("bla")).isNull();
        mockCache.put("bla", "foo");
        assertThat(mockCache.get("bla")).isEqualTo("foo");

        assertThat(cacheWithOptional.get("bla")).isNull();
        cacheWithOptional.put("bla", Optional.of("foo"));
        assertThat(cacheWithOptional.get("bla").get()).isEqualTo("foo");
	}

	@Test
	public void getAll() throws Exception {
        mockCache.put("bla", "foo");
        mockCache.put("bloe", "bar");
        mockCache.put("null", null);

        assertThat(mockCache.getAll(new HashSet<>(Arrays.asList("bla")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"));
        assertThat(mockCache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "blie")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"));
        assertThat(mockCache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "null")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"), new AbstractMap.SimpleEntry<>("null", null));
	}

	@Test
	public void containsKey() throws Exception {
        mockCache.put("bla", "foo");
        mockCache.put("null", null);


        assertThat(mockCache.containsKey("bla")).isTrue();
        assertThat(mockCache.containsKey("bloe")).isFalse();
        assertThat(mockCache.containsKey("null")).isTrue();
	}

	@Test
	public void loadAll() throws Exception {
        // Not supported.
	}

	@Test
	public void put() throws Exception {
        mockCache.put("bla", "foo");
        // Test e.g. in containsKey
	}

	@Test
	public void getAndPut() throws Exception {
        assertThat(mockCache.getAndPut("bla", "foo")).isNull();
        assertThat(mockCache.getAndPut("bla", "bar")).isEqualTo("foo");
	}

	@Test
	public void putAll() throws Exception {
	    Map<String, String> map = new HashMap<>();
        map.put("bla", "foo");
        map.put("blie", "bar");
	    mockCache.putAll(map);

        assertThat(mockCache.get("bla")).isEqualTo("foo");
        assertThat(mockCache.get("blie")).isEqualTo("bar");
        assertThat(mockCache).hasSize(2);

	}

	@Test
	public void putIfAbsent() throws Exception {
        assertThat(mockCache.putIfAbsent("bla", "foo")).isTrue();
        assertThat(mockCache.putIfAbsent("bla", "bar")).isFalse();

        assertThat(mockCache.get("bla")).isEqualTo("foo");

	}

	@Test
	public void remove() throws Exception {
        mockCache.put("bla", "foo");

        assertThat(mockCache.remove("bla")).isTrue();
        assertThat(mockCache.remove("bla")).isFalse();
        assertThat(mockCache.remove("blie")).isFalse();

        assertThat(mockCache.containsKey("bla")).isFalse();
	}

	@Test
	public void removeWithValue() throws Exception {
        mockCache.put("bla", "foo");

        assertThat(mockCache.remove("bla", "bar")).isFalse();
        assertThat(mockCache.containsKey("bla")).isTrue();
        assertThat(mockCache.remove("bla", "foo")).isTrue();
        assertThat(mockCache.containsKey("bla")).isFalse();

	}

	@Test
	public void getAndRemove() throws Exception {
        assertThat(mockCache.getAndRemove("bla")).isNull();
        mockCache.put("bla", "foo");
        assertThat(mockCache.getAndRemove("bla")).isEqualTo("foo");
	}

	@Test
	public void replace() throws Exception {
        assertThat(mockCache.replace("bla", "bar")).isFalse();
        assertThat(mockCache.containsKey("bla")).isFalse();
        mockCache.put("bla", "xx");
        assertThat(mockCache.replace("bla", "foo")).isTrue();
        assertThat(mockCache.get("bla")).isEqualTo("foo");


	}

	@Test
	public void replaceWithValue() throws Exception {
        assertThat(mockCache.replace("bla", "bar")).isFalse();
        assertThat(mockCache.containsKey("bla")).isFalse();
        mockCache.put("bla", "bar");
        assertThat(mockCache.containsKey("bla")).isTrue();
        assertThat(mockCache.replace("bla", "bar", "foo")).isTrue();
        assertThat(mockCache.get("bla")).isEqualTo("foo");
        assertThat(mockCache.replace("bla", "bar", "xx")).isFalse();
        assertThat(mockCache.get("bla")).isEqualTo("foo");
	}

	@Test
	public void getAndReplace() throws Exception {
        assertThat(mockCache.getAndReplace("bla", "bar")).isNull();
        assertThat(mockCache).hasSize(0);
        mockCache.put("bla", "foo");
        assertThat(mockCache.getAndReplace("bla", "bar")).isEqualTo("foo");
        assertThat(mockCache).hasSize(1);
        assertThat(mockCache.get("bla")).isEqualTo("bar");

	}

	@Test
	public void removeAll() throws Exception {
        mockCache.put("bla", "foo");
        mockCache.put("blie", "bar");
        mockCache.put("null", null);
        assertThat(mockCache).hasSize(3);
        mockCache.removeAll();
        assertThat(mockCache).isEmpty();

	}

	@Test
	public void removeAllWithKeys() throws Exception {
        mockCache.put("bla", "foo");
        mockCache.put("blie", "bar");
        mockCache.put("null", null);

        assertThat(mockCache).hasSize(3);
        mockCache.removeAll(new HashSet<>(Arrays.asList("bla")));
        assertThat(mockCache).hasSize(2);
	}

	@Test
	public void clear() throws Exception {
        mockCache.put("bla", "foo");
        mockCache.put("blie", "bar");
        mockCache.put("null", null);
        assertThat(mockCache).hasSize(3);
        mockCache.clear();
        assertThat(mockCache).isEmpty();
	}


	@Test
	public void iterator() throws Exception {
        mockCache.put("bla", "foo");
        mockCache.put("blie", "bar");
        mockCache.put("null", null);

        Iterator<Cache.Entry<String, String>> i = mockCache.iterator();
        assertThat(i.hasNext());
        Cache.Entry<String, String> e;
        e = i.next();
        assertThat(e.getKey()).isEqualTo("bla");
        assertThat(e.getValue()).isEqualTo("foo");
        e = i.next();
        assertThat(e.getKey()).isEqualTo("blie");
        assertThat(e.getValue()).isEqualTo("bar");
        e = i.next();
        assertThat(e.getKey()).isEqualTo("null");
        assertThat(e.getValue()).isNull();
        assertThat(i.hasNext()).isFalse();

	}


	@Test
    public void unwrap() {
	    assertThat(mockCache.unwrap(info.magnolia.module.cache.Cache.class)).isInstanceOf(MockCache.class);
    }

    @Test
    public void registerTestAndDeregisterCacheEntryListener() {
        registerTestAndDeregisterCacheEntryListener(mockCache);

    }
     @Test
    public void registerTestAndDeregisterCacheEntryListenerEhcache() {
        registerTestAndDeregisterCacheEntryListener(ehCache);

    }



    protected void registerTestAndDeregisterCacheEntryListener(AdaptedCache<String, String> cache) {
        Listener listener = new Listener();
        MutableCacheEntryListenerConfiguration<String, String> configuration = new MutableCacheEntryListenerConfiguration<>(
            new FactoryBuilder.SingletonFactory<>(listener),
            new FactoryBuilder.SingletonFactory<>(event -> true),
            false,
            true
        );
        cache.registerCacheEntryListener(configuration);

        cache.put("bla", "blie");
        cache.remove("bla");
        assertThat(listener.received).containsExactly("CREATED test#bla", "REMOVED test#bla");

        listener.received.clear();
        cache.put("pling", "blie");
        cache.removeAll();
        assertThat(listener.received).containsExactly(
            "CREATED test#pling",
            "REMOVED test#pling");

        cache.deregisterCacheEntryListener(configuration);;
        listener.received.clear();
        cache.put("pling", "blie");
        assertThat(listener.received).containsExactly();
        MutableCacheEntryListenerConfiguration<String, String> configurationWithListener = new MutableCacheEntryListenerConfiguration<>(
            new FactoryBuilder.SingletonFactory<>(listener),
            new FactoryBuilder.SingletonFactory<CacheEntryEventFilter<String, String>>(event -> event.getKey().startsWith("b")),
            false,
            true
        );
        cache.registerCacheEntryListener(configurationWithListener);

        cache.put("abc", "1");
        cache.put("bcd", "2");
        cache.put("cde", "3");
        cache.removeAll();
        assertThat(listener.received).containsExactly("CREATED test#bcd", "REMOVED test#bcd");

    }


    @Test
    public void registerUpdatedListener() {
        List<String> catcher = new ArrayList<>();
        CacheEntryUpdatedListener<String, String> listener = cacheEntryEvents -> {
            for (CacheEntryEvent<? extends String, ? extends String> e : cacheEntryEvents) {
                if (e.isOldValueAvailable()) {
                    catcher.add(e.getKey() + ":" + e.getOldValue() + " -> " + e.getValue());
                } else {
                    catcher.add(e.getKey() + ":-> " + e.getValue());
                }
            }
        };
        MutableCacheEntryListenerConfiguration<String, String> configuration = new MutableCacheEntryListenerConfiguration<>(
            new FactoryBuilder.SingletonFactory<>(listener),
            new FactoryBuilder.SingletonFactory<>(event -> true),
            false,
            true
        );
        mockCache.registerCacheEntryListener(configuration);
        mockCache.put("A", "a");
        mockCache.put("A", "b");
        mockCache.put("B", "a");
        mockCache.put("A", "c");
        mockCache.put("A", null);
        mockCache.put("A", "d");



        assertThat(catcher).containsExactly("A:a -> b", "A:b -> c", "A:c -> null", "A:null -> d");
    }


    private static class Listener
        implements
        CacheEntryCreatedListener<String, String>,
        CacheEntryExpiredListener<String, String>,
        CacheEntryUpdatedListener<String, String>,
        CacheEntryRemovedListener<String, String> {

        final List<String> received = new ArrayList<>();


        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents) throws CacheEntryListenerException {
            log.info("created {}", cacheEntryEvents);
            cacheEntryEvents.forEach((e) -> received.add(e.toString()));

        }

        @Override
        public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents) throws CacheEntryListenerException {
            log.info("expired {}", cacheEntryEvents);
            cacheEntryEvents.forEach((e) -> received.add(e.toString()));
        }

        @Override
        public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents) throws CacheEntryListenerException {
            log.info("removed {}", cacheEntryEvents);
            cacheEntryEvents.forEach((e) -> received.add(e.toString()));
        }

        @Override
        public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents) throws CacheEntryListenerException {
            log.info("updated {}", cacheEntryEvents);
            cacheEntryEvents.forEach((e) -> received.add(e.toString()));
        }
    }
}

