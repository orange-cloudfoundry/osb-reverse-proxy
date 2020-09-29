
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