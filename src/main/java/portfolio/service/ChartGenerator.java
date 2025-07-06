package portfolio.service;

import org.springframework.stereotype.Service;
import portfolio.model.ChartData;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 다양한 형태의 차트 생성을 담당하는 서비스
 */
@Service
public class ChartGenerator {

    private final ChartConfigurationService configurationService;

    public ChartGenerator(ChartConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * 시계열 차트 데이터 생성
     */
    public ChartData generateTimeSeriesChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new HashMap<>();
        List<LocalDate> dates = null;

        for (StockReturnData stockData : portfolioData.getStockReturns()) {
            series.put(stockData.getTicker(), stockData.getCumulativeReturns());
            if (dates == null) {
                dates = stockData.getDates();
            }
        }

        ChartData.ChartConfiguration config = configurationService.createTimeSeriesConfiguration();

        return new ChartData(
            "Portfolio Time Series Analysis",
            "timeseries",
            dates,
            series,
            config
        );
    }

    /**
     * 비교 차트 데이터 생성
     */
    public ChartData generateComparisonChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new HashMap<>();
        
        List<Double> priceReturns = portfolioData.getStockReturns().stream()
            .map(StockReturnData::getPriceReturn)
            .collect(Collectors.toList());
            
        List<Double> totalReturns = portfolioData.getStockReturns().stream()
            .map(StockReturnData::getTotalReturn)
            .collect(Collectors.toList());

        series.put("Price Return", priceReturns);
        series.put("Total Return", totalReturns);

        ChartData.ChartConfiguration config = configurationService.createComparisonConfiguration();

        return new ChartData(
            "Stock Performance Comparison",
            "bar",
            List.of(), // 바 차트는 날짜가 필요 없음
            series,
            config
        );
    }

    /**
     * 누적 수익률 차트 데이터 생성
     */
    public ChartData generateCumulativeReturnChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new HashMap<>();
        List<LocalDate> dates = null;

        for (StockReturnData stockData : portfolioData.getStockReturns()) {
            series.put(stockData.getTicker(), stockData.getCumulativeReturns());
            if (dates == null) {
                dates = stockData.getDates();
            }
        }

        ChartData.ChartConfiguration config = configurationService.createLineChartConfiguration();

        return new ChartData(
            "Cumulative Returns",
            "line",
            dates,
            series,
            config
        );
    }
}
