package portfolio.model;

import java.time.LocalDate;
import java.util.List;

public class PortfolioReturnData {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StockReturnData> stockReturns;
    private double portfolioPriceReturn;
    private double portfolioTotalReturn;
    private double portfolioCAGR;
    private double volatility;
    private double sharpeRatio;
    private List<Double> maxDrawdowns;
    private double maxDrawdown;
    private List<Double> portfolioCumulativeReturns;

    public PortfolioReturnData(List<StockReturnData> stockReturns) {
        this.stockReturns = stockReturns;
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

    public List<StockReturnData> getStockReturns() {
        return stockReturns;
    }

    public void setStockReturns(List<StockReturnData> stockReturns) {
        this.stockReturns = stockReturns;
    }

    public double getPortfolioPriceReturn() {
        return portfolioPriceReturn;
    }

    public void setPortfolioPriceReturn(double portfolioPriceReturn) {
        this.portfolioPriceReturn = portfolioPriceReturn;
    }

    public double getPortfolioTotalReturn() {
        return portfolioTotalReturn;
    }

    public void setPortfolioTotalReturn(double portfolioTotalReturn) {
        this.portfolioTotalReturn = portfolioTotalReturn;
    }

    public double getPortfolioCAGR() {
        return portfolioCAGR;
    }

    public void setPortfolioCAGR(double portfolioCAGR) {
        this.portfolioCAGR = portfolioCAGR;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(double sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public List<Double> getPortfolioCumulativeReturns() {
        return portfolioCumulativeReturns;
    }

    public void setPortfolioCumulativeReturns(List<Double> portfolioCumulativeReturns) {
        this.portfolioCumulativeReturns = portfolioCumulativeReturns;
    }

    public List<Double> getMaxDrawdowns() {
        return maxDrawdowns;
    }

    public void setMaxDrawdowns(List<Double> maxDrawdowns) {
        this.maxDrawdowns = maxDrawdowns;
    }   
    
    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(double maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }
}
