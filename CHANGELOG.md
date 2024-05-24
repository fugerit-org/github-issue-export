# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.1] - 2024-05-24

### Fixed

- fj-universe-tool parent set to 2.3.1

### Fixed

- vulnerable dependancies

### Added

## [1.2.0] - 2024-05-24

- handles param 'assignee_date_mode' (when set to 'skip' the report is much faster)

## [1.1.0] - 2024-05-22

### Added

- 'xlsx' output format (simple specify xlsx extension for file path)

## [1.0.0] - 2024-02-19

### Changed

- parent set to fj-universe-tool 0.5.8

### Fixed

- cell content over 32767 is now truncated, see [Worksheet and workbook specifications and limits](https://support.microsoft.com/en-gb/office/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3)

## [0.7.0] - 2023-09-28

### Added

- 'help' and 'github-token' parameter parameter
- command line reference

### Changed

- parent set to fj-universe-tool 0.4.7

### Fixed

- removed link from Java and Maven badges
- placeholder in LICENSE set
- software version links

## [0.6.2] - 2023-09-24

### Added

- src/main/config/log4j2.xml configuration to use on singlepackage profile

### Changed

- now a github token is expected instead of the password, the github user is ignored (anyway github does not support anymore user/pass basic auth)
- in GUI github password renamed in github token

### Security

- [Basic authentication should not be used](https://github.com/fugerit-org/github-issue-export/issues/22)

## [0.6.1] - 2023-09-24

### Changed

- update code of conduct and added badge
- fj-bom set to 1.4.4
- increased test coverage
- substituted XMLResourceBundleControl  with the class in fj-core

### Removed

- Sonar cloud workflow yml removed. (after being merged with maven build)

## [0.6.0] - 2023-09-24

### Added

- [workflow deploy on branch deploy](.github/workflows/deploy_maven_package.yml)
- [Sonar cloud workflow merged in maven build](.github/workflows/deploy_maven_package.yml)
- keep a changelog and coverage badge
- fj-bom version set to 1.4.2

### Changed

- Workflow adapted for gui test (running on windows)
- fj-core version set to 8.3.8

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
