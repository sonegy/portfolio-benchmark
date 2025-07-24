package portfolio.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import portfolio.api.ChartResponse;
import portfolio.api.StockFetcher;

@Service
public class PortfolioDataService {
    
    private final StockFetcher stockFetcher;
    
    public PortfolioDataService(StockFetcher stockFetcher) {
        this.stockFetcher = stockFetcher;
    }
    
    public CompletableFuture<Map<String, ChartResponse>> fetchMultipleDividends(List<String> tickers, long period1, long period2) {
        return fetchMultipleData(tickers, period1, period2, stockFetcher::fetchDividends);
    }
    
    private CompletableFuture<Map<String, ChartResponse>> fetchMultipleData(
            List<String> tickers, 
            long period1, 
            long period2, 
            TriFunction<String, Long, Long, ChartResponse> fetcher) {
        
        List<CompletableFuture<Map.Entry<String, ChartResponse>>> futures = tickers.stream()
            .map(ticker -> CompletableFuture.supplyAsync(() -> 
                Map.entry(ticker, fetcher.apply(ticker, period1, period2))))
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}
