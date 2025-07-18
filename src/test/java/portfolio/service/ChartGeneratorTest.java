package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import portfolio.model.ChartData;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChartGeneratorTest {
    private ChartGenerator chartGenerator;
    private PortfolioReturnData samplePortfolioData;

    @BeforeEach
    void setUp() {
        chartGenerator = new ChartGenerator(new ChartConfigurationService());
        // 포트폴리오 단일 데이터 생성
        StockReturnData portfolioData = StockReturnData.builder()
                .ticker("Portfolio")
                .priceReturn(0.135)
                .totalReturn(0.16)
                .cagr(0.11)
                .volatility(0.08)
                .cumulativeReturns(List.of(1.0, 1.06, 1.13, 1.20))
                .amountChanges(List.of(100.0, 106.0, 113.0, 120.0))
                .dates(List.of(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2023, 4, 1),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 10, 1)))
                .build();
        samplePortfolioData = new PortfolioReturnData(List.of());
        samplePortfolioData.setPortfolioStockReturn(portfolioData);
    }

    @Test
    void generateComparisonChart_usesPortfolioAndStockReturns() {
        // given
        StockReturnData apple = StockReturnData.builder()
                .ticker("AAPL")
                .priceReturn(0.15)
                .totalReturn(0.18)
                .build();
        StockReturnData msft = StockReturnData.builder()
                .ticker("MSFT")
                .priceReturn(0.12)
                .totalReturn(0.14)
                .build();
        samplePortfolioData.setStockReturns(List.of(apple, msft));

        // when
        ChartData chartData = chartGenerator.generateComparisonChart(samplePortfolioData);

        // then
        assertNotNull(chartData);
        assertEquals("Stock Performance Comparison", chartData.getTitle());
        assertEquals("bar", chartData.getType());
        Map<String, List<Double>> series = chartData.getSeries();
        assertTrue(series.containsKey("Price Return"));
        assertTrue(series.containsKey("Total Return"));
        assertEquals(List.of(0.135, 0.15, 0.12), series.get("Price Return"));
        assertEquals(List.of(0.16, 0.18, 0.14), series.get("Total Return"));
    }

    @Test
    void generateAmountChangeChart_usesPortfolioAndStockReturns() {
        // given
        StockReturnData apple = StockReturnData.builder()
                .ticker("AAPL")
                .amountChanges(List.of(100.0, 103.0, 108.0, 112.0))
                .dates(List.of(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2023, 4, 1),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 10, 1)))
                .build();
        samplePortfolioData.setStockReturns(List.of(apple));

        // when
        ChartData chartData = chartGenerator.generateAmountChangeChart(samplePortfolioData);

        // then
        assertNotNull(chartData);
        assertEquals("Portfolio Amount Changes", chartData.getTitle());
        assertEquals("line", chartData.getType());
        Map<String, List<Double>> series = chartData.getSeries();
        assertTrue(series.containsKey("Portfolio"));
        assertTrue(series.containsKey("AAPL"));
        assertEquals(List.of(100.0, 106.0, 113.0, 120.0), series.get("Portfolio"));
        assertEquals(List.of(100.0, 103.0, 108.0, 112.0), series.get("AAPL"));
    }

    @Test
    void generateTimeSeriesChart_usesPortfolioAndStockReturns() {
        // given: 포트폴리오와 두 개의 종목 데이터
        StockReturnData apple = StockReturnData.builder()
                .ticker("AAPL")
                .cumulativeReturns(List.of(1.0, 1.05, 1.10, 1.15))
                .dates(List.of(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2023, 4, 1),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 10, 1)))
                .build();
        StockReturnData msft = StockReturnData.builder()
                .ticker("MSFT")
                .cumulativeReturns(List.of(1.0, 1.03, 1.08, 1.12))
                .dates(List.of(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2023, 4, 1),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 10, 1)))
                .build();
        samplePortfolioData.setStockReturns(List.of(apple, msft));

        // when
        ChartData chartData = chartGenerator.generateTimeSeriesChart(samplePortfolioData);

        // then: 포트폴리오 및 각 종목의 누적수익률이 모두 포함되어야 함
        assertNotNull(chartData);
        assertEquals("Portfolio Time Series Analysis", chartData.getTitle());
        assertEquals("timeseries", chartData.getType());
        assertEquals(List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 4, 1),
                LocalDate.of(2023, 7, 1),
                LocalDate.of(2023, 10, 1)
        ), chartData.getDates());
        Map<String, List<Double>> series = chartData.getSeries();
        assertEquals(3, series.size());
        assertEquals(List.of(1.0, 1.06, 1.13, 1.20), series.get("Portfolio"));
        assertEquals(List.of(1.0, 1.05, 1.10, 1.15), series.get("AAPL"));
        assertEquals(List.of(1.0, 1.03, 1.08, 1.12), series.get("MSFT"));
    }
}
