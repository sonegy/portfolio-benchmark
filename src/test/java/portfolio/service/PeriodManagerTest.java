package portfolio.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;

class PeriodManagerTest {

    @Test
    void shouldValidatePeriodWhenStartDateIsBeforeEndDate() {
        // Given
        PeriodManager periodManager = new PeriodManager();
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        // When & Then
        assertDoesNotThrow(() -> {
            periodManager.validatePeriod(startDate, endDate);
        });
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // Given
        PeriodManager periodManager = new PeriodManager();
        LocalDate startDate = LocalDate.of(2023, 12, 31);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            periodManager.validatePeriod(startDate, endDate);
        });
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsNull() {
        // Given
        PeriodManager periodManager = new PeriodManager();
        LocalDate startDate = null;
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            periodManager.validatePeriod(startDate, endDate);
        });
    }

    @Test
    void shouldCalculateTradingDaysForWeekdays() {
        // Given
        PeriodManager periodManager = new PeriodManager();
        LocalDate startDate = LocalDate.of(2023, 1, 2); // Monday
        LocalDate endDate = LocalDate.of(2023, 1, 6);   // Friday
        
        // When
        int tradingDays = periodManager.calculateTradingDays(startDate, endDate);
        
        // Then
        assertEquals(5, tradingDays); // Monday to Friday = 5 trading days
    }

    @Test
    void shouldCalculateTradingDaysExcludingWeekends() {
        // Given
        PeriodManager periodManager = new PeriodManager();
        LocalDate startDate = LocalDate.of(2023, 1, 2); // Monday
        LocalDate endDate = LocalDate.of(2023, 1, 8);   // Sunday (includes weekend)
        
        // When
        int tradingDays = periodManager.calculateTradingDays(startDate, endDate);
        
        // Then
        assertEquals(5, tradingDays); // Monday to Friday only, excluding Sat & Sun
    }

    @Test
    void shouldSortDatesInAscendingOrder() {
        // Given
        PeriodManager periodManager = new PeriodManager();
        List<LocalDate> unsortedDates = Arrays.asList(
            LocalDate.of(2023, 3, 15),
            LocalDate.of(2023, 1, 10),
            LocalDate.of(2023, 2, 20)
        );
        
        // When
        List<LocalDate> sortedDates = periodManager.sortDates(unsortedDates);
        
        // Then
        assertEquals(LocalDate.of(2023, 1, 10), sortedDates.get(0));
        assertEquals(LocalDate.of(2023, 2, 20), sortedDates.get(1));
        assertEquals(LocalDate.of(2023, 3, 15), sortedDates.get(2));
    }
}
