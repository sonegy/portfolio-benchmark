# JsonLoggingUtils 사용 가이드

## 개요

`JsonLoggingUtils`는 logback의 특성을 활용하여 객체를 JSON으로 변환하여 로깅하는 유틸리티입니다. toString() 메서드가 호출되는 시점에만 JSON 변환을 수행하여 성능을 최적화합니다.

## 주요 특징

- **Lazy Evaluation**: 로그 레벨이 비활성화된 경우 JSON 변환이 수행되지 않음
- **안전한 예외 처리**: JSON 변환 실패 시 안전한 fallback 문자열 제공
- **Pretty Print 지원**: 디버깅을 위한 포맷된 JSON 출력
- **Java 8 시간 API 지원**: LocalDateTime 등 자동 변환

## 사용법

### 1. 기본 사용법

```java
import portfolio.util.JsonLoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleService {
    private static final Logger log = LoggerFactory.getLogger(ExampleService.class);
    
    public void processUser(User user) {
        // 객체를 JSON으로 로깅
        log.info("Processing user: {}", JsonLoggingUtils.asJsonLoggable(user));
        
        // 복잡한 객체도 가능
        Map<String, Object> context = Map.of(
            "user", user,
            "timestamp", LocalDateTime.now(),
            "action", "process"
        );
        log.debug("Context: {}", JsonLoggingUtils.asJsonLoggable(context));
    }
}
```

### 2. Pretty Print 사용법

```java
// 디버깅 시 가독성을 위한 포맷된 JSON
log.debug("User details:\n{}", JsonLoggingUtils.asJsonLoggablePretty(user));
```

### 3. 직접 JSON 문자열 변환

```java
// 로깅 외의 용도로 JSON 문자열이 필요한 경우
String userJson = JsonLoggingUtils.toJson(user);
String prettyJson = JsonLoggingUtils.toJsonPretty(user);
```

## 성능 최적화

### Lazy Evaluation의 이점

```java
// TRACE 레벨이 비활성화된 경우, JSON 변환이 수행되지 않음
log.trace("Heavy object: {}", JsonLoggingUtils.asJsonLoggable(heavyObject));

// INFO 레벨이 활성화된 경우에만 JSON 변환 수행
log.info("User data: {}", JsonLoggingUtils.asJsonLoggable(user));
```

### 기존 방식과의 비교

```java
// ❌ 비효율적 - 로그 레벨과 관계없이 항상 JSON 변환 수행
log.debug("User: {}", objectMapper.writeValueAsString(user));

// ✅ 효율적 - 로그 레벨이 활성화된 경우에만 JSON 변환 수행
log.debug("User: {}", JsonLoggingUtils.asJsonLoggable(user));
```

## 예외 처리

JSON 변환이 실패하는 경우 (예: 순환 참조), 안전한 fallback 문자열이 제공됩니다:

```java
CircularReference obj1 = new CircularReference("obj1");
CircularReference obj2 = new CircularReference("obj2");
obj1.reference = obj2;
obj2.reference = obj1;

log.info("Circular object: {}", JsonLoggingUtils.asJsonLoggable(obj1));
// 출력: {"error":"JSON serialization failed","class":"CircularReference","toString":"CircularReference{name='obj1'}","exception":"..."}
```

## 설정

### ObjectMapper 설정

JsonLoggingUtils는 다음과 같이 설정된 ObjectMapper를 사용합니다:

- Java 8 시간 API 지원 (JavaTimeModule)
- ISO-8601 형식의 날짜/시간 출력
- 알 수 없는 속성 무시
- Pretty print 옵션 지원

### 로그백 설정 예시

```xml
<!-- logback-spring.xml -->
<logger name="com.yourpackage" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</logger>
```

## 실제 사용 예시

### API 응답 로깅

```java
@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        
        log.info("Retrieved user: {}", JsonLoggingUtils.asJsonLoggable(user));
        
        return ResponseEntity.ok(user);
    }
}
```

### 에러 로깅

```java
@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    public void processPayment(PaymentRequest request) {
        try {
            // 결제 처리 로직
        } catch (PaymentException e) {
            log.error("Payment failed for request: {}", 
                JsonLoggingUtils.asJsonLoggable(request), e);
            throw e;
        }
    }
}
```

### 성능 모니터링

```java
@Component
public class PerformanceMonitor {
    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    public void logSlowQuery(String query, long executionTime, Map<String, Object> params) {
        if (executionTime > 1000) {
            Map<String, Object> slowQueryInfo = Map.of(
                "query", query,
                "executionTimeMs", executionTime,
                "parameters", params,
                "timestamp", LocalDateTime.now()
            );
            
            log.warn("Slow query detected: {}", 
                JsonLoggingUtils.asJsonLoggable(slowQueryInfo));
        }
    }
}
```

## 주의사항

1. **순환 참조**: 객체 간 순환 참조가 있는 경우 JSON 변환이 실패할 수 있습니다. 이 경우 안전한 fallback이 제공됩니다.

2. **대용량 객체**: 매우 큰 객체의 경우 JSON 변환에 시간이 걸릴 수 있으므로 적절한 로그 레벨을 사용하세요.

3. **민감한 정보**: 비밀번호나 개인정보가 포함된 객체는 로깅하지 않도록 주의하세요.

## 테스트

JsonLoggingUtils의 동작을 확인하려면 `JsonLoggingUtilsTest` 클래스를 참조하세요:

```bash
./gradlew test --tests JsonLoggingUtilsTest
