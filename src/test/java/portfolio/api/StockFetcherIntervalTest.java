package portfolio.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0, stubs = "classpath:/mappings")
class StockFetcherIntervalTest {

    @Autowired
    StockFetcher fetcher;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setup() {
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).clear();
        }
        WireMock.reset();
    }

    @Test
    void shouldUse1dIntervalForShortPeriod() {
        // given: 7일 기간
        String ticker = "AAPL";
        long period1 = 1720224000L;
        long period2 = 1720828800L; // 7일 후

        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("1d"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-aapl-response.json")));

        // when
        ChartResponse response = fetcher.fetchHistory(ticker, period1, period2);

        // then
        assertNotNull(response);
        verify(1, getRequestedFor(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("1d")));
    }

    @Test
    void shouldUse5dIntervalForMediumPeriod() {
        // given: 90일 기간
        String ticker = "MSFT";
        long period1 = 1720224000L;
        long period2 = 1728000000L; // 90일 후

        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("5d"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-msft-response.json")));

        // when
        ChartResponse response = fetcher.fetchHistory(ticker, period1, period2);

        // then
        assertNotNull(response);
        verify(1, getRequestedFor(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("5d")));
    }

    @Test
    void shouldUse1moIntervalForLongPeriod() {
        // given: 365일 기간
        String ticker = "GOOGL";
        long period1 = 1720224000L;
        long period2 = 1751760000L; // 365일 후

        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("1mo"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-aapl-response.json"))); // 기존 파일 재사용

        // when
        ChartResponse response = fetcher.fetchHistory(ticker, period1, period2);

        // then
        assertNotNull(response);
        verify(1, getRequestedFor(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("1mo")));
    }

    @Test
    void shouldUse1moIntervalForVeryLongPeriod() {
        // given: 5년 기간 (1825일)
        String ticker = "TSLA";
        long period1 = 1720224000L;
        long period2 = 1877904000L; // 약 5년 후

        stubFor(get(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("1mo"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fetch-history-aapl-response.json"))); // 기존 파일 재사용

        // when
        ChartResponse response = fetcher.fetchHistory(ticker, period1, period2);

        // then
        assertNotNull(response);
        verify(1, getRequestedFor(urlPathEqualTo("/v8/finance/chart/" + ticker))
                .withQueryParam("interval", equalTo("1mo")));
    }
}
