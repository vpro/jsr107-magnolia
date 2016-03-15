# jsr107-magnolia
[![Build Status](https://travis-ci.org/vpro/jsr107-magnolia.svg?)](https://travis-ci.org/vpro/jsr107-magnolia)

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

Note: Sadly, model classes are not instantiated by guice, but by Magnolia itself, so they cannot be proxied by guice.


##Installation

Download the most recent jar from: https://oss.sonatype.org/content/repositories/snapshots/nl/vpro/jsr107-magnolia and install it like you'd normally would.

Or you can add this to your pom.xml
```xml
<dependency>
  <groupId>nl.vpro</groupId>
  <artifactId>jsr107-magnolia</artifactId>
  <version>1.0</version>
</dependency>
```
###Configuration
You can configure the create cache like so in the JCR-tree:
![cache configuration](cache-config.png?raw=true "Cache configuration")

