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

#### Lombok Issues

##### Annotation Processor Error

When I run the test, Intellij complain about:
"java: variable postService not initialized in the default constructor"

How to solve  
1. Go to Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Switch to “obtain processors from project classpath”

