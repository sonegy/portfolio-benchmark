package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;

import portfolio.model.PortfolioRequest;

class PortfolioReturnServiceTest {
    @Mock
    private PortfolioDataService portfolioDataService;
    @Mock
    private StockReturnCalculator stockReturnCalculator;
    private PortfolioReturnService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PortfolioReturnService(portfolioDataService, stockReturnCalculator);
    }

    @Test
    void 요청이_null이면_예외() {
        assertThrows(IllegalArgumentException.class, () -> service.analyzePortfolio(null));
    }

    @Test
    void ticker가_null이면_예외() {
        var req = new PortfolioRequest();
        req.setTickers(null);
        req.setStartDate(java.time.LocalDate.now());
        req.setEndDate(java.time.LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> service.analyzePortfolio(req));
    }

    @Test
    void ticker가_empty면_예외() {
        var req = new PortfolioRequest();
        req.setTickers(java.util.Collections.emptyList());
        req.setStartDate(java.time.LocalDate.now());
        req.setEndDate(java.time.LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> service.analyzePortfolio(req));
    }

    @Test
    void 정상_요청시_포트폴리오_리턴_데이터_반환() {
        var req = new PortfolioRequest();
        req.setTickers(java.util.List.of("AAPL"));
        req.setWeights(java.util.List.of(1.0));
        req.setStartDate(java.time.LocalDate.now().minusMonths(2));
        req.setEndDate(java.time.LocalDate.now());

        // 실제 가격/타임스탬프가 있는 ChartResponse 생성
        var chartResponse = new portfolio.api.ChartResponse();
        var chart = new portfolio.api.ChartResponse.Chart();
        var result = new portfolio.api.ChartResponse.Result();
        var indicators = new portfolio.api.ChartResponse.Indicators();
        var quote = new portfolio.api.ChartResponse.Quote();
        quote.setClose(java.util.List.of(100.0, 110.0, 120.0));
        indicators.setQuote(java.util.List.of(quote));
        result.setIndicators(indicators);
        result.setTimestamp(java.util.List.of(
                req.getStartDate().toEpochDay() * 24 * 60 * 60,
                req.getStartDate().plusMonths(1).toEpochDay() * 24 * 60 * 60,
                req.getEndDate().toEpochDay() * 24 * 60 * 60));
        chart.setResult(java.util.List.of(result));
        chartResponse.setChart(chart);

        // 인덱스도 동일하게 생성
        var indexChartResponse = new portfolio.api.ChartResponse();
        var indexChart = new portfolio.api.ChartResponse.Chart();
        var indexResult = new portfolio.api.ChartResponse.Result();
        var indexIndicators = new portfolio.api.ChartResponse.Indicators();
        var indexQuote = new portfolio.api.ChartResponse.Quote();
        indexQuote.setClose(java.util.List.of(4000.0, 4100.0, 4200.0));
        indexIndicators.setQuote(java.util.List.of(indexQuote));
        indexResult.setIndicators(indexIndicators);
        indexResult.setTimestamp(result.getTimestamp());
        indexChart.setResult(java.util.List.of(indexResult));
        indexChartResponse.setChart(indexChart);

        var map = new java.util.HashMap<String, portfolio.api.ChartResponse>();
        map.put("AAPL", chartResponse);
        map.put("^GSPC", indexChartResponse);

        when(portfolioDataService.fetchMultipleDividends(anyList(), anyLong(), anyLong()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(map));

        // 최소한 정상 StockReturnData 반환
        var stockReturnData = portfolio.model.StockReturnData.builder()
                .ticker("AAPL")
                .prices(java.util.List.of(100.0, 110.0, 120.0))
                .timestamps(java.util.List.of(
                        req.getStartDate().toEpochDay() * 24 * 60 * 60,
                        req.getStartDate().plusMonths(1).toEpochDay() * 24 * 60 * 60,
                        req.getEndDate().toEpochDay() * 24 * 60 * 60))
                .dividends(java.util.Collections.emptyList())
                .build();

        when(stockReturnCalculator.calculateStockReturns(any(), any()))
                .thenReturn(java.util.List.of(stockReturnData));

        assertDoesNotThrow(() -> service.analyzePortfolio(req));
    }

    // --- calculatePortfolioStockReturn 단위 테스트 ---

    @Test
    void stockReturns가_empty면_예외() {
        assertThrows(UnsupportedOperationException.class, () -> service.calculatePortfolioStockReturn(true, List.of(),
                List.of(1.0), List.of(100.0, 110.0, 120.0)));
    }

    @Test
    void weights가_null이면_예외() {
        var stockReturn = minimalStockReturnData();
        assertThrows(IllegalArgumentException.class, () -> service.calculatePortfolioStockReturn(true,
                List.of(stockReturn), null, List.of(100.0, 110.0, 120.0)));
    }

    @Test
    void stockReturns와_weights_길이_불일치시_예외() {
        var stockReturn = minimalStockReturnData();
        assertThrows(IllegalArgumentException.class, () -> service.calculatePortfolioStockReturn(true,
                List.of(stockReturn), List.of(0.5, 0.5), List.of(100.0, 110.0, 120.0)));
    }

    @Test
    void 정상_입력시_포트폴리오_StockReturnData_반환() {
        var stock1 = portfolio.model.StockReturnData.builder()
                .ticker("AAA")
                .prices(java.util.List.of(100.0, 110.0, 120.0))
                .timestamps(java.util.List.of(1L, 2L, 3L))
                .dividends(List.of())
                .initialAmount(1000.0)
                .build();
        var stock2 = portfolio.model.StockReturnData.builder()
                .ticker("BBB")
                .prices(java.util.List.of(200.0, 210.0, 220.0))
                .timestamps(java.util.List.of(1L, 2L, 3L))
                .dividends(List.of())
                .initialAmount(2000.0)
                .build();

        var weights = java.util.List.of(0.5, 0.5);
        var indexPrices = java.util.List.of(1000.0, 1100.0, 1200.0);

        // 정상 반환값 stub 추가
        when(stockReturnCalculator.calculateStockReturn(anyBoolean(),
                anyString(), anyList(), anyList(), anyList(), anyList(), anyDouble(), anyDouble()))
                .thenReturn(portfolio.model.StockReturnData.builder()
                        .ticker("Portfolio")
                        .prices(java.util.List.of(1.0, 2.0, 3.0))
                        .timestamps(java.util.List.of(1L, 2L, 3L))
                        .dividends(List.of())
                        .initialAmount(3000.0)
                        .build());

        var result = service.calculatePortfolioStockReturn(true, List.of(stock1, stock2), weights, indexPrices);

        assertNotNull(result);
        assertEquals(3, result.getPrices().size());
    }

    // 최소 StockReturnData 생성 유틸
    private portfolio.model.StockReturnData minimalStockReturnData() {
        return portfolio.model.StockReturnData.builder()
                .ticker("AAA")
                .prices(java.util.List.of(100.0, 110.0, 120.0))
                .timestamps(java.util.List.of(1L, 2L, 3L))
                .dividends(List.of())
                .initialAmount(1000.0)
                .build();
    }
}