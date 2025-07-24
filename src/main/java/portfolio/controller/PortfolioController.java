package portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import portfolio.model.AnalysisReport;
import portfolio.model.ChartData;
import portfolio.model.PortfolioFullAnalysisResponse;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.service.ChartGenerator;
import portfolio.service.PortfolioReturnService;
import portfolio.service.ReportGenerator;

import java.time.LocalDate;

/**
 * 포트폴리오 분석 REST API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    /**
     * 여러 분석/차트/리포트 데이터를 한 번에 반환하는 통합 엔드포인트
     */
    @PostMapping("/analyze/all")
    public ResponseEntity<PortfolioFullAnalysisResponse> analyzeAll(@RequestBody PortfolioRequest request) {
        return ResponseEntity.ok(generateFullAnalysisResponse(request));
    }

    /**
     * 여러 분석/차트/리포트 데이터를 한 번에 생성하는 내부 메서드 (구조적 변경)
     */
    private PortfolioFullAnalysisResponse generateFullAnalysisResponse(PortfolioRequest request) {
        adjustToPreviousMonthLastDay(request);
        validateRequest(request);
        PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
        ChartData timeSeriesChart = chartGenerator.generateTimeSeriesChart(portfolioData);
        ChartData comparisonChart = chartGenerator.generateComparisonChart(portfolioData);
        ChartData amountChart = chartGenerator.generateAmountChangeChart(portfolioData);
        ChartData dividendsAmountComparisonChart = chartGenerator.generateDividendsAmountComparisonChart(portfolioData);
        AnalysisReport report = reportGenerator.generateReport(request, portfolioData);
        return new PortfolioFullAnalysisResponse(
            portfolioData, timeSeriesChart, comparisonChart, amountChart, dividendsAmountComparisonChart, report
        );
    }

    private final PortfolioReturnService portfolioReturnService;
    private final ChartGenerator chartGenerator;
    private final ReportGenerator reportGenerator;

    public PortfolioController(PortfolioReturnService portfolioReturnService,
                              ChartGenerator chartGenerator,
                              ReportGenerator reportGenerator) {
        this.portfolioReturnService = portfolioReturnService;
        this.chartGenerator = chartGenerator;
        this.reportGenerator = reportGenerator;
    }

    /**
     * 포트폴리오 분석 실행
     */
    @PostMapping("/analyze")
    public ResponseEntity<PortfolioReturnData> analyzePortfolio(@RequestBody PortfolioRequest request) {
        adjustToPreviousMonthLastDay(request);
        validateRequest(request);
        PortfolioReturnData result = portfolioReturnService.analyzePortfolio(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 시계열 차트 데이터 생성
     */
    @PostMapping("/chart/timeseries")
    public ResponseEntity<ChartData> generateTimeSeriesChart(@RequestBody PortfolioRequest request) {
        adjustToPreviousMonthLastDay(request);
        validateRequest(request);
        PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
        ChartData chartData = chartGenerator.generateTimeSeriesChart(portfolioData);
        return ResponseEntity.ok(chartData);
    }

    /**
     * 비교 차트 데이터 생성
     */
    @PostMapping("/chart/comparison")
    public ResponseEntity<ChartData> generateComparisonChart(@RequestBody PortfolioRequest request) {
        adjustToPreviousMonthLastDay(request);
        validateRequest(request);
        PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
        ChartData chartData = chartGenerator.generateComparisonChart(portfolioData);
        return ResponseEntity.ok(chartData);
    }

    /**
     * 금액 변화 차트 데이터 생성
     */
    @PostMapping("/chart/amount")
    public ResponseEntity<ChartData> generateAmountChart(@RequestBody PortfolioRequest request) {
        adjustToPreviousMonthLastDay(request);
        validateRequest(request);
        PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
        ChartData chartData = chartGenerator.generateAmountChangeChart(portfolioData);
        return ResponseEntity.ok(chartData);
    }

    /**
     * 분석 리포트 생성
     */
    @PostMapping("/report")
    public ResponseEntity<AnalysisReport> generateReport(@RequestBody PortfolioRequest request) {
        adjustToPreviousMonthLastDay(request);
        validateRequest(request);
        PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
        AnalysisReport report = reportGenerator.generateReport(request, portfolioData);
        return ResponseEntity.ok(report);
    }

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Portfolio Analysis Service is running");
    }

    /**
     * 요청 유효성 검증
     */
    void adjustToPreviousMonthLastDay(PortfolioRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            LocalDate start = request.getStartDate();
            LocalDate end = request.getEndDate();
            request.setStartDate(start.withDayOfMonth(1));
            request.setEndDate(end.withDayOfMonth(end.lengthOfMonth()));
            log.debug("adjustToPreviousMonthLastDay startDate {} endDate {}", request.getStartDate(), request.getEndDate());
        }
    }

    private void validateRequest(PortfolioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            throw new IllegalArgumentException("Tickers list cannot be empty");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    /**
     * 전역 예외 처리
     */
}
