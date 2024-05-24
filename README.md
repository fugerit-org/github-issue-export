# github-issue-export

Simple propject to export github issues to a xls spreadsheet.

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](https://github.com/fugerit-org/github-issue-export/blob/master/CHANGELOG.md) 
[![Maven Central](https://img.shields.io/maven-central/v/org.fugerit.java/github-issue-export.svg)](https://mvnrepository.com/artifact/org.fugerit.java/github-issue-export)
[![license](https://img.shields.io/badge/License-Apache%20License%202.0-teal.svg)](https://opensource.org/licenses/Apache-2.0)
[![code of conduct](https://img.shields.io/badge/conduct-Contributor%20Covenant-purple.svg)](https://github.com/fugerit-org/fj-universe/blob/main/CODE_OF_CONDUCT.md)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fugerit-org_github-issue-export&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fugerit-org_github-issue-export)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=fugerit-org_github-issue-export&metric=coverage)](https://sonarcloud.io/summary/new_code?id=fugerit-org_github-issue-export)

[![Java runtime version](https://img.shields.io/badge/run%20on-java%208+-%23113366.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://universe.fugerit.org/src/docs/versions/java11.html)
[![Java build version](https://img.shields.io/badge/build%20on-java%2011+-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://universe.fugerit.org/src/docs/versions/java11.html)
[![Apache Maven](https://img.shields.io/badge/Apache%20Maven-3.9.0+-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)](https://universe.fugerit.org/src/docs/versions/maven3_9.html)
[![Fugerit Github Project Conventions](https://img.shields.io/badge/Fugerit%20Org-Project%20Conventions-1A36C7?style=for-the-badge&logo=Onlinect%20Playground&logoColor=white)](https://universe.fugerit.org/src/docs/conventions/index.html)

## Quickstart

build : 

`mvn clean install -P singlepackage`

run as gui : 

`java -jar target/dist-github-issue-export-*.jar`

run command line : 

``` 
java -jar target/dist-github-issue-export-*.jar --gui 0 \
  --owner fugerit-org \
  --repo github-issue-export \
  --lang it \
  --xls-file target/report.xls 
```

## **parameter help**  

| **name**             | **required** | **default** | **description**                                           | **since** | **info**                                                                |
|----------------------|--------------|-------------|-----------------------------------------------------------|-----------|-------------------------------------------------------------------------|
| `gui`                | `false`      | `true`      | If `true` will open the Export GUI.                       | 0.6.2     | `true` or `1` will both evaluate to `true`.                             |
| `owner`              | `false`      | none        | Repository owner (ex. 'fugerit-org').                     | 0.6.2     | Required in command line mode (gui parameter = 0).                      |
| `repo`               | `false`      | none        | Repository name (ex. 'github-issue-export').              | 0.6.2     | Required in command line mode (gui parameter = 0).                      |
| `xls-file`           | `false`      | none        | Path to the .xls file (ex. 'report.xls').                 | 0.6.2     | Required in command line mode (gui parameter = 0).                      |
| `github-token`       | `false`      | none        | Github auth token.                                        | 0.6.2     | Needed for privare repositories or to increase github api usage limits. |
| `lang`               | `false`      | none        | Language code, currently supported : 'en', 'it'.          | 0.6.2     | If not set will default to default locale or en.                        |
| `assignee_date_mode` | `false`      | all         | Accepted value are 'all', 'skip', 'cache', 'skip-closed'. | 1.2.0     | When set to skip export is faster.                                      |
| `help`               | `false`      | none        | Print help about the tool.                                | 0.1.0     |                                                                         |

