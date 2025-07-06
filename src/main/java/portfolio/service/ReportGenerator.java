package portfolio.service;

import portfolio.model.AnalysisReport;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 포트폴리오 분석 결과 리포트 생성을 담당하는 서비스
 */
public class ReportGenerator {

    /**
     * 포트폴리오 분석 리포트 생성
     */
    public AnalysisReport generateReport(PortfolioRequest request, PortfolioReturnData portfolioData) {
        String reportId = generateReportId();
        LocalDateTime generatedAt = LocalDateTime.now();
        
        AnalysisReport.Summary summary = generateSummary(request, portfolioData);
        List<AnalysisReport.StockAnalysis> stockAnalyses = generateStockAnalyses(portfolioData);
        AnalysisReport.RiskMetrics riskMetrics = generateRiskMetrics(portfolioData);

        return new AnalysisReport(
            reportId,
            generatedAt,
            request,
            portfolioData,
            summary,
            stockAnalyses,
            riskMetrics
        );
    }

    private String generateReportId() {
        return "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AnalysisReport.Summary generateSummary(PortfolioRequest request, PortfolioReturnData portfolioData) {
        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        
        // 최고/최저 성과 주식 찾기
        StockReturnData bestStock = portfolioData.getStockReturns().stream()
            .max((s1, s2) -> Double.compare(s1.getTotalReturn(), s2.getTotalReturn()))
            .orElseThrow();
            
        StockReturnData worstStock = portfolioData.getStockReturns().stream()
            .min((s1, s2) -> Double.compare(s1.getTotalReturn(), s2.getTotalReturn()))
            .orElseThrow();

        return new AnalysisReport.Summary(
            request.getStartDate(),
            request.getEndDate(),
            totalDays,
            bestStock.getTotalReturn(),
            bestStock.getTicker(),
            worstStock.getTotalReturn(),
            worstStock.getTicker()
        );
    }

    private List<AnalysisReport.StockAnalysis> generateStockAnalyses(PortfolioReturnData portfolioData) {
        return portfolioData.getStockReturns().stream()
            .map(this::createStockAnalysis)
            .collect(Collectors.toList());
    }

    private AnalysisReport.StockAnalysis createStockAnalysis(StockReturnData stockData) {
        double volatility = calculateVolatility(stockData.getCumulativeReturns());
        String recommendation = generateRecommendation(stockData.getTotalReturn(), volatility);

        return new AnalysisReport.StockAnalysis(
            stockData.getTicker(),
            stockData.getPriceReturn(),
            stockData.getTotalReturn(),
            stockData.getCagr(),
            volatility,
            recommendation
        );
    }

    private double calculateVolatility(List<Double> returns) {
        if (returns == null || returns.size() < 2) {
            return 0.0;
        }

        // 일일 수익률 계산
        List<Double> dailyReturns = returns.stream()
            .skip(1)
            .collect(Collectors.toList());

        // 평균 수익률 계산
        double meanReturn = dailyReturns.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        // 분산 계산
        double variance = dailyReturns.stream()
            .mapToDouble(r -> Math.pow(r - meanReturn, 2))
            .average()
            .orElse(0.0);

        // 표준편차 (변동성) 반환
        return Math.sqrt(variance);
    }

    private String generateRecommendation(double totalReturn, double volatility) {
        if (totalReturn > 0.15 && volatility < 0.2) {
            return "Strong Buy";
        } else if (totalReturn > 0.10 && volatility < 0.3) {
            return "Buy";
        } else if (totalReturn > 0.05) {
            return "Hold";
        } else if (totalReturn > 0.0) {
            return "Weak Hold";
        } else {
            return "Sell";
        }
    }

    private AnalysisReport.RiskMetrics generateRiskMetrics(PortfolioReturnData portfolioData) {
        double maxDrawdown = calculateMaxDrawdown(portfolioData);
        Map<String, Double> correlationMatrix = calculateCorrelationMatrix(portfolioData);

        return new AnalysisReport.RiskMetrics(
            portfolioData.getVolatility(),
            portfolioData.getSharpeRatio(),
            maxDrawdown,
            correlationMatrix
        );
    }

    private double calculateMaxDrawdown(PortfolioReturnData portfolioData) {
        // 간단한 최대 낙폭 계산 (실제로는 더 복잡한 계산이 필요)
        double maxDrawdown = 0.0;
        
        for (StockReturnData stockData : portfolioData.getStockReturns()) {
            List<Double> returns = stockData.getCumulativeReturns();
            if (returns != null && returns.size() > 1) {
                double peak = returns.get(0);
                for (double value : returns) {
                    if (value > peak) {
                        peak = value;
                    }
                    double drawdown = (peak - value) / peak;
                    if (drawdown > maxDrawdown) {
                        maxDrawdown = drawdown;
                    }
                }
            }
        }
        
        return -maxDrawdown; // 음수로 반환
    }

    private Map<String, Double> calculateCorrelationMatrix(PortfolioReturnData portfolioData) {
        Map<String, Double> correlationMatrix = new HashMap<>();
        
        // 간단한 상관관계 매트릭스 (실제로는 더 복잡한 계산이 필요)
        List<StockReturnData> stocks = portfolioData.getStockReturns();
        for (int i = 0; i < stocks.size(); i++) {
            for (int j = i + 1; j < stocks.size(); j++) {
                String key = stocks.get(i).getTicker() + "-" + stocks.get(j).getTicker();
                // 임시로 0.5 고정값 사용 (실제로는 피어슨 상관계수 계산 필요)
                correlationMatrix.put(key, 0.5);
            }
        }
        
        return correlationMatrix;
    }
}
