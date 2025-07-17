package portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import portfolio.model.*;
import portfolio.service.ChartGenerator;
import portfolio.service.PortfolioReturnService;
import portfolio.service.ReportGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioReturnService portfolioReturnService;

    @MockitoBean
    private ChartGenerator chartGenerator;

    @MockitoBean
    private ReportGenerator reportGenerator;

    private PortfolioRequest sampleRequest;
    private PortfolioReturnData samplePortfolioData;
    private ChartData sampleChartData;
    private AnalysisReport sampleReport;

    @BeforeEach
    void setUp() {
        // 샘플 요청 데이터
        sampleRequest = new PortfolioRequest();
        sampleRequest.setTickers(List.of("AAPL", "MSFT"));
        sampleRequest.setStartDate(LocalDate.of(2023, 1, 1));
        sampleRequest.setEndDate(LocalDate.of(2023, 12, 31));
        sampleRequest.setIncludeDividends(true);

        // 샘플 포트폴리오 데이터
        StockReturnData appleData = new StockReturnData("AAPL", 0.15, 0.18, 0.12, 0.0);
        StockReturnData microsoftData = new StockReturnData("MSFT", 0.12, 0.14, 0.10, 0.0);
        
        samplePortfolioData = new PortfolioReturnData(List.of(appleData, microsoftData));
        samplePortfolioData.setPortfolioPriceReturn(0.135);
        samplePortfolioData.setPortfolioTotalReturn(0.16);

        // 샘플 차트 데이터
        ChartData.ChartConfiguration config = new ChartData.ChartConfiguration(
            "Date", "Return", Map.of(), true
        );
        sampleChartData = new ChartData(
            "Test Chart", "line", List.of(), Map.of(), config
        );

        // 샘플 리포트
        AnalysisReport.Summary summary = new AnalysisReport.Summary(
            LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31), 365,
            0.18, "AAPL", 0.14, "MSFT"
        );
        AnalysisReport.RiskMetrics riskMetrics = new AnalysisReport.RiskMetrics(
            0.08, 1.2, -0.05, Map.of()
        );
        sampleReport = new AnalysisReport(
            "RPT-12345678", LocalDateTime.now(), sampleRequest, samplePortfolioData,
            summary, List.of(), riskMetrics
        );
    }

    @Test
    void shouldAnalyzeAllAtOnce() throws Exception {
        // Given
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .thenReturn(samplePortfolioData);
        when(chartGenerator.generateTimeSeriesChart(any(PortfolioReturnData.class)))
            .thenReturn(sampleChartData);
        when(chartGenerator.generateComparisonChart(any(PortfolioReturnData.class)))
            .thenReturn(sampleChartData);
        when(chartGenerator.generateAmountChangeChart(any(PortfolioReturnData.class)))
            .thenReturn(sampleChartData);
        when(reportGenerator.generateReport(any(PortfolioRequest.class), any(PortfolioReturnData.class)))
            .thenReturn(sampleReport);

        // When & Then
        mockMvc.perform(post("/api/portfolio/analyze/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.portfolioData").exists())
                .andExpect(jsonPath("$.timeSeriesChart").exists())
                .andExpect(jsonPath("$.comparisonChart").exists())
                .andExpect(jsonPath("$.amountChart").exists())
                .andExpect(jsonPath("$.report").exists());
    }

    @Test
    void shouldAnalyzePortfolio() throws Exception {
        // Given
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .thenReturn(samplePortfolioData);

        // When & Then
        mockMvc.perform(post("/api/portfolio/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.portfolioPriceReturn").value(0.135))
                .andExpect(jsonPath("$.portfolioTotalReturn").value(0.16))
                .andExpect(jsonPath("$.stockReturns").isArray())
                .andExpect(jsonPath("$.stockReturns.length()").value(2));
    }

    @Test
    void shouldGenerateTimeSeriesChart() throws Exception {
        // Given
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .thenReturn(samplePortfolioData);
        when(chartGenerator.generateTimeSeriesChart(any(PortfolioReturnData.class)))
            .thenReturn(sampleChartData);

        // When & Then
        mockMvc.perform(post("/api/portfolio/chart/timeseries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Chart"))
                .andExpect(jsonPath("$.type").value("line"));
    }

    @Test
    void shouldGenerateComparisonChart() throws Exception {
        // Given
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .thenReturn(samplePortfolioData);
        when(chartGenerator.generateComparisonChart(any(PortfolioReturnData.class)))
            .thenReturn(sampleChartData);

        // When & Then
        mockMvc.perform(post("/api/portfolio/chart/comparison")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Chart"))
                .andExpect(jsonPath("$.type").value("line"));
    }

    @Test
    void shouldGenerateReport() throws Exception {
        // Given
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .thenReturn(samplePortfolioData);
        when(reportGenerator.generateReport(any(PortfolioRequest.class), any(PortfolioReturnData.class)))
            .thenReturn(sampleReport);

        // When & Then
        mockMvc.perform(post("/api/portfolio/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId").value("RPT-12345678"))
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.riskMetrics").exists());
    }

    @Test
    void shouldHandleInvalidRequest() throws Exception {
        // Given - 빈 요청
        PortfolioRequest invalidRequest = new PortfolioRequest();

        // When & Then
        mockMvc.perform(post("/api/portfolio/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceException() throws Exception {
        // Given
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/portfolio/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldSetStartToFirstDayAndEndToLastDayOfMonthWhenGivenYearMonth() throws Exception {
        // Given: 사용자가 2024년 7월을 선택했다고 가정 (년월만 입력)
        PortfolioRequest yearMonthRequest = new PortfolioRequest();
        yearMonthRequest.setTickers(List.of("AAPL"));
        yearMonthRequest.setStartDate(LocalDate.of(2024, 7, 1));
        yearMonthRequest.setEndDate(LocalDate.of(2024, 7, 1));
        yearMonthRequest.setIncludeDividends(true);

        // Mock: 서비스가 내부적으로 2024-07-01 ~ 2024-07-31을 사용했는지 검증
        when(portfolioReturnService.analyzePortfolio(any(PortfolioRequest.class)))
            .then(invocation -> {
                PortfolioRequest req = invocation.getArgument(0);
                LocalDate expectedStart = LocalDate.of(2024, 7, 1);
                LocalDate expectedEnd = LocalDate.of(2024, 7, 31);
                org.assertj.core.api.Assertions.assertThat(req.getStartDate()).isEqualTo(expectedStart);
                org.assertj.core.api.Assertions.assertThat(req.getEndDate()).isEqualTo(expectedEnd);
                return samplePortfolioData;
            });

        // When & Then
        mockMvc.perform(post("/api/portfolio/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(yearMonthRequest)))
                .andExpect(status().isOk());
    }

}
