package portfolio.util;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DateUtilsTest {

    @Test
    void testToUnixTimestamp_LocalDate() {
        // 2024-07-06 should convert to 1720224000L
        LocalDate date = LocalDate.of(2024, 7, 6);
        long expected = 1720224000L;
        long actual = DateUtils.toUnixTimeSeconds(date);
        assertEquals(expected, actual);
    }

    @Test
    void testToUnixTimestamp_LocalDateTime() {
        // 2024-07-06 12:00:00 should convert to 1720267200L
        LocalDateTime dateTime = LocalDateTime.of(2024, 7, 6, 12, 0, 0);
        long expected = 1720267200L;
        long actual = DateUtils.toUnixTimeSeconds(dateTime);
        assertEquals(expected, actual);
    }

    @Test
    void testToUnixTimestamp_String() {
        // "2024-07-06" should convert to 1720224000L
        String dateString = "2024-07-06";
        long expected = 1720224000L;
        long actual = DateUtils.toUnixTimeSeconds(dateString);
        assertEquals(expected, actual);
    }

    @Test
    void testTodayUnixTimestamp() {
        long today = DateUtils.todayUnixTimeSeconds();
        long expectedToday = DateUtils.toUnixTimeSeconds(LocalDate.now());
        assertEquals(expectedToday, today);
    }

    @Test
    void testDaysAgoUnixTimestamp() {
        int daysAgo = 7;
        long sevenDaysAgo = DateUtils.daysAgoUnixTimeSeconds(daysAgo);
        long expected = DateUtils.toUnixTimeSeconds(LocalDate.now().minusDays(daysAgo));
        assertEquals(expected, sevenDaysAgo);
    }

    @Test
    void test() {
        log.info("{}", DateUtils.toLocalDate(1420088400));
    }


}
