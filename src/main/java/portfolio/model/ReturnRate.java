package portfolio.model;

/**
 * 가격 리스트의 첫 번째와 마지막 가격을 기준으로 가격 수익률을 계산합니다. (Return Rate)
 */
public class ReturnRate {
    private double startPrice;
    private double endPrice;

    public ReturnRate(double startPrice, double endPrice) {
        this.startPrice = startPrice;
        this.endPrice = endPrice;
    }

    public ReturnRate(double rate) {
        this.startPrice = 1.0;
        this.endPrice = 1.0 + rate;
    }

    public double rate() {
        return (endPrice - startPrice) / startPrice;
    }
}
