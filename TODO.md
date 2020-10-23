
Related issue: https://github.com/orange-cloudfoundry/paas-templates/issues/847

* Requirements
   * proxifies over http proxy (without authentication)
   * proxifies OSB endpoints because CF cloud controller does not support http_proxy per broker
   * DOES NOT proxifies custom dashboard endpoints (accessed by end users directly)
   * provides actuator and reverse proxy (spring-cloud-gateway) metrics
   * eventually multi-tenant: 
      * list of proxified endpoints configureable through configuration
         * map: broker-name -> broker url
         * broker-name is also used 
            * as a route on the reverse-proxy cf-app: broker-name.domain -> broker-url
            * as a path on the reverse-proxy cf-app: reverse-proxy.domain/broker-name -> broker-url
      * eventually self-service for osb-cmdb 3rd party service providers
         * use cases:
            * Register 3rd party broker to RP (reverse proxy) 
            * Register RP to master-depl/cf + service visibility to be picked up by 

```    
dev -> OSB client PF -> osb-cmdb -> master-depls/cf -> RP -> 3rd party broker on intranet/internet
```

* alternative designs
   * A) osb-reverse-proxy has almost no logic, paas-templates generates spring-cloud-config and cf manifest including potential dynamic routes
      * paas-templates yml (looping other brokers) done through
         * ytt templating ?
         * spruce
      * spring-cloud-gateway supported syntax is included as static config and asserted in tests   
   * B) osb-reverse-proxy encapsulates business logic and does not depend upon paas-templates templating
      * java configuration for spring cloud gateway
      * java acceptance tests

Selecting A) for now, and delaying B) when multi-tenancy gets worked on  
 
   
* [x] bump dependencies to latest
* [x] Set up circle ci, with unit test and saving test results 
* [ ] Set up and test release publication to github 

* [x] Set up configuration of the reverse proxy through environment variables 

* [x] set up http proxy in SCG
   * [x] look up documentation & google it
      * https://github.com/spring-cloud/spring-cloud-gateway/issues/176#issuecomment-554674253
      
* [ ] Initiate paas-template deployment: ops-depls
   * internal-controlplane-cf.paas domain https://miro.com/app/board/o9J_krEBr-4=/
   * org internal
   * isolation segment internal
* [ ] investigation how to set up smoke tests
   * Set up common broker scripts
   * Use overview broker
   * Assert overview broker received XFF header

* [ ] investigation how to set up acceptance tests
   * [x] http proxy java impl
      * https://github.com/adamfisk/LittleProxy
      * example end to end test at https://github.com/adamfisk/LittleProxy/blob/master/src/test/java/org/littleshoot/proxy/EndToEndStoppingTest.java
   * wiremock simulating OSB broker
   * [x] Lookup SCG tests
      * https://github.com/spring-cloud/spring-cloud-gateway/blob/578628377420ffe0237c86fbe3648ecd30434f15/spring-cloud-gateway-core/src/test/java/org/springframework/cloud/gateway/test/GatewayIntegrationTests.java

* [ ] troubleshoot basic auth not being apparently propagated
   * wiretap traces only include user-facing request and response (http server), missing http client traces
   * [ ] read SCG manual about basic auth propagation  

* [ ] enable actuator endpoints
   * [ ] backport spring security tests
      * Pb: my existing sec-group-broker/osb-cmdb tests are springweb tests and not springwebflux
         * spring-cloud-gateway requires webflux, see https://cloud.spring.io/spring-cloud-gateway/reference/html
         >  	Spring Cloud Gateway is built on Spring Boot 2.x, Spring WebFlux, and Project Reactor. As a consequence, many of the familiar synchronous libraries (Spring Data and Spring Security, for example) and patterns you know may not apply when you use Spring Cloud Gateway. If you are unfamiliar with these projects, we suggest you begin by reading their documentation to familiarize yourself with some of the new concepts before working with Spring Cloud Gateway.
         >  	Spring Cloud Gateway requires the Netty runtime provided by Spring Boot and Spring Webflux. It does not work in a traditional Servlet Container or when built as a WAR. 
   * [ ] backport spring security in webflux matter
      * https://stackoverflow.com/questions/60603772/spring-security-configuration-basic-auth-spring-cloud-gateway
   * [x] Enable spring security reactive logging
   * [ ] troubleshooting incorrect registered matcher
```
2020-10-12T09:24:37.51+0200 [APP/PROC/WEB/0] OUT 2020-10-12 07:24:37.519 DEBUG 6 --- [oundedElastic-1] a.DelegatingReactiveAuthorizationManager : Checking authorization on '/actuator/conditions' using org.springframework.security.authorization.AuthenticatedReactiveAuthorizationManager@562d7288
2020-10-12T09:24:37.52+0200 [APP/PROC/WEB/0] OUT 2020-10-12 07:24:37.521 DEBUG 6 --- [oundedElastic-1] ebSessionServerSecurityContextRepository : No SecurityContext found in WebSession: 'org.springframework.web.server.session.InMemoryWebSessionStore$InMemoryWebSession@7a2507b0'
2020-10-12T09:24:37.52+0200 [APP/PROC/WEB/0] OUT 2020-10-12 07:24:37.522 DEBUG 6 --- [oundedElastic-1] o.s.s.w.s.a.AuthorizationWebFilter       : Authorization failed: Access Denied
2020-10-12T09:24:37.52+0200 [APP/PROC/WEB/0] OUT 2020-10-12 07:24:37.523 DEBUG 6 --- [oundedElastic-1] ebSessionServerSecurityContextRepository : No SecurityContext found in WebSession: 'org.springframework.web.server.session.InMemoryWebSessionStore$InMemoryWebSession@7a2507b0'
```
   * [x] Get back to basics https://spring.io/guides/topicals/spring-security-architecture
   * [x] local set up & debugging support
   * [x] dichotomy isolate faulty scenario: 
      * authenticated() manages to authenticate actuator endpoints, but then rejects antPatchMatcher("/v2/**")
      * available spring-cloug-security traces are not helping enough  
``` 
2020-10-13 09:20:41.064 DEBUG 28766 --- [oundedElastic-1] a.DelegatingReactiveAuthorizationManager : Checking authorization on '/actuator/loggers' using org.springframework.security.authorization.AuthenticatedReactiveAuthorizationManager@46215405
2020-10-13 09:20:41.067 DEBUG 28766 --- [oundedElastic-1] ebSessionServerSecurityContextRepository : No SecurityContext found in WebSession: 'org.springframework.web.server.session.InMemoryWebSessionStore$InMemoryWebSession@245db275'
2020-10-13 09:20:41.101 DEBUG 28766 --- [oundedElastic-1] o.s.s.w.s.a.AuthorizationWebFilter       : Authorization failed: Access Denied
2020-10-13 09:20:41.115 DEBUG 28766 --- [oundedElastic-1] ebSessionServerSecurityContextRepository : No SecurityContext found in WebSession: 'org.springframework.web.server.session.InMemoryWebSessionStore$InMemoryWebSession@245db275'

```
      * same with two security config beans in the same config. Order does not seem to have effects
      * springboot actuator config isn't loaded as soon as custom websecurity is configured
   * [ ] try to bump spring security: 5.4.1
      * https://docs.spring.io/spring-security/site/docs/5.4.0/reference/html5/#new
      https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#new
   * [x] get inspiration from spring security unit tests to find the right reactive syntax to define an antMatcher at the top
   * Remaining questions
      * How to list the webflux security filters ?
         * https://stackoverflow.com/questions/61334409/how-can-i-print-the-filter-chain-order-list-for-spring-webflux-apps (unanswered, point servlet property)
      * How to get detailed trace indicating which filter is rejecting the request ? 
      * How does actuactor gets served by spring cloud gateway ? Is it in forwardRoutingFilter ?
   * Complete 
   
   
-----

How to provide self service http request log details ?
* spring boot 2.0 actuator httptrace can not include request/response body. 
   * See https://github.com/spring-projects/spring-boot/issues/12953#issuecomment-383830749
   * Workarounds based on boot 1.x are not working with boot 2.0 designed to support webflux https://stackoverflow.com/questions/49991723/spring-boot-2-actuator-2-0-1-release-request-and-response-body?noredirect=1&lq=1
   * custom endpoint using webflux API that record http traces (e.g. on disk) and then serve it over http
   * modify httptrace impl
      * can't read directly the http request as it can only be read once
      * https://stackoverflow.com/a/64080867/1484823
>  another approach work in spring cloud gateway 2.2.5, we will use ReadBodyPredicateFactory, as this will cache requestBody to ServerWebExchange with attribute key cachedRequestBodyObject

      * pb: ServerWebExchangeTraceableRequest is hard coded into HttpTraceWebFilter, see https://github.com/spring-projects/spring-boot/blob/aef92b9295f62d008faa9ab79905a474bf3496f3/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/web/trace/reactive/HttpTraceWebFilter.java#L87
         * need to duplicate it
      * pb: ReadBodyPredicateFactory returns filter=false when no body in the request (e.g. /v2/catalog) leading to 404 https://github.com/spring-cloud/spring-cloud-gateway/issues/1762
         * duplicated it and fixed it
      * Pb: ReadBodyPredicateFactory only handles requests body and not response body
         * reuse ResponseBodyGatewayFilterFactory with a filter which just records the original response into the exchange
            * Pb: the extended filter is never recognized as a filter because its Config object does not match its Filter class name
               * Duplicate ResponseBodyGatewayFilterFactory
            * Pb: this requires java dsl, and does not support yaml
               * Duplicate it with hardcoding Config entries to result to an empty Config object
                  * Pb: this breaks compilation as most code is using type inference. 
                     * Consider keeping a ReadBodyPredicateFactory.Config statically initialized 
            * [ ] Use java config to set up ResponseBodyGatewayFilterFactory unmodified
               * Implies to move the whole Route configuration to java config
                  * [x] ConfigurationProperties for SCG java config 
                  * [x] Initial Route config
                    - request and response body recorded as headers into actuator httptrace response
                    - duplicates actuator httptraces
                  * Fix Initial Route config
                    - [x] request and response body recorded as headers into actuator httptrace response
                    - [ ] duplicates actuator httptraces
            
            
            
            
            
            
            
            
            
            
            
            
            
            
* spring cloud gateway 
   * custom filter and custom endpoint https://github.com/spring-cloud/spring-cloud-gateway/issues/1003#issuecomment-541740864
   * Pivotal spring cloud gateway does not display http req/res body in actuator endpoint either https://docs.pivotal.io/spring-cloud-gateway/1-0/actuator-endpoints.html#instance-accessing-httptrace-endpoint 
* logback http filter is likely for servlet api (not working with webflux) http://logback.qos.ch/recipes/captureHttp.html
* zipkin
   * spring cloud sleuth is NOT logging requests body
      * https://stackoverflow.com/questions/53013039/how-to-configure-spring-cloud-gateway-to-use-sleuth-to-log-request-response-body
      * https://docs.spring.io/spring-cloud-sleuth/docs/current/reference/html/#http
      * https://github.com/openzipkin/brave/tree/master/instrumentation/http#span-data-policy
>      By default, the following are added to both http client and server spans:
>          Span.name is the http method in lowercase: ex "get" or a route described below
>          Tags:
>              "http.method", eg "GET"
>              "http.path", which does not include query parameters.
>              "http.status_code" when the status is not success.
>              "error", when there is an exception or status is >=400
>          Remote IP and port information
      * https://gitter.im/openzipkin/zipkin?at=5ca0156693fb4a7dc2a9e791 march 2019
> @kirenjolly right now we don't expose getting the http body generically. 
> We probably should add a comment to that file as to why. Not only does this likely result in things too big to put into a span, but also it can cause problems in your production requests. For example, not all http bodies are replayable.. ex if it is a stream and we consume it, the app couldn't.
you can ask here about "blob service" which is what @jcchavezs was referring to.
   * logging req/resp body would require
      * a custom netty HttpTracing impl
         * See base impl at https://github.com/openzipkin/brave/blob/3a8bb062f68d6729f80c38969e87c32ce42a7681/instrumentation/netty-codec-http/src/main/java/brave/netty/http/TracingHttpServerHandler.java#L35
         * See https://github.com/netty/netty/blob/c061bd17986785426552df659a30f4ada491350c/handler/src/main/java/io/netty/handler/logging/LoggingHandler.java#L40 about built-in netty logger
         * No evidence of HttpTracing http impl in sleuth source code that would add request trace        
   * https://www.baeldung.com/tracing-services-with-zipkin
   * https://www.baeldung.com/spring-cloud-sleuth-single-application
* temporary grant service consummers read access to the reverse-proxy logs
