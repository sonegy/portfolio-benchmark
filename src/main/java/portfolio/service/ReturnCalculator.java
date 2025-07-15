package portfolio.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import portfolio.api.ChartResponse.Dividend;

/**
 * A service class for calculating various types of investment returns.
 * This includes price return, total return with dividends, CAGR, and cumulative returns.
 */
@Service
public class ReturnCalculator {

    /**
     * Validates that the prices list is not null and contains at least two prices.
     *
     * @param prices The list of prices to validate.
     * @throws IllegalArgumentException if the list contains fewer than two prices.
     */
    private void validatePricesForReturn(List<Double> prices) {
        if (prices == null || prices.size() < 2) {
            throw new IllegalArgumentException("At least two prices are required");
        }
    }

    /**
     * Calculates the price return based on the first and last price in a list.
     *
     * @param prices A list of prices, must contain at least two elements.
     * @return The price return as a decimal value (e.g., 0.1 for 10%).
     * @throws IllegalArgumentException if the prices list contains fewer than two prices.
     */
    public double calculatePriceReturn(List<Double> prices) {
        validatePricesForReturn(prices);
        
        double startPrice = prices.get(0);
        double endPrice = prices.get(prices.size() - 1);
        
        return (endPrice - startPrice) / startPrice;
    }

    /**
     * Calculates the total return, including price changes and reinvested dividends.
     * If no dividends are provided, it calculates the simple price return.
     *
     * @param prices     A list of prices.
     * @param timestamps A list of timestamps corresponding to the prices.
     * @param dividends  A list of dividends.
     * @return The total return as a decimal value.
     * @throws IllegalArgumentException if the prices list contains fewer than two prices.
     */
    public double calculateTotalReturn(List<Double> prices, List<Long> timestamps, List<Dividend> dividends) {
        validatePricesForReturn(prices);

        if (dividends == null || dividends.isEmpty()) {
            return calculatePriceReturn(prices);
        }

        List<Double> cumulativeReturns = calculateCumulativeReturns(prices, timestamps, dividends);
        return cumulativeReturns.get(cumulativeReturns.size() - 1);
    }

    /**
     * Calculates the Compound Annual Growth Rate (CAGR).
     *
     * @param startValue The starting value of the investment.
     * @param endValue   The ending value of the investment.
     * @param years      The number of years over which the investment grew.
     * @return The CAGR as a decimal value.
     * @throws IllegalArgumentException if start value, end value, or years are not positive.
     */
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

    /**
     * Calculates the cumulative returns over a period, assuming dividends are reinvested.
     * It simulates holding one share initially and reinvesting all cash from dividends.
     * This method correctly handles dividends paid between price data points.
     *
     * @param prices     A list of prices, chronologically ordered.
     * @param timestamps A list of timestamps for each price, chronologically ordered.
     * @param dividends  A list of dividends paid during the period. This list is not modified.
     * @return A list of cumulative returns for each price point, as decimal values.
     * @throws IllegalArgumentException if prices or timestamps are null, empty, or of different sizes,
     *                                  or if the start price is not positive.
     */
    public List<Double> calculateCumulativeReturns(List<Double> prices, List<Long> timestamps, List<Dividend> dividends) {
        double startPrice = prices.get(0);
        if (startPrice <= 0) {
            throw new IllegalArgumentException("Start price must be positive for cumulative return calculation.");
        }

        List<Double> portfolioValues = calculatePortfolioValues(prices, timestamps, dividends, 1.0);
        
        List<Double> cumulativeReturns = new ArrayList<>();
        for (double value : portfolioValues) {
            cumulativeReturns.add((value - startPrice) / startPrice);
        }
        return cumulativeReturns;
    }

    /**
     * Calculates how an initial investment amount changes over time, including reinvested dividends.
     *
     * @param prices        A list of prices, chronologically ordered.
     * @param timestamps    A list of timestamps for each price.
     * @param dividends     A list of dividends paid during the period.
     * @param initialAmount The initial investment amount.
     * @return A list representing the value of the investment at each point in time.
     */
    public List<Double> calculateAmountChanges(List<Double> prices, List<Long> timestamps, List<Dividend> dividends, double initialAmount) {
        return calculateAmountChanges(prices, timestamps, dividends, initialAmount, 1.0);
    }

    /**
     * Calculates how a weighted portion of an initial investment amount changes over time, including reinvested dividends.
     *
     * @param prices        A list of prices, chronologically ordered.
     * @param timestamps    A list of timestamps for each price.
     * @param dividends     A list of dividends paid during the period.
     * @param initialAmount The total initial investment amount.
     * @param weight        The weight (between 0.0 and 1.0) of the initial amount to allocate to this asset.
     * @return A list representing the value of the weighted investment at each point in time.
     */
    public List<Double> calculateAmountChanges(List<Double> prices, List<Long> timestamps, List<Dividend> dividends, double initialAmount, double weight) {
        double startPrice = prices.get(0);
        if (startPrice <= 0) {
            List<Double> amountChanges = new ArrayList<>();
            for (int i = 0; i < prices.size(); i++) amountChanges.add(0.0);
            return amountChanges;
        }
        
        double allocatedAmount = initialAmount * weight;
        double initialShares = allocatedAmount / startPrice;
        
        return calculatePortfolioValues(prices, timestamps, dividends, initialShares);
    }

    /**
     * Core private method to calculate portfolio values over time based on an initial number of shares.
     *
     * @param prices        A list of prices.
     * @param timestamps    A list of timestamps for each price.
     * @param dividends     A list of dividends.
     * @param initialShares The starting number of shares.
     * @return A list of portfolio values at each timestamp.
     */
    private List<Double> calculatePortfolioValues(List<Double> prices, List<Long> timestamps, List<Dividend> dividends, double initialShares) {
        if (prices == null || prices.isEmpty() || timestamps == null || timestamps.isEmpty()) {
            throw new IllegalArgumentException("Prices and timestamps lists cannot be null or empty");
        }
        if (prices.size() != timestamps.size()) {
            throw new IllegalArgumentException("Prices and timestamps lists must have the same size");
        }

        List<Double> portfolioValues = new ArrayList<>();
        
        // Create a mutable, sorted copy of dividends to avoid modifying the original list
        List<Dividend> sortedDividends = new ArrayList<>();
        if (dividends != null) {
            sortedDividends.addAll(dividends);
            sortedDividends.sort((d1, d2) -> Long.compare(d1.getDate(), d2.getDate()));
        }

        double shares = initialShares;
        double cash = 0.0;

        for (int i = 0; i < prices.size(); i++) {
            long currentTimestamp = timestamps.get(i);
            double currentPrice = prices.get(i);
            long previousTimestamp = (i == 0) ? 0 : timestamps.get(i - 1);

            // Accumulate cash from dividends paid between the last price point and the current one
            java.util.Iterator<Dividend> iterator = sortedDividends.iterator();
            while (iterator.hasNext()) {
                Dividend div = iterator.next();
                if (div.getDate() > previousTimestamp && div.getDate() <= currentTimestamp) {
                    cash += shares * div.getAmount();
                    iterator.remove(); // Simplify by removing processed dividends
                } else if (div.getDate() > currentTimestamp) {
                    // Since the list is sorted, we can stop checking for this period
                    break;
                }
            }

            // Reinvest any available cash at the current price
            if (cash > 0 && currentPrice > 0) {
                shares += cash / currentPrice;
                cash = 0;
            }

            double portfolioValue = shares * currentPrice;
            portfolioValues.add(portfolioValue);
        }

        return portfolioValues;
    }
}
