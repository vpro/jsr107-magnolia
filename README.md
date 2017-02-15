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


##Installation

Download the most recent jar from: https://oss.sonatype.org/content/repositories/snapshots/nl/vpro/jsr107-magnolia and install it like you'd normally would.

Or you can add this to your pom.xml
```xml
<dependency>
  <groupId>nl.vpro</groupId>
  <artifactId>jsr107-magnolia</artifactId>
  <version>1.4</version>
</dependency>
```
###Configuration
You can configure the create cache like so in the JCR-tree:
![cache configuration](cache-config.png?raw=true "Cache configuration")

Cache-configurations can be automaticly created like this using tasks on the version handler of your module.
E.g.
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


