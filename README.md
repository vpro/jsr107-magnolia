# jsr107-magnolia
[![Build Status](https://travis-ci.org/vpro/jsr107-magnolia.svg?)](https://travis-ci.org/vpro/jsr107-magnolia)

See https://jira.magnolia-cms.com/browse/SUPPORT-5655


Configure it like so:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module SYSTEM "module.dtd">
<module>
  <name>Your module/name>
  ...
  <components>
    <id>main</id>
    <configurer>
      <class>nl.vpro.magnolia.jsr107.CacheConfigurer</class>
    </configurer>
  </components>
</module>
```

Then you can cache like so:

```java
 @CacheResult(cacheName = "CinemaUtil-sortedMovies")
public List<Map.Entry<Movie, Set<RoleType>>> sortedMovies(Person person) {
   ...
```

In the magnolia cache configuration automaticly a cache 'CinemaUtil-sortedMovies' will appear.


Installation

Download the most recent jar from: https://oss.sonatype.org/content/repositories/snapshots/nl/vpro/jsr107-magnolia

Or you can add this to your pom.xml

<dependency>
    <groupId>nl.vpro</groupId>
    <artifactId>jsr107-magnolia</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
