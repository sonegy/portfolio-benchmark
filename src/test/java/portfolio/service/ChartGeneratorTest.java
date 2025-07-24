package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import portfolio.api.ChartResponse.Dividend;
import portfolio.model.ChartData;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;
import portfolio.util.DateUtils;

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
    void calculateYearlyDividends_returnsEmptyMap_whenNoDividends() {
        StockReturnData stock = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of())
                .build();
        Map<Integer, Double> result = chartGenerator.calculateYearlyDividends(stock);
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateYearlyDividends_singleDividend() {
        portfolio.api.ChartResponse.Dividend div = new portfolio.api.ChartResponse.Dividend();
        div.setAmount(100.0);
        div.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2023, 5, 1)));
        StockReturnData stock = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of(div))
                .build();
        Map<Integer, Double> result = chartGenerator.calculateYearlyDividends(stock);
        assertEquals(100.0, result.get(2023));
    }

    @Test
    void calculateYearlyDividends_multipleYears() {
        portfolio.api.ChartResponse.Dividend div1 = new portfolio.api.ChartResponse.Dividend();
        div1.setAmount(50.0);
        div1.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2022, 1, 1)));
        portfolio.api.ChartResponse.Dividend div2 = new portfolio.api.ChartResponse.Dividend();
        div2.setAmount(70.0);
        div2.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2023, 1, 1)));
        StockReturnData stock = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of(div1, div2))
                .build();
        Map<Integer, Double> result = chartGenerator.calculateYearlyDividends(stock);
        assertEquals(50.0, result.get(2022));
        assertEquals(70.0, result.get(2023));
    }

    @Test
    void calculateYearlyDividends_multipleTickers() {
        portfolio.api.ChartResponse.Dividend div1 = new portfolio.api.ChartResponse.Dividend();
        div1.setAmount(30.0);
        div1.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2022, 1, 1)));
        portfolio.api.ChartResponse.Dividend div2 = new portfolio.api.ChartResponse.Dividend();
        div2.setAmount(40.0);
        div2.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2022, 1, 1)));
        StockReturnData stock1 = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of(div1))
                .build();
        StockReturnData stock2 = StockReturnData.builder()
                .ticker("MSFT")
                .dividends(List.of(div2))
                .build();
        Map<String, Map<Integer, Double>> result = chartGenerator
                .calculateAllYearlyDividends(List.of(stock1, stock2));
        assertEquals(30.0, result.get("AAPL").get(2022));
        assertEquals(40.0, result.get("MSFT").get(2022));
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
    void calculateYearlyDividends_reflectsActualShares() {
        // initialAmount = 1000, 시작가격 = 10 → 주식수 = 100
        double initialAmount = 1000.0;
        double startPrice = 10.0;
        double expectedShares = initialAmount / startPrice; // 100

        portfolio.api.ChartResponse.Dividend div = new portfolio.api.ChartResponse.Dividend();
        div.setAmount(2.0); // 1주당 배당금
        div.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2023, 6, 1)));

        StockReturnData stock = StockReturnData.builder()
                .ticker("AAPL")
                .initialAmount(initialAmount)
                .prices(List.of(startPrice, 12.0, 13.0))
                .dividends(List.of(div))
                .build();
        Map<Integer, Double> result = chartGenerator.calculateYearlyDividends(stock);
        // 실제 분배금 = 배당금 * 주식수 = 2.0 * 100 = 200
        assertEquals(200.0, result.get(2023));
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
    void generateDividendsAmountComparisonChart_emptyStockReturns() {
        samplePortfolioData.setStockReturns(List.of());
        ChartData chartData = chartGenerator.generateDividendsAmountComparisonChart(samplePortfolioData);
        assertNotNull(chartData);
        assertEquals("연도별 배당금 비교", chartData.getTitle());
        assertEquals("bar", chartData.getType());
        assertTrue(chartData.getSeries().isEmpty());
        assertTrue(chartData.getLabels() == null || chartData.getLabels().isEmpty());
    }

    @Test
    void generateDividendsAmountComparisonChart_singleStockSingleYear() {
        Dividend div = new Dividend();
        div.setAmount(100.0);
        div.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2023, 5, 1)));
        StockReturnData stock = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of(div))
                .build();
        samplePortfolioData.setStockReturns(List.of(stock));
        ChartData chartData = chartGenerator.generateDividendsAmountComparisonChart(samplePortfolioData);
        assertEquals(List.of("2023"), chartData.getLabels());
        assertEquals(List.of(100.0), chartData.getSeries().get("AAPL"));
    }

    @Test
    void generateDividendsAmountComparisonChart_multipleStocksMultipleYears() {
        Dividend div1 = new Dividend();
        div1.setAmount(50.0);
        div1.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2022, 1, 1)));
        Dividend div2 = new Dividend();
        div2.setAmount(70.0);
        div2.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2023, 1, 1)));
        StockReturnData stock1 = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of(div1, div2))
                .build();
        Dividend div3 = new Dividend();
        div3.setAmount(30.0);
        div3.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2022, 1, 1)));
        StockReturnData stock2 = StockReturnData.builder()
                .ticker("MSFT")
                .dividends(List.of(div3))
                .build();
        samplePortfolioData.setStockReturns(List.of(stock1, stock2));
        ChartData chartData = chartGenerator.generateDividendsAmountComparisonChart(samplePortfolioData);
        assertEquals(List.of("2022", "2023"), chartData.getLabels());
        assertEquals(List.of(50.0, 70.0), chartData.getSeries().get("AAPL"));
        assertEquals(List.of(30.0, 0.0), chartData.getSeries().get("MSFT"));
    }

    @Test
    void generateDividendsAmountComparisonChart_missingYearFilledWithZero() {
        Dividend div1 = new Dividend();
        div1.setAmount(40.0);
        div1.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2022, 1, 1)));
        StockReturnData stock1 = StockReturnData.builder()
                .ticker("AAPL")
                .dividends(List.of(div1))
                .build();
        Dividend div2 = new Dividend();
        div2.setAmount(60.0);
        div2.setDate(DateUtils.toUnixTimeSeconds(LocalDate.of(2023, 1, 1)));
        StockReturnData stock2 = StockReturnData.builder()
                .ticker("MSFT")
                .dividends(List.of(div2))
                .build();
        samplePortfolioData.setStockReturns(List.of(stock1, stock2));
        ChartData chartData = chartGenerator.generateDividendsAmountComparisonChart(samplePortfolioData);
        assertEquals(List.of("2022", "2023"), chartData.getLabels());
        assertEquals(List.of(40.0, 0.0), chartData.getSeries().get("AAPL"));
        assertEquals(List.of(0.0, 60.0), chartData.getSeries().get("MSFT"));
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
                LocalDate.of(2023, 10, 1)), chartData.getDates());
        Map<String, List<Double>> series = chartData.getSeries();
        assertEquals(3, series.size());
        assertEquals(List.of(1.0, 1.06, 1.13, 1.20), series.get("Portfolio"));
        assertEquals(List.of(1.0, 1.05, 1.10, 1.15), series.get("AAPL"));
        assertEquals(List.of(1.0, 1.03, 1.08, 1.12), series.get("MSFT"));
    }
}
