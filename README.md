# github-issue-export

Simple propject to export github issues to a xls spreadsheet.

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](https://github.com/fugerit-org/github-issue-export/blob/master/CHANGELOG.md) 
[![Maven Central](https://img.shields.io/maven-central/v/org.fugerit.java/github-issue-export.svg)](https://mvnrepository.com/artifact/org.fugerit.java/github-issue-export)
[![license](https://img.shields.io/badge/License-Apache%20License%202.0-teal.svg)](https://opensource.org/licenses/Apache-2.0)
[![code of conduct](https://img.shields.io/badge/conduct-Contributor%20Covenant-purple.svg)](https://github.com/fugerit-org/fj-universe/blob/main/CODE_OF_CONDUCT.md)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fugerit-org_github-issue-export&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fugerit-org_github-issue-export)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=fugerit-org_github-issue-export&metric=coverage)](https://sonarcloud.io/summary/new_code?id=fugerit-org_github-issue-export)

![Java runtime version](https://img.shields.io/badge/run%20on-java%208+-%23113366.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Java build version](https://img.shields.io/badge/build%20on-java%2011+-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-3.9.0+-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

## Quickstart

### Clone (use master or develop branch for latest snapshot, release/x.x.x branch for stable versions)
git clone https://github.com/fugerit-org/github-issue-export.git

### Build
From base dir : 
mvn clean install -P singlepackage

### Run with a simple gui
java -jar target/dist-github-issue-export-X.X.X.jar

### Run on command line
java -jar target/dist-github-issue-export-X.X.X.jar --gui 0 --owner fugerit-org --repo github-issue-export --lang it --xls-file target/report.xls

