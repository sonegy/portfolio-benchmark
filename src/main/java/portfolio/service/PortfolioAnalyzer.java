package portfolio.service;

import java.util.List;

public class PortfolioAnalyzer {
    
    private void validateReturnsNotNullOrEmpty(List<Double> returns, String parameterName) {
        if (returns == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
        if (returns.isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be empty");
        }
    }
    
    private void validateMinimumSize(List<Double> returns, int minSize, String operation) {
        if (returns.size() < minSize) {
            throw new IllegalArgumentException("At least " + minSize + " returns are required to " + operation);
        }
    }
    
    private double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    public double calculateWeightedReturn(List<Double> stockReturns, List<Double> weights) {
        if (stockReturns == null || weights == null) {
            throw new IllegalArgumentException("Stock returns and weights cannot be null");
        }
        
        if (stockReturns.size() != weights.size()) {
            throw new IllegalArgumentException("Stock returns and weights must have the same size");
        }
        
        double weightedReturn = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedReturn += stockReturns.get(i) * weights.get(i);
        }
        
        return weightedReturn;
    }
    
    public double calculateVolatility(List<Double> returns) {
        validateReturnsNotNullOrEmpty(returns, "Returns");
        validateMinimumSize(returns, 2, "calculate volatility");
        
        // Calculate mean
        double mean = calculateMean(returns);
        
        // Calculate variance
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average()
            .orElse(0.0);
        
        // Return standard deviation (volatility)
        return Math.sqrt(variance);
    }
    
    public double calculateSharpeRatio(List<Double> returns, double riskFreeRate) {
        validateReturnsNotNullOrEmpty(returns, "Returns");
        
        // Calculate mean return
        double meanReturn = calculateMean(returns);
        
        // Calculate volatility
        double volatility = calculateVolatility(returns);
        
        if (volatility == 0.0) {
            throw new IllegalArgumentException("Cannot calculate Sharpe ratio when volatility is zero");
        }
        
        // Sharpe Ratio = (Mean Return - Risk Free Rate) / Volatility
        return (meanReturn - riskFreeRate) / volatility;
    }
    
    public double calculateCorrelation(List<Double> returns1, List<Double> returns2) {
        validateReturnsNotNullOrEmpty(returns1, "Returns1");
        validateReturnsNotNullOrEmpty(returns2, "Returns2");
        
        if (returns1.size() != returns2.size()) {
            throw new IllegalArgumentException("Both return series must have the same size");
        }
        
        validateMinimumSize(returns1, 2, "calculate correlation");
        
        // Calculate means
        double mean1 = calculateMean(returns1);
        double mean2 = calculateMean(returns2);
        
        // Calculate covariance and standard deviations
        double covariance = 0.0;
        double sumSquares1 = 0.0;
        double sumSquares2 = 0.0;
        
        for (int i = 0; i < returns1.size(); i++) {
            double diff1 = returns1.get(i) - mean1;
            double diff2 = returns2.get(i) - mean2;
            
            covariance += diff1 * diff2;
            sumSquares1 += diff1 * diff1;
            sumSquares2 += diff2 * diff2;
        }
        
        double stdDev1 = Math.sqrt(sumSquares1 / returns1.size());
        double stdDev2 = Math.sqrt(sumSquares2 / returns2.size());
        
        if (stdDev1 == 0.0 || stdDev2 == 0.0) {
            throw new IllegalArgumentException("Cannot calculate correlation when standard deviation is zero");
        }
        
        // Correlation = Covariance / (StdDev1 * StdDev2)
        return (covariance / returns1.size()) / (stdDev1 * stdDev2);
    }
}
