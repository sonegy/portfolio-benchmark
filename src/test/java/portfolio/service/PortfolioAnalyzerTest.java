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
}
