package portfolio.api;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import lombok.extern.slf4j.Slf4j;
import portfolio.service.PortfolioDataService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).clear();
        }
        WireMock.reset();
    }

    @Test
    void fetchHistory_shouldReturnApiResponse() {
        String ticker = "AAPL";
        long period1 = 1720224000L;
        long period2 = 1720483200L;
        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-aapl-response.json")));

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

        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-msft-response.json"))); // Use existing mock data

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
    void fetchHistory_shouldBeCached_TwoGivenValues() {
        // given
        List<String> tickers = new ArrayList<>();
        tickers.add("MSFT");
        tickers.add("AAPL");

        long period1 = 1720224000L;
        long period2 = 1720483200L;

        for (String ticker : tickers) {
            stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile(switch (ticker) {
                                case "MSFT" -> "fetch-history-msft-response.json";
                                case "AAPL" -> "fetch-history-aapl-response.json";
                                default -> "fetch-history-aapl-response.json";
                            }))); // Use existing mock data
        }

        for (int i = 0; i < 10; i++) {
            CompletableFuture<Map<String, ChartResponse>> stocks = new PortfolioDataService(fetcher)
                    .fetchMultipleStocks(tickers, period1, period2);
            Map<String, ChartResponse> map = stocks.join();
            for (Map.Entry<String, ChartResponse> entry : map.entrySet()) {
                String key = entry.getKey();
                ChartResponse value = entry.getValue();
                log.debug("key and value {} {}", key, value);
                assertEquals(key,
                        value.getChart().getResult().stream().findFirst().orElseThrow().getMeta().getSymbol());
            }
        }
    }
}
