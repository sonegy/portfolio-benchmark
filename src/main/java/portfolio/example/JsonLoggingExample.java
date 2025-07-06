package portfolio.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portfolio.util.JsonLoggingUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * JsonLoggingUtils 사용 예시를 보여주는 클래스
 */
public class JsonLoggingExample {
    
    private static final Logger log = LoggerFactory.getLogger(JsonLoggingExample.class);
    
    public static void main(String[] args) {
        demonstrateJsonLogging();
    }
    
    public static void demonstrateJsonLogging() {
        log.info("=== JsonLoggingUtils 사용 예시 ===");
        
        // 1. 간단한 객체 로깅
        User user = new User("홍길동", 30, "hong@example.com", LocalDateTime.now());
        log.info("사용자 정보: {}", JsonLoggingUtils.asJsonLoggable(user));
        
        // 2. Pretty print로 로깅 (디버깅 시 유용)
        log.debug("사용자 정보 (Pretty Print):\n{}", JsonLoggingUtils.asJsonLoggablePretty(user));
        
        // 3. 복잡한 객체 로깅
        ApiResponse response = new ApiResponse(
            200, 
            "success", 
            Map.of(
                "users", List.of(user, new User("김철수", 25, "kim@example.com", LocalDateTime.now())),
                "totalCount", 2,
                "hasMore", false
            ),
            LocalDateTime.now()
        );
        log.info("API 응답: {}", JsonLoggingUtils.asJsonLoggable(response));
        
        // 4. 직접 JSON 문자열 변환
        String jsonString = JsonLoggingUtils.toJson(user);
        log.info("직접 변환된 JSON: {}", jsonString);
        
        // 5. 성능 최적화 데모 - TRACE 레벨은 비활성화되어 있어 JSON 변환이 일어나지 않음
        log.trace("이 로그는 출력되지 않으므로 JSON 변환도 일어나지 않습니다: {}", 
                JsonLoggingUtils.asJsonLoggable(response));
        
        // 6. null 객체 처리
        log.info("null 객체: {}", JsonLoggingUtils.asJsonLoggable(null));
        
        log.info("=== 데모 완료 ===");
    }
    
    // 예시용 User 클래스
    public static class User {
        private String name;
        private int age;
        private String email;
        private LocalDateTime createdAt;
        
        public User(String name, int age, String email, LocalDateTime createdAt) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.createdAt = createdAt;
        }
        
        // Getters
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getEmail() { return email; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
    
    // 예시용 ApiResponse 클래스
    public static class ApiResponse {
        private int status;
        private String message;
        private Object data;
        private LocalDateTime timestamp;
        
        public ApiResponse(int status, String message, Object data, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.data = data;
            this.timestamp = timestamp;
        }
        
        // Getters
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
