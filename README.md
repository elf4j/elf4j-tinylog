# elf4j-tinylog

The [tinylog](https://tinylog.org/v2/) service provider binding for the Easy Logging Facade for
Java ([ELF4J](https://github.com/elf4j/elf4j-api)) SPI

## User story

As a service provider of the Easy Logging Facade for Java ([ELF4J](https://github.com/elf4j/elf4j-api)) SPI, I want to
bind the logging capabilities of tinylog to the ELF4J client application via
the [Java Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism, so
that any application using the ELF4J API for logging can opt to use the tinylog framework at deployment time without
code change.

## Prerequisite

- Java 8+
- [ELF4J](https://github.com/elf4j/elf4j-api) 2.1.0+
- tinylog 2.5.0+

## Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-tinylog.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-tinylog%22)

## Use it...

If you are using the ELF4J API for logging, and wish to select or change to use tinylog as the run-time implementation,
then simply pack this binding JAR in the classpath when the application deploys. e.g. with Maven, this would be a
dependency of runtime scope:

```
        <dependency>
            <groupId>io.github.elf4j</groupId>
            <artifactId>elf4j-tinylog</artifactId>
            <version>grab the latest from maven central</version>
            <scope>runtime</scope>
        </dependency>
```

No code change needed.

At compile time, the client code is unaware of this run-time logging service provider. Because of the ELF4J API, opting
for tinylog as the logging implementation is a deployment-time decision.

The usual [tinylog configuration](https://tinylog.org/v2/configuration/) applies.

