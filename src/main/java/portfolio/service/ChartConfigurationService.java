package portfolio.service;

import portfolio.model.ChartData;

import java.util.HashMap;
import java.util.Map;

/**
 * 차트 스타일 및 설정 관리를 담당하는 서비스
 */
public class ChartConfigurationService {

    private static final Map<String, String> DEFAULT_COLOR_PALETTE = new HashMap<>();
    private static final String DEFAULT_COLOR = "#6c757d";

    static {
        DEFAULT_COLOR_PALETTE.put("AAPL", "#1f77b4");
        DEFAULT_COLOR_PALETTE.put("MSFT", "#ff7f0e");
        DEFAULT_COLOR_PALETTE.put("GOOGL", "#2ca02c");
        DEFAULT_COLOR_PALETTE.put("AMZN", "#d62728");
        DEFAULT_COLOR_PALETTE.put("TSLA", "#9467bd");
        DEFAULT_COLOR_PALETTE.put("META", "#8c564b");
        DEFAULT_COLOR_PALETTE.put("NVDA", "#e377c2");
        DEFAULT_COLOR_PALETTE.put("NFLX", "#7f7f7f");
        DEFAULT_COLOR_PALETTE.put("ADBE", "#bcbd22");
        DEFAULT_COLOR_PALETTE.put("CRM", "#17becf");
    }

    /**
     * 시계열 차트용 기본 설정 생성
     */
    public ChartData.ChartConfiguration createTimeSeriesConfiguration() {
        return new ChartData.ChartConfiguration(
            "Date",
            "Cumulative Return",
            getColorPalette(),
            true
        );
    }

    /**
     * 비교 차트용 기본 설정 생성
     */
    public ChartData.ChartConfiguration createComparisonConfiguration() {
        return new ChartData.ChartConfiguration(
            "Stocks",
            "Return (%)",
            getColorPalette(),
            true
        );
    }

    /**
     * 커스텀 차트 설정 생성
     */
    public ChartData.ChartConfiguration createCustomConfiguration(
            String xAxisLabel, String yAxisLabel, 
            Map<String, String> colors, boolean showLegend) {
        return new ChartData.ChartConfiguration(
            xAxisLabel,
            yAxisLabel,
            colors,
            showLegend
        );
    }

    /**
     * 기본 색상 팔레트 반환
     */
    public Map<String, String> getColorPalette() {
        return new HashMap<>(DEFAULT_COLOR_PALETTE);
    }

    /**
     * 특정 티커에 대한 색상 반환
     */
    public String getColorForTicker(String ticker) {
        return DEFAULT_COLOR_PALETTE.getOrDefault(ticker, DEFAULT_COLOR);
    }

    /**
     * 라인 차트용 설정 생성
     */
    public ChartData.ChartConfiguration createLineChartConfiguration() {
        return new ChartData.ChartConfiguration(
            "Date",
            "Value",
            getColorPalette(),
            true
        );
    }

    /**
     * 바 차트용 설정 생성
     */
    public ChartData.ChartConfiguration createBarChartConfiguration() {
        return new ChartData.ChartConfiguration(
            "Category",
            "Value",
            getColorPalette(),
            true
        );
    }
}
