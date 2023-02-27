The [tinylog](https://tinylog.org/v2/) service provider binding for Easy Logging Facade for
Java ([ELF4J](https://github.com/elf4j/elf4j)) SPI

# User story

As a logging service provider of the [ELF4J](https://github.com/elf4j/elf4j) SPI, I want to bind the logging
capabilities of tinylog to ELF4J client applications, so that any application using the ELF4J API for logging can opt to
use tinylog at deployment time without code change.

# Prerequisite

Java 8+

# Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-tinylog.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-tinylog%22)

# Use it...

If you are using the [ELF4J API](https://github.com/elf4j/elf4j) for logging, and wish to select or
change to use tinylog as the run-time implementation, then simply pack this binding JAR in the classpath when the
application deploys. No code change needed. At compile time, the client code is unaware of this run-time logging service
provider. With the ELF4J facade, opting for tinylog as the logging implementation is a deployment-time decision.

The usual [tinylog configuration](https://tinylog.org/v2/configuration/) applies.

With Maven, in addition to use compile-scope on the [ELF4J API](https://github.com/elf4j/elf4j) dependency, an end-user
application would use runtime-scope for this provider as a dependency:

```html

<dependency>
    <groupId>io.github.elf4j</groupId>
    <artifactId>elf4j</artifactId>
    <scope>compile</scope>
</dependency>

<dependency>
    <groupId>io.github.elf4j</groupId>
    <artifactId>elf4j-tinylog</artifactId>
    <scope>runtime</scope>
</dependency>
```

Note: Only one logging provider such as this should be in effect at run-time. If multiple providers end up in the final
build of an application, somehow, then the `elf4j.logger.factory.fqcn` system property will have to be used to select
the desired provider. For example,

```
java -Delf4j.logger.factory.fqcn="elf4j.tinylog.TinylogLoggerFactory" -jar MyApplication.jar
```