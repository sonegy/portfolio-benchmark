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
    void shouldCalculateWeightedPortfolioReturn() {
        // Given
        List<Double> stockReturns = Arrays.asList(0.10, 0.20, 0.15); // 10%, 20%, 15%
        List<Double> weights = Arrays.asList(0.4, 0.3, 0.3); // 40%, 30%, 30%
        
        // When
        double portfolioReturn = portfolioAnalyzer.calculateWeightedReturn(stockReturns, weights);
        
        // Then
        // Expected: 0.4 * 0.10 + 0.3 * 0.20 + 0.3 * 0.15 = 0.04 + 0.06 + 0.045 = 0.145
        assertEquals(0.145, portfolioReturn, 0.001);
    }
    
    @Test
    void shouldCalculateVolatility() {
        // Given
        List<StockReturnData> returns = Arrays.asList(
                new StockReturnData("A", 0.10, 0.10, 0),
                new StockReturnData("B", 0.05, 0.05, 0),
                new StockReturnData("C", -0.02, -0.02, 0),
                new StockReturnData("D", 0.08, 0.08, 0),
                new StockReturnData("E", 0.12, 0.12, 0)
        );
        List<Double> weights = Arrays.asList(0.2, 0.2, 0.2, 0.2, 0.2);

        // When
        double volatility = portfolioAnalyzer.calculateVolatility(returns, weights);

        // Then
        // Manual calculation:
        // Mean = (0.10 + 0.05 + (-0.02) + 0.08 + 0.12) / 5 = 0.33 / 5 = 0.066
        // Weighted Variance = 0.2 * (0.10-0.066)² + 0.2 * (0.05-0.066)² + 0.2 * (-0.02-0.066)² + 0.2 * (0.08-0.066)² + 0.2 * (0.12-0.066)²
        // Weighted Variance = 0.2 * 0.001156 + 0.2 * 0.000256 + 0.2 * 0.007396 + 0.2 * 0.000196 + 0.2 * 0.002916
        // Weighted Variance = 0.0002312 + 0.0000512 + 0.0014792 + 0.0000392 + 0.0005832 = 0.002384
        // Standard deviation = sqrt(0.002384) = 0.048826
        assertTrue(volatility > 0);
        assertEquals(0.048826, volatility, 0.00001);
    }
    
    @Test
    void shouldCalculateSharpeRatio() {
        // Given
        double portfolioTotalReturn = 0.066;
        double volatility = 0.04883;
        
        // When
        double sharpeRatio = portfolioAnalyzer.calculateSharpeRatio(portfolioTotalReturn, volatility);
        
        // Then
        // Mean return = 0.066, Volatility = 0.04883, Risk-free rate = 0.0 (default)
        // Sharpe Ratio = (0.066 - 0.0) / 0.04883 = 1.3516
        assertTrue(sharpeRatio > 0);
        assertEquals(1.3516, sharpeRatio, 0.0001);
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
}
