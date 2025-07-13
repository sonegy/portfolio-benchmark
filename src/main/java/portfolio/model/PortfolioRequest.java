package portfolio.model;

import java.time.LocalDate;
import java.util.List;

public class PortfolioRequest {
    private List<String> tickers;
    private List<Double> weights;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean includeDividends;
    private double initialAmount = 0.0;
    
    public PortfolioRequest() {}
    
    public PortfolioRequest(List<String> tickers, LocalDate startDate, LocalDate endDate, boolean includeDividends) {
        this.tickers = tickers;
        this.startDate = startDate;
        this.endDate = endDate;
        this.includeDividends = includeDividends;
    }
    
    public PortfolioRequest(List<String> tickers, List<Double> weights, LocalDate startDate, LocalDate endDate, boolean includeDividends) {
        this.tickers = tickers;
        this.weights = weights;
        this.startDate = startDate;
        this.endDate = endDate;
        this.includeDividends = includeDividends;
    }
    
    public PortfolioRequest(List<String> tickers, LocalDate startDate, LocalDate endDate, boolean includeDividends, double initialAmount) {
        this.tickers = tickers;
        this.startDate = startDate;
        this.endDate = endDate;
        this.includeDividends = includeDividends;
        this.initialAmount = initialAmount;
    }
    
    public List<String> getTickers() {
        return tickers;
    }
    
    public void setTickers(List<String> tickers) {
        this.tickers = tickers;
    }
    
    public List<Double> getWeights() {
        return weights;
    }
    
    public void setWeights(List<Double> weights) {
        this.weights = weights;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean isIncludeDividends() {
        return includeDividends;
    }
    
    public void setIncludeDividends(boolean includeDividends) {
        this.includeDividends = includeDividends;
    }
    
    public double getInitialAmount() {
        return initialAmount;
    }
    
    public void setInitialAmount(double initialAmount) {
        this.initialAmount = initialAmount;
    }
}
