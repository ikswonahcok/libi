# Libi

**Libi** is a simple command line tool to analyse and visualise dependencies between projects.
Written in Java for gradle projects, but can be extended further to use with other languages and
build tools.

## Getting Started

## LibiEL

## Dependencies

- [JGraphT](https://jgrapht.org/) is Java library providing graph-theory objects, algorithms and
  serialization utils to many common formats. JGraphT is licensed under the terms of the Eclipse
  Public License (EPL).
- [ANTLR](https://www.antlr.org/) is a parser generator. ANTLR is licensed under the terms of the
  BSD 3-clause license
- [Spring](https://spring.io/) is a general purpose application framework for Java platform. Spring
  is licensed under the terms of the Apache License 2.0
- [Gradle](https://gradle.org/) is a build tool licensed under the terms of the Apache License 2.0

## External applications

### Graphviz

Libi can call graphviz as external program to automatically generate graph diagrams.

### Gradlew

When scanning for dependencies in every folder gradlew script is called to get dependency tree.