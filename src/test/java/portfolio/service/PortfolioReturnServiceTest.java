package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.CAGR;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.ReturnRate;
import portfolio.model.StockReturnData;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
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
                Arrays.asList(0.5, 0.5),
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
        when(returnCalculator.calculatePriceReturn(anyList())).thenReturn(new ReturnRate(100.0, 110.0));
        when(returnCalculator.calculateTotalReturn(anyList(), anyList(), anyList())).thenReturn(new ReturnRate(100.0, 112.0));
        when(returnCalculator.calculateCAGR(anyDouble(), anyDouble(), anyDouble())).thenReturn(new CAGR(100.0, 112.0, 1));

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
                Arrays.asList(1.0),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                false);

        // Mock ChartResponse with proper structure
        ChartResponse mockResponse = createMockChartResponse();
        Map<String, ChartResponse> stockData = new HashMap<>();
        stockData.put("AAPL", mockResponse);

        when(portfolioDataService.fetchMultipleStocks(anyList(), anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(stockData));
        when(returnCalculator.calculatePriceReturn(anyList())).thenReturn(new ReturnRate(100, 115));
        when(returnCalculator.calculateTotalReturn(anyList(), anyList(), anyList())).thenReturn(new ReturnRate(100, 115));
        when(returnCalculator.calculateCAGR(anyDouble(), anyDouble(), anyDouble())).thenReturn(new CAGR(100, 115, 1));

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
        // 타임스탬프 추가 (예: 2023-01-01, 2023-02-01, 2023-03-01)
        result.setTimestamp(Arrays.asList(1672531200L, 1675209600L, 1677628800L));
        chart.setResult(Arrays.asList(result));
        response.setChart(chart);

        return response;
    }

    private ChartResponse createMockDividendChartResponse() {
        ChartResponse response = new ChartResponse();
        ChartResponse.Chart chart = new ChartResponse.Chart();
        ChartResponse.Result result = new ChartResponse.Result();
        ChartResponse.Events events = new ChartResponse.Events();

        

        HashMap<String, Dividend> dividends = new HashMap<>();
        Dividend dividend = new Dividend();
        dividend.setAmount(1.0);
        dividend.setDate(1672531200L);
        dividends.put("1672531200",dividend);
        events.setDividends(dividends);

        // Mock price data
        result.setEvents(events);

        ChartResponse.Indicators indicators = new ChartResponse.Indicators();
        ChartResponse.Quote quote = new ChartResponse.Quote();
        quote.setClose(Arrays.asList(100.0, 110.0, 115.0));
        indicators.setQuote(Arrays.asList(quote));
        result.setIndicators(indicators);
        // 타임스탬프 추가 (예: 2023-01-01, 2023-02-01, 2023-03-01)
        result.setTimestamp(Arrays.asList(1672531200L, 1675209600L, 1677628800L));
        chart.setResult(Arrays.asList(result));
        response.setChart(chart);

        return response;
    }

    @Test
    void shouldCalculatePortfolioStockReturnWithTwoStocksAndEqualWeights() {
        // Given: 두 개의 StockReturnData와 동일한 길이의 가격 시계열, 동일한 타임스탬프
        List<Long> timestamps = Arrays.asList(1L, 2L, 3L);
        StockReturnData stock1 = StockReturnData.builder()
                .ticker("AAA")
                .prices(Arrays.asList(100.0, 110.0, 120.0))
                .timestamps(timestamps)
                .initialAmount(1000.0)
                .dividends(new ArrayList<>())
                .build();
        StockReturnData stock2 = StockReturnData.builder()
                .ticker("BBB")
                .prices(Arrays.asList(200.0, 210.0, 220.0))
                .timestamps(timestamps)
                .initialAmount(1000.0)
                .dividends(new ArrayList<>())
                .build();
        List<StockReturnData> stockReturns = Arrays.asList(stock1, stock2);
        List<Double> weights = Arrays.asList(0.5, 0.5);

        // When: calculatePortfolioStockReturn 호출

        PortfolioReturnService x = new PortfolioReturnService(portfolioDataService, new ReturnCalculator(), portfolioAnalyzer, periodManager);
        StockReturnData result = x.calculatePortfolioStockReturn(stockReturns, weights);

        // Then: 결과가 null이 아니고, 가격 시계열 길이가 동일해야 함
        assertNotNull(result);
        System.out.println("result=" + result);
        System.out.println("result.getTimestamps()=" + result.getTimestamps());
        assertEquals(timestamps, result.getTimestamps());
        assertEquals(3, result.getPrices().size());
        // 실제 수익률 등은 구현에 따라 달라질 수 있으므로, 여기서는 존재 여부만 확인
    }

    @Test
    void shouldExtractDatesFromChartResponse() {
        // Given
        var timestamps = Arrays.asList(1593576000L, 1596254400L, 1598932800L, 1601524800L);

        // When
        var dates = portfolioReturnService.extractDates(timestamps);

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
        var dates = portfolioReturnService.extractDates(
            response.getChart() != null && response.getChart().getResult() != null && !response.getChart().getResult().isEmpty() ?
                response.getChart().getResult().get(0).getTimestamp() : null
        );

        // Then
        assertTrue(dates.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForEmptyChartResponse() {
        // Given
        ChartResponse response = new ChartResponse();

        // When
        var dates = portfolioReturnService.extractDates(
            response.getChart() != null && response.getChart().getResult() != null && !response.getChart().getResult().isEmpty() ?
                response.getChart().getResult().get(0).getTimestamp() : null
        );

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
