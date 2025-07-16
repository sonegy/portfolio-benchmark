package portfolio.model;

import java.io.Serializable;

/**
 * 여러 분석/차트/리포트 결과를 한 번에 반환하는 통합 DTO
 */
public class PortfolioFullAnalysisResponse implements Serializable {
    private PortfolioReturnData portfolioData;
    private ChartData timeSeriesChart;
    private ChartData comparisonChart;
    private ChartData cumulativeChart;
    private ChartData amountChart;
    private AnalysisReport report;

    public PortfolioFullAnalysisResponse() {}

    public PortfolioFullAnalysisResponse(PortfolioReturnData portfolioData,
                                         ChartData timeSeriesChart,
                                         ChartData comparisonChart,
                                         ChartData cumulativeChart,
                                         ChartData amountChart,
                                         AnalysisReport report) {
        this.portfolioData = portfolioData;
        this.timeSeriesChart = timeSeriesChart;
        this.comparisonChart = comparisonChart;
        this.cumulativeChart = cumulativeChart;
        this.amountChart = amountChart;
        this.report = report;
    }

    public PortfolioReturnData getPortfolioData() {
        return portfolioData;
    }
    public void setPortfolioData(PortfolioReturnData portfolioData) {
        this.portfolioData = portfolioData;
    }
    public ChartData getTimeSeriesChart() {
        return timeSeriesChart;
    }
    public void setTimeSeriesChart(ChartData timeSeriesChart) {
        this.timeSeriesChart = timeSeriesChart;
    }
    public ChartData getComparisonChart() {
        return comparisonChart;
    }
    public void setComparisonChart(ChartData comparisonChart) {
        this.comparisonChart = comparisonChart;
    }
    public ChartData getCumulativeChart() {
        return cumulativeChart;
    }
    public void setCumulativeChart(ChartData cumulativeChart) {
        this.cumulativeChart = cumulativeChart;
    }
    public ChartData getAmountChart() {
        return amountChart;
    }
    public void setAmountChart(ChartData amountChart) {
        this.amountChart = amountChart;
    }
    public AnalysisReport getReport() {
        return report;
    }
    public void setReport(AnalysisReport report) {
        this.report = report;
    }
}
