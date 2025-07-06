package portfolio.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import portfolio.api.ChartResponse.Dividend;

class ReturnCalculatorTest {

    @Test
    void shouldCalculatePriceReturnForTwoPrices() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        
        // When
        double priceReturn = calculator.calculatePriceReturn(prices);
        
        // Then
        assertEquals(0.1, priceReturn, 0.001);
    }

    @Test
    void shouldCalculatePriceReturnForMultiplePrices() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 105.0, 95.0, 120.0);
        
        // When
        double priceReturn = calculator.calculatePriceReturn(prices);
        
        // Then
        assertEquals(0.2, priceReturn, 0.001);
    }

    @Test
    void shouldThrowExceptionWhenPricesListIsNull() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePriceReturn(null);
        });
    }

    @Test
    void shouldCalculateTotalReturnWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        List<Dividend> dividends = List.of();
        
        // When
        double totalReturn = calculator.calculateTotalReturn(prices, dividends);
        
        // Then
        assertEquals(0.1, totalReturn, 0.001);
    }

    @Test
    void shouldCalculateTotalReturnWithSingleDividend() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(System.currentTimeMillis() / 1000); // timestamp in seconds
        List<Dividend> dividends = List.of(dividend);
        
        // When
        double totalReturn = calculator.calculateTotalReturn(prices, dividends);
        
        // Then
        // Price return: 10%, Dividend yield: 2%, Total return should be higher than price return
        assertTrue(totalReturn > 0.1);
        assertEquals(0.12, totalReturn, 0.001); // 10% price + 2% dividend
    }

    @Test
    void shouldCalculateCAGRForOneYear() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        double startValue = 100.0;
        double endValue = 110.0;
        int years = 1;
        
        // When
        double cagr = calculator.calculateCAGR(startValue, endValue, years);
        
        // Then
        assertEquals(0.1, cagr, 0.001); // 10% for 1 year
    }

    @Test
    void shouldCalculateCAGRForMultipleYears() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        double startValue = 100.0;
        double endValue = 121.0; // 21% total return over 2 years
        int years = 2;
        
        // When
        double cagr = calculator.calculateCAGR(startValue, endValue, years);
        
        // Then
        assertEquals(0.1, cagr, 0.001); // 10% CAGR (sqrt(1.21) - 1 = 0.1)
    }

    @Test
    void shouldCalculateCumulativeReturnsWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 121.0);
        List<Dividend> dividends = List.of();
        
        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, dividends);
        
        // Then
        assertEquals(3, cumulativeReturns.size());
        assertEquals(0.0, cumulativeReturns.get(0), 0.001); // Starting point
        assertEquals(0.1, cumulativeReturns.get(1), 0.001); // 10% return
        assertEquals(0.21, cumulativeReturns.get(2), 0.001); // 21% cumulative return
    }
}
