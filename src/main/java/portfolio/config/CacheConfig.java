package portfolio.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String StockFetcher_fetchHistory = "StockFetcher_fetchHistory";
    public static final String StockFetcher_fetchDividends = "StockFetcher_fetchDividends";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(StockFetcher_fetchHistory,
                StockFetcher_fetchDividends);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100)); // 캐시 최대 크기 100개로 제한
        return cacheManager;
    }
}
