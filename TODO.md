
Related issue: https://github.com/orange-cloudfoundry/paas-templates/issues/847

* Requirements
   * proxifies over http proxy (without authentication)
   * proxifies OSB endpoints
   * proxifies custom dashboard endpoints
   * provides actuator and reverse proxy (spring-cloud-gateway) metrics
   * multi-tenant: 
      * list of proxified endpoints configureable through configuration
      * eventually self-service for osb-cmdb 3rd party service providers
         * use cases:
            * Register 3rd party broker to RP (reverse proxy) 
            * Register RP to master-depl/cf + service visibility to be picked up by 

```    
dev -> OSB client PF -> osb-cmdb -> master-depls/cf -> RP -> 3rd party broker on intranet/internet
```
* [x] bump dependencies to latest
* [x] Set up circle ci, with unit test and saving test results 
* [ ] Set up and test release publication to github 

* [ ] Set up configuration of the reverse proxy through environment variables 
   * 

* [ ] set up http proxy in SCG
   * [ ] look up documentation & google it
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
