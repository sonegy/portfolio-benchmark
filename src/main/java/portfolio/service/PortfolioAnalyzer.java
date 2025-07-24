package portfolio.service;

import java.util.List;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
}
