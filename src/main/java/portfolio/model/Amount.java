package portfolio.model;

/**
 * 주식의 수량과 가격을 저장하는 레코드입니다.
 */
public record Amount(double shares, double price) {

    /**
     * 주식의 가치를 계산합니다.
     * @return
     */
    public double amount() {
        return shares * price;
    }

}
