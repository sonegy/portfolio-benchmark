package portfolio.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 차트 생성을 위한 데이터 모델
 */
public class ChartData {
    private final String title;
    private final String type;
    private final List<LocalDate> dates;
    private final Map<String, List<Double>> series;
    private final ChartConfiguration configuration;
    private final List<String> labels;

    public ChartData(String title, String type, List<LocalDate> dates, 
                     Map<String, List<Double>> series, ChartConfiguration configuration) {
        this.title = title;
        this.type = type;
        this.dates = dates;
        this.series = series;
        this.configuration = configuration;
        this.labels = null;
    }

    public ChartData(String title, String type, List<LocalDate> dates, 
                     Map<String, List<Double>> series, ChartConfiguration configuration, List<String> labels) {
        this.title = title;
        this.type = type;
        this.dates = dates;
        this.series = series;
        this.configuration = configuration;
        this.labels = labels;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public List<LocalDate> getDates() {
        return dates;
    }

    public Map<String, List<Double>> getSeries() {
        return series;
    }

    public ChartConfiguration getConfiguration() {
        return configuration;
    }

    public List<String> getLabels() {
        return labels;
    }

    public static class ChartConfiguration {
        private final String xAxisLabel;
        private final String yAxisLabel;
        private final Map<String, String> colors;
        private final boolean showLegend;

        public ChartConfiguration(String xAxisLabel, String yAxisLabel, 
                                Map<String, String> colors, boolean showLegend) {
            this.xAxisLabel = xAxisLabel;
            this.yAxisLabel = yAxisLabel;
            this.colors = colors;
            this.showLegend = showLegend;
        }

        public String getXAxisLabel() {
            return xAxisLabel;
        }

        public String getYAxisLabel() {
            return yAxisLabel;
        }

        public Map<String, String> getColors() {
            return colors;
        }

        public boolean isShowLegend() {
            return showLegend;
        }
    }
}
