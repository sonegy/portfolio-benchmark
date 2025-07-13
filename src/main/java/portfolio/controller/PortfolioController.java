package portfolio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import portfolio.model.AnalysisReport;
import portfolio.model.ChartData;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.service.ChartGenerator;
import portfolio.service.PortfolioReturnService;
import portfolio.service.ReportGenerator;

/**
 * 포트폴리오 분석 REST API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioReturnService portfolioReturnService;
    private final ChartGenerator chartGenerator;
    private final ReportGenerator reportGenerator;

    @Autowired
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
        try {
            validateRequest(request);
            PortfolioReturnData result = portfolioReturnService.analyzePortfolio(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("analyzePortfolio", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("analyzePortfolio", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 시계열 차트 데이터 생성
     */
    @PostMapping("/chart/timeseries")
    public ResponseEntity<ChartData> generateTimeSeriesChart(@RequestBody PortfolioRequest request) {
        try {
            validateRequest(request);
            PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
            ChartData chartData = chartGenerator.generateTimeSeriesChart(portfolioData);
            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 비교 차트 데이터 생성
     */
    @PostMapping("/chart/comparison")
    public ResponseEntity<ChartData> generateComparisonChart(@RequestBody PortfolioRequest request) {
        try {
            validateRequest(request);
            PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
            ChartData chartData = chartGenerator.generateComparisonChart(portfolioData);
            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 누적 수익률 차트 데이터 생성
     */
    @PostMapping("/chart/cumulative")
    public ResponseEntity<ChartData> generateCumulativeChart(@RequestBody PortfolioRequest request) {
        try {
            validateRequest(request);
            PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
            ChartData chartData = chartGenerator.generateCumulativeReturnChart(portfolioData);
            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 금액 변화 차트 데이터 생성
     */
    @PostMapping("/chart/amount")
    public ResponseEntity<ChartData> generateAmountChart(@RequestBody PortfolioRequest request) {
        try {
            validateRequest(request);
            PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
            ChartData chartData = chartGenerator.generateAmountChangeChart(portfolioData);
            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 분석 리포트 생성
     */
    @PostMapping("/report")
    public ResponseEntity<AnalysisReport> generateReport(@RequestBody PortfolioRequest request) {
        try {
            validateRequest(request);
            PortfolioReturnData portfolioData = portfolioReturnService.analyzePortfolio(request);
            AnalysisReport report = reportGenerator.generateReport(request, portfolioData);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError().body("Internal server error occurred");
    }
}
