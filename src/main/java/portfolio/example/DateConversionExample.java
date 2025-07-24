package portfolio.example;

import portfolio.util.DateUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Example class demonstrating how to convert dates to unix timestamps
 * for use with period1 parameter in StockFetcher
 */
public class DateConversionExample {
    
    public static void main(String[] args) {
        System.out.println("=== Date to Unix Timestamp Conversion Examples ===\n");
        
        // Example 1: Convert specific date to unix timestamp
        LocalDate date = LocalDate.of(2024, 7, 6);
        long period1 = DateUtils.toUnixTimeSeconds(date);
        System.out.println("Date: " + date + " -> Unix timestamp: " + period1);
        
        // Example 2: Convert date string to unix timestamp
        String dateString = "2024-07-08";
        long period2 = DateUtils.toUnixTimeSeconds(dateString);
        System.out.println("Date string: " + dateString + " -> Unix timestamp: " + period2);
        
        // Example 3: Get unix timestamp for today
        long today = DateUtils.todayUnixTimeSeconds();
        System.out.println("Today -> Unix timestamp: " + today);
        
        // Example 4: Get unix timestamp for 30 days ago
        long thirtyDaysAgo = DateUtils.daysAgoUnixTimeSeconds(30);
        System.out.println("30 days ago -> Unix timestamp: " + thirtyDaysAgo);
        
        // Example 5: Convert specific date and time
        LocalDateTime dateTime = LocalDateTime.of(2024, 7, 6, 12, 0, 0);
        long period3 = DateUtils.toUnixTimeSeconds(dateTime);
        System.out.println("DateTime: " + dateTime + " -> Unix timestamp: " + period3);
        
        System.out.println("\n=== Usage with StockFetcher ===");
        System.out.println("// Get stock data from July 6, 2024 to July 8, 2024");
        System.out.println("long period1 = DateUtils.toUnixTimestamp(\"2024-07-06\"); // " + DateUtils.toUnixTimeSeconds("2024-07-06"));
        System.out.println("long period2 = DateUtils.toUnixTimestamp(\"2024-07-08\"); // " + DateUtils.toUnixTimeSeconds("2024-07-08"));
        System.out.println("ChartResponse response = fetcher.fetchHistory(\"AAPL\", period1, period2);");
        
        System.out.println("\n// Get stock data from 30 days ago to today");
        System.out.println("long period1 = DateUtils.daysAgoUnixTimestamp(30); // " + DateUtils.daysAgoUnixTimeSeconds(30));
        System.out.println("long period2 = DateUtils.todayUnixTimestamp(); // " + DateUtils.todayUnixTimeSeconds());
        System.out.println("ChartResponse response = fetcher.fetchHistory(\"AAPL\", period1, period2);");
    }
}
