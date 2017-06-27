# jsr107-magnolia
[![Build Status](https://travis-ci.org/vpro/jsr107-magnolia.svg?)](https://travis-ci.org/vpro/jsr107-magnolia)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/nl.vpro/jsr107-magnolia/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/nl.vpro/jsr107-magnolia)

See 
 - https://www.magnolia-cms.com/
 - https://github.com/jsr107

 
After installation you can cache the result of any method of any guice managed bean by adding the `@CacheResult` annotation.
```java
@CacheResult(cacheName = "CinemaUtil-sortedMovies")
public List<Map.Entry<Movie, Set<RoleType>>> sortedMovies(Person person) {
   ...
}
```

In this case in the magnolia cache configuration automaticly a cache 'CinemaUtil-sortedMovies' will appear.

## Possible cache values
The cache values may be `null` and `Optional`. This implementation will arrange that no nulls are stored in the underlying magnolia cache. If the value is `Optional`, the value of the `Optional` will be serialized.

Non serializable values are only possible if the underlying eh-cache is configured not to store to disk.

## Model classes
Sadly, [model classes are not instantiated by guice, but by Magnolia itself](https://jira.magnolia-cms.com/browse/MAGNOLIA-6601), so they cannot be proxied by guice.


## Installation

Download the most recent jar from: https://oss.sonatype.org/content/repositories/snapshots/nl/vpro/jsr107-magnolia and install it like you'd normally would.

Or you can add this to your pom.xml
```xml
<dependency>
  <groupId>nl.vpro</groupId>
  <artifactId>jsr107-magnolia</artifactId>
  <version>1.14</version>
</dependency>
```
### Configuration
For versions older then 1.14 caches were configure like so in the JCR-tree:
![cache configuration](cache-config.png?raw=true "Cache configuration")
From 1.14 onwards as Magnolia 5.5.4 uses ehcache3 so the configuration has changed and looks like this:
![cache configuration](cache-config-ehcache3.png?raw=true "Ehcache 3 configuration")


Cache-configurations can be automaticly created using tasks on the version handler of your module.
E.g. like this:
```java
@Slf4j
public class CinemaVersionHandler extends DefaultModuleVersionHandler {
 
    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        List<Task> tasks = super.getBasicInstallTasks(installContext);
        tasks.addAll(CreateConfigurationTasks.createConfigurationTasks(CinemaUtilWithCaching.class));
        log.info("Created tasks {}", tasks);
        return tasks;
    }
}
```
Default settings could be configured using the `nl.vpro.magnolia.jsr107.DefaultCacheSettings` annotation:
```java
    @CacheResult(cacheName = "CinemaUtil-scheduleForChannel")
    @DefaultCacheSettings(blockingTimeout = 30000)
    List<ScheduleItem> scheduleForChannel(String channel, LocalDate date) {
        log.info("Getting movies for  {} {}", channel, date);
        MediaSearch search = new MediaSearch();
        ....        
```     
If you use an 'exception cache' too, you may want to configure this separately. You need to wrap a @nl.vpro.magnolia.jsr107.Defaults then.
```java
 @CacheResult(cacheKeyGenerator = ImageCacheKey.class, cacheName = ASSET_LINKS_CACHE, exceptionCacheName = ASSET_LINKS_CACHE + "-exceptions")
    @Defaults(
        overrideOnUpdate = true,
        exceptionCacheSettings = @DefaultCacheSettings(maxElementsInMemory = 200, timeToLiveSeconds = 300, timeToIdleSeconds = 300),
        cacheSettings = @DefaultCacheSettings(maxElementsInMemory = 2000, timeToLiveSeconds = 3600, timeToIdleSeconds = 3600)
    )
    @Override
   public String getAssetLink(Image image, String variation) {

```
Actually the code can also be accessed if you want to configure a cache programmaticly for some other reason. This more or less eliminates the need to configure cache outside code altogether.
The cache settings are in this way still visible in the JCR-tree, and can be modified and viewed via JMX, but they can be maintained in the code of your application.
```java
       // Create browser cache for api clients
        setInstallOrUpdateTask(CreateCacheConfigurationTask.builder()
            .name(CACHE)
            .settings(CacheSettings.builder()
                .eternal(true)
                .overflowToDisk(true)
                .diskSpoolBufferSizeMB(500)
                .maxElementsInMemory(200)
                .diskExpiryThreadInterval(Duration.ofHours(24))
                .overflowToDisk(true)
            )
            .overrideOnUpdate(true)
            .build());


```
## MgnlCacheManager

The `nl.vpro.magnolia.jsr107.MgnlCacheManager` implementation of `javax.cache.CacheManager` contains a few utility which may come in useful when interacting with caches. E.g. utilities to get existing values from the caches, or all keys, which can be used when activily refreshing entries in the cache (e.g. in conjection with `@javax.cache.annotation.CachePut`)

A `MgnlCacheManager` can simply be obtained using `@Inject`.
