# Weather Aggregator Service

A production-style Spring Boot REST API that aggregates weather information from multiple external weather providers.

The application demonstrates:

* REST API development with Spring Boot
* External REST API integration using `WebClient`
* Multiple weather providers
* Provider fallback
* Redis-based response caching
* Resilience4j Retry
* Resilience4j Circuit Breaker
* Timeout handling
* DTO-based API design
* Global exception handling
* Bean validation
* Actuator monitoring
* Swagger/OpenAPI
* Unit testing
* Docker-based Redis

---

## 1. Problem Statement

Applications frequently need weather information but should not depend directly on a single third-party weather provider.

A production-grade weather service should:

1. Call an external weather provider.
2. Cache frequently requested weather data.
3. Avoid repeatedly calling external APIs for the same location.
4. Retry temporary provider failures.
5. Stop calling an unhealthy provider after repeated failures.
6. Automatically fall back to another provider.
7. Hide external provider-specific response formats from API consumers.

This project implements these requirements.

---

# 2. High-Level Architecture

```text
                         Client
                           |
                           | GET /api/weather?city=Bengaluru
                           |
                           v
                  +-------------------+
                  | Weather Controller |
                  +---------+---------+
                            |
                            v
                  +-------------------+
                  |  Weather Service   |
                  +---------+---------+
                            |
                            v
                     +-------------+
                     |    Redis    |
                     |    Cache    |
                     +------+------+
                            |
                       Cache MISS
                            |
                            v
                  +-------------------+
                  | Provider Chain    |
                  +---------+---------+
                            |
                +-----------+-----------+
                |                       |
                v                       v
       +----------------+      +----------------+
       |  Open-Meteo    |      |   WeatherAPI   |
       |    Primary     |      |   Secondary    |
       +-------+--------+      +-------+--------+
               |                       |
          Retry + CB              Retry + CB
               |                       |
               +-----------+-----------+
                           |
                           v
                    WeatherResponse
                           |
                           v
                         Redis
                           |
                           v
                         Client
```

---

# 3. Technology Stack

| Technology        | Purpose                   |
| ----------------- | ------------------------- |
| Java 17           | Programming language      |
| Spring Boot       | Application framework     |
| Spring MVC        | REST API                  |
| Spring WebClient  | External API calls        |
| Spring Cache      | Cache abstraction         |
| Redis             | Distributed cache         |
| Resilience4j      | Retry and Circuit Breaker |
| Maven             | Build tool                |
| Docker            | Redis container           |
| Spring Actuator   | Monitoring                |
| SpringDoc OpenAPI | Swagger                   |
| JUnit 5           | Testing                   |
| Mockito           | Mocking                   |

---

# 4. Project Structure

```text
weather-aggregator-service/
│
├── pom.xml
│
├── README.md
│
└── src/
    │
    ├── main/
    │   │
    │   ├── java/
    │   │   └── com/example/weather/
    │   │       │
    │   │       ├── WeatherAggregatorApplication.java
    │   │       │
    │   │       ├── client/
    │   │       │   ├── OpenMeteoClient.java
    │   │       │   └── WeatherApiClient.java
    │   │       │
    │   │       ├── config/
    │   │       │   ├── RedisCacheConfig.java
    │   │       │   └── WebClientConfig.java
    │   │       │
    │   │       ├── controller/
    │   │       │   └── WeatherController.java
    │   │       │
    │   │       ├── dto/
    │   │       │   └── WeatherResponse.java
    │   │       │
    │   │       ├── exception/
    │   │       │   ├── ExternalWeatherApiException.java
    │   │       │   ├── GlobalExceptionHandler.java
    │   │       │   └── LocationNotFoundException.java
    │   │       │
    │   │       ├── model/
    │   │       │   ├── ForecastResponse.java
    │   │       │   ├── GeocodingResponse.java
    │   │       │   └── WeatherApiResponse.java
    │   │       │
    │   │       ├── provider/
    │   │       │   ├── WeatherProvider.java
    │   │       │   ├── OpenMeteoProvider.java
    │   │       │   └── WeatherApiProvider.java
    │   │       │
    │   │       └── service/
    │   │           └── WeatherService.java
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/
        └── java/
            └── com/example/weather/
                └── service/
                    └── WeatherServiceTest.java
```

---

# 5. API

## Get Current Weather

### Endpoint

```http
GET /api/weather
```

### Request Parameter

```text
city
```

### Example

```http
GET http://localhost:8080/api/weather?city=Bengaluru
```

### cURL

```bash
curl "http://localhost:8080/api/weather?city=Bengaluru"
```

---

# 6. Example Response

```json
{
  "city": "Bengaluru",
  "country": "India",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "temperatureCelsius": 27.4,
  "feelsLikeCelsius": 28.1,
  "humidityPercent": 68,
  "windSpeedKmh": 8.4,
  "description": "Partly cloudy"
}
```

The exact weather values will vary because the service retrieves live data from the external provider.

---

# 7. Redis Caching

Redis is used to avoid unnecessary calls to external weather APIs.

The service uses:

```java
@Cacheable(
    value = "weather",
    key = "#city.trim().toLowerCase()"
)
```

### First Request

```text
GET /api/weather?city=Bengaluru
             |
             v
        Redis MISS
             |
             v
       Weather Provider
             |
             v
       Weather Response
             |
             v
          Redis
             |
             v
          Client
```

### Subsequent Request

```text
GET /api/weather?city=Bengaluru
             |
             v
        Redis HIT
             |
             v
          Client
```

The external provider is not called when a valid cached response exists.

---

# 8. Cache Configuration

Default cache TTL:

```text
10 minutes
```

Example:

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 10m
      cache-null-values: false
```

Redis is particularly useful when the application runs multiple instances:

```text
                  Load Balancer
                       |
             +---------+---------+
             |                   |
             v                   v
        Application 1       Application 2
             |                   |
             +---------+---------+
                       |
                       v
                    Redis
```

All application instances can share the same cached weather data.

---

# 9. Redis Setup

The easiest way to run Redis locally is Docker.

```bash
docker run -d \
  --name weather-redis \
  -p 6379:6379 \
  redis:7
```

Verify:

```bash
docker ps
```

Test Redis:

```bash
docker exec -it weather-redis redis-cli
```

Then:

```text
PING
```

Expected:

```text
PONG
```

---

# 10. Weather Providers

The application uses two providers.

## Primary Provider

```text
Open-Meteo
```

## Secondary Provider

```text
WeatherAPI
```

The abstraction is:

```java
public interface WeatherProvider {

    WeatherResponse getWeather(String city);
}
```

This follows the Strategy Pattern and allows additional providers to be added without changing the controller.

---

# 11. Provider Flow

Normal request:

```text
WeatherService
      |
      v
Open-Meteo
      |
   SUCCESS
      |
      v
WeatherResponse
```

If Open-Meteo fails:

```text
WeatherService
      |
      v
Open-Meteo
      |
    FAIL
      |
      v
WeatherAPI
      |
   SUCCESS
      |
      v
WeatherResponse
```

If both providers fail:

```text
WeatherService
      |
      +---- Open-Meteo ---- FAIL
      |
      +---- WeatherAPI ---- FAIL
      |
      v
HTTP 502
```

---

# 12. Retry

Temporary failures should not immediately trigger fallback.

Resilience4j Retry is used.

Example configuration:

```yaml
resilience4j:

  retry:

    instances:

      openMeteo:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2

      weatherApi:
        max-attempts: 2
        wait-duration: 500ms
```

For Open-Meteo:

```text
Attempt 1
   |
 FAIL
   |
500 ms
   |
Attempt 2
   |
 FAIL
   |
1000 ms
   |
Attempt 3
```

Only after the retry policy is exhausted do we move toward fallback handling.

---

# 13. Circuit Breaker

The Circuit Breaker protects the application from continuously calling an unhealthy provider.

Circuit states:

```text
              +---------+
              | CLOSED  |
              +----+----+
                   |
             Too many failures
                   |
                   v
              +---------+
              |  OPEN   |
              +----+----+
                   |
              Wait period
                   |
                   v
              +----------+
              | HALF_OPEN|
              +----+-----+
                   |
              Test requests
              /          \
          SUCCESS        FAIL
             |             |
             v             v
          CLOSED          OPEN
```

Example:

```yaml
circuitbreaker:

  instances:

    openMeteo:

      sliding-window-type: COUNT_BASED

      sliding-window-size: 10

      minimum-number-of-calls: 5

      failure-rate-threshold: 50

      wait-duration-in-open-state: 30s

      permitted-number-of-calls-in-half-open-state: 2

      automatic-transition-from-open-to-half-open-enabled: true
```

---

# 14. Why Circuit Breaker + Retry?

Retry and Circuit Breaker solve different problems.

### Retry

Handles temporary failures.

```text
Network timeout
      |
      v
Retry
      |
      v
Success
```

### Circuit Breaker

Handles persistent failures.

```text
Provider
   |
Repeated failures
   |
   v
Circuit OPEN
   |
   X
Stop calling provider
   |
   v
Fallback provider
```

Together:

```text
Request
   |
   v
Primary Provider
   |
   v
Retry
   |
   +---- Success ---> Response
   |
   +---- Failure
           |
           v
      Circuit Breaker
           |
           v
        Fallback
           |
           v
    Secondary Provider
```

---

# 15. WebClient Timeout

The application configures:

```text
Connection timeout = 3 seconds

Response timeout = 5 seconds
```

Example:

```java
HttpClient httpClient =
        HttpClient.create()
                .option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    3000
                )
                .responseTimeout(
                    Duration.ofSeconds(5)
                );
```

This prevents requests from hanging indefinitely.

---

# 16. Environment Variables

WeatherAPI requires an API key.

Set:

```text
WEATHER_API_KEY
```

### Windows CMD

```cmd
set WEATHER_API_KEY=YOUR_API_KEY
```

### PowerShell

```powershell
$env:WEATHER_API_KEY="YOUR_API_KEY"
```

### Linux/macOS

```bash
export WEATHER_API_KEY=YOUR_API_KEY
```

The application reads:

```yaml
api-key: ${WEATHER_API_KEY:}
```

Do not commit the real API key to Git.

---

# 17. Running the Application

Start Redis first:

```bash
docker start weather-redis
```

Build the project:

```bash
mvn clean install
```

Run:

```bash
mvn spring-boot:run
```

Application:

```text
http://localhost:8080
```

---

# 18. Testing the API

### Bengaluru

```bash
curl "http://localhost:8080/api/weather?city=Bengaluru"
```

### Mumbai

```bash
curl "http://localhost:8080/api/weather?city=Mumbai"
```

### Delhi

```bash
curl "http://localhost:8080/api/weather?city=Delhi"
```

### London

```bash
curl "http://localhost:8080/api/weather?city=London"
```

---

# 19. Testing Cache

Call:

```bash
curl "http://localhost:8080/api/weather?city=Bengaluru"
```

First request:

```text
Redis cache MISS
Calling primary provider: Open-Meteo
```

Call again:

```bash
curl "http://localhost:8080/api/weather?city=Bengaluru"
```

The second request should be served from Redis.

You should not see another external-provider invocation.

---

# 20. Inspect Redis

Connect:

```bash
docker exec -it weather-redis redis-cli
```

List keys:

```text
KEYS *
```

You may see:

```text
weather::bengaluru
```

Check TTL:

```text
TTL weather::bengaluru
```

Example:

```text
592
```

The value represents the number of seconds remaining before expiration.

---

# 21. Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI documentation:

```text
http://localhost:8080/v3/api-docs
```

---

# 22. Actuator

Health:

```text
http://localhost:8080/actuator/health
```

Metrics:

```text
http://localhost:8080/actuator/metrics
```

Cache information:

```text
http://localhost:8080/actuator/caches
```

---

# 23. Testing

Run:

```bash
mvn test
```

The project contains unit tests for the service layer.

Recommended additional tests:

```text
WeatherServiceTest
OpenMeteoProviderTest
WeatherApiProviderTest
WeatherControllerTest
OpenMeteoClientTest
WeatherApiClientTest
```

For external API integration testing, WireMock can be added to simulate provider responses.

---

# 24. Error Handling

The service returns meaningful HTTP status codes.

### City Not Found

```http
404 NOT FOUND
```

Example:

```json
{
  "status": 404,
  "error": "Location Not Found",
  "message": "City not found: XYZ"
}
```

### Provider Failure

```http
502 BAD GATEWAY
```

Example:

```json
{
  "status": 502,
  "error": "Weather Provider Unavailable",
  "message": "All weather providers are unavailable"
}
```

---

# 25. Design Patterns Used

## Strategy Pattern

Weather providers implement:

```java
WeatherProvider
```

Implementations:

```text
OpenMeteoProvider
WeatherApiProvider
```

This makes adding another provider easy.

---

## Adapter Pattern

External provider responses are converted into:

```java
WeatherResponse
```

The API consumer doesn't need to know the external provider's response structure.

---

## Cache-Aside Pattern

The application effectively follows:

```text
Read Redis
   |
   +---- HIT ----> Return
   |
   +---- MISS
            |
            v
      External API
            |
            v
          Redis
            |
            v
         Return
```

---

## Circuit Breaker Pattern

Protects the application from unhealthy providers.

---

## Retry Pattern

Handles transient failures.

---

# 26. SOLID Principles

### Single Responsibility

Each component has a focused responsibility:

```text
Controller  -> HTTP
Service     -> Business logic
Client      -> External API communication
Provider    -> Provider-specific mapping
Config      -> Configuration
```

### Open/Closed Principle

A new provider can be added:

```java
class AccuWeatherProvider
        implements WeatherProvider {
}
```

without modifying the existing controller.

### Dependency Inversion

The service works with provider abstractions rather than directly embedding HTTP logic.

---

# 27. Production Deployment Architecture

A production deployment could look like:

```text
                         Internet
                            |
                            v
                       API Gateway
                            |
                            v
                      Load Balancer
                            |
             +--------------+--------------+
             |                             |
             v                             v
       Weather Service 1            Weather Service 2
             |                             |
             +--------------+--------------+
                            |
                            v
                      Redis Cluster
                            |
                            v
                    External Providers
                     /             \
                    /               \
             Open-Meteo          WeatherAPI
```

---

# 28. Kubernetes Deployment

For Kubernetes:

```text
                     Ingress
                        |
                        v
                 Weather Service
                  Deployment
                 /           \
                v             v
             Pod 1          Pod 2
                \             /
                 \           /
                    Redis
```

Recommended:

* Horizontal Pod Autoscaler
* Kubernetes Secrets
* ConfigMaps
* Readiness probe
* Liveness probe
* Redis HA
* Resource limits
* Network policies

---

# 29. Observability

For production, expose:

```text
Actuator
   |
   +---- Health
   |
   +---- Metrics
   |
   +---- Cache statistics
   |
   +---- Circuit breaker metrics
   |
   +---- Retry metrics
```

A production monitoring stack can be:

```text
Spring Boot
     |
     v
Micrometer
     |
     v
Prometheus
     |
     v
Grafana
```

Useful metrics include:

```text
weather_provider_requests_total
weather_provider_failures_total
weather_provider_latency
weather_cache_hits
weather_cache_misses
circuit_breaker_state
retry_attempts
```

---

# 30. Future Enhancements

The project can be extended with:

### Redis Cluster

For high availability.

### Resilience4j Rate Limiter

Protect external APIs from excessive requests.

### Bulkhead

Prevent one failing provider from consuming all application resources.

### Kafka

Publish weather updates asynchronously.

### Scheduled Refresh

Refresh frequently accessed cities before cache expiration.

### Distributed Tracing

Use:

```text
OpenTelemetry
        |
        v
     Jaeger
```

### Prometheus/Grafana

For production monitoring.

### Docker Compose

Run the complete application and Redis together.

### Kubernetes

Deploy the service using:

```text
Deployment
Service
ConfigMap
Secret
HPA
Ingress
```

---

# 31. Interview Discussion Points

This project can be used to discuss several senior-level interview topics.

### Why Redis instead of Caffeine?

Caffeine is local to one JVM.

Redis provides a shared cache:

```text
Application 1
      |
      +---- Redis
      |
Application 2
      |
      +---- Redis
```

This is more suitable when multiple application instances need the same cached data.

---

### Why use WebClient?

`WebClient` is the modern Spring HTTP client and supports both synchronous and reactive usage.

---

### Why retry?

Transient failures shouldn't immediately cause a user-visible failure.

---

### Why circuit breaker?

Repeated retries against a failing provider can make an outage worse.

Circuit Breaker stops calls when the provider is unhealthy.

---

### Why have a second provider?

External services can experience:

* outages
* rate limits
* network problems
* maintenance
* degraded performance

A secondary provider improves availability.

---

### What happens if Redis goes down?

A production implementation should decide explicitly whether Redis is:

```text
Critical dependency
```

or:

```text
Performance optimization
```

For this weather service, Redis should ideally be treated as a **performance optimization**, allowing the application to call the provider directly when the cache is unavailable rather than making the entire API unavailable.

---

# 32. Complete Request Lifecycle

```text
                    HTTP Request
                         |
                         v
              WeatherController
                         |
                         v
                WeatherService
                         |
                         v
                  Redis Cache
                  /         \
                HIT         MISS
                 |            |
                 |            v
                 |      Open-Meteo
                 |            |
                 |       Retry 1
                 |            |
                 |       Retry 2
                 |            |
                 |       Retry 3
                 |            |
                 |       Circuit Breaker
                 |            |
                 |        +---+---+
                 |        |       |
                 |     Success   Failure
                 |        |       |
                 |        |       v
                 |        |   WeatherAPI
                 |        |       |
                 |        |     Retry
                 |        |       |
                 +--------+-------+
                          |
                          v
                   WeatherResponse
                          |
                          v
                        Redis
                          |
                          v
                        Client
```

---

# 33. Key Learning Outcomes

After completing this project, you should be able to explain and demonstrate:

```text
Spring Boot REST API
        +
WebClient
        +
External API integration
        +
Redis caching
        +
Cache-aside pattern
        +
Retry
        +
Circuit Breaker
        +
Fallback
        +
Strategy Pattern
        +
DTO mapping
        +
Exception handling
        +
Timeout handling
        +
Actuator
        +
Swagger
        +
Docker
```

This makes the project a useful hands-on exercise for **Senior Java Developer, Spring Boot Developer, Technical Lead, and Solution Architect interviews**.
