package portfolio.model;

import java.util.List;

public class PortfolioReturnData {
    private List<StockReturnData> stockReturns;
    private double portfolioPriceReturn;
    private double portfolioTotalReturn;
    private double portfolioCAGR;
    private double volatility;
    private double sharpeRatio;
    
    public PortfolioReturnData() {}
    
    public PortfolioReturnData(List<StockReturnData> stockReturns) {
        this.stockReturns = stockReturns;
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
    
}
