# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- [Sonar cloud workflow merged in maven build](.github/workflows/deploy_maven_package.yml)
- Workflow adapted for gui test (running on windows)

### Removed

- Sonar cloud workflow yml removed. (after being merged with maven build)


## [0.8.0] - 2023-09-21

### Added

- [workflow deploy on branch deploy](.github/workflows/deploy_maven_package.yml)
- [workflow maven build](.github/workflows/build_maven_package.yml)
- keep a changelog and coverage badge
- fj-bom version set to 1.4.0

### Changed

- [workflow sonar cloud](.github/workflows/sonarcloud-maven.yml)
- fj-core version set to 8.3.7

### Security 

- fixed dependabot issues

## [0.5.1] - 2019-06-08

### Added

- Repository authentication implemeneted (api limits are higher this way)

## [0.4.0] - 2018-01-12

### Added

- support for assign date cache

## [0.3.0] - 2017-09-29

### Added

- save configuration in GUI

## [0.3.0] - 2017-09-25

### Added

- CODE_OF_CONDUCT.md, CONTRIBUTING.md, LICENSE
- gui interface (with english and italian it18n)
- proxy support
