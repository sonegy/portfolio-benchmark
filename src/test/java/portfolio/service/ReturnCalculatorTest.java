package portfolio.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;
import static portfolio.util.DateUtils.toUnixTimeSeconds;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.ReturnRate;

@Slf4j
@ActiveProfiles("test")
class ReturnCalculatorTest {

    @Test
    void shouldCalculateBeta() {
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> etfReturns = List.of(0.02, 0.01, -0.01, 0.03, 0.015);
        List<Double> marketReturns = List.of(0.018, 0.012, -0.008, 0.028, 0.017);

        double beta = calculator.calculateBeta(etfReturns, marketReturns);

        assertTrue(beta > 0.5 && beta < 1.5); // 예시: 실제 값은 샘플 데이터에 따라 다름
    }


    @Test
    void shouldCalculateVolatilityWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 121.0); // 월별 가격
        List<Long> timestamps = List.of(
                toUnixTimeSeconds(LocalDate.of(2023, 1, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 2, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 3, 1)));

        // When
        List<ReturnRate> periodicReturnRates = calculator.calculatePeriodicReturnRates(prices, timestamps);
        log.info("periodicReturnRate {}", periodicReturnRates);
        double volatility = calculator.calculateVolatility(periodicReturnRates);
        log.info("volatility {}", volatility);

        // Then
        // 월별 수익률: (110-100)/100=0.1, (121-110)/110=0.1
        // 평균: 0.1
        // 분산: ((0.1-0.1)^2 + (0.1-0.1)^2)/2 = 0
        // 표준편차: 0
        assertEquals(0.0, volatility, 0.0001);
    }

    @Test
    void shouldCalculateVolatility_Given_VOO_History() {
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(
                443.82000732421875, 466.92999267578125, 480.70001220703125, 461.42999267578125,
                484.6199951171875, 500.1300048828125, 505.92999267578125, 518.0399780273438, 527.6699829101562,
                522.6699829101562, 553.4500122070312, 538.8099975585938);
        List<Long> timestamps = List.of(
                toUnixTimeSeconds(LocalDate.of(2023, 1, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 2, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 3, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 4, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 5, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 6, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 7, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 8, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 9, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 10, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 11, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 12, 1)));

        // When
        List<ReturnRate> periodicReturnRates = calculator.calculatePeriodicReturnRates(prices, timestamps);
        log.info("periodicReturnRate {}", periodicReturnRates);
        double volatility = calculator.calculateVolatility(periodicReturnRates);
        log.info("volatility {}", volatility);
    }

    @Test
    void shouldCalculatePriceReturnForTwoPrices() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0);

        // When
        double priceReturn = calculator.calculatePriceReturn(prices).rate();

        // Then
        assertEquals(0.1, priceReturn, 0.001);
    }

    @Test
    void shouldCalculatePriceReturnForMultiplePrices() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 105.0, 95.0, 120.0);

        // When
        double priceReturn = calculator.calculatePriceReturn(prices).rate();

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
        double totalReturn = calculator.calculateTotalReturn(prices, timestamps, dividends).rate();

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
                LocalDate.of(2023, 2, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(LocalDate.of(2023, 2, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);

        // When
        double totalReturn = calculator.calculateTotalReturn(prices, timestamps, dividends).rate();
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);
        double lastCumulativeReturn = cumulativeReturns.get(cumulativeReturns.size() - 1).rate();

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
        double cagr = calculator.calculateCAGR(startValue, endValue, years).rate();

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
        double cagr = calculator.calculateCAGR(startValue, endValue, years).rate();

        // Then
        assertEquals(0.1, cagr, 0.001); // 10% CAGR (sqrt(1.21) - 1 = 0.1)
    }

    @Test
    void shouldCalculateCumulativeReturnsWithoutDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 121.0);
        List<Long> timestamps = List.of(
                toUnixTimeSeconds(LocalDate.of(2023, 1, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 2, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 3, 1)));
        List<Dividend> dividends = List.of();

        // When
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        assertEquals(3, cumulativeReturns.size());
        assertEquals(0.0, cumulativeReturns.get(0).rate(), 0.001); // Starting point
        assertEquals(0.1, cumulativeReturns.get(1).rate(), 0.001); // 10% return
        assertEquals(0.21, cumulativeReturns.get(2).rate(), 0.001); // 21% cumulative return
    }

    @Test
    void shouldCalculateCumulativeReturnsWithDividends() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0, 108.0); // Price drops after dividend ex-date
        List<Long> timestamps = List.of(
                toUnixTimeSeconds(LocalDate.of(2023, 1, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 2, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 3, 1)));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(toUnixTimeSeconds(LocalDate.of(2023, 2, 3)));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        assertEquals(3, cumulativeReturns.size());
        // Day 1: Start
        assertEquals(0.0, cumulativeReturns.get(0).rate(), 0.001);

        // Day 2: Price is 110. Return is (110-100)/100 = 0.1
        // Dividend of 2.0 is paid. Cash becomes 2.0.
        // Reinvest at 110: 2.0 / 110 = 0.01818 shares. Total shares = 1.01818
        // Value = 1.01818 * 110 = 112. Return = (112-100)/100 = 0.12
        assertEquals(0.12, cumulativeReturns.get(1).rate(), 0.001);

        // Day 3: Price is 108.
        // Value = 1.01818 * 108 = 109.96344
        // Return = (109.96344 - 100) / 100 = 0.0996
        assertEquals(0.0996, cumulativeReturns.get(2).rate(), 0.001);
    }

    @Test
    void shouldHandleDividendPaidBetweenPricePoints() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0); // Prices are for Day 1 and Day 3
        List<Long> timestamps = List.of(
                toUnixTimeSeconds(LocalDate.of(2023, 1, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 2, 3)));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        // Dividend is paid on Day 2, where there is no price point
        dividend.setDate(toUnixTimeSeconds(LocalDate.of(2023, 2, 2)));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        assertEquals(2, cumulativeReturns.size());
        // Day 1: Start
        assertEquals(0.0, cumulativeReturns.get(0).rate(), 0.001);

        // Day 3: Dividend from Day 2 is reinvested at Day 3's price (110)
        // Cash from dividend = 1 share * 2.0 = 2.0
        // New shares = 2.0 / 110 = 0.01818. Total shares = 1.01818
        // Value = 1.01818 * 110 = 112.0
        // Return = (112.0 - 100) / 100 = 0.12
        assertEquals(0.12, cumulativeReturns.get(1).rate(), 0.001);
    }

    @Test
    void shouldHandleMultipleDividendsBetweenPricePoints() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 110.0); // Day 1 and Day 4
        List<Long> timestamps = List.of(
                toUnixTimeSeconds(LocalDate.of(2023, 1, 1)),
                toUnixTimeSeconds(LocalDate.of(2023, 2, 4)));

        Dividend div1 = new Dividend();
        div1.setAmount(2.0);
        div1.setDate(LocalDate.of(2023, 2, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        Dividend div2 = new Dividend();
        div2.setAmount(3.0);
        div2.setDate(LocalDate.of(2023, 2, 3).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        List<Dividend> dividends = List.of(div1, div2);

        // When
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        // Cash from dividends = 1 share * (2.0 + 3.0) = 5.0
        // Reinvest at 110: 5.0 / 110 = 0.04545 shares. Total shares = 1.04545
        // Value = 1.04545 * 110 = 115.0
        // Return = (115.0 - 100) / 100 = 0.15
        assertEquals(0.15, cumulativeReturns.get(1).rate(), 0.001);
    }

    @Test
    void shouldNotReinvestIfPriceIsZero() {
        // Given
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(100.0, 0.0); // Price drops to zero
        List<Long> timestamps = List.of(
                LocalDate.of(2023, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                LocalDate.of(2023, 2, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));

        Dividend dividend = new Dividend();
        dividend.setAmount(2.0);
        dividend.setDate(LocalDate.of(2023, 2, 2).atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        List<Dividend> dividends = List.of(dividend);

        // When
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        // Day 2: Dividend is paid, cash becomes 2.0. Price is 0, so no reinvestment
        // occurs.
        // Value = 1 share * 0 price = 0.
        // Return = (0 - 100) / 100 = -1.0
        assertEquals(-1.0, cumulativeReturns.get(1).rate(), 0.001);
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
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, null);

        // Then
        assertEquals(0.1, cumulativeReturns.get(1).rate(), 0.001);
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
        List<ReturnRate> cumulativeReturns = calculator.calculateCumulativeReturns(prices, timestamps, dividends);

        // Then
        // Day 1: Dividend paid. Cash = 2.0. Reinvest at 100. New shares = 2/100 = 0.02.
        // Total shares = 1.02
        // Value = 1.02 * 100 = 102. Return = (102-100)/100 = 0.02
        assertEquals(0.02, cumulativeReturns.get(0).rate(), 0.001);

        // Day 2: No dividend. Value = 1.02 * 110 = 112.2. Return = (112.2-100)/100 =
        // 0.122
        assertEquals(0.122, cumulativeReturns.get(1).rate(), 0.001);
    }

    @Test
    void shouldCalculateMaxDrawdowns() {
        ReturnCalculator calculator = new ReturnCalculator();
        // 예시: [100, 120, 110, 130, 90, 95]
        List<Double> prices = List.of(100.0, 120.0, 110.0, 130.0, 90.0, 95.0);
        List<Double> expected = List.of(
                0.0, // 100 -> peak=100, drawdown=0
                0.0, // 120 -> peak=120, drawdown=0
                0.08333333, // 110 -> peak=120, drawdown=(120-110)/120
                0.0, // 130 -> peak=130, drawdown=0
                0.30769231, // 90 -> peak=130, drawdown=(130-90)/130
                0.26923077 // 95 -> peak=130, drawdown=(130-95)/130
        );
        List<Double> result = calculator.calculateMaxDrawdowns(prices);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i), 1e-6,
                    "index=" + i + ", price=" + prices.get(i));
        }
    }

    @Test
    void shouldHandleZeroStartPriceInMaxDrawdowns() {
        ReturnCalculator calculator = new ReturnCalculator();
        List<Double> prices = List.of(0.0, 1.0, 2.0);
        List<Double> expected = List.of(0.0, 0.0, 0.0);
        List<Double> result = calculator.calculateMaxDrawdowns(prices);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i), 1e-6, "index=" + i + ", price=" + prices.get(i));
        }
    }
}
