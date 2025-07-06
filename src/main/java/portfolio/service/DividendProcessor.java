package portfolio.service;

import portfolio.api.ChartResponse.Dividend;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DividendProcessor {
    
    public double calculateTotalDividends(List<Dividend> dividends) {
        if (dividends == null || dividends.isEmpty()) {
            return 0.0;
        }
        
        return dividends.stream()
            .mapToDouble(Dividend::getAmount)
            .sum();
    }
    
    public List<Dividend> filterDividendsByDateRange(List<Dividend> dividends, long startDate, long endDate) {
        if (dividends == null || dividends.isEmpty()) {
            return new ArrayList<>();
        }
        
        return dividends.stream()
            .filter(dividend -> dividend.getDate() >= startDate && dividend.getDate() <= endDate)
            .collect(Collectors.toList());
    }
    
    public double calculateReinvestedDividendValue(List<Dividend> dividends, List<Double> prices, List<Long> timestamps) {
        if (dividends == null || dividends.isEmpty() || prices == null || prices.isEmpty() || timestamps == null || timestamps.isEmpty()) {
            return 0.0;
        }
        
        double totalReinvestedValue = 0.0;
        double finalPrice = prices.get(prices.size() - 1);
        
        for (Dividend dividend : dividends) {
            // Find the price at the dividend date
            double priceAtDividendDate = findPriceAtDate(dividend.getDate(), prices, timestamps);
            if (priceAtDividendDate > 0) {
                // Calculate shares bought with dividend
                double sharesBought = dividend.getAmount() / priceAtDividendDate;
                // Calculate value of those shares at final price
                totalReinvestedValue += sharesBought * finalPrice;
            }
        }
        
        return totalReinvestedValue;
    }
    
    public double calculateDividendYield(List<Dividend> dividends, double averagePrice) {
        if (dividends == null || dividends.isEmpty() || averagePrice <= 0) {
            return 0.0;
        }
        
        double totalDividends = calculateTotalDividends(dividends);
        return totalDividends / averagePrice;
    }
    
    private double findPriceAtDate(long targetDate, List<Double> prices, List<Long> timestamps) {
        for (int i = 0; i < timestamps.size(); i++) {
            if (timestamps.get(i) >= targetDate) {
                return prices.get(i);
            }
        }
        // If no exact match, return the last available price
        return prices.get(prices.size() - 1);
    }
}
