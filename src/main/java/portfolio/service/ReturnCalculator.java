package portfolio.service;

import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import portfolio.api.ChartResponse.Dividend;

@Service
public class ReturnCalculator {
    
    private void validatePricesForReturn(List<Double> prices) {
        if (prices == null || prices.size() < 2) {
            throw new IllegalArgumentException("At least two prices are required");
        }
    }
    
    public double calculatePriceReturn(List<Double> prices) {
        validatePricesForReturn(prices);
        
        double startPrice = prices.get(0);
        double endPrice = prices.get(prices.size() - 1);
        
        return (endPrice - startPrice) / startPrice;
    }
    
    public double calculateTotalReturn(List<Double> prices, List<Dividend> dividends) {
        validatePricesForReturn(prices);
        
        // For now, if no dividends, total return equals price return
        if (dividends == null || dividends.isEmpty()) {
            return calculatePriceReturn(prices);
        }
        
        double startPrice = prices.get(0);
        double endPrice = prices.get(prices.size() - 1);
        
        // Calculate total dividends
        double totalDividends = dividends.stream()
            .mapToDouble(Dividend::getAmount)
            .sum();
        
        // Simple total return calculation: (End Price + Dividends - Start Price) / Start Price
        return (endPrice + totalDividends - startPrice) / startPrice;
    }
    
    public double calculateCAGR(double startValue, double endValue, int years) {
        if (startValue <= 0) {
            throw new IllegalArgumentException("Start value must be positive");
        }
        if (endValue <= 0) {
            throw new IllegalArgumentException("End value must be positive");
        }
        if (years <= 0) {
            throw new IllegalArgumentException("Years must be positive");
        }
        
        // CAGR = (End Value / Start Value)^(1/years) - 1
        return Math.pow(endValue / startValue, 1.0 / years) - 1.0;
    }
    
    public List<Double> calculateCumulativeReturns(List<Double> prices, List<Dividend> dividends) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Prices list cannot be null or empty");
        }
        
        List<Double> cumulativeReturns = new ArrayList<>();
        double startPrice = prices.get(0);
        
        for (int i = 0; i < prices.size(); i++) {
            double currentPrice = prices.get(i);
            double cumulativeReturn = (currentPrice - startPrice) / startPrice;
            cumulativeReturns.add(cumulativeReturn);
        }
        
        // For now, ignore dividends in cumulative returns calculation
        // TODO: Implement dividend reinvestment in cumulative returns
        
        return cumulativeReturns;
    }
    
    public List<Double> calculateAmountChanges(List<Double> prices, double initialAmount) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Prices list cannot be null or empty");
        }
        if (initialAmount < 0) {
            throw new IllegalArgumentException("Initial amount cannot be negative");
        }
        
        List<Double> amountChanges = new ArrayList<>();
        double startPrice = prices.get(0);
        
        for (double currentPrice : prices) {
            double currentAmount = initialAmount * (currentPrice / startPrice);
            amountChanges.add(currentAmount);
        }
        
        return amountChanges;
    }
    
    /**
     * 가중치를 고려한 금액 변화 계산
     */
    public List<Double> calculateAmountChanges(List<Double> prices, double initialAmount, double weight) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Prices list cannot be null or empty");
        }
        if (initialAmount < 0) {
            throw new IllegalArgumentException("Initial amount cannot be negative");
        }
        if (weight < 0 || weight > 1) {
            throw new IllegalArgumentException("Weight must be between 0 and 1");
        }
        
        List<Double> amountChanges = new ArrayList<>();
        double startPrice = prices.get(0);
        double allocatedAmount = initialAmount * weight;
        
        for (double currentPrice : prices) {
            double currentAmount = allocatedAmount * (currentPrice / startPrice);
            amountChanges.add(currentAmount);
        }
        
        return amountChanges;
    }
}
