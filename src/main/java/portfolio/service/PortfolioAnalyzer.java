package portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

@Service
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
    
    public double calculateVolatility(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        
        double portfolioMeanReturn = calculatePortfolioTotalReturn(stockReturns, weights);

        double variance = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            variance += finalWeights.get(i) * Math.pow(stockReturns.get(i).getTotalReturn() - portfolioMeanReturn, 2);
        }

        // This is a simplified variance calculation. A more accurate calculation would involve covariances.
        // For now, we use the weighted average of individual variances from the mean portfolio return.
        
        return Math.sqrt(variance);
    }

    public double calculatePortfolioPriceReturn(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        double weightedPriceReturn = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedPriceReturn += stockReturns.get(i).getPriceReturn() * finalWeights.get(i);
        }
        return weightedPriceReturn;
    }

    public double calculatePortfolioTotalReturn(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        double weightedTotalReturn = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedTotalReturn += stockReturns.get(i).getTotalReturn() * finalWeights.get(i);
        }
        return weightedTotalReturn;
    }

    public double calculatePortfolioCAGR(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        double weightedCagr = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedCagr += stockReturns.get(i).getCagr() * finalWeights.get(i);
        }
        return weightedCagr;
    }

    private List<Double> getFinalWeights(int numStocks, List<Double> weights) {
        if (weights != null && !weights.isEmpty()) {
            if (weights.size() != numStocks) {
                throw new IllegalArgumentException("The number of weights must match the number of tickers.");
            }
            double sumOfWeights = weights.stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(sumOfWeights - 1.0) > 1e-9) {
                throw new IllegalArgumentException("The sum of weights must be equal to 1.");
            }
            return weights;
        } else {
            // Default to equal weights if none are provided
            double equalWeight = 1.0 / numStocks;
            List<Double> equalWeights = new ArrayList<>();
            for (int i = 0; i < numStocks; i++) {
                equalWeights.add(equalWeight);
            }
            return equalWeights;
        }
    }
    
    /**
     * portfolioTotalReturn, volatility 이 먼저 계산되어야 한다. 
     * @param portfolioData
     * @return
     */
    public double calculateSharpeRatio(double portfolioTotalReturn, double volatility) {
        double meanReturn = portfolioTotalReturn;
        
        // Assuming a risk-free rate of 0 for simplicity. This can be made configurable.
        double riskFreeRate = 0.0;
        
        if (volatility == 0.0) {
            return 0.0; // Or throw an exception, depending on desired behavior
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
