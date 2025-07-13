package portfolio.service;

import org.springframework.stereotype.Service;
import portfolio.model.ChartData;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.time.LocalDate;
import java.util.ArrayList;
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

        // 주식 심볼 라벨 추출
        List<String> labels = portfolioData.getStockReturns().stream()
            .map(StockReturnData::getTicker)
            .collect(Collectors.toList());

        series.put("Price Return", priceReturns);
        series.put("Total Return", totalReturns);

        ChartData.ChartConfiguration config = configurationService.createComparisonConfiguration();

        return new ChartData(
            "Stock Performance Comparison",
            "bar",
            List.of(), // 바 차트는 날짜가 필요 없음
            series,
            config,
            labels // 라벨 정보 추가
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

    /**
     * 금액 변화 차트 데이터 생성
     */
    public ChartData generateAmountChangeChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new HashMap<>();
        List<LocalDate> dates = null;

        // 개별 주식 금액 변화
        for (StockReturnData stockData : portfolioData.getStockReturns()) {
            if (stockData.getAmountChanges() != null) {
                series.put(stockData.getTicker(), stockData.getAmountChanges());
                if (dates == null) {
                    dates = stockData.getDates();
                }
            }
        }

        // 포트폴리오 전체 금액 변화 계산
        if (!series.isEmpty() && dates != null) {
            List<Double> portfolioAmounts = calculatePortfolioAmounts(portfolioData);
            series.put("Portfolio Total", portfolioAmounts);
        }

        ChartData.ChartConfiguration config = configurationService.createAmountChartConfiguration();

        return new ChartData(
            "Portfolio Amount Changes",
            "line",
            dates,
            series,
            config
        );
    }

    /**
     * 포트폴리오 전체 금액 변화 계산
     */
    private List<Double> calculatePortfolioAmounts(PortfolioReturnData portfolioData) {
        List<StockReturnData> stockReturns = portfolioData.getStockReturns();
        if (stockReturns.isEmpty()) {
            return List.of();
        }

        // 첫 번째 주식의 날짜 수를 기준으로 함
        int dateCount = stockReturns.get(0).getAmountChanges() != null ? 
            stockReturns.get(0).getAmountChanges().size() : 0;
        
        if (dateCount == 0) {
            return List.of();
        }

        List<Double> portfolioAmounts = new ArrayList<>();
        
        for (int i = 0; i < dateCount; i++) {
            double totalAmount = 0.0;
            
            for (StockReturnData stockData : stockReturns) {
                if (stockData.getAmountChanges() != null && i < stockData.getAmountChanges().size()) {
                    totalAmount += stockData.getAmountChanges().get(i);
                }
            }
            
            portfolioAmounts.add(totalAmount);
        }
        
        return portfolioAmounts;
    }
}
