package portfolio.api;

import java.net.URI;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClient;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import portfolio.config.CacheConfig;

@Slf4j
@Service
public class StockFetcher {
    private final RestClient restClient;
    private final String scheme; // "http" 또는 "https"
    private final String host;
    private final int port;

    public StockFetcher(RestClient restClient, @Value("${stock.api.url}") String url) {
        this.restClient = restClient;
        URI uri = URI.create(url);
        this.scheme = uri.getScheme();
        this.host = uri.getHost();
        this.port = uri.getPort();
        log.info("StockFetcher initialized with url: {}", url);
    }

    @Cacheable(value = CacheConfig.STOCK_DATA_CACHE, key = "#ticker + '-' + #period1 + '-' + #period2 + '-history'")
    public ChartResponse fetchHistory(String ticker, long period1, long period2) {
        log.info("Fetching history for {} from {} to {}", ticker, period1, period2);
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(scheme)
                        .host(host)
                        .port(port)
                        .path("/v8/finance/chart/{ticker}")
                        .queryParam("period1", period1)
                        .queryParam("period2", period2)
                        .queryParam("interval", "1d")
                        .build(ticker))
                .retrieve()
                .body(ChartResponse.class);
    }

    /**
     * 2011년부터 현재까지 특정 ticker의 분배금(배당금) 내역을 조회한다.
     * 
     * @param ticker ETF 심볼 (예: SCHD)
     * @return Yahoo Finance API의 JSON 응답 문자열
     */
    @Cacheable(value = CacheConfig.STOCK_DATA_CACHE, key = "#ticker + '-' + #period1 + '-' + #period2 + '-dividends'")
    public ChartResponse fetchDividends(String ticker, long period1, long period2) {
        log.info("Fetching dividends for {} from {} to {}", ticker, period1, period2);
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(scheme)
                        .host(host)
                        .port(port)
                        .path("/v8/finance/chart/{ticker}")
                        .queryParam("period1", period1)
                        .queryParam("period2", period2)
                        .queryParam("interval", "1mo")
                        .queryParam("events", "div")
                        .build(ticker))
                .retrieve()
                .body(ChartResponse.class);
    }
}
