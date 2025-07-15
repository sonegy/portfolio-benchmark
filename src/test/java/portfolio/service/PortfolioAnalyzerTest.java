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
    void shouldCalculatePortfolioVolatilityWithIntervalReturns() {
        // Given: 2종목, 3기간, 각 기간별 수익률 명시
        StockReturnData a = new StockReturnData("A", 0, 0, 0, 0.0);
        a.setIntervalReturns(Arrays.asList(0.1, 0.2, 0.05));
        StockReturnData b = new StockReturnData("B", 0, 0, 0, 0.0);
        b.setIntervalReturns(Arrays.asList(0.0, 0.1, -0.05));
        List<StockReturnData> stocks = Arrays.asList(a, b);
        List<Double> weights = Arrays.asList(0.6, 0.4);

        // When: 각 기간별 포트폴리오 수익률 = 0.6*A + 0.4*B
        // t0: 0.6*0.1 + 0.4*0.0 = 0.06
        // t1: 0.6*0.2 + 0.4*0.1 = 0.16
        // t2: 0.6*0.05 + 0.4*(-0.05) = 0.03 - 0.02 = 0.01
        // 평균: (0.06+0.16+0.01)/3 = 0.076666...
        // 분산: ((0.06-0.0767)^2 + (0.16-0.0767)^2 + (0.01-0.0767)^2)/3
        // = (0.000277 + 0.006922 + 0.004460)/3 = 0.003886
        // 표준편차: sqrt(0.003886) = 0.06236
        double volatility = portfolioAnalyzer.calculateVolatility(stocks, weights);

        assertEquals(0.06236, volatility, 0.0001);
    }

    @Test
    void shouldCalculatePortfolioVolatilityWithMultipleStocks() {
        // Given: 3종목, 4기간
        StockReturnData a = new StockReturnData("A", 0, 0, 0, 0.0);
        a.setIntervalReturns(Arrays.asList(0.05, 0.07, 0.10, 0.02));
        StockReturnData b = new StockReturnData("B", 0, 0, 0, 0.0);
        b.setIntervalReturns(Arrays.asList(0.03, 0.06, -0.02, 0.01));
        StockReturnData c = new StockReturnData("C", 0, 0, 0, 0.0);
        c.setIntervalReturns(Arrays.asList(-0.01, 0.04, 0.08, 0.00));
        List<StockReturnData> stocks = Arrays.asList(a, b, c);
        List<Double> weights = Arrays.asList(0.5, 0.3, 0.2);

        // When: 각 기간별 포트폴리오 수익률 계산
        // t0: 0.5*0.05 + 0.3*0.03 + 0.2*(-0.01) = 0.025 + 0.009 - 0.002 = 0.032
        // t1: 0.5*0.07 + 0.3*0.06 + 0.2*0.04 = 0.035 + 0.018 + 0.008 = 0.061
        // t2: 0.5*0.10 + 0.3*(-0.02) + 0.2*0.08 = 0.05 - 0.006 + 0.016 = 0.06
        // t3: 0.5*0.02 + 0.3*0.01 + 0.2*0.00 = 0.01 + 0.003 + 0.0 = 0.013
        // 평균: (0.032+0.061+0.06+0.013)/4 = 0.0415
        // 분산: ((0.032-0.0415)^2 + (0.061-0.0415)^2 + (0.06-0.0415)^2 + (0.013-0.0415)^2)/4
        // = (0.00009025 + 0.00038025 + 0.00034225 + 0.00081225)/4 = 0.00040675
        // 표준편차: sqrt(0.00040675) = 0.02017
        double volatility = portfolioAnalyzer.calculateVolatility(stocks, weights);
        assertEquals(0.02017, volatility, 0.0001);
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
