package portfolio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import portfolio.api.ChartResponse.Dividend;

class ReturnCalculatorAmountTest {

    private ReturnCalculator returnCalculator;

    @BeforeEach
    void setUp() {
        returnCalculator = new ReturnCalculator();
    }

    @Test
    void shouldCalculateAmountChangesWithoutDividends() {
        // Given
        List<Double> prices = List.of(100.0, 110.0, 120.0);
        List<Long> timestamps = List.of(
            LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
            LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
            LocalDate.of(2023, 1, 3).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        );
        double initialAmount = 1000.0;

        // When
        List<Double> amountChanges = returnCalculator.calculateAmountChanges(prices, timestamps, new ArrayList<>(), initialAmount);

        // Then
        assertEquals(1000.0, amountChanges.get(0), 0.001);
        assertEquals(1100.0, amountChanges.get(1), 0.001);
        assertEquals(1200.0, amountChanges.get(2), 0.001);
    }

    @Test
    void shouldCalculateAmountChangesWithDividends() {
        // Given
        List<Double> prices = List.of(100.0, 110.0, 108.0);
        List<Long> timestamps = List.of(
            LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
            LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
            LocalDate.of(2023, 1, 3).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        );
        Dividend dividend = new Dividend();
        dividend.setAmount(2.0); // Dividend per share
        dividend.setDate(LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);
        double initialAmount = 1000.0;

        // When
        List<Double> amountChanges = returnCalculator.calculateAmountChanges(prices, timestamps, dividends, initialAmount);

        // Then
        // Day 1: Initial amount is 1000.0. Shares = 1000 / 100 = 10.
        assertEquals(1000.0, amountChanges.get(0), 0.001);

        // Day 2: Price is 110. Dividend of 2.0/share is paid.
        // Cash from dividend = 10 shares * 2.0 = 20.0.
        // Reinvest cash: 20.0 / 110 = 0.181818... shares. Total shares = 10.181818...
        // Value = 10.181818... * 110 = 1120.0
        assertEquals(1120.0, amountChanges.get(1), 0.001);

        // Day 3: Price is 108.
        // Value = 10.181818... * 108 = 1099.636
        assertEquals(1099.636, amountChanges.get(2), 0.001);
    }

    @Test
    void shouldCalculateAmountChangesWithWeightingAndDividends() {
        // Given
        List<Double> prices = List.of(100.0, 110.0);
        List<Long> timestamps = List.of(
            LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
            LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        );
        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);
        double initialAmount = 1000.0;
        double weight = 0.5; // 50% allocated

        // When
        List<Double> amountChanges = returnCalculator.calculateAmountChanges(prices, timestamps, dividends, initialAmount, weight);

        // Then
        // Day 1: Allocated amount = 1000 * 0.5 = 500.0. Shares = 500 / 100 = 5.
        assertEquals(500.0, amountChanges.get(0), 0.001);

        // Day 2: Price is 110. Dividend of 2.0/share.
        // Cash = 5 shares * 2.0 = 10.0.
        // Reinvest: 10.0 / 110 = 0.090909... shares. Total shares = 5.090909...
        // Value = 5.090909... * 110 = 560.0
        assertEquals(560.0, amountChanges.get(1), 0.001);
    }

    @Test
    void shouldHandleEmptyDividendsList() {
        // Given
        List<Double> prices = List.of(100.0, 110.0);
        List<Long> timestamps = List.of(
            LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
            LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        );
        double initialAmount = 1000.0;

        // When
        List<Double> amountChanges = returnCalculator.calculateAmountChanges(prices, timestamps, new ArrayList<>(), initialAmount);

        // Then
        assertEquals(1000.0, amountChanges.get(0), 0.001);
        assertEquals(1100.0, amountChanges.get(1), 0.001);
    }
}
