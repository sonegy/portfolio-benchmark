package portfolio.model;

import java.time.LocalDate;
import java.util.List;

public class StockReturnData {
    private String ticker;
    private double priceReturn;
    private double totalReturn;
    private double cagr;
    private List<Double> cumulativeReturns;
    private List<LocalDate> dates;
    private List<Double> amountChanges;
    private List<Double> intervalReturns; // 기간별 수익율 목록(ex 0.1,0.2,0.1,-0.1) 기간별 수익율을 나타낸다.
    private double volatility;

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
}
