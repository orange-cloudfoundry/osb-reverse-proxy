
* [ ] investigation how to set up acceptance tests
   * [x] http proxy java impl
      * https://github.com/adamfisk/LittleProxy
      * example end to end test at https://github.com/adamfisk/LittleProxy/blob/master/src/test/java/org/littleshoot/proxy/EndToEndStoppingTest.java
   * wiremock simulating OSB broker
   * [x] Lookup SCG tests
      * https://github.com/spring-cloud/spring-cloud-gateway/blob/578628377420ffe0237c86fbe3648ecd30434f15/spring-cloud-gateway-core/src/test/java/org/springframework/cloud/gateway/test/GatewayIntegrationTests.java

* [ ] add automated tests for actuator traces
   * [ ] add base skeletton by copy/paste spring cloud gateway tests related to modify body filter    
      * [ ] relies on http://httpbin.org:80 online service    
   * [ ] invoke httpbin with request body      
      * [ ] invoke httptrace actuator endpoint, and assert request and response headers is present
   * Result: 
      * good for testing custom gateway routes designed to comply to httpbin endpoints. 
      * Not suited for testing OSB v2 endpoints, which would require an echo service or a wiremock
         * No traces of wiremock in spring-cloud-gateway source code
         * Transiently paused this test effort for now
