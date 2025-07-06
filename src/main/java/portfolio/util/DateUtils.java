package portfolio.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    
    /**
     * Convert LocalDate to unix timestamp (seconds since epoch)
     * @param date LocalDate to convert
     * @return unix timestamp as long
     */
    public static long toUnixTimestamp(LocalDate date) {
        return date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }
    
    /**
     * Convert LocalDateTime to unix timestamp (seconds since epoch)
     * @param dateTime LocalDateTime to convert
     * @return unix timestamp as long
     */
    public static long toUnixTimestamp(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
    
    /**
     * Convert date string (yyyy-MM-dd format) to unix timestamp
     * @param dateString date in yyyy-MM-dd format
     * @return unix timestamp as long
     */
    public static long toUnixTimestamp(String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        return toUnixTimestamp(date);
    }
    
    /**
     * Get unix timestamp for start of today
     * @return unix timestamp as long
     */
    public static long todayUnixTimestamp() {
        return toUnixTimestamp(LocalDate.now());
    }
    
    /**
     * Get unix timestamp for N days ago
     * @param daysAgo number of days to subtract from today
     * @return unix timestamp as long
     */
    public static long daysAgoUnixTimestamp(int daysAgo) {
        return toUnixTimestamp(LocalDate.now().minusDays(daysAgo));
    }
}
