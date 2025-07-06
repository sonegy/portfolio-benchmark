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
    public void shouldCreatePortfolioDataService() {
        // Given & When
        PortfolioDataService service = new PortfolioDataService();
        
        // Then
        assertNotNull(service);
    }

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
    public void shouldFetchMultipleStocks() throws Exception {
        // Given
        StockFetcher stockFetcher = mock(StockFetcher.class);
        ChartResponse mockResponse = new ChartResponse();
        when(stockFetcher.fetchHistory("AAPL", 1609459200L, 1640995200L)).thenReturn(mockResponse);
        
        PortfolioDataService service = new PortfolioDataService(stockFetcher);
        List<String> tickers = List.of("AAPL");
        long period1 = 1609459200L; // 2021-01-01
        long period2 = 1640995200L; // 2022-01-01
        
        // When
        CompletableFuture<Map<String, ChartResponse>> result = service.fetchMultipleStocks(tickers, period1, period2);
        Map<String, ChartResponse> data = result.get();
        
        // Then
        assertEquals(1, data.size());
        assertEquals(mockResponse, data.get("AAPL"));
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

    @Test
    public void shouldFetchMultipleStocksInParallel() throws Exception {
        // Given
        StockFetcher stockFetcher = mock(StockFetcher.class);
        ChartResponse mockResponse1 = new ChartResponse();
        ChartResponse mockResponse2 = new ChartResponse();
        when(stockFetcher.fetchHistory("AAPL", 1609459200L, 1640995200L)).thenReturn(mockResponse1);
        when(stockFetcher.fetchHistory("GOOGL", 1609459200L, 1640995200L)).thenReturn(mockResponse2);
        
        PortfolioDataService service = new PortfolioDataService(stockFetcher);
        List<String> tickers = List.of("AAPL", "GOOGL");
        long period1 = 1609459200L;
        long period2 = 1640995200L;
        
        // When
        CompletableFuture<Map<String, ChartResponse>> result = service.fetchMultipleStocks(tickers, period1, period2);
        Map<String, ChartResponse> data = result.get();
        
        // Then
        assertEquals(2, data.size());
        assertEquals(mockResponse1, data.get("AAPL"));
        assertEquals(mockResponse2, data.get("GOOGL"));
    }
}
