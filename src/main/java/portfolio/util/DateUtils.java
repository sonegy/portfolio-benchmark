package portfolio.util;

import java.time.Instant;
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
    public static long toUnixTimeSeconds(LocalDate date) {
        return date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * Convert LocalDateTime to unix timestamp (seconds since epoch)
     * @param dateTime LocalDateTime to convert
     * @return unix timestamp as long
     */
    public static long toUnixTimeSeconds(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
    
    /**
     * Convert date string (yyyy-MM-dd format) to unix timestamp
     * @param dateString date in yyyy-MM-dd format
     * @return unix timestamp as long
     */
    public static long toUnixTimeSeconds(String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        return toUnixTimeSeconds(date);
    }
    
    /**
     * Get unix timestamp for start of today
     * @return unix timestamp as long
     */
    public static long todayUnixTimeSeconds() {
        return toUnixTimeSeconds(LocalDate.now());
    }
    
    /**
     * Get unix timestamp for N days ago
     * @param daysAgo number of days to subtract from today
     * @return unix timestamp as long
     */
    public static long daysAgoUnixTimeSeconds(int daysAgo) {
        return toUnixTimeSeconds(LocalDate.now().minusDays(daysAgo));
    }

    /**
     * Convert unix timestamp (seconds since epoch) to LocalDate
     * 
     * @param epochSecond unix timestamp as long
     * @return LocalDate
     */
    public static LocalDate toLocalDate(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
