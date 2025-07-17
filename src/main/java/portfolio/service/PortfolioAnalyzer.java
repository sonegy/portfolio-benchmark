package portfolio.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import portfolio.model.ReturnRate;
import portfolio.model.StockReturnData;
import portfolio.model.Volatility;

@Slf4j
@Service
public class PortfolioAnalyzer {

    private void validateReturnsNotNullOrEmpty(List<Double> returns, String parameterName) {
        if (returns == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
        if (returns.isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be empty");
        }
    }

    private void validateMinimumSize(List<Double> returns, int minSize, String operation) {
        if (returns.size() < minSize) {
            throw new IllegalArgumentException("At least " + minSize + " returns are required to " + operation);
        }
    }

    /**
     * 주어진 수치 리스트의 평균값을 계산한다.
     * 
     * @param values 수치 리스트
     * @return 평균값
     */
    private double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 각 ticker별 intervalReturns와 비중을 이용해 포트폴리오의 변동성(표준편차)을 계산한다.
     * 1. 모든 ticker의 intervalReturns 길이가 같다고 가정한다.
     * 2. 각 기간별 포트폴리오 수익률 시계열을 만든다.
     * 3. 그 시계열의 표준편차를 반환한다.
     *
     * @param stockReturns 각 ticker별 수익률 데이터 (intervalReturns 필드 활용)
     * @param weights      각 ticker별 비중
     * @return 포트폴리오 변동성(표준편차)
     */
    public double calculateVolatility(List<StockReturnData> stockReturns, List<Double> weights) {
        List<List<Double>> returnsInStocks = stockReturns.stream().map(stock -> stock.getIntervalReturns()).toList();
        if (returnsInStocks.isEmpty() || weights.isEmpty()) {
            throw new IllegalArgumentException("returnsInStocks, weights must not be empty");
        }

        List<Double> finalWeights = getFinalWeights(returnsInStocks.size(), weights);
        int n = returnsInStocks.size();
        int periods = returnsInStocks.get(0).size();
        // 각 ticker의 intervalReturns 길이 체크
        for (List<Double> returns : returnsInStocks) {
            if (returns.size() != periods)
                throw new IllegalArgumentException("All intervalReturns must have the same length");
        }
        // 각 기간별 포트폴리오 수익률 시계열 계산
        List<Double> returnRateValues = new ArrayList<>();
        for (int t = 0; t < periods; t++) {
            double r = 0.0;
            for (int i = 0; i < n; i++) {
                r += finalWeights.get(i) * returnsInStocks.get(i).get(t);
            }
            returnRateValues.add(r);
        }
        List<ReturnRate> returnRates = returnRateValues.stream().map(value -> new ReturnRate(value)).toList();
        return new Volatility(returnRates).volatility();
    }

    public double calculatePortfolioPriceReturn(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        double weightedPriceReturn = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedPriceReturn += stockReturns.get(i).getPriceReturn() * finalWeights.get(i);
        }
        return weightedPriceReturn;
    }

    public double calculatePortfolioTotalReturn(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        double weightedTotalReturn = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedTotalReturn += stockReturns.get(i).getTotalReturn() * finalWeights.get(i);
        }
        return weightedTotalReturn;
    }

    public double calculatePortfolioCAGR(List<StockReturnData> stockReturns, List<Double> weights) {
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);
        double weightedCagr = 0.0;
        for (int i = 0; i < stockReturns.size(); i++) {
            weightedCagr += stockReturns.get(i).getCagr() * finalWeights.get(i);
        }
        return weightedCagr;
    }

    /**
     * 포트폴리오의 누적 수익률(분배금포함가능)을 계산합니다.
     *
     * @param stockReturns 각 주식의 수익률 데이터
     * @param weights      각 주식의 비중
     * @return 포트폴리오의 누적 수익률
     */
    public List<Double> calculatePortfolioCumulativeReturns(List<StockReturnData> stockReturns, List<Double> weights) {
        if (stockReturns == null || stockReturns.isEmpty() || weights == null || weights.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<List<Double>> returnsInStocks = stockReturns.stream()
                .map(stock -> stock.getCumulativeReturns() != null ? stock.getCumulativeReturns() : List.<Double>of())
                .collect(java.util.stream.Collectors.toList());
        return calculatePortfolioCumulativeReturnsValue(returnsInStocks, weights);
    }

    /**
     * 포트폴리오의 누적 가격 수익률(분배금포함하지않음)을 계산합니다.
     *
     * @param stockReturns 각 주식의 수익률 데이터
     * @param weights      각 주식의 비중
     * @return 포트폴리오의 누적 가격 수익률
     */
    public List<Double> calculatePortfolioCumulativePriceReturns(List<StockReturnData> stockReturns, List<Double> weights) {
        if (stockReturns == null || stockReturns.isEmpty() || weights == null || weights.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<List<Double>> returnsInStocks = stockReturns.stream()
                .map(stock -> stock.getCumulativePriceReturns() != null ? stock.getCumulativePriceReturns() : List.<Double>of())
                .collect(java.util.stream.Collectors.toList());
        return calculatePortfolioCumulativeReturnsValue(returnsInStocks, weights);
    }

    private List<Double> calculatePortfolioCumulativeReturnsValue(List<List<Double>> returnsInStocks, List<Double> weights) {
        if (returnsInStocks == null || returnsInStocks.isEmpty() || weights == null || weights.isEmpty()) {
            return new ArrayList<>();
        }
        // 내부 원소 null 체크 및 길이 체크
        int n = returnsInStocks.size();
        int numDataPoints = -1;
        for (List<Double> returns : returnsInStocks) {
            if (returns == null || returns.isEmpty()) {
                throw new IllegalArgumentException("All returnsInStocks must not be null or empty.");
            }
            if (numDataPoints == -1) {
                numDataPoints = returns.size();
            } else if (returns.size() != numDataPoints) {
                throw new IllegalArgumentException("All returnsInStocks must have the same length.");
            }
        }
        if (weights.size() != n) {
            throw new IllegalArgumentException("The number of weights must match the number of tickers.");
        }

        List<Double> finalWeights = getFinalWeights(n, weights);
        List<Double> portfolioCumulativeReturns = new ArrayList<>();
        for (int i = 0; i < numDataPoints; i++) {
            double dailyPortfolioReturn = 0.0;
            for (int j = 0; j < n; j++) {
                dailyPortfolioReturn += returnsInStocks.get(j).get(i) * finalWeights.get(j);
            }
            portfolioCumulativeReturns.add(dailyPortfolioReturn);
        }
        return portfolioCumulativeReturns;
    }

    private List<Double> getFinalWeights(int numStocks, List<Double> weights) {
        if (weights != null && !weights.isEmpty()) {
            if (weights.size() != numStocks) {
                throw new IllegalArgumentException("The number of weights must match the number of tickers.");
            }
            double sumOfWeights = weights.stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(sumOfWeights - 1.0) > 1e-9) {
                throw new IllegalArgumentException("The sum of weights must be equal to 1.");
            }
            return weights;
        } else {
            // Default to equal weights if none are provided
            double equalWeight = 1.0 / numStocks;
            List<Double> equalWeights = new ArrayList<>();
            for (int i = 0; i < numStocks; i++) {
                equalWeights.add(equalWeight);
            }
            return equalWeights;
        }
    }

    public double calculateCorrelation(List<Double> returns1, List<Double> returns2) {
        validateReturnsNotNullOrEmpty(returns1, "Returns1");
        validateReturnsNotNullOrEmpty(returns2, "Returns2");

        if (returns1.size() != returns2.size()) {
            throw new IllegalArgumentException("Both return series must have the same size");
        }

        validateMinimumSize(returns1, 2, "calculate correlation");

        // Calculate means
        double mean1 = calculateMean(returns1);
        double mean2 = calculateMean(returns2);

        // Calculate covariance and standard deviations
        double covariance = 0.0;
        double sumSquares1 = 0.0;
        double sumSquares2 = 0.0;

        for (int i = 0; i < returns1.size(); i++) {
            double diff1 = returns1.get(i) - mean1;
            double diff2 = returns2.get(i) - mean2;

            covariance += diff1 * diff2;
            sumSquares1 += diff1 * diff1;
            sumSquares2 += diff2 * diff2;
        }

        double stdDev1 = Math.sqrt(sumSquares1 / returns1.size());
        double stdDev2 = Math.sqrt(sumSquares2 / returns2.size());

        if (stdDev1 == 0.0 || stdDev2 == 0.0) {
            throw new IllegalArgumentException("Cannot calculate correlation when standard deviation is zero");
        }

        // Correlation = Covariance / (StdDev1 * StdDev2)
        return (covariance / returns1.size()) / (stdDev1 * stdDev2);
    }

    public double calculateSharpeRatio(List<StockReturnData> stockReturns, List<Double> weights) {
        List<List<Double>> returnsInStocks = stockReturns.stream().map(stock -> stock.getIntervalReturns()).toList();
        List<Double> finalWeights = getFinalWeights(stockReturns.size(), weights);

        int n = returnsInStocks.size();
        int periods = returnsInStocks.get(0).size();
        // 각 ticker의 intervalReturns 길이 체크
        for (List<Double> returns : returnsInStocks) {
            if (returns.size() != periods)
                throw new IllegalArgumentException("All intervalReturns must have the same length");
        }
        // 각 기간별 포트폴리오 수익률 시계열 계산
        List<Double> returnRateValues = new ArrayList<>();
        for (int t = 0; t < periods; t++) {
            double r = 0.0;
            for (int i = 0; i < n; i++) {
                r += finalWeights.get(i) * returnsInStocks.get(i).get(t);
            }
            returnRateValues.add(r);
        }
        List<ReturnRate> returnRates = returnRateValues.stream().map(value -> new ReturnRate(value)).toList();
        return new ReturnCalculator().calculateSharpeRatio(returnRates);
    }
}
