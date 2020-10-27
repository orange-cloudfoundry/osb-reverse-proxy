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
            * [x] Use java config to set up ResponseBodyGatewayFilterFactory unmodified
               * Implies to move the whole Route configuration to java config
                  * [x] ConfigurationProperties for SCG java config 
                  * [x] Initial Route config
                    - request and response body recorded as headers into actuator httptrace response
                    - duplicates actuator httptraces
                  * Fix Initial Route config
                    - [x] request and response body recorded as headers into actuator httptrace response
                    - [x] duplicates actuator httptraces
            * [x] clean up http proxy handling to allow local debugging without proxy
               * remove input validation, and only keep a single property injected in application.yml
               * replace osbreverseproxy proxy with spring cloud gateway properties
                  * possibly validating input by getting them injected
            * [x] submit issue to springboot to simplify injection of custom behavior. https://github.com/spring-projects/spring-boot/issues/23907
            * [ ] simplify forked springboot actuactor code ?  
            * [x] test replacement of  ReadBodyPredicate with ReplaceRequestBody lambda
            * [x] refine security config to restrict actuator httptrace access to service consummer
               * [x] add new service consummer login/pwd entries in properties
            * [x] Limit DoS by 
               * [x] trimming saved context to 10kB    
               * [x] removing saved body from exchange
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
                     
            * [x] PR doc spring security
            * [x] manual end 2 end test
            * [x] refine smoke tests assertions ?
                * httptrace contain request/response body     
                * httptrace rejected on app domain     
                * v2/catalog rejected without auth     
            
            
            
            
            
            
            
            
            
            
            
            
            
            
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
