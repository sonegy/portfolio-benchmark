package portfolio.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntervalCalculatorTest {

    @Test
    void shouldReturn1dForShortPeriod() {
        // given: 1일 기간 (86400초)
        long period1 = 1720224000L;
        long period2 = 1720310400L; // 1일 후
        
        // when
        String interval = IntervalCalculator.calculateOptimalInterval(period1, period2);
        
        // then
        assertEquals("1d", interval);
    }

    @Test
    void shouldReturn1dForWeekPeriod() {
        // given: 7일 기간
        long period1 = 1720224000L;
        long period2 = 1720828800L; // 7일 후
        
        // when
        String interval = IntervalCalculator.calculateOptimalInterval(period1, period2);
        
        // then
        assertEquals("1d", interval);
    }

    @Test
    void shouldReturn1dForMonthPeriod() {
        // given: 30일 기간
        long period1 = 1720224000L;
        long period2 = 1722816000L; // 30일 후
        
        // when
        String interval = IntervalCalculator.calculateOptimalInterval(period1, period2);
        
        // then
        assertEquals("1d", interval);
    }

    @Test
    void shouldReturn5dForThreeMonthPeriod() {
        // given: 90일 기간
        long period1 = 1720224000L;
        long period2 = 1728000000L; // 약 90일 후
        
        // when
        String interval = IntervalCalculator.calculateOptimalInterval(period1, period2);
        
        // then
        assertEquals("5d", interval);
    }

    @Test
    void shouldReturn1moForOneYearPeriod() {
        // given: 365일 기간
        long period1 = 1720224000L;
        long period2 = 1751760000L; // 약 365일 후
        
        // when
        String interval = IntervalCalculator.calculateOptimalInterval(period1, period2);
        
        // then
        assertEquals("1mo", interval);
    }

    @Test
    void shouldReturn1moForLongPeriod() {
        // given: 5년 기간 (1825일)
        long period1 = 1720224000L;
        long period2 = 1877904000L; // 약 5년 후
        
        // when
        String interval = IntervalCalculator.calculateOptimalInterval(period1, period2);
        
        // then
        assertEquals("1mo", interval);
    }
}
