
# osb-reverse-proxy

[![CircleCI Build status](https://circleci.com/gh/orange-cloudfoundry/osb-reverse-proxy.svg?style=svg)](https://app.circleci.com/pipelines/github/orange-cloudfoundry/osb-reverse-proxy)

A reverse http proxy tailored for open service broker (OSB) api endpoints

## Introduction

Some osb-clients such as Cloud Foundry do not support brokers that need to be reached through the http(s) proxy. 

osb-reverse-proxy provides a generic reverse proxy to front service brokers. 

The following ascii art diagram illustrates the network path when a developer requests a `cf create-service` command

```
developer -> Cloud Foundry cloud controller -> osb-reverse-proxy -> http proxy -> service broker  
```

## Deploying 

This reverse proxy is a java springboot app which can be deployed onto cloufoundry using the java buildpack.

See the expected environment variables in the [ApplicationTest](src/test/java/com/orange/oss/osbreverseproxy/ApplicationTest.java)

In this initial version, the jar distribution merely contains a packaged version of [spring-cloud-gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html) and is designed to be configured using additional springboot configuration matching the [spring cloud gateway configuration](https://cloud.spring.io/spring-cloud-gateway/reference/html/#configuring-route-predicate-factories-and-gateway-filter-factories)

Multi-tenancy support with osb-specific configuration is planned. See [TODO](TODO.md) for details. 

## Releasing

* manually edit the version in `gradle.properties` (e.g `version=0.1.0`), commit & push
* git tag v0.1.0 -a -m "0.1.0 release"
* git push origin  v0.1.0
* let circle ci build and upload the binaries to github
* edit the github release to complete release notes
* manually edit the version in `gradle.properties`, commit & push e.g. `version=0.2.0.BUILD-SNAPSHOT`
