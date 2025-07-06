package portfolio.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 분석 결과 리포트 모델
 */
public class AnalysisReport {
    private final String reportId;
    private final LocalDateTime generatedAt;
    private final PortfolioRequest request;
    private final PortfolioReturnData portfolioData;
    private final Summary summary;
    private final List<StockAnalysis> stockAnalyses;
    private final RiskMetrics riskMetrics;

    public AnalysisReport(String reportId, LocalDateTime generatedAt, 
                         PortfolioRequest request, PortfolioReturnData portfolioData,
                         Summary summary, List<StockAnalysis> stockAnalyses, 
                         RiskMetrics riskMetrics) {
        this.reportId = reportId;
        this.generatedAt = generatedAt;
        this.request = request;
        this.portfolioData = portfolioData;
        this.summary = summary;
        this.stockAnalyses = stockAnalyses;
        this.riskMetrics = riskMetrics;
    }

    public String getReportId() {
        return reportId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public PortfolioRequest getRequest() {
        return request;
    }

    public PortfolioReturnData getPortfolioData() {
        return portfolioData;
    }

    public Summary getSummary() {
        return summary;
    }

    public List<StockAnalysis> getStockAnalyses() {
        return stockAnalyses;
    }

    public RiskMetrics getRiskMetrics() {
        return riskMetrics;
    }

    public static class Summary {
        private final LocalDate analysisStartDate;
        private final LocalDate analysisEndDate;
        private final int totalDays;
        private final double bestPerformingStockReturn;
        private final String bestPerformingStock;
        private final double worstPerformingStockReturn;
        private final String worstPerformingStock;

        public Summary(LocalDate analysisStartDate, LocalDate analysisEndDate, int totalDays,
                      double bestPerformingStockReturn, String bestPerformingStock,
                      double worstPerformingStockReturn, String worstPerformingStock) {
            this.analysisStartDate = analysisStartDate;
            this.analysisEndDate = analysisEndDate;
            this.totalDays = totalDays;
            this.bestPerformingStockReturn = bestPerformingStockReturn;
            this.bestPerformingStock = bestPerformingStock;
            this.worstPerformingStockReturn = worstPerformingStockReturn;
            this.worstPerformingStock = worstPerformingStock;
        }

        public LocalDate getAnalysisStartDate() {
            return analysisStartDate;
        }

        public LocalDate getAnalysisEndDate() {
            return analysisEndDate;
        }

        public int getTotalDays() {
            return totalDays;
        }

        public double getBestPerformingStockReturn() {
            return bestPerformingStockReturn;
        }

        public String getBestPerformingStock() {
            return bestPerformingStock;
        }

        public double getWorstPerformingStockReturn() {
            return worstPerformingStockReturn;
        }

        public String getWorstPerformingStock() {
            return worstPerformingStock;
        }
    }

    public static class StockAnalysis {
        private final String ticker;
        private final double priceReturn;
        private final double totalReturn;
        private final double cagr;
        private final double volatility;
        private final String recommendation;

        public StockAnalysis(String ticker, double priceReturn, double totalReturn, 
                           double cagr, double volatility, String recommendation) {
            this.ticker = ticker;
            this.priceReturn = priceReturn;
            this.totalReturn = totalReturn;
            this.cagr = cagr;
            this.volatility = volatility;
            this.recommendation = recommendation;
        }

        public String getTicker() {
            return ticker;
        }

        public double getPriceReturn() {
            return priceReturn;
        }

        public double getTotalReturn() {
            return totalReturn;
        }

        public double getCagr() {
            return cagr;
        }

        public double getVolatility() {
            return volatility;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    public static class RiskMetrics {
        private final double portfolioVolatility;
        private final double sharpeRatio;
        private final double maxDrawdown;
        private final Map<String, Double> correlationMatrix;

        public RiskMetrics(double portfolioVolatility, double sharpeRatio, 
                          double maxDrawdown, Map<String, Double> correlationMatrix) {
            this.portfolioVolatility = portfolioVolatility;
            this.sharpeRatio = sharpeRatio;
            this.maxDrawdown = maxDrawdown;
            this.correlationMatrix = correlationMatrix;
        }

        public double getPortfolioVolatility() {
            return portfolioVolatility;
        }

        public double getSharpeRatio() {
            return sharpeRatio;
        }

        public double getMaxDrawdown() {
            return maxDrawdown;
        }

        public Map<String, Double> getCorrelationMatrix() {
            return correlationMatrix;
        }
    }
}
