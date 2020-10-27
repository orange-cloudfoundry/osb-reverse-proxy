
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

## Osb api logs

Osb-reverse proxy collects and serves logs of OSB API requests using an extension of springboot actuator httptrace support with request and response body added as http headers

```bash
$ curl -u redacted-user:redacted-password https://osb-reverse-proxy.redacted-domain.org/actuator/httptrace | jq .
[
    {
      "timestamp": "2020-10-27T11:22:53.529Z",
      "principal": null,
      "session": null,
      "request": {
        "method": "GET",
        "uri": "https://osb-reverse-proxy.internal-controlplane-cf.paas/v2/catalog",
        "headers": {
          "X-Cf-Instanceid": [
            "fa7644e2-7faf-4bb7-49a0-b3aa"
          ],
          "X-Broker-Api-Version": [
            "2.15"
          ],
          "Accept": [
            "application/json"
          ],
          "X-Forwarded-Proto": [
            "https"
          ],
          "X-Broker-Api-Request-Identity": [
            "e452c1f5-fdfe-4e47-b754-fbb31665f6ad"
          ],
          "User-Agent": [
            "HTTPClient/1.0 (2.8.3, ruby 2.5.5 (2019-03-15))"
          ],
          "X-Request-Start": [
            "1603797773450"
          ],
          "X-Broker-Api-Originating-Identity": [
            "cloudfoundry ewogICJ1c2VyX2lkIjogIjBkMDIxMTdiLWFhMjEtNDNlMi1iMzVlLThhZDZmODIyMzUxOSIKfQ=="
          ],
          "Host": [
            "osb-reverse-proxy.internal-controlplane-cf.paas"
          ],
          "X-Vcap-Request-Id": [
            "4ae90935-358e-45f4-6cde-83fca76bc6e7"
          ],
          "Date": [
            "Tue, 27 Oct 2020 11:22:53 GMT"
          ],
          "X-Cf-Instanceindex": [
            "0"
          ],
          "B3": [
            "d23918d4d277397c-d23918d4d277397c"
          ],
          "X-Api-Info-Location": [
            "api.redacted-cf-api-domain.org/v2/info"
          ],
          "X-B3-Spanid": [
            "d23918d4d277397c"
          ],
          "X-Cf-Applicationid": [
            "1c5cce4c-6f2c-439d-b647-8cb09d453c16"
          ],
          "X-Forwarded-For": [
            "192.168.35.66, 192.168.35.50"
          ],
          "X-B3-Traceid": [
            "d23918d4d277397c"
          ]
        },
        "remoteAddress": null
      },
      "response": {
        "status": 200,
        "headers": {
          "X-Content-Type-Options": [
            "nosniff"
          ],
          "response_body": [
            "{\"services\":[{\"name\":\"overview-service\",\"description\":\"Provides an ..."
          ],
          "Pragma": [
            "no-cache"
          ],
          "X-Vcap-Request-Id": [
            "ceef1981-6c91-4d51-701a-17f1f6b5a54c"
          ],
          "Date": [
            "Tue, 27 Oct 2020 11:22:53 GMT"
          ],
          "Referrer-Policy": [
            "no-referrer"
          ],
          "X-Frame-Options": [
            "DENY"
          ],
          "Strict-Transport-Security": [
            "max-age=31536000 ; includeSubDomains"
          ],
          "Cache-Control": [
            "no-cache, no-store, max-age=0, must-revalidate"
          ],
          "Etag": [
            "W/\"37ea-JtM0TfwLN4WsJxfxq42Qe2dtP7U\""
          ],
          "Expires": [
            "0"
          ],
          "X-XSS-Protection": [
            "1 ; mode=block"
          ],
          "Content-Length": [
            "14314"
          ],
          "X-Powered-By": [
            "Express"
          ],
          "Content-Type": [
            "application/json; charset=utf-8"
          ]
        }
      },
      "timeTaken": 0
    },
[...]
]
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
