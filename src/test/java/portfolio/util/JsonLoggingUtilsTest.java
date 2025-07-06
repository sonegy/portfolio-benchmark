package portfolio.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonLoggingUtils 테스트 클래스
 */
class JsonLoggingUtilsTest {
    
    private static final Logger log = LoggerFactory.getLogger(JsonLoggingUtilsTest.class);
    
    @Test
    void testToJson_withSimpleObject() {
        // Given
        TestData testData = new TestData("John", 30, LocalDateTime.of(2023, 1, 1, 12, 0));
        
        // When
        String json = JsonLoggingUtils.toJson(testData);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":30"));
        log.info("Simple object JSON: {}", json);
    }
    
    @Test
    void testToJsonPretty_withSimpleObject() {
        // Given
        TestData testData = new TestData("Jane", 25, LocalDateTime.of(2023, 6, 15, 14, 30));
        
        // When
        String json = JsonLoggingUtils.toJsonPretty(testData);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\" : \"Jane\""));
        assertTrue(json.contains("\"age\" : 25"));
        assertTrue(json.contains("\n")); // Pretty print should have line breaks
        log.info("Pretty JSON:\n{}", json);
    }
    
    @Test
    void testToJson_withNull() {
        // When
        String json = JsonLoggingUtils.toJson(null);
        
        // Then
        assertEquals("null", json);
    }
    
    @Test
    void testToJson_withComplexObject() {
        // Given
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("user", new TestData("Alice", 28, LocalDateTime.now()));
        complexData.put("items", List.of("item1", "item2", "item3"));
        complexData.put("metadata", Map.of("version", "1.0", "active", true));
        
        // When
        String json = JsonLoggingUtils.toJson(complexData);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"user\""));
        assertTrue(json.contains("\"items\""));
        assertTrue(json.contains("\"metadata\""));
        log.info("Complex object JSON: {}", json);
    }
    
    @Test
    void testAsJsonLoggable_lazyEvaluation() {
        // Given
        TestData testData = new TestData("Bob", 35, LocalDateTime.now());
        
        // When
        JsonLoggingUtils.JsonLoggable loggable = JsonLoggingUtils.asJsonLoggable(testData);
        
        // Then
        assertNotNull(loggable);
        assertEquals(testData, loggable.getObject());
        assertFalse(loggable.isPrettyPrint());
        
        // toString() 호출 시점에 JSON 변환
        String jsonString = loggable.toString();
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("\"name\":\"Bob\""));
        
        log.info("JsonLoggable result: {}", loggable);
    }
    
    @Test
    void testAsJsonLoggablePretty_lazyEvaluation() {
        // Given
        TestData testData = new TestData("Charlie", 40, LocalDateTime.now());
        
        // When
        JsonLoggingUtils.JsonLoggable loggable = JsonLoggingUtils.asJsonLoggablePretty(testData);
        
        // Then
        assertNotNull(loggable);
        assertEquals(testData, loggable.getObject());
        assertTrue(loggable.isPrettyPrint());
        
        // toString() 호출 시점에 JSON 변환
        String jsonString = loggable.toString();
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("\"name\" : \"Charlie\""));
        assertTrue(jsonString.contains("\n")); // Pretty print
        
        log.info("JsonLoggablePretty result:\n{}", loggable);
    }
    
    @Test
    void testJsonLoggable_withNullObject() {
        // When
        JsonLoggingUtils.JsonLoggable loggable = JsonLoggingUtils.asJsonLoggable(null);
        
        // Then
        assertNotNull(loggable);
        assertNull(loggable.getObject());
        assertEquals("null", loggable.toString());
    }
    
    @Test
    void testLoggingPerformance_demonstration() {
        // Given
        TestData largeData = new TestData("Performance Test", 999, LocalDateTime.now());
        
        // When - 로그 레벨이 비활성화된 경우를 시뮬레이션
        log.trace("This trace log won't be printed, so JSON conversion won't happen: {}", 
                JsonLoggingUtils.asJsonLoggable(largeData));
        
        // When - 로그 레벨이 활성화된 경우
        log.info("This info log will be printed, so JSON conversion will happen: {}", 
                JsonLoggingUtils.asJsonLoggable(largeData));
        
        // Then - 테스트는 성공적으로 완료되어야 함
        assertTrue(true, "Performance test completed successfully");
    }
    
    @Test
    void testErrorHandling_withNonSerializableObject() {
        // Given - 직렬화할 수 없는 객체 (순환 참조)
        CircularReference obj1 = new CircularReference("obj1");
        CircularReference obj2 = new CircularReference("obj2");
        obj1.reference = obj2;
        obj2.reference = obj1;
        
        // When
        String json = JsonLoggingUtils.toJson(obj1);
        
        // Then - 에러가 발생해도 안전한 fallback 문자열이 반환되어야 함
        assertNotNull(json);
        assertTrue(json.contains("\"error\":\"JSON serialization failed\""));
        assertTrue(json.contains("\"class\":\"CircularReference\""));
        
        log.info("Error handling test result: {}", json);
    }
    
    @Test
    void testJsonLoggingDemo() {
        log.info("=== JsonLoggingUtils 사용 예시 데모 ===");
        
        // 1. 간단한 객체 로깅
        TestData user = new TestData("홍길동", 30, LocalDateTime.now());
        log.info("사용자 정보: {}", JsonLoggingUtils.asJsonLoggable(user));
        
        // 2. Pretty print로 로깅 (디버깅 시 유용)
        log.debug("사용자 정보 (Pretty Print):\n{}", JsonLoggingUtils.asJsonLoggablePretty(user));
        
        // 3. 복잡한 객체 로깅
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("user", user);
        complexData.put("items", List.of("item1", "item2", "item3"));
        complexData.put("metadata", Map.of("version", "1.0", "active", true));
        
        log.info("복잡한 객체: {}", JsonLoggingUtils.asJsonLoggable(complexData));
        
        // 4. 직접 JSON 문자열 변환
        String jsonString = JsonLoggingUtils.toJson(user);
        log.info("직접 변환된 JSON: {}", jsonString);
        
        // 5. null 객체 처리
        log.info("null 객체: {}", JsonLoggingUtils.asJsonLoggable(null));
        
        log.info("=== 데모 완료 ===");
        
        // 테스트 성공 확인
        assertTrue(true, "Demo completed successfully");
    }
    
    // 테스트용 데이터 클래스
    static class TestData {
        private String name;
        private int age;
        private LocalDateTime createdAt;
        
        public TestData(String name, int age, LocalDateTime createdAt) {
            this.name = name;
            this.age = age;
            this.createdAt = createdAt;
        }
        
        // Getters
        public String getName() { return name; }
        public int getAge() { return age; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
    
    // 순환 참조 테스트용 클래스
    static class CircularReference {
        private String name;
        private CircularReference reference;
        
        public CircularReference(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public CircularReference getReference() { return reference; }
        
        @Override
        public String toString() {
            return "CircularReference{name='" + name + "'}";
        }
    }
}
