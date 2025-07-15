package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
class PortfolioReturnServiceTest {

    @Mock
    private PortfolioDataService portfolioDataService;

    @Mock
    private ReturnCalculator returnCalculator;

    @Mock
    private PortfolioAnalyzer portfolioAnalyzer;

    @Mock
    private PeriodManager periodManager;

    private PortfolioReturnService portfolioReturnService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        portfolioReturnService = new PortfolioReturnService(
                portfolioDataService,
                returnCalculator,
                portfolioAnalyzer,
                periodManager);
    }

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portfolioReturnService.analyzePortfolio(null));

        assertEquals("Portfolio request cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTickersIsNull() {
        // Given
        PortfolioRequest request = new PortfolioRequest();
        request.setTickers(null);
        request.setStartDate(LocalDate.of(2023, 1, 1));
        request.setEndDate(LocalDate.of(2023, 12, 31));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portfolioReturnService.analyzePortfolio(request));

        assertEquals("Tickers cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTickersIsEmpty() {
        // Given
        PortfolioRequest request = new PortfolioRequest();
        request.setTickers(Arrays.asList());
        request.setStartDate(LocalDate.of(2023, 1, 1));
        request.setEndDate(LocalDate.of(2023, 12, 31));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portfolioReturnService.analyzePortfolio(request));

        assertEquals("Tickers cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldReturnPortfolioReturnDataForValidRequest() {
        // Given
        PortfolioRequest request = new PortfolioRequest(
                Arrays.asList("AAPL", "GOOGL"),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                true);

        // Mock data for both stocks
        Map<String, ChartResponse> mockData = new HashMap<>();
        mockData.put("AAPL", createMockChartResponse());
        mockData.put("GOOGL", createMockChartResponse());

        Map<String, ChartResponse> mockDividendData = new HashMap<>();
        mockDividendData.put("AAPL", createMockDividendChartResponse());
        mockDividendData.put("GOOGL", createMockDividendChartResponse());

        when(portfolioDataService.fetchMultipleStocks(anyList(), anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(mockData));
        when(portfolioDataService.fetchMultipleDividends(anyList(), anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(mockDividendData));
        when(returnCalculator.calculatePriceReturn(anyList())).thenReturn(0.10);
        when(returnCalculator.calculateTotalReturn(anyList(), anyList(), anyList())).thenReturn(0.12);
        when(returnCalculator.calculateCAGR(anyDouble(), anyDouble(), anyInt())).thenReturn(0.11);

        // When
        PortfolioReturnData result = portfolioReturnService.analyzePortfolio(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getStockReturns());
        assertEquals(2, result.getStockReturns().size());
    }

    @Test
    void shouldCalculateStockReturnsForSingleStock() {
        // Given
        PortfolioRequest request = new PortfolioRequest(
                Arrays.asList("AAPL"),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                false);

        // Mock ChartResponse with proper structure
        ChartResponse mockResponse = createMockChartResponse();
        Map<String, ChartResponse> stockData = new HashMap<>();
        stockData.put("AAPL", mockResponse);

        when(portfolioDataService.fetchMultipleStocks(anyList(), anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(stockData));
        when(returnCalculator.calculatePriceReturn(anyList())).thenReturn(0.15);
        when(returnCalculator.calculateTotalReturn(anyList(), anyList(), anyList())).thenReturn(0.15);
        when(returnCalculator.calculateCAGR(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.15);

        // When
        PortfolioReturnData result = portfolioReturnService.analyzePortfolio(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getStockReturns());
        assertEquals(1, result.getStockReturns().size());

        StockReturnData stockReturn = result.getStockReturns().get(0);
        assertEquals("AAPL", stockReturn.getTicker());
        assertEquals(0.15, stockReturn.getPriceReturn(), 0.001);
        assertEquals(0.15, stockReturn.getTotalReturn(), 0.001);
        assertEquals(0.15, stockReturn.getCagr(), 0.001);
    }

    private ChartResponse createMockChartResponse() {
        ChartResponse response = new ChartResponse();
        ChartResponse.Chart chart = new ChartResponse.Chart();
        ChartResponse.Result result = new ChartResponse.Result();
        ChartResponse.Indicators indicators = new ChartResponse.Indicators();
        ChartResponse.Quote quote = new ChartResponse.Quote();

        // Mock price data
        quote.setClose(Arrays.asList(100.0, 110.0, 115.0));
        indicators.setQuote(Arrays.asList(quote));
        result.setIndicators(indicators);
        chart.setResult(Arrays.asList(result));
        response.setChart(chart);

        return response;
    }

    private ChartResponse createMockDividendChartResponse() {
        ChartResponse response = new ChartResponse();
        ChartResponse.Chart chart = new ChartResponse.Chart();
        ChartResponse.Result result = new ChartResponse.Result();
        ChartResponse.Events events = new ChartResponse.Events();

        events.setDividends(new HashMap<>());

        // Mock price data
        result.setEvents(events);
        chart.setResult(Arrays.asList(result));
        response.setChart(chart);

        return response;
    }

    @Test
    void shouldExtractDatesFromChartResponse() {
        // Given
        ChartResponse response = new ChartResponse();
        ChartResponse.Chart chart = new ChartResponse.Chart();
        ChartResponse.Result result = new ChartResponse.Result();
        result.setTimestamp(Arrays.asList(1593576000L, 1596254400L, 1598932800L, 1601524800L));
        chart.setResult(Arrays.asList(result));
        response.setChart(chart);

        // When
        var dates = portfolioReturnService.extractDates(response);

        // Then
        assertEquals(4, dates.size());
        assertEquals(LocalDate.of(2020, 7, 1), dates.get(0));
        assertEquals(LocalDate.of(2020, 8, 1), dates.get(1));
        assertEquals(LocalDate.of(2020, 9, 1), dates.get(2));
        assertEquals(LocalDate.of(2020, 10, 1), dates.get(3));
    }

    @Test
    void shouldReturnEmptyListWhenTimestampsAreMissing() {
        // Given
        ChartResponse response = createMockChartResponse(); // This one has results but not timestamps
        response.getChart().getResult().get(0).setTimestamp(null);

        // When
        var dates = portfolioReturnService.extractDates(response);

        // Then
        assertTrue(dates.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForEmptyChartResponse() {
        // Given
        ChartResponse response = new ChartResponse();

        // When
        var dates = portfolioReturnService.extractDates(response);

        // Then
        assertTrue(dates.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenStockDataHasDifferentStartDates() {
        // Given
        PortfolioRequest request = new PortfolioRequest(
                Arrays.asList("AAPL", "GOOGL"),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                true);

        Map<String, ChartResponse> mockData = new HashMap<>();
        // Timestamps for 2023-01-01 (1672531200) and 2023-02-01 (1675209600) in UTC
        mockData.put("AAPL", createMockChartResponseWithTimestamp(1672531200L, Arrays.asList(100.0, 110.0)));
        mockData.put("GOOGL", createMockChartResponseWithTimestamp(1675209600L, Arrays.asList(200.0, 210.0)));

        when(portfolioDataService.fetchMultipleDividends(anyList(), anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(mockData));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portfolioReturnService.analyzePortfolio(request));

        assertEquals("Stock data has different start dates. Please align them. The latest start date is 2023-02-01.",
                exception.getMessage());
    }

    private ChartResponse createMockChartResponseWithTimestamp(long startTimestamp, java.util.List<Double> prices) {
        ChartResponse response = new ChartResponse();
        ChartResponse.Chart chart = new ChartResponse.Chart();
        ChartResponse.Result result = new ChartResponse.Result();
        ChartResponse.Indicators indicators = new ChartResponse.Indicators();
        ChartResponse.Quote quote = new ChartResponse.Quote();

        quote.setClose(prices);
        indicators.setQuote(Arrays.asList(quote));
        result.setIndicators(indicators);
        result.setTimestamp(Arrays.asList(startTimestamp, startTimestamp + 86400L * 30)); // approx 1 month later
        chart.setResult(Arrays.asList(result));
        response.setChart(chart);
        return response;
    }
}
