# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Faster `OrderedPMap.containsKey()` and `OrderedPSet.contains()` [[#122](https://github.com/hrldcpr/pcollections/pull/122) [#123](https://github.com/hrldcpr/pcollections/pull/123) by [@oxbowlakes](https://github.com/oxbowlakes)]
- …

## [4.0.2] - 2024-03-17

- Serialization bug fix [[#115](https://github.com/hrldcpr/pcollections/pull/115) by [@tonivade](https://github.com/tonivade)]

## [4.0.1] - 2022-10-28

### Added

- Java 9+ Module support [[#109](https://github.com/hrldcpr/pcollections/pull/109) by [@BomBardyGamer](https://github.com/BomBardyGamer)]

### Changed

- More specific return types for each `PSet.intersect()` implementation [[#107](https://github.com/hrldcpr/pcollections/pull/107) by [@prdoyle](https://github.com/prdoyle)]

## [4.0.0] - 2022-10-18

### Added

- `OrderedPMap` [[#102](https://github.com/hrldcpr/pcollections/pull/102)]
- `PSet.intersect()` [[#99](https://github.com/hrldcpr/pcollections/pull/99) by [@prdoyle](https://github.com/prdoyle)]

### Changed

- Mutator methods consistently throw `UnsupportedOperationException` [[#93](https://github.com/hrldcpr/pcollections/issues/93)] [[#97](https://github.com/hrldcpr/pcollections/pull/97) by [@prdoyle](https://github.com/prdoyle)]
- `OrderedPSet.minus()` is faster—logarithmic instead of linear [[#101](https://github.com/hrldcpr/pcollections/pull/101)]
- Null values are now supported [[#97](https://github.com/hrldcpr/pcollections/pull/97) by [@prdoyle](https://github.com/prdoyle)] [[7e2f912...6094912](https://github.com/hrldcpr/pcollections/compare/7e2f912...6094912)]

### Removed

- ~`POrderedSet`~ interface, since it adds nothing beyond `PSet` [[#98](https://github.com/hrldcpr/pcollections/pull/98)]
- ~`OrderedPSet.get()`~ and ~`OrderedPSet.indexOf()`~ [[#98](https://github.com/hrldcpr/pcollections/pull/98)]

## [3.2.0] - 2022-08-17

### Added

- Sorted maps and sorted sets [[#92](https://github.com/hrldcpr/pcollections/pull/92) by [@ran-arigur](https://github.com/ran-arigur)]

### Changed

- Only use one build system [[#64](https://github.com/hrldcpr/pcollections/issues/64)]

## [3.1.4] - 2020-09-13

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
