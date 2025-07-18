package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PortfolioAnalyzerTest {
    
    private PortfolioAnalyzer portfolioAnalyzer;
    
    @BeforeEach
    void setUp() {
        portfolioAnalyzer = new PortfolioAnalyzer();
    }
    
    @Test
    void shouldCalculateCorrelation() {
        // Given
        List<Double> returns1 = Arrays.asList(0.10, 0.05, -0.02, 0.08, 0.12);
        List<Double> returns2 = Arrays.asList(0.08, 0.03, -0.01, 0.06, 0.10);
        
        // When
        double correlation = portfolioAnalyzer.calculateCorrelation(returns1, returns2);
        
        // Then
        // Correlation should be between -1 and 1
        assertTrue(correlation >= -1.0 && correlation <= 1.0);
        // These returns should be positively correlated
        assertTrue(correlation > 0);
        assertEquals(0.998, correlation, 0.01);
    }

    @Test
    void shouldCalculatePortfolioCumulativeReturns() {
        StockReturnData a = StockReturnData.builder()
                .ticker("A")
                .priceReturn(0)
                .totalReturn(0)
                .cagr(0)
                .volatility(0.0)
                .cumulativeReturns(Arrays.asList(0.0, 0.1, 0.2))
                .build();
        StockReturnData b = StockReturnData.builder()
                .ticker("B")
                .priceReturn(0)
                .totalReturn(0)
                .cagr(0)
                .volatility(0.0)
                .cumulativeReturns(Arrays.asList(0.0, 0.05, 0.15))
                .build();
        List<StockReturnData> stocks = Arrays.asList(a, b);
        List<Double> weights = Arrays.asList(0.6, 0.4);
        PortfolioAnalyzer analyzer = new PortfolioAnalyzer();
        List<Double> result = analyzer.calculatePortfolioCumulativeReturns(stocks, weights);
        List<Double> expected = Arrays.asList(0.0, 0.08, 0.18);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i), 1e-6, "index="+i);
        }
    }

    @Test
    void shouldCalculatePortfolioCumulativePriceReturns() {
        StockReturnData a = StockReturnData.builder()
                .ticker("A")
                .priceReturn(0)
                .totalReturn(0)
                .cagr(0)
                .volatility(0.0)
                .cumulativePriceReturns(Arrays.asList(0.0, 0.2, 0.4))
                .build();
        StockReturnData b = StockReturnData.builder()
                .ticker("B")
                .priceReturn(0)
                .totalReturn(0)
                .cagr(0)
                .volatility(0.0)
                .cumulativePriceReturns(Arrays.asList(0.0, 0.1, 0.3))
                .build();
        List<StockReturnData> stocks = Arrays.asList(a, b);
        List<Double> weights = Arrays.asList(0.7, 0.3);
        PortfolioAnalyzer analyzer = new PortfolioAnalyzer();
        List<Double> result = analyzer.calculatePortfolioCumulativePriceReturns(stocks, weights);
        List<Double> expected = Arrays.asList(0.0, 0.17, 0.37);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i), 1e-6, "index="+i);
        }
    }

    @Test
    void shouldReturnEmptyListIfInputIsNullOrEmpty() {
        PortfolioAnalyzer analyzer = new PortfolioAnalyzer();
        assertTrue(analyzer.calculatePortfolioCumulativeReturns(null, Arrays.asList(1.0)).isEmpty());
        assertTrue(analyzer.calculatePortfolioCumulativeReturns(Arrays.asList(), Arrays.asList(1.0)).isEmpty());
        assertTrue(analyzer.calculatePortfolioCumulativeReturns(Arrays.asList(
            StockReturnData.builder().ticker("A").priceReturn(0).totalReturn(0).cagr(0).volatility(0.0).build()), null).isEmpty());
        assertTrue(analyzer.calculatePortfolioCumulativeReturns(Arrays.asList(
            StockReturnData.builder().ticker("A").priceReturn(0).totalReturn(0).cagr(0).volatility(0.0).build()), Arrays.asList()).isEmpty());
    
    }
}
