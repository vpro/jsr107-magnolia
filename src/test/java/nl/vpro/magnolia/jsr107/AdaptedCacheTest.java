package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.*;

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

	private AdaptedCache<String, String> cache;
    private AdaptedCache<String, Optional<String>> cacheWithOptional;


    @Before
	public void init() {
        cache = new AdaptedCache<>(new MockCache("test"), null, null);
        cacheWithOptional = new AdaptedCache<>(new MockCache("test"), null, null);
	}
	@Test
	public void get() throws Exception {
		assertThat(cache.get("bla")).isNull();
        cache.put("bla", "foo");
        assertThat(cache.get("bla")).isEqualTo("foo");

        assertThat(cacheWithOptional.get("bla")).isNull();
        cacheWithOptional.put("bla", Optional.of("foo"));
        assertThat(cacheWithOptional.get("bla").get()).isEqualTo("foo");
	}

	@Test
	public void getAll() throws Exception {
        cache.put("bla", "foo");
        cache.put("bloe", "bar");
        cache.put("null", null);

        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"));
        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "blie")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"));
        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "null")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"), new AbstractMap.SimpleEntry<>("null", null));
	}

	@Test
	public void containsKey() throws Exception {
        cache.put("bla", "foo");
        cache.put("null", null);


        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.containsKey("bloe")).isFalse();
        assertThat(cache.containsKey("null")).isTrue();
	}

	@Test
	public void loadAll() throws Exception {
        // Not supported.
	}

	@Test
	public void put() throws Exception {
        cache.put("bla", "foo");
        // Test e.g. in containsKey
	}

	@Test
	public void getAndPut() throws Exception {
        assertThat(cache.getAndPut("bla", "foo")).isNull();
        assertThat(cache.getAndPut("bla", "bar")).isEqualTo("foo");
	}

	@Test
	public void putAll() throws Exception {
	    Map<String, String> map = new HashMap<>();
        map.put("bla", "foo");
        map.put("blie", "bar");
	    cache.putAll(map);

        assertThat(cache.get("bla")).isEqualTo("foo");
        assertThat(cache.get("blie")).isEqualTo("bar");
        assertThat(cache).hasSize(2);

	}

	@Test
	public void putIfAbsent() throws Exception {
        assertThat(cache.putIfAbsent("bla", "foo")).isTrue();
        assertThat(cache.putIfAbsent("bla", "bar")).isFalse();

        assertThat(cache.get("bla")).isEqualTo("foo");

	}

	@Test
	public void remove() throws Exception {
        cache.put("bla", "foo");

        assertThat(cache.remove("bla")).isTrue();
        assertThat(cache.remove("bla")).isFalse();
        assertThat(cache.remove("blie")).isFalse();

        assertThat(cache.containsKey("bla")).isFalse();
	}

	@Test
	public void removeWithValue() throws Exception {
        cache.put("bla", "foo");

        assertThat(cache.remove("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.remove("bla", "foo")).isTrue();
        assertThat(cache.containsKey("bla")).isFalse();

	}

	@Test
	public void getAndRemove() throws Exception {
        assertThat(cache.getAndRemove("bla")).isNull();
        cache.put("bla", "foo");
        assertThat(cache.getAndRemove("bla")).isEqualTo("foo");
	}

	@Test
	public void replace() throws Exception {
        assertThat(cache.replace("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isFalse();
        cache.put("bla", "xx");
        assertThat(cache.replace("bla", "foo")).isTrue();
        assertThat(cache.get("bla")).isEqualTo("foo");


	}

	@Test
	public void replaceWithValue() throws Exception {
        assertThat(cache.replace("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isFalse();
        cache.put("bla", "bar");
        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.replace("bla", "bar", "foo")).isTrue();
        assertThat(cache.get("bla")).isEqualTo("foo");
        assertThat(cache.replace("bla", "bar", "xx")).isFalse();
        assertThat(cache.get("bla")).isEqualTo("foo");
	}

	@Test
	public void getAndReplace() throws Exception {
        assertThat(cache.getAndReplace("bla", "bar")).isNull();
        assertThat(cache).hasSize(0);
        cache.put("bla", "foo");
        assertThat(cache.getAndReplace("bla", "bar")).isEqualTo("foo");
        assertThat(cache).hasSize(1);
        assertThat(cache.get("bla")).isEqualTo("bar");

	}

	@Test
	public void removeAll() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);
        assertThat(cache).hasSize(3);
        cache.removeAll();
        assertThat(cache).isEmpty();

	}

	@Test
	public void removeAllWithKeys() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);

        assertThat(cache).hasSize(3);
        cache.removeAll(new HashSet<>(Arrays.asList("bla")));
        assertThat(cache).hasSize(2);
	}

	@Test
	public void clear() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);
        assertThat(cache).hasSize(3);
        cache.clear();
        assertThat(cache).isEmpty();
	}


	@Test
	public void iterator() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);

        Iterator<Cache.Entry<String, String>> i = cache.iterator();
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
	    assertThat(cache.unwrap(info.magnolia.module.cache.Cache.class)).isInstanceOf(MockCache.class);
    }

    @Test
    public void registerCacheEntryListener() {
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
    public void deregisterCacheEntryListener() {
        // unsupported
    }


    public static class Listener
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

