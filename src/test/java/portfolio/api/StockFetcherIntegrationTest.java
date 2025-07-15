package portfolio.api;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@Disabled
class StockFetcherIntegrationTest {

    @Autowired
    StockFetcher fetcher;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup(){
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).clear();
        }
    }

    @DisplayName("실제 API 통합 테스트: 수동 실행용")
    @Test
    void fetchHistory_realApi_shouldPrintFirstAndLastClose() {
        // given
        String ticker = "SCHD";
        long period1 = java.time.LocalDate.parse("2015-01-01").atStartOfDay(java.time.ZoneId.of("America/New_York")).toEpochSecond();
        long period2 = java.time.LocalDate.parse("2016-01-31").atStartOfDay(java.time.ZoneId.of("America/New_York")).toEpochSecond();

        // when
        ChartResponse response = fetcher.fetchHistory(ticker, period1, period2);

        // then
        assertNotNull(response);
        assertNotNull(response.getChart());
        assertNotNull(response.getChart().getResult());
        java.util.List<portfolio.api.ChartResponse.Result> results = response.getChart().getResult();
        assertFalse(results.isEmpty());
        portfolio.api.ChartResponse.Result result = results.get(0);
        java.util.List<Double> close = result.getIndicators().getQuote().get(0).getClose();
        assertFalse(close.isEmpty());
        System.out.println("First close: " + close.get(0));
        System.out.println("Last close: " + close.get(close.size() - 1));
    }
}
