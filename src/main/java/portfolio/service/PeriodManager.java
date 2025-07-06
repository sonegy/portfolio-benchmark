package portfolio.service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PeriodManager {
    
    public void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
    
    public int calculateTradingDays(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        int tradingDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                tradingDays++;
            }
            current = current.plusDays(1);
        }
        
        return tradingDays;
    }
    
    public List<LocalDate> sortDates(List<LocalDate> dates) {
        if (dates == null) {
            throw new IllegalArgumentException("Dates list cannot be null");
        }
        
        return dates.stream()
            .sorted()
            .toList();
    }
}
