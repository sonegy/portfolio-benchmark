package portfolio.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JSON 로깅을 위한 유틸리티 클래스
 * logback의 특성을 활용하여 toString() 호출 시점에만 JSON 변환을 수행합니다.
 */
public class JsonLoggingUtils {
    
    private static volatile ObjectMapper objectMapper;
    private static volatile ObjectMapper prettyObjectMapper;
    private static final ReentrantLock lock = new ReentrantLock();
    
    /**
     * 기본 ObjectMapper를 lazy initialization으로 생성
     */
    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            lock.lock();
            try {
                if (objectMapper == null) {
                    objectMapper = createObjectMapper(false);
                }
            } finally {
                lock.unlock();
            }
        }
        return objectMapper;
    }
    
    /**
     * Pretty print용 ObjectMapper를 lazy initialization으로 생성
     */
    private static ObjectMapper getPrettyObjectMapper() {
        if (prettyObjectMapper == null) {
            lock.lock();
            try {
                if (prettyObjectMapper == null) {
                    prettyObjectMapper = createObjectMapper(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return prettyObjectMapper;
    }
    
    /**
     * ObjectMapper 생성 및 설정
     */
    private static ObjectMapper createObjectMapper(boolean prettyPrint) {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 시간 API 지원
        mapper.registerModule(new JavaTimeModule());
        
        // 타임스탬프를 문자열로 출력 (ISO-8601 형식)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 알 수 없는 속성 무시 (역직렬화 시)
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Pretty print 설정
        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        
        return mapper;
    }
    
    /**
     * 객체를 JSON 문자열로 변환
     * @param obj 변환할 객체
     * @return JSON 문자열, 변환 실패 시 안전한 fallback 문자열
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return createFallbackString(obj, e);
        }
    }
    
    /**
     * 객체를 예쁘게 포맷된 JSON 문자열로 변환
     * @param obj 변환할 객체
     * @return 포맷된 JSON 문자열, 변환 실패 시 안전한 fallback 문자열
     */
    public static String toJsonPretty(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            return getPrettyObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return createFallbackString(obj, e);
        }
    }
    
    /**
     * 로깅용 JSON 래퍼 객체 생성
     * toString() 호출 시점에 JSON 변환을 수행합니다.
     * @param obj 래핑할 객체
     * @return JsonLoggable 래퍼 객체
     */
    public static JsonLoggable asJsonLoggable(Object obj) {
        return new JsonLoggable(obj, false);
    }
    
    /**
     * 로깅용 Pretty Print JSON 래퍼 객체 생성
     * toString() 호출 시점에 포맷된 JSON 변환을 수행합니다.
     * @param obj 래핑할 객체
     * @return JsonLoggable 래퍼 객체
     */
    public static JsonLoggable asJsonLoggablePretty(Object obj) {
        return new JsonLoggable(obj, true);
    }
    
    /**
     * JSON 변환 실패 시 안전한 fallback 문자열 생성
     */
    private static String createFallbackString(Object obj, Exception e) {
        return String.format("{\"error\":\"JSON serialization failed\",\"class\":\"%s\",\"toString\":\"%s\",\"exception\":\"%s\"}", 
                obj.getClass().getSimpleName(), 
                escapeJsonString(obj.toString()), 
                e.getMessage());
    }
    
    /**
     * JSON 문자열 내의 특수 문자 이스케이프
     */
    private static String escapeJsonString(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 로깅용 JSON 래퍼 클래스
     * toString() 메서드가 호출될 때만 JSON 변환을 수행하여 성능을 최적화합니다.
     */
    public static class JsonLoggable {
        private final Object object;
        private final boolean prettyPrint;
        
        private JsonLoggable(Object object, boolean prettyPrint) {
            this.object = object;
            this.prettyPrint = prettyPrint;
        }
        
        /**
         * toString() 호출 시점에 JSON 변환 수행
         * logback에서 로그 레벨이 비활성화된 경우 이 메서드가 호출되지 않아 성능상 이점을 제공합니다.
         */
        @Override
        public String toString() {
            if (prettyPrint) {
                return toJsonPretty(object);
            } else {
                return toJson(object);
            }
        }
        
        /**
         * 래핑된 원본 객체 반환
         */
        public Object getObject() {
            return object;
        }
        
        /**
         * Pretty print 여부 반환
         */
        public boolean isPrettyPrint() {
            return prettyPrint;
        }
    }
}
