package portfolio.model;

public record CAGR(double startValue, double endValue, double years) {

    public double rate() {
        // CAGR = (End Value / Start Value)^(1/years) - 1
        return Math.pow(endValue / startValue, 1.0 / years) - 1.0;
    }
}
