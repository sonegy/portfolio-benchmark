package portfolio.model;

import java.time.LocalDate;
import java.util.List;

public class StockReturnData {
    /** 종목 티커(예: AAPL, MSFT) */
    private String ticker;
    /** 해당 종목의 가격 시계열 데이터 */
    private List<Double> prices;
    /** 각 가격에 대응하는 타임스탬프(Unix time, ms) */
    private List<Long> timestamps;
    /** 단순 가격 수익률 */
    private double priceReturn;
    /** 배당 등 모든 요소를 포함한 총수익률 */
    private double totalReturn;
    /**
     * 연평균 복리수익률(CAGR)
     * 분배율(가중치)이 적용될 수 있음
     */
    private double cagr;
    /**
     * 누적 수익률 시계열
     * 분배율(가중치)이 적용될 수 있음
     */
    private List<Double> cumulativeReturns;
    /**
     * 누적 슈익율 시계열 
     * 분배율이 적용되지 않음.
     */
    private List<Double> cumulativePriceReturns;
    /** 가격 데이터에 대응하는 날짜 리스트 */
    private List<LocalDate> dates;
    /**
     * 구간별 금액 변화(리밸런싱 등으로 인한 변화)
     * 분배율(가중치)이 적용될 수 있음
     */
    private List<Double> amountChanges;
    /**
     * 기간별 수익률 목록 (예: 0.1, 0.2, 0.1, -0.1)
     * 각 기간별 수익률을 나타냄
     * 분배율(가중치)이 적용될 수 있음
     */
    private List<Double> intervalReturns;
    /**
     * 변동성(표준편차 등으로 계산)
     * 분배율(가중치)이 적용될 수 있음
     */
    private double volatility;
    /** 최대 낙폭(MDD, Max Drawdown) */
    private List<Double> maxDrawdowns;
    private double maxDrawdown;

    public StockReturnData() {
    }

    public StockReturnData(String ticker, double priceReturn, double totalReturn, double cagr, double volatility) {
        this.ticker = ticker;
        this.priceReturn = priceReturn;
        this.totalReturn = totalReturn;
        this.cagr = cagr;
        this.volatility = volatility;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getPriceReturn() {
        return priceReturn;
    }

    public void setPriceReturn(double priceReturn) {
        this.priceReturn = priceReturn;
    }

    public double getTotalReturn() {
        return totalReturn;
    }

    public void setTotalReturn(double totalReturn) {
        this.totalReturn = totalReturn;
    }

    public double getCagr() {
        return cagr;
    }

    public void setCagr(double cagr) {
        this.cagr = cagr;
    }

    public List<Double> getCumulativeReturns() {
        return cumulativeReturns;
    }

    public void setCumulativeReturns(List<Double> cumulativeReturns) {
        this.cumulativeReturns = cumulativeReturns;
    }

    public List<Double> getCumulativePriceReturns() {
        return cumulativePriceReturns;
    }

    public void setCumulativePriceReturns(List<Double> cumulativePriceReturns) {
        this.cumulativePriceReturns = cumulativePriceReturns;
    }   

    public List<LocalDate> getDates() {
        return dates;
    }

    public void setDates(List<LocalDate> dates) {
        this.dates = dates;
    }

    public List<Double> getAmountChanges() {
        return amountChanges;
    }

    public void setAmountChanges(List<Double> amountChanges) {
        this.amountChanges = amountChanges;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public List<Double> getIntervalReturns() {
        return intervalReturns;
    }

    public void setIntervalReturns(List<Double> intervalReturns) {
        this.intervalReturns = intervalReturns;
    }

    public List<Double> getMaxDrawdowns() {
        return maxDrawdowns;
    }

    public void setMaxDrawdowns(List<Double> maxDrawdowns) {
        this.maxDrawdowns = maxDrawdowns;
    }

    public List<Double> getPrices() {
        return prices;
    }

    public void setPrices(List<Double> prices) {
        this.prices = prices;
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(List<Long> timestamps) {
        this.timestamps = timestamps;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(double maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }
}
