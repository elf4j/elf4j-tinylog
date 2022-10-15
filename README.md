# elf4j-tinylog

The tinylog provider binding for Easy Logging Facade for Java (ELF4J)

## User story

As a Service Provider of the [Easy Logging Facade for Java (ELF4J)](https://github.com/elf4j/elf4j-api), I want to bind
the logging capabilities of [tinylog](https://tinylog.org/v2/) to the ELF4J client application via the
Java [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism, so that the client application can
use the tinylog features at run-time.

## Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-tinylog.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-tinylog%22)

## Use it...

Simply pack the JAR of this binding in the deployment classpath of any ELF4J client application. e.g. in Maven pom.xml,
this provider bind JAR will be added as a runtime-scoped dependency.

At compile time, the client application codebase only has dependency on the Easy Logging Facade for Java (ELF4J) API,
and is unaware of this run-time logging service provider. Using tinylog as the logging implementation is a deployment
time decision of such client application.