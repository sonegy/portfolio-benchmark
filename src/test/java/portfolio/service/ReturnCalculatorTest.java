package portfolio.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static portfolio.util.DateUtils.toUnixTimestamp;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import portfolio.api.ChartResponse.Dividend;

@ActiveProfiles("test")
class ReturnCalculatorTest {

    @Test
    void shouldCalculateVolatilityWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 121.0); // 월별 가격
        List<Long> timestamps = List.of(
            toUnixTimestamp(LocalDate.of(2023, 1, 1)),
            toUnixTimestamp(LocalDate.of(2023, 2, 1)),
            toUnixTimestamp(LocalDate.of(2023, 3, 1))
        );
        List<Dividend> dividends = List.of();

        // When
        List<Double> returns = calculator.calculateReturn(prices, timestamps, dividends);
        double volatility = calculator.calculateVolatility(returns);

        // Then
        // 월별 수익률: (110-100)/100=0.1, (121-110)/110=0.1
        // 평균: 0.1
        // 분산: ((0.1-0.1)^2 + (0.1-0.1)^2)/2 = 0
        // 표준편차: 0
        assertEquals(0.0, volatility, 0.0001);
    }

    @Test
    void shouldCalculateVolatilityWithDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 108.0);
        List<Long> timestamps = List.of(
            toUnixTimestamp(LocalDate.of(2023, 1, 1)),
            toUnixTimestamp(LocalDate.of(2023, 2, 1)),
            toUnixTimestamp(LocalDate.of(2023, 3, 1))
        );
        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(toUnixTimestamp(LocalDate.of(2023, 2, 3)));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<Double> returns = calculator.calculateReturn(prices, timestamps, dividends);
        double volatility = calculator.calculateVolatility(returns);

        // Then
        // 수익률 수동 계산:
        // 1. 첫달: 100→110, 배당 없음. 수익률 = (110-100)/100 = 0.1
        // 2. 둘째달: 110→108, 배당 2.0 재투자. 현금 2.0/108=0.018518...주 추가, 총 1.018518...주
        // 가치: 1.018518...*108=110
        // 수익률: (110-110)/110 = 0
        // 평균: (0.1+0)/2=0.05
        // 분산: ((0.1-0.05)^2 + (0-0.05)^2)/2 = (0.0025+0.0025)/2=0.0025
        // 표준편차: sqrt(0.0025)=0.05
        assertEquals(0.05, volatility, 0.0001);
    }

    @Test
    void shouldCalculatePriceReturnForTwoPrices() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);

        // When
        double priceReturn = calculator.calculatePriceReturn(prices);

        // Then
        assertEquals(0.1, priceReturn, 0.001);
    }

    @Test
    void shouldCalculatePriceReturnForMultiplePrices() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 105.0, 95.0, 120.0);

        // When
        double priceReturn = calculator.calculatePriceReturn(prices);

        // Then
        assertEquals(0.2, priceReturn, 0.001);
    }

    @Test
    void shouldThrowExceptionWhenPricesListIsNull() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePriceReturn(null);
        });
    }

    @Test
    void shouldCalculateTotalReturnWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        List<Long> timestamps = List.of(
                LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of();

        // When
        double totalReturn = calculator.calculateTotalReturn(prices, timestamps, dividends);

        // Then
        assertEquals(0.1, totalReturn, 0.001);
    }

    @Test
    void shouldCalculateTotalReturnWithSingleDividend() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        List<Long> timestamps = List.of(
                LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);

        // When
        double totalReturn = calculator.calculateTotalReturn(prices, timestamps, dividends);
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);
        double lastCumulativeReturn = cumulativeReturns.get(cumulativeReturns.size() - 1);

        // Then
        // Total return should be consistent with the final cumulative return, which
        // includes reinvestment.
        assertEquals((110.0 + 2.0 - 100.0) / 100.0, totalReturn, 0.001);
        assertEquals(lastCumulativeReturn, totalReturn, 0.001);
    }

    @Test
    void shouldCalculateCAGRForOneYear() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        double startValue = 100.0;
        double endValue = 110.0;
        int years = 1;

        // When
        double cagr = calculator.calculateCAGR(startValue, endValue, years);

        // Then
        assertEquals(0.1, cagr, 0.001); // 10% for 1 year
    }

    @Test
    void shouldCalculateCAGRForMultipleYears() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        double startValue = 100.0;
        double endValue = 121.0; // 21% total return over 2 years
        int years = 2;

        // When
        double cagr = calculator.calculateCAGR(startValue, endValue, years);

        // Then
        assertEquals(0.1, cagr, 0.001); // 10% CAGR (sqrt(1.21) - 1 = 0.1)
    }

    @Test
    void shouldCalculateCumulativeReturnsWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 121.0);
        List<Long> timestamps = List.of(
                toUnixTimestamp(LocalDate.of(2023, 1, 1)),
                toUnixTimestamp(LocalDate.of(2023, 1, 2)),
                toUnixTimestamp(LocalDate.of(2023, 1, 3)));
        List<Dividend> dividends = List.of();

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        assertEquals(3, cumulativeReturns.size());
        assertEquals(0.0, cumulativeReturns.get(0), 0.001); // Starting point
        assertEquals(0.1, cumulativeReturns.get(1), 0.001); // 10% return
        assertEquals(0.21, cumulativeReturns.get(2), 0.001); // 21% cumulative return
    }

    @Test
    void shouldCalculateCumulativeReturnsWithDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 108.0); // Price drops after dividend ex-date
        List<Long> timestamps = List.of(
                toUnixTimestamp(LocalDate.of(2023, 1, 1)),
                toUnixTimestamp(LocalDate.of(2023, 1, 5)),
                toUnixTimestamp(LocalDate.of(2023, 1, 10)));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(toUnixTimestamp(LocalDate.of(2023, 1, 3)));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        assertEquals(3, cumulativeReturns.size());
        // Day 1: Start
        assertEquals(0.0, cumulativeReturns.get(0), 0.001);

        // Day 2: Price is 110. Return is (110-100)/100 = 0.1
        // Dividend of 2.0 is paid. Cash becomes 2.0.
        // Reinvest at 110: 2.0 / 110 = 0.01818 shares. Total shares = 1.01818
        // Value = 1.01818 * 110 = 112. Return = (112-100)/100 = 0.12
        assertEquals(0.12, cumulativeReturns.get(1), 0.001);

        // Day 3: Price is 108.
        // Value = 1.01818 * 108 = 109.96344
        // Return = (109.96344 - 100) / 100 = 0.0996
        assertEquals(0.0996, cumulativeReturns.get(2), 0.001);
    }

    @Test
    void shouldHandleDividendPaidBetweenPricePoints() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0); // Prices are for Day 1 and Day 3
        List<Long> timestamps = List.of(
                toUnixTimestamp(LocalDate.of(2023, 1, 1)),
                toUnixTimestamp(LocalDate.of(2023, 1, 3)));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        // Dividend is paid on Day 2, where there is no price point
        dividend.setDate(toUnixTimestamp(LocalDate.of(2023, 1, 2)));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        assertEquals(2, cumulativeReturns.size());
        // Day 1: Start
        assertEquals(0.0, cumulativeReturns.get(0), 0.001);

        // Day 3: Dividend from Day 2 is reinvested at Day 3's price (110)
        // Cash from dividend = 1 share * 2.0 = 2.0
        // New shares = 2.0 / 110 = 0.01818. Total shares = 1.01818
        // Value = 1.01818 * 110 = 112.0
        // Return = (112.0 - 100) / 100 = 0.12
        assertEquals(0.12, cumulativeReturns.get(1), 0.001);
    }

    @Test
    void shouldHandleMultipleDividendsBetweenPricePoints() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0); // Day 1 and Day 4
        List<Long> timestamps = List.of(
                toUnixTimestamp(LocalDate.of(2023, 1, 1)),
                toUnixTimestamp(LocalDate.of(2023, 1, 4)));

        Dividend div1 = new Dividend();
        div1.setAmount(2.0);
        div1.setDate(LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        Dividend div2 = new Dividend();
        div2.setAmount(3.0);
        div2.setDate(LocalDate.of(2023, 1, 3).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        List<Dividend> dividends = List.of(div1, div2);

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        // Cash from dividends = 1 share * (2.0 + 3.0) = 5.0
        // Reinvest at 110: 5.0 / 110 = 0.04545 shares. Total shares = 1.04545
        // Value = 1.04545 * 110 = 115.0
        // Return = (115.0 - 100) / 100 = 0.15
        assertEquals(0.15, cumulativeReturns.get(1), 0.001);
    }

    @Test
    void shouldNotReinvestIfPriceIsZero() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 0.0); // Price drops to zero
        List<Long> timestamps = List.of(
                LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        // Day 2: Dividend is paid, cash becomes 2.0. Price is 0, so no reinvestment
        // occurs.
        // Value = 1 share * 0 price = 0.
        // Return = (0 - 100) / 100 = -1.0
        assertEquals(-1.0, cumulativeReturns.get(1), 0.001);
    }

    @Test
    void shouldHandleNoDividendsGracefully() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        List<Long> timestamps = List.of(
                LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, null);

        // Then
        assertEquals(0.1, cumulativeReturns.get(1), 0.001);
    }

    @Test
    void shouldHandleDividendOnFirstDay() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);
        List<Long> timestamps = List.of(
                LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                LocalDate.of(2023, 1, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<Double> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        // Day 1: Dividend paid. Cash = 2.0. Reinvest at 100. New shares = 2/100 = 0.02.
        // Total shares = 1.02
        // Value = 1.02 * 100 = 102. Return = (102-100)/100 = 0.02
        assertEquals(0.02, cumulativeReturns.get(0), 0.001);

        // Day 2: No dividend. Value = 1.02 * 110 = 112.2. Return = (112.2-100)/100 =
        // 0.122
        assertEquals(0.122, cumulativeReturns.get(1), 0.001);
    }
}
