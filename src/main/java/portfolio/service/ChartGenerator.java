package portfolio.service;

import org.springframework.stereotype.Service;
import portfolio.model.ChartData;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 다양한 형태의 차트 생성을 담당하는 서비스
 */
@Service
public class ChartGenerator {

    private static final String PORTFOLIO = "Portfolio";
    private final ChartConfigurationService configurationService;

    public ChartGenerator(ChartConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * 시계열 차트 데이터 생성
     */
    public ChartData generateTimeSeriesChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new LinkedHashMap<>();
        List<LocalDate> dates = null;

        // 포트폴리오 데이터를 먼저 추가하여 차트에서 가장 앞에 오도록 설정
        final StockReturnData portfolioStockReturn = portfolioData.getPortfolioStockReturn();
        final List<StockReturnData> stockReturns = portfolioData.getStockReturns();

        if (portfolioStockReturn.getCumulativeReturns() != null) {
            series.put(PORTFOLIO, portfolioStockReturn.getCumulativeReturns());
        }

        for (StockReturnData stockData : stockReturns) {
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
                config);
    }

    /**
     * 비교 차트 데이터 생성
     */
    public ChartData generateComparisonChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new HashMap<>();

        final List<Double> priceReturns = new ArrayList<>();
        final StockReturnData portfolioStock = portfolioData.getPortfolioStockReturn();
        final List<StockReturnData> stockReturns = portfolioData.getStockReturns();

        priceReturns.add(portfolioStock.getPriceReturn());
        priceReturns.addAll(stockReturns.stream()
                .map(StockReturnData::getPriceReturn)
                .toList());

        List<Double> totalReturns = new ArrayList<>();
        totalReturns.add(portfolioStock.getTotalReturn());
        totalReturns.addAll(stockReturns.stream()
                .map(StockReturnData::getTotalReturn)
                .toList());

        // 주식 심볼 라벨 추출
        List<String> labels = new ArrayList<>();
        labels.add(PORTFOLIO);
        labels.addAll(stockReturns.stream()
                .map(StockReturnData::getTicker)
                .toList());

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
     * 금액 변화 차트 데이터 생성
     */
    public ChartData generateAmountChangeChart(PortfolioReturnData portfolioData) {
        Map<String, List<Double>> series = new LinkedHashMap<>();
        List<LocalDate> dates = null;

        // 포트폴리오 전체 금액 변화 계산
        final StockReturnData portfolioStockReturn = portfolioData.getPortfolioStockReturn();
        List<Double> portfolioAmounts = portfolioStockReturn.getAmountChanges();
        series.put(PORTFOLIO, portfolioAmounts);

        // 개별 주식 금액 변화
        for (StockReturnData stockData : portfolioData.getStockReturns()) {
            if (stockData.getAmountChanges() != null) {
                series.put(stockData.getTicker(), stockData.getAmountChanges());
                if (dates == null) {
                    dates = stockData.getDates();
                }
            }
        }

        ChartData.ChartConfiguration config = configurationService.createAmountChartConfiguration();

        return new ChartData(
                "Portfolio Amount Changes",
                "line",
                dates,
                series,
                config);
    }
}
