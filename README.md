### Lesson Learned

#### Exception Handler
I create a `GlobalExceptionHandler` class, a Spring Boot component—annotated with @ControllerAdvice—that sits “above” all your @RestController endpoints and intercepts any uncaught exceptions, turning them into structured HTTP responses.

What it is and how it works?  
1. Centralized exception interception  
   - Declared with `@RestControllerAdvice`, it applies across every controller in your application.  
   - Methods annotated with `@ExceptionHandler` specify which exception types to catch (e.g. your `ApplicationException`, validation errors, or even `Exception` as a fallback).  
2. Consistent error payload  
   - Builds a uniform `ApiError` JSON object (timestamp, HTTP status, error text, message, request path, optional details).  
   - Ensures every error response follows the same schema, simplifying client-side parsing and documentation.  
3. Clear mapping to HTTP status codes  
   - For your custom exceptions (via `ApplicationException`), it reads the embedded `ErrorCode` and sets the proper status automatically.  
   - For validation failures (`MethodArgumentNotValidException`, `BindException`), it returns 400 with a list of field errors.  
   - A catch-all handler ensures any other uncaught exception becomes a 500 Internal Server Error, without leaking internal stack traces.

Why you need a `GlobalExceptionHandler`?  
* DRY error handling  
Without it, every controller (or service) would need boilerplate try/catch blocks to translate exceptions into HTTP responses. Centralizing eradicates repetition.  
* Separation of concerns  
Business logic stays focused on domain rules—throwing exceptions when things go wrong—while the handler deals solely with “how to present” errors to clients.   
* Consistent API contract  
Clients (mobile apps, front-ends, third-party integrations) can rely on a single, documented error schema instead of dealing with ad-hoc JSON or HTML error pages.  
* Easier observability and logging  
You can hook into the handler to log errors in a uniform way, enrich metrics, or trigger alerts for certain exception types, all in one place.  
* Future extensibility  
Need multi-language error messages, correlation IDs, or hints for remediation? You only update the handler (and ApiError), not dozens of controllers or service classes.

#### About RedisCache
##### RedisTemplate vs RedisCache
For most CRUD‐style cache‐aside patterns—where you simply want “check cache → fallback to DB → populate cache” and “evict on write”—Spring’s Cache Abstraction (via @Cacheable/@CacheEvict) is usually the easier, more declarative choice:  

| Criterion                    | RedisTemplate + Manual Service      | Spring Cache Abstraction                         |
| ---------------------------- | ----------------------------------- | ------------------------------------------------ |
| Control over serialization   | Full, per–operation via RedisConfig | Central, via RedisCacheConfiguration             |
| Boilerplate in service layer | High (explicit get/put/evict calls) | Low (just annotate methods)                      |
| Fine–grained cache behavior  | Unlimited flexibility               | Annotations cover 90% of use–cases               |
| Metrics & monitoring         | You must instrument manually        | Built-in via Micrometer (cache hits/misses)      |
| Migration to clustered/HA    | Manual setup                        | Still manual, but all config in RedisCacheConfig |

###### When to pick RedisTemplate + Manual Service
* You need per‐entry TTLs, per‐operation differing key structures or eviction rules.  
* You’re doing complex operations (e.g. list/set/hashes) beyond simple key→value.  
* You need explicit control over serialization, expiration, or pipelining within your service logic.

###### When to pick Spring Cache Abstraction
* Your use‐case is simply cache this method’s return or evict that key on update/delete.  
* You want to minimize manual cache code and lean on Spring’s annotations.  
* You’d like built-in cache metrics (hit/miss ratios) without extra code.

###### In summary

* For simple cache‐aside on your service methods, go with Spring Cache Abstraction plus the configuration `RedisCacheConfig`.  
* If you have specialized caching logic (per‐request TTL, complex key math, reactive needs), stick with RedisTemplate and your service.

##### Why choose lettuce instead of jedis
Spring Boot’s choice of Lettuce over Jedis as the default Redis client is driven by several key advantages:  
1. Thread-safety & Connection Multiplexing  
   * Lettuce uses a single, Netty-based RedisClient instance that can safely be shared across multiple threads. Internally it multiplexes requests over a small number of TCP connections.  
   * Jedis, in contrast, requires you to manage a pool of separate connections (JedisPool) if you want to be thread-safe—otherwise you risk data corruption or exceptions when sharing a Jedis instance across threads.  
2. Non-blocking I/O & Reactive Support  
   * Lettuce is built on Netty and supports non-blocking I/O out of the box, making it a natural fit for both imperative and reactive (Spring WebFlux) applications.  
   * Jedis is purely blocking/block-per-connection, so it can’t be used in a reactive pipeline without costly thread-switching wrappers.  
3. Scalability Under High Concurrency  
   * With connection multiplexing, Lettuce maintains high throughput and lower resource usage under heavy concurrent loads—fewer sockets open, less pooling overhead.  
   * Jedis’s pool scales only by opening more connections, which increases memory and file-descriptor usage.  
4. Cluster & Sentinel Features
   * Lettuce has first-class support for Redis Cluster and Sentinel, handling topology changes, failover events, and slot migrations seamlessly.  
   * While Jedis also supports cluster and sentinel, its blocking model means failover events can block client threads.  
5. Simpler Configuration & Fewer Moving Parts  
   * With Lettuce you don’t need to configure and tune a separate pool—you just set spring.redis.lettuce.pool.* if you want pooling on top, but the default “single client” model often suffices.  
   * Jedis always requires a pool; missing or mis-configured pools can easily lead to resource exhaustion.

When might you choose Jedis instead?  
* Legacy codebases already tied to Jedis and its API.
* Simple scripts or one-off utilities where blocking I/O is fine and dependency size matters.  

But for a modern Spring Boot microservice—especially one that may grow into reactive or clustered deployments—Lettuce is the recommend choice for its performance, scalability, and feature set.

#### Lombok Issues

##### Annotation Processor Error

When I run the test, Intellij complain about:
"java: variable postService not initialized in the default constructor"

How to solve  
1. Go to Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Switch to “obtain processors from project classpath”

