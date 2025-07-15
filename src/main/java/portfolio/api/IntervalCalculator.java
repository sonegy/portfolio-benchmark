package portfolio.api;

public class IntervalCalculator {
    
    private static final long SECONDS_PER_DAY = 86400L;
    private static final long DAYS_PER_WEEK = 7L;
    private static final long DAYS_PER_MONTH = 30L;
    private static final long DAYS_PER_QUARTER = 90L;
    private static final long DAYS_PER_YEAR = 365L;
    
    public static String calculateOptimalInterval(long period1, long period2) {
        long periodInSeconds = period2 - period1;
        long periodInDays = periodInSeconds / SECONDS_PER_DAY;
        
        // 목표: 약 50개의 데이터 포인트
        // 1d interval: 1일마다 1개 포인트 → 50일까지
        // 5d interval: 5일마다 1개 포인트 → 250일까지 (50 * 5)
        // 1mo interval: 30일마다 1개 포인트 → 1500일까지 (50 * 30)
        if (periodInDays <= 50) {
            return "1d";
        } else if (periodInDays <= 250) {
            return "5d";
        } else {
            return "1mo";
        }
    }
}
