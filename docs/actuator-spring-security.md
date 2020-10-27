* [x] enable actuator endpoints
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
         * A: Actuator httptrace collects info through a registered WebFilter
         * A: Actuator registers a HandlerMapping bean to receive some traffic, see more at https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web-reactive.html#webflux-special-bean-types 
   * Complete 
