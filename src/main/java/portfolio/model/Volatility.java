package portfolio.model;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import portfolio.util.JsonLoggingUtils;

@Slf4j
public class Volatility {
    private final List<ReturnRate> periodicReturnRates;

    public Volatility(List<ReturnRate> periodicReturnRates) {
        this.periodicReturnRates = periodicReturnRates;
    }

    public double volatility() {
        return standardDeviation() * Math.sqrt(periodicReturnRates.size() - 1); 
    }

    public double standardDeviation() {
        log.debug("Volatility.standardDeviation.periodicReturnRates {}", JsonLoggingUtils.toJsonPretty(periodicReturnRates));
        double mean = periodicReturnRates.stream().mapToDouble(ReturnRate::rate).average().orElse(0.0);
        double variance = periodicReturnRates.stream().mapToDouble(r -> Math.pow(r.rate() - mean, 2)).average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

}
