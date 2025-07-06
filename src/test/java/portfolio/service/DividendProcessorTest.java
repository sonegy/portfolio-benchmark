package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import portfolio.api.ChartResponse.Dividend;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DividendProcessorTest {
    
    private DividendProcessor dividendProcessor;
    
    @BeforeEach
    void setUp() {
        dividendProcessor = new DividendProcessor();
    }
    
    @Test
    void shouldCalculateTotalDividendAmount() {
        // Given
        List<Dividend> dividends = new ArrayList<>();
        Dividend dividend1 = new Dividend();
        dividend1.setAmount(1.0);
        dividend1.setDate(1640995200L); // 2022-01-01
        
        Dividend dividend2 = new Dividend();
        dividend2.setAmount(1.5);
        dividend2.setDate(1648771200L); // 2022-04-01
        
        dividends.add(dividend1);
        dividends.add(dividend2);
        
        // When
        double totalDividends = dividendProcessor.calculateTotalDividends(dividends);
        
        // Then
        assertEquals(2.5, totalDividends, 0.001);
    }
    
    @Test
    void shouldFilterDividendsByDateRange() {
        // Given
        List<Dividend> dividends = new ArrayList<>();
        Dividend dividend1 = new Dividend();
        dividend1.setAmount(1.0);
        dividend1.setDate(1640995200L); // 2022-01-01
        
        Dividend dividend2 = new Dividend();
        dividend2.setAmount(1.5);
        dividend2.setDate(1648771200L); // 2022-04-01
        
        Dividend dividend3 = new Dividend();
        dividend3.setAmount(2.0);
        dividend3.setDate(1672531200L); // 2023-01-01
        
        dividends.add(dividend1);
        dividends.add(dividend2);
        dividends.add(dividend3);
        
        long startDate = 1640995200L; // 2022-01-01
        long endDate = 1656633600L;   // 2022-07-01
        
        // When
        List<Dividend> filteredDividends = dividendProcessor.filterDividendsByDateRange(dividends, startDate, endDate);
        
        // Then
        assertEquals(2, filteredDividends.size());
        assertEquals(1.0, filteredDividends.get(0).getAmount(), 0.001);
        assertEquals(1.5, filteredDividends.get(1).getAmount(), 0.001);
    }
    
    @Test
    void shouldCalculateReinvestedDividendValue() {
        // Given
        List<Dividend> dividends = new ArrayList<>();
        Dividend dividend1 = new Dividend();
        dividend1.setAmount(1.0);
        dividend1.setDate(1640995200L); // 2022-01-01
        
        dividends.add(dividend1);
        
        List<Double> prices = List.of(100.0, 110.0, 120.0); // prices at dividend date and later
        List<Long> timestamps = List.of(1640995200L, 1648771200L, 1656633600L);
        
        // When
        double reinvestedValue = dividendProcessor.calculateReinvestedDividendValue(dividends, prices, timestamps);
        
        // Then
        // Dividend of $1.0 at price $100 buys 0.01 shares
        // At final price of $120, those shares are worth $1.2
        assertEquals(1.2, reinvestedValue, 0.001);
    }
    
    @Test
    void shouldCalculateDividendYield() {
        // Given
        List<Dividend> dividends = new ArrayList<>();
        Dividend dividend1 = new Dividend();
        dividend1.setAmount(1.0);
        dividend1.setDate(1640995200L); // 2022-01-01
        
        Dividend dividend2 = new Dividend();
        dividend2.setAmount(1.0);
        dividend2.setDate(1648771200L); // 2022-04-01
        
        dividends.add(dividend1);
        dividends.add(dividend2);
        
        double averagePrice = 100.0;
        
        // When
        double dividendYield = dividendProcessor.calculateDividendYield(dividends, averagePrice);
        
        // Then
        // Total dividends: $2.0, Average price: $100.0
        // Dividend yield: 2.0 / 100.0 = 0.02 (2%)
        assertEquals(0.02, dividendYield, 0.001);
    }
    
    @Test
    void shouldReturnZeroForNullOrEmptyDividends() {
        // When & Then
        assertEquals(0.0, dividendProcessor.calculateTotalDividends(null), 0.001);
        assertEquals(0.0, dividendProcessor.calculateTotalDividends(new ArrayList<>()), 0.001);
        assertEquals(0.0, dividendProcessor.calculateDividendYield(null, 100.0), 0.001);
        assertEquals(0.0, dividendProcessor.calculateReinvestedDividendValue(null, List.of(100.0), List.of(1640995200L)), 0.001);
    }
    
    @Test
    void shouldReturnZeroForInvalidInputs() {
        // Given
        List<Dividend> dividends = new ArrayList<>();
        Dividend dividend = new Dividend();
        dividend.setAmount(1.0);
        dividend.setDate(1640995200L);
        dividends.add(dividend);
        
        // When & Then
        assertEquals(0.0, dividendProcessor.calculateDividendYield(dividends, 0.0), 0.001);
        assertEquals(0.0, dividendProcessor.calculateDividendYield(dividends, -100.0), 0.001);
        assertEquals(0.0, dividendProcessor.calculateReinvestedDividendValue(dividends, null, List.of(1640995200L)), 0.001);
        assertEquals(0.0, dividendProcessor.calculateReinvestedDividendValue(dividends, List.of(100.0), null), 0.001);
    }
}
