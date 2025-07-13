package portfolio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReturnCalculatorAmountTest {

    private ReturnCalculator returnCalculator;

    @BeforeEach
    void setUp() {
        returnCalculator = new ReturnCalculator();
    }

    @Test
    void shouldCalculateAmountChangesFromInitialAmount() {
        // Given
        List<Double> prices = List.of(100.0, 110.0, 105.0, 120.0);
        double initialAmount = 10000.0;

        // When
        List<Double> amountChanges = returnCalculator.calculateAmountChanges(prices, initialAmount);

        // Then
        assertEquals(4, amountChanges.size());
        assertEquals(10000.0, amountChanges.get(0), 0.01); // Initial amount
        assertEquals(11000.0, amountChanges.get(1), 0.01); // 10% increase
        assertEquals(10500.0, amountChanges.get(2), 0.01); // 5% increase from initial
        assertEquals(12000.0, amountChanges.get(3), 0.01); // 20% increase from initial
    }

    @Test
    void shouldThrowExceptionWhenPricesIsNull() {
        // Given
        double initialAmount = 10000.0;

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> returnCalculator.calculateAmountChanges(null, initialAmount));
    }

    @Test
    void shouldThrowExceptionWhenPricesIsEmpty() {
        // Given
        List<Double> prices = List.of();
        double initialAmount = 10000.0;

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> returnCalculator.calculateAmountChanges(prices, initialAmount));
    }

    @Test
    void shouldThrowExceptionWhenInitialAmountIsNegative() {
        // Given
        List<Double> prices = List.of(100.0, 110.0);
        double initialAmount = -1000.0;

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> returnCalculator.calculateAmountChanges(prices, initialAmount));
    }
}
