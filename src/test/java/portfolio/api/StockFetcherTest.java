package portfolio.api;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import lombok.extern.slf4j.Slf4j;
import portfolio.config.CacheConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import org.springframework.beans.factory.annotation.Value;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import portfolio.util.DateUtils;
import portfolio.util.JsonLoggingUtils;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0, stubs = "classpath:/mappings")
class StockFetcherTest {
    
    @Autowired
    StockFetcher fetcher;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        cacheManager.getCache(CacheConfig.STOCK_DATA_CACHE).clear();
        WireMock.reset();
    }

    @Test
    void fetchHistory_shouldReturnApiResponse() {
        String ticker = "AAPL";
        long period1 = 1720224000L;
        long period2 = 1720483200L;
        ChartResponse response = fetcher.fetchHistory(ticker, period1, period2);
        assertNotNull(response);
        assertNotNull(response.getChart());
        assertNotNull(response.getChart().getResult());
        assertEquals("AAPL", response.getChart().getResult().get(0).getMeta().getSymbol());
    }

    @Test
    void fetchHistory_shouldBeCached() {
        // given
        String ticker = "MSFT";
        long period1 = 1720224000L;
        long period2 = 1720483200L;
        String url = String.format("/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d", ticker, period1, period2);
        
        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-aapl-response.json"))); // Use existing mock data

        // when
        ChartResponse response1 = fetcher.fetchHistory(ticker, period1, period2);
        ChartResponse response2 = fetcher.fetchHistory(ticker, period1, period2);

        // then
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1, response2); // Check if the same object is returned
        
        verify(1, getRequestedFor(urlPathEqualTo("/v8/finance/chart/" + ticker)));
    }

    @Test
    @Disabled
    void fetchHistory_Real() throws JsonProcessingException {
        // var realFetcher = new StockFetcher(restClient); // This needs a RestClient bean
        // var period1DateTime = LocalDateTime.now().minusDays(4);
        // var period2DateTime = LocalDateTime.now();
        
        // // period1를 unix time long으로 변환
        // long period1 = DateUtils.toUnixTimestamp(period1DateTime.toLocalDate());
        // long period2 = DateUtils.toUnixTimestamp(period2DateTime.toLocalDate());

        // ChartResponse response = realFetcher.fetchHistory("AAPL", period1, period2);
        // log.debug("ChartResponse:{}", JsonLoggingUtils.asJsonLoggablePretty(response));
        // assertNotNull(response);
    }


    @Test
    @Disabled
    void fetchDividends_Real() throws JsonProcessingException {
        // var realFetcher = new StockFetcher(restClient); // This needs a RestClient bean
        // var period1DateTime = LocalDateTime.now().minusDays(60);
        // var period2DateTime = LocalDateTime.now();
        
        // // period1를 unix time long으로 변환
        // long period1 = DateUtils.toUnixTimestamp(period1DateTime.toLocalDate());
        // long period2 = DateUtils.toUnixTimestamp(period2DateTime.toLocalDate());

        // ChartResponse response = realFetcher.fetchDividends("SCHD", period1, period2);
        // log.debug("ChartResponse:{}", JsonLoggingUtils.asJsonLoggablePretty(response));
        // assertNotNull(response);
    }

}
