package portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import portfolio.api.ChartResponse;
import portfolio.api.StockFetcher;

public class PortfolioDataServiceTest {

    @Test
    public void shouldCreatePortfolioDataServiceWithStockFetcher() {
        // Given
        StockFetcher stockFetcher = mock(StockFetcher.class);
        
        // When
        PortfolioDataService service = new PortfolioDataService(stockFetcher);
        
        // Then
        assertNotNull(service);
    }

    @Test
    public void shouldFetchMultipleDividends() throws Exception {
        // Given
        StockFetcher stockFetcher = mock(StockFetcher.class);
        ChartResponse mockResponse = new ChartResponse();
        when(stockFetcher.fetchDividends("AAPL", 1609459200L, 1640995200L)).thenReturn(mockResponse);
        
        PortfolioDataService service = new PortfolioDataService(stockFetcher);
        List<String> tickers = List.of("AAPL");
        long period1 = 1609459200L;
        long period2 = 1640995200L;
        
        // When
        CompletableFuture<Map<String, ChartResponse>> result = service.fetchMultipleDividends(tickers, period1, period2);
        Map<String, ChartResponse> data = result.get();
        
        // Then
        assertEquals(1, data.size());
        assertEquals(mockResponse, data.get("AAPL"));
    }
}
