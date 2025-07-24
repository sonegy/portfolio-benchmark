package portfolio.model;

import java.io.Serializable;

import lombok.Getter;

/**
 * 여러 분석/차트/리포트 결과를 한 번에 반환하는 통합 DTO
 */
@Getter
public class PortfolioFullAnalysisResponse implements Serializable {
    private PortfolioReturnData portfolioData;
    private ChartData timeSeriesChart;
    private ChartData comparisonChart;
    private ChartData amountChart;
    private ChartData dividendsAmountComparisonChart;
    private AnalysisReport report;

    public PortfolioFullAnalysisResponse() {}

    public PortfolioFullAnalysisResponse(PortfolioReturnData portfolioData,
                                         ChartData timeSeriesChart,
                                         ChartData comparisonChart,
                                         ChartData amountChart,
                                         ChartData dividendsAmountComparisonChart,
                                         AnalysisReport report) {
        this.portfolioData = portfolioData;
        this.timeSeriesChart = timeSeriesChart;
        this.comparisonChart = comparisonChart;
        this.amountChart = amountChart;
        this.dividendsAmountComparisonChart = dividendsAmountComparisonChart;
        this.report = report;
    }
}
