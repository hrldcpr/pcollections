# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- Empty Iterator.next() throws `NoSuchElementException` [[#46](https://github.com/hrldcpr/pcollections/pull/46) by [@ilya-g](https://github.com/ilya-g)]

## [3.1.3] - 2020-01-28
### Fixed
- `ConsPStack.listIterator()` indices and `ConsPStack.indexOf()` were broken
- Stack overflows in `ConsPStack` [[#82](https://github.com/hrldcpr/pcollections/pull/82)]
### Changed
- `ConsPStack.minusAll(list)` reuses existing structure when possible

## [3.1.2] - 2019-12-14
### Added
- Config file for users of GraalVM native images [[#80](https://github.com/hrldcpr/pcollections/pull/80) by [@jkremser](https://github.com/jkremser)]

## [3.1.1] - 2019-12-11
### Fixed
- Serialization crash for `IntTreePMap` and associated classes such as `HashTreePSet` and `TreePVector` [[#79](https://github.com/hrldcpr/pcollections/issues/79) reported by [@Maaartinus](https://github.com/Maaartinus)]

## [3.1.0] - 2019-08-02
### Added
- `IntTreePMap.minusRange()`
### Changed
- Faster `TreePVector.subList()` makes fewer calls to `TreePVector()`, `IntTreePMap()`, and `IntTreePMap.withKeysChangedAbove()` [suggested by [@Groostav](https://github.com/Groostav) in [#74](https://github.com/hrldcpr/pcollections/issues/74)]
- Reformat with [google-java-format](https://github.com/google/google-java-format)

## [3.0.4] - 2019-07-24
### Fixed
- Stack overflows for large stacks and vectors when calling `ConsPStack.subList()` and `TreePVector.subList()` [[#74](https://github.com/hrldcpr/pcollections/issues/74) reported by [@Groostav](https://github.com/Groostav)]

## [3.0.3] - 2018-09-12
### Fixed
- HashPMap serialization no longer breaks after `.entrySet()` has been called [[#71](https://github.com/hrldcpr/pcollections/issues/71) reported by [@Noctune](https://github.com/Noctune)]

## [3.0.2] - 2018-05-14
### Added
- This changelog!
### Changed
- Compatibility with Java 1.6+, and Android [[#67](https://github.com/hrldcpr/pcollections/pull/67) by [@guenhter](https://github.com/guenhter)]
- Use Gradle 4.7 [[#66](https://github.com/hrldcpr/pcollections/pull/66) by [@guenhter](https://github.com/guenhter)]
