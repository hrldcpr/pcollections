# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.0.5] - 2019-08-02
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
