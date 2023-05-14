[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-tinylog.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-tinylog%22)

# elf4j-tinylog

An adapter to use [tinylog](https://tinylog.org/v2/) as service provider and runtime log engine for
the [ELF4J](https://github.com/elf4j/elf4j) (Easy Logging Facade for Java) API

## User story

As an application developer using the [ELF4J](https://github.com/elf4j/elf4j) API, I want to have the option of
selecting [tinylog](https://tinylog.org/v2/) as my log engine, at application deploy time without code change or
re-compile.

## Prerequisite

Java 8+

## Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-tinylog.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-tinylog%22)

In Maven or other build tools alike, install [ELF4J API](https://github.com/elf4j/elf4j) as compile-scope dependency;
and this provider as runtime-scope dependency.

## Use it...

If you are using the [ELF4J API](https://github.com/elf4j/elf4j) for logging, and wish to select or
change to use tinylog as the run-time log engine, then simply pack this binding JAR in the classpath when the
application deploys. No code change needed. At compile time, the client code is unaware of this run-time logging service
provider. With the ELF4J facade, opting for tinylog as the logging implementation is a deployment-time decision.

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

The usual [tinylog configuration](https://tinylog.org/v2/configuration/) applies.

Note: Only one logging provider such as this should be in effect at run-time. If multiple providers end up in the final
build of an application, somehow, then the `elf4j.logger.factory.fqcn` system property will have to be used to select
the desired provider. For example,

```
java -Delf4j.logger.factory.fqcn="elf4j.tinylog.TinylogLoggerFactory" -jar MyApplication.jar
```
