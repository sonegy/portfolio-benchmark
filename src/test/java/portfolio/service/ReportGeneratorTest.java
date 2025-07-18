package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import portfolio.model.AnalysisReport;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private PortfolioRequest sampleRequest;
    private PortfolioReturnData samplePortfolioData;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator();

        // 샘플 요청 데이터 생성
        sampleRequest = new PortfolioRequest();
        sampleRequest.setTickers(List.of("AAPL", "MSFT"));
        sampleRequest.setStartDate(LocalDate.of(2023, 1, 1));
        sampleRequest.setEndDate(LocalDate.of(2023, 12, 31));
        sampleRequest.setIncludeDividends(true);

        // 샘플 포트폴리오 데이터 생성
        StockReturnData appleData = StockReturnData.builder()
                .ticker("AAPL")
                .priceReturn(0.15)
                .totalReturn(0.18)
                .cagr(0.12)
                .volatility(0.0)
                .cumulativeReturns(List.of(1.0, 1.05, 1.10, 1.15))
                .dates(List.of(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2023, 4, 1),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 10, 1)))
                .build();

        StockReturnData microsoftData = StockReturnData.builder()
                .ticker("MSFT")
                .priceReturn(0.12)
                .totalReturn(0.14)
                .cagr(0.10)
                .volatility(0.0)
                .cumulativeReturns(List.of(1.0, 1.03, 1.08, 1.12))
                .dates(List.of(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2023, 4, 1),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 10, 1)))
                .build();

        samplePortfolioData = new PortfolioReturnData(List.of(appleData, microsoftData));
        samplePortfolioData.setPortfolioStockReturn(StockReturnData.builder()
                .ticker("Portfolio")
                .priceReturn(0.135)
                .totalReturn(0.16)
                .cagr(0.11)
                .volatility(0.08)
                .sharpeRatio(1.2)
                .build());
    }

    @Test
    void shouldGenerateAnalysisReport() {
        // When
        AnalysisReport report = reportGenerator.generateReport(sampleRequest, samplePortfolioData);

        // Then
        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertNotNull(report.getGeneratedAt());
        assertEquals(sampleRequest, report.getRequest());
        assertEquals(samplePortfolioData, report.getPortfolioData());
        assertNotNull(report.getSummary());
        assertNotNull(report.getStockAnalyses());
        assertNotNull(report.getRiskMetrics());
    }

    @Test
    void shouldGenerateCorrectSummary() {
        // When
        AnalysisReport report = reportGenerator.generateReport(sampleRequest, samplePortfolioData);
        AnalysisReport.Summary summary = report.getSummary();

        // Then
        assertNotNull(summary);
        assertEquals(LocalDate.of(2023, 1, 1), summary.getAnalysisStartDate());
        assertEquals(LocalDate.of(2023, 12, 31), summary.getAnalysisEndDate());
        assertTrue(summary.getTotalDays() > 0);
        assertEquals("AAPL", summary.getBestPerformingStock());
        assertEquals(0.18, summary.getBestPerformingStockReturn(), 0.001);
        assertEquals("MSFT", summary.getWorstPerformingStock());
        assertEquals(0.14, summary.getWorstPerformingStockReturn(), 0.001);
    }

    @Test
    void shouldGenerateStockAnalyses() {
        // When
        AnalysisReport report = reportGenerator.generateReport(sampleRequest, samplePortfolioData);
        List<AnalysisReport.StockAnalysis> analyses = report.getStockAnalyses();

        // Then
        assertNotNull(analyses);
        assertEquals(2, analyses.size());

        AnalysisReport.StockAnalysis appleAnalysis = analyses.stream()
                .filter(a -> "AAPL".equals(a.getTicker()))
                .findFirst()
                .orElseThrow();

        assertEquals("AAPL", appleAnalysis.getTicker());
        assertEquals(0.15, appleAnalysis.getPriceReturn(), 0.001);
        assertEquals(0.18, appleAnalysis.getTotalReturn(), 0.001);
        assertEquals(0.12, appleAnalysis.getCagr(), 0.001);
        assertNotNull(appleAnalysis.getRecommendation());
    }

    @Test
    void shouldGenerateRiskMetrics() {
        // When
        AnalysisReport report = reportGenerator.generateReport(sampleRequest, samplePortfolioData);
        AnalysisReport.RiskMetrics riskMetrics = report.getRiskMetrics();

        // Then
        assertNotNull(riskMetrics);
        assertEquals(0.08, riskMetrics.getPortfolioVolatility(), 0.001);
        assertEquals(1.2, riskMetrics.getSharpeRatio(), 0.001);
        assertTrue(riskMetrics.getMaxDrawdown() <= 0);
        assertNotNull(riskMetrics.getCorrelationMatrix());
    }

    @Test
    void shouldGenerateUniqueReportIds() {
        // When
        AnalysisReport report1 = reportGenerator.generateReport(sampleRequest, samplePortfolioData);
        AnalysisReport report2 = reportGenerator.generateReport(sampleRequest, samplePortfolioData);

        // Then
        assertNotEquals(report1.getReportId(), report2.getReportId());
    }
}
