package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

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
        List<Double> returns = Arrays.asList(0.10, 0.05, -0.02, 0.08, 0.12);
        
        // When
        double volatility = portfolioAnalyzer.calculateVolatility(returns);
        
        // Then
        // Manual calculation:
        // Mean = (0.10 + 0.05 + (-0.02) + 0.08 + 0.12) / 5 = 0.33 / 5 = 0.066
        // Variance = [(0.10-0.066)² + (0.05-0.066)² + (-0.02-0.066)² + (0.08-0.066)² + (0.12-0.066)²] / 5
        // Variance = [0.001156 + 0.000256 + 0.007396 + 0.000196 + 0.002916] / 5 = 0.01192 / 5 = 0.002384
        // Standard deviation = sqrt(0.002384) = 0.04883
        assertTrue(volatility > 0);
        assertEquals(0.04883, volatility, 0.001);
    }
    
    @Test
    void shouldCalculateSharpeRatio() {
        // Given
        List<Double> returns = Arrays.asList(0.10, 0.05, -0.02, 0.08, 0.12);
        double riskFreeRate = 0.02; // 2% risk-free rate
        
        // When
        double sharpeRatio = portfolioAnalyzer.calculateSharpeRatio(returns, riskFreeRate);
        
        // Then
        // Mean return = 0.066, Risk-free rate = 0.02, Volatility = 0.04883
        // Sharpe Ratio = (0.066 - 0.02) / 0.04883 = 0.046 / 0.04883 = 0.942
        assertTrue(sharpeRatio > 0);
        assertEquals(0.942, sharpeRatio, 0.01);
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
