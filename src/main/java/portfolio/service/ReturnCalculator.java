package portfolio.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.Amount;
import portfolio.model.CAGR;
import portfolio.model.ReturnRate;
import portfolio.model.Volatility;
import portfolio.util.JsonLoggingUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

/**
 * 다양한 유형의 투자 수익률을 계산하는 서비스 클래스입니다.
 * 단순 가격 수익률, 배당금을 포함한 총수익률, 연평균 복리 성장률(CAGR), 누적 수익률 등을 계산할 수 있습니다.
 *
 * <p>
 * 이 클래스는 주가, 타임스탬프, 배당금 정보를 바탕으로 투자 성과를 정확하게 산출하는 데 사용됩니다.
 */
@Slf4j
@Service
public class ReturnCalculator {

    /**
     * ETF와 시장 월별 수익률로 베타를 계산합니다.
     * @param etfReturns ETF 월별 수익률
     * @param marketReturns 시장 월별 수익률
     * @return 베타 값
     */
    public double calculateBeta(List<Double> etfReturns, List<Double> marketReturns) {
        if (etfReturns == null || marketReturns == null || etfReturns.size() != marketReturns.size() || etfReturns.size() < 2) {
            throw new IllegalArgumentException("Input lists must be non-null, same size, and have at least 2 elements");
        }
        // finmath-lib 사용
        double[] x = etfReturns.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = marketReturns.stream().mapToDouble(Double::doubleValue).toArray();
        double cov = new Covariance().covariance(x, y);
        double var = new Variance().evaluate(y);
        return cov / var;
    }


    /**
     * 가격 리스트가 null이 아니고 최소 두 개 이상의 가격을 포함하는지 검증합니다.
     *
     * @param prices 검증할 가격 리스트
     * @throws IllegalArgumentException 가격 리스트가 2개 미만일 경우 발생합니다.
     */
    private void validatePricesForReturn(List<Double> prices) {
        if (prices == null || prices.size() < 2) {
            throw new IllegalArgumentException("At least two prices are required");
        }
    }

    /**
     * 가격 리스트의 첫 번째와 마지막 가격을 기준으로 단순 가격 수익률을 계산합니다. (Rate of Return)
     *
     * @param prices 최소 두 개 이상의 가격이 포함된 리스트 (시간순 정렬)
     * @return 가격 수익률(예: 0.1은 10% 수익률)
     * @throws IllegalArgumentException 가격 리스트가 2개 미만일 경우 발생합니다.
     */
    public ReturnRate calculatePriceReturn(List<Double> prices) {
        validatePricesForReturn(prices);
        double startPrice = prices.get(0);
        double endPrice = prices.get(prices.size() - 1);
        return new ReturnRate(startPrice, endPrice);
    }

    /**
     * 가격 변동과 배당 재투자를 모두 반영한 총수익률을 계산합니다. (Rate of Return)
     * 배당금이 제공되지 않으면 단순 가격 수익률을 반환합니다.
     *
     * @param prices     가격 리스트 (시간순 정렬)
     * @param timestamps 각 가격에 대응하는 타임스탬프 리스트
     * @param dividends  배당금 리스트
     * @return 총수익률(소수값, 예: 0.15는 15% 수익률)
     * @throws IllegalArgumentException 가격 리스트가 2개 미만일 경우 발생합니다.
     */
    public ReturnRate calculateTotalReturn(List<Double> prices, List<Long> timestamps, List<Dividend> dividends) {
        validatePricesForReturn(prices);

        if (dividends == null || dividends.isEmpty()) {
            return calculatePriceReturn(prices);
        }

        List<ReturnRate> cumulativeReturns = calculateCumulativeReturns(prices, timestamps, dividends);
        return cumulativeReturns.get(cumulativeReturns.size() - 1);
    }

    /**
     * 연평균 복리 성장률(CAGR, Compound Annual Growth Rate)을 계산합니다.
     *
     * @param startValue 투자 시작 금액
     * @param endValue   투자 종료 금액
     * @param years      투자 기간(년)
     * @return CAGR(소수값, 예: 0.07은 연 7% 성장)
     * @throws IllegalArgumentException 시작값, 종료값, 기간이 0 이하일 경우 발생합니다.
     */
    public CAGR calculateCAGR(double startValue, double endValue, double years) {
        if (startValue <= 0) {
            throw new IllegalArgumentException("Start value must be positive");
        }
        if (endValue <= 0) {
            throw new IllegalArgumentException("End value must be positive");
        }
        if (years <= 0) {
            throw new IllegalArgumentException("Years must be positive");
        }

        // CAGR = (End Value / Start Value)^(1/years) - 1
        return new CAGR(startValue, endValue, years);
    }

    /**
     * 배당 재투자를 가정하여 기간 내 누적 수익률을 계산합니다.
     *
     * <p>
     * 초기 1주를 보유하고, 배당금은 모두 재투자한다고 가정합니다.
     * 가격 데이터 포인트 사이에 지급된 배당도 정확히 반영합니다.
     *
     * @param prices     가격 리스트 (시간순 정렬)
     * @param timestamps 각 가격에 대응하는 타임스탬프 리스트 (시간순 정렬)
     * @param dividends  기간 중 지급된 배당금 리스트 (원본 리스트는 변경되지 않음)
     * @return 각 시점별 누적 수익률 리스트(소수값)
     * @throws IllegalArgumentException 가격, 타임스탬프가 null/비어있거나 크기가 다르거나, 시작 가격이 0 이하인
     *                                  경우
     */
    public List<ReturnRate> calculateCumulativeReturns(List<Double> prices, List<Long> timestamps,
            List<Dividend> dividends) {
        double startPrice = prices.get(0);
        if (startPrice <= 0) {
            throw new IllegalArgumentException("Start price must be positive for cumulative return calculation.");
        }

        List<Amount> cumulativeValues = calculateCumulativeAmounts(prices, timestamps, dividends, 1.0);

        List<ReturnRate> cumulativeReturnRates = new ArrayList<>();
        for (Amount amount : cumulativeValues) {
            ReturnRate returnRate = new ReturnRate(startPrice, amount.amount());
            cumulativeReturnRates.add(returnRate);
        }
        return cumulativeReturnRates;
    }

    /**
     * 배당 재투자를 포함하여 초기 투자 금액이 시간에 따라 어떻게 변화하는지 계산합니다.
     *
     * @param prices        가격 리스트 (시간순 정렬)
     * @param timestamps    각 가격에 대응하는 타임스탬프 리스트
     * @param dividends     기간 중 지급된 배당금 리스트
     * @param initialAmount 초기 투자 금액
     * @return 각 시점별 투자 가치 리스트
     */
    public List<Amount> calculateAmountChanges(List<Double> prices, List<Long> timestamps, List<Dividend> dividends,
            double initialAmount) {
        return calculateCumulativeAmounts(prices, timestamps, dividends, initialAmount, 1.0);
    }

    /**
     * 초기 투자 금액의 일부 비율만 투자했을 때, 배당 재투자를 포함하여 시간에 따라 가치가 어떻게 변화하는지 계산합니다.
     *
     * @param prices        가격 리스트 (시간순 정렬)
     * @param timestamps    각 가격에 대응하는 타임스탬프 리스트
     * @param dividends     기간 중 지급된 배당금 리스트
     * @param initialAmount 전체 초기 투자 금액
     * @param weight        이 자산에 투자할 비율(0.0~1.0)
     * @return 각 시점별 가중 투자 가치 리스트
     */
    public List<Amount> calculateCumulativeAmounts(List<Double> prices, List<Long> timestamps, List<Dividend> dividends,
            double initialAmount, double weight) {
        double startPrice = prices.get(0);
        if (startPrice <= 0) {
            List<Amount> amountChanges = new ArrayList<>();
            for (int i = 0; i < prices.size(); i++) {
                amountChanges.add(new Amount(0, prices.get(i)));
            }
            log.error("startPrice is less than or equal to 0");
            return amountChanges;
        }

        double allocatedAmount = initialAmount * weight;
        double initialShares = allocatedAmount / startPrice;

        return calculateCumulativeAmounts(prices, timestamps, dividends, initialShares);
    }

    /**
     * 주어진 수익률 리스트의 변동성(표준편차)을 계산합니다.
     *
     * @param periodicReturnRate 수익률 리스트
     * @return 변동성(표준편차)
     */
    public double calculateVolatility(List<ReturnRate> periodicReturnRates) {
        return new Volatility(periodicReturnRates).volatility();
    }

    /**
     * 초기 투자금액 1.0으로 설정하고, 배당 재투자를 포함하여 시간에 따라 포트폴리오의 수익률을 계산합니다.
     * timestamps에 따른 기간별 수익율
     *
     * @param prices     가격 리스트 (시간순 정렬)
     * @param timestamps 각 가격에 대응하는 타임스탬프 리스트
     * @param dividends  기간 중 지급된 배당금 리스트
     * @return 각 시점별 포트폴리오의 수익률 리스트
     */
    public List<ReturnRate> calculatePeriodicReturnRates(List<Double> prices, List<Long> timestamps,
            List<Dividend> dividends) {
        List<Amount> pList = calculateCumulativeAmounts(prices, timestamps, dividends, 1.0);
        List<ReturnRate> returns = new ArrayList<>();
        for (int i = 1; i < pList.size(); i++) {
            Double current = pList.get(i).amount();
            Double prev = pList.get(i - 1).amount();
            // double r = (current - prev) / prev;
            ReturnRate returnRate = new ReturnRate(prev, current);
//            log.debug("calculateReturn {} current {} prev {} r {}", i, current, prev, returnRate);
            returns.add(returnRate);
        }
        return returns;
    }

    /**
     * 주어진 가격 리스트의 최대낙폭을 계산합니다.
     *
     * @param prices 가격 리스트
     * @return 최대낙폭(목록)
     */
    public List<Double> calculateMaxDrawdowns(List<Double> prices) {
        if (prices == null || prices.size() < 2)
            return List.of(0.0);

        List<Double> drawdowns = new ArrayList<>();
        double peak = prices.get(0);

        for (double price : prices) {
            if (price > peak) {
                peak = price;
            }
            double drawdown = (peak == 0.0) ? 0.0 : (peak - price) / peak;
            // log.debug("calculateMaxDrawdowns price {} peak {} drawdown {}", price, peak, drawdown);
            drawdowns.add(drawdown);
        }
        return drawdowns;
    }

    /**
     * 초기 보유 주식 수를 기준으로 시간에 따라 포트폴리오 가치를 계산하는 핵심 내부 메서드입니다.
     *
     * <p>
     * 배당금 발생 시 현금으로 누적한 뒤, 다음 가격 데이터 포인트에서 재투자합니다.
     *
     * @param prices        가격 리스트 (시간순 정렬)
     * @param timestamps    각 가격에 대응하는 타임스탬프 리스트
     * @param dividends     배당금 리스트
     * @param initialShares 초기 보유 주식 수
     * @return 각 시점별 포트폴리오 가치 리스트
     */
    private List<Amount> calculateCumulativeAmounts(List<Double> prices, List<Long> timestamps,
            List<Dividend> dividends,
            double initialShares) {
        if (prices == null || prices.isEmpty() || timestamps == null || timestamps.isEmpty()) {
            throw new IllegalArgumentException("Prices and timestamps lists cannot be null or empty");
        }
        if (prices.size() != timestamps.size()) {
            throw new IllegalArgumentException("Prices and timestamps lists must have the same size");
        }

        List<Amount> cumulativeAmounts = new ArrayList<>();

        // Create a mutable, sorted copy of dividends to avoid modifying the original
        // list
        List<Dividend> sortedDividends = new ArrayList<>();
        if (dividends != null) {
            sortedDividends.addAll(dividends);
            sortedDividends.sort((d1, d2) -> Long.compare(d1.getDate(), d2.getDate()));
        }

        double shares = initialShares;
        double cash = 0.0;

        for (int i = 0; i < prices.size(); i++) {
            long currentTimestamp = timestamps.get(i);
            double currentPrice = prices.get(i);
            long previousTimestamp = (i == 0) ? 0 : timestamps.get(i - 1);

            // Accumulate cash from dividends paid between the last price point and the
            // current one
            java.util.Iterator<Dividend> iterator = sortedDividends.iterator();
            while (iterator.hasNext()) {
                Dividend div = iterator.next();
                if (div.getDate() > previousTimestamp && div.getDate() <= currentTimestamp) {
                    cash += shares * div.getAmount();
                    iterator.remove(); // Simplify by removing processed dividends
                } else if (div.getDate() > currentTimestamp) {
                    // Since the list is sorted, we can stop checking for this period
                    break;
                }
            }

            // Reinvest any available cash at the current price
            if (cash > 0 && currentPrice > 0) {
                shares += cash / currentPrice;
                cash = 0;
            }

            cumulativeAmounts.add(new Amount(shares, currentPrice));
        }

        return cumulativeAmounts;
    }

    public Double calculateMaxValue(List<Double> values) {
        // double값에서 가장 큰수를 찾는데, stream 사용하지 않고 for문으로 구현
        double max = Double.MIN_VALUE;
        for (Double value : values) {
            if (value > max) {
                max = value;
            }
//            log.debug("calculateMaxValue max {} value {}", max, value);
        }
        return max;
    }

    public List<Double> calculatePrices(List<Double> priceReturns, double initialAmount) {
        List<Double> prices = priceReturns.stream().map(d -> initialAmount + (d * initialAmount)).toList();
        return prices;
    }

    /**
     * 주어진 수익률 리스트의 샤르프비율을 계산합니다.
     * 
     * @param periodicReturnRates
     * @return
     */
    public double calculateSharpeRatio(List<ReturnRate> periodicReturnRates) {
        double meanReturnRate = periodicReturnRates.stream().mapToDouble(ReturnRate::rate).average().orElse(0.0);
        double standardDeviation = new Volatility(periodicReturnRates).standardDeviation();
        return (meanReturnRate - (0.04 / 12)) / standardDeviation * Math.sqrt(periodicReturnRates.size());
    }
}
