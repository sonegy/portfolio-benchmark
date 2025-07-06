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
    
    public StockReturnData() {}
    
    public StockReturnData(String ticker, double priceReturn, double totalReturn, double cagr) {
        this.ticker = ticker;
        this.priceReturn = priceReturn;
        this.totalReturn = totalReturn;
        this.cagr = cagr;
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
}
