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
        
        // 샘플 데이터 생성
        StockReturnData appleData = new StockReturnData("AAPL", 0.15, 0.18, 0.12, 0.0);
        appleData.setCumulativeReturns(List.of(1.0, 1.05, 1.10, 1.15));
        appleData.setDates(List.of(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 4, 1),
            LocalDate.of(2023, 7, 1),
            LocalDate.of(2023, 10, 1)
        ));

        StockReturnData microsoftData = new StockReturnData("MSFT", 0.12, 0.14, 0.10, 0.0);
        microsoftData.setCumulativeReturns(List.of(1.0, 1.03, 1.08, 1.12));
        microsoftData.setDates(List.of(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 4, 1),
            LocalDate.of(2023, 7, 1),
            LocalDate.of(2023, 10, 1)
        ));

        samplePortfolioData = new PortfolioReturnData(List.of(appleData, microsoftData));
        samplePortfolioData.setPortfolioPriceReturn(0.135);
        samplePortfolioData.setPortfolioTotalReturn(0.16);
        samplePortfolioData.setPortfolioCAGR(0.11);
        samplePortfolioData.setVolatility(0.08);
        samplePortfolioData.setSharpeRatio(1.2);
    }

    @Test
    void shouldGenerateTimeSeriesChartData() {
        // When
        ChartData chartData = chartGenerator.generateTimeSeriesChart(samplePortfolioData);

        // Then
        assertNotNull(chartData);
        assertEquals("Portfolio Time Series Analysis", chartData.getTitle());
        assertEquals("timeseries", chartData.getType());
        assertFalse(chartData.getDates().isEmpty());
        assertEquals(2, chartData.getSeries().size());
        assertTrue(chartData.getSeries().containsKey("AAPL"));
        assertTrue(chartData.getSeries().containsKey("MSFT"));
    }

    @Test
    void shouldGenerateComparisonChartData() {
        // When
        ChartData chartData = chartGenerator.generateComparisonChart(samplePortfolioData);

        // Then
        assertNotNull(chartData);
        assertEquals("Stock Performance Comparison", chartData.getTitle());
        assertEquals("bar", chartData.getType());
        assertEquals(2, chartData.getSeries().size());
        assertTrue(chartData.getSeries().containsKey("Price Return"));
        assertTrue(chartData.getSeries().containsKey("Total Return"));
    }

    @Test
    void shouldGenerateCumulativeReturnChartData() {
        // When
        ChartData chartData = chartGenerator.generateCumulativeReturnChart(samplePortfolioData);

        // Then
        assertNotNull(chartData);
        assertEquals("Cumulative Returns", chartData.getTitle());
        assertEquals("line", chartData.getType());
        assertFalse(chartData.getDates().isEmpty());
        assertEquals(2, chartData.getSeries().size());
        assertTrue(chartData.getSeries().containsKey("AAPL"));
        assertTrue(chartData.getSeries().containsKey("MSFT"));
    }
}
