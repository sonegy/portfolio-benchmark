package portfolio.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.ChartData;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;
import portfolio.util.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 다양한 형태의 차트 생성을 담당하는 서비스
 */
@Slf4j
@Service
public class ChartGenerator {

    private static final String PORTFOLIO = "Portfolio";
    private final ChartConfigurationService configurationService;

    public ChartGenerator(ChartConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * 여러 종목의 연도별 배당금 합계 데이터를 반환
     * 
     * @param stockReturnDataList 각 종목별 수익 데이터 리스트
     * @return Map<String, Map<Integer, Double>> (티커별 -> 연도별 -> 배당금 합계)
     */
    public Map<String, Map<Integer, Double>> calculateAllYearlyDividends(List<StockReturnData> stockReturnDataList) {
        Map<String, Map<Integer, Double>> result = new LinkedHashMap<>();
        if (stockReturnDataList == null)
            return result;
        for (StockReturnData stock : stockReturnDataList) {
            if (stock == null)
                continue;
            result.put(stock.getTicker(), calculateYearlyDividends(stock));
        }
        return result;
    }

    /**
     * 단일 종목의 연도별 배당금 합계 데이터를 반환
     * 
     * @param stock 단일 종목 수익 데이터
     * @return Map<Integer, Double> (연도별 -> 배당금 합계)
     */
    public Map<Integer, Double> calculateYearlyDividends(StockReturnData stock) {
        Map<Integer, Double> yearly = new HashMap<>();
        if (stock == null || stock.getDividends() == null)
            return yearly;

        List<Long> timestamps = stock.getTimestamps();
        for (int i = 0; i < timestamps.size(); i++) {
            long timestamp = timestamps.get(i);
            int year = DateUtils.toLocalDate(timestamp).getYear();
            Double dividend = stock.getAmountDividens().isEmpty() ? 0.0 : stock.getAmountDividens().get(i);
            //log.debug("calculateYearlyDividends year: {}, dividend: {}", year, dividend);
            yearly.put(year, yearly.getOrDefault(year, 0.0) + dividend);
        }

        return yearly;
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
     * 연도별 주식별 배당금 합계 비교 차트 데이터 생성
     * 
     * @param portfolioData 포트폴리오 수익 데이터
     * @return ChartData (labels: 연도, series: 티커별 연도별 배당 합계)
     */
    public ChartData generateDividendsAmountComparisonChart(PortfolioReturnData portfolioData) {
        // List<StockReturnData> stockReturns = portfolioData.getStockReturns();
        List<StockReturnData> stockReturns = new ArrayList<>();
        stockReturns.add(portfolioData.getPortfolioStockReturn());
        stockReturns.addAll(portfolioData.getStockReturns());
        if (stockReturns == null || stockReturns.isEmpty()) {
            return new ChartData(
                    "연도별 배당금 비교",
                    "bar",
                    new ArrayList<>(),
                    new HashMap<>(),
                    configurationService.createAmountChartConfiguration());
        }

        // 1. 연도별 배당금 합계 계산
        Map<String, Map<Integer, Double>> yearlyDividends = calculateAllYearlyDividends(stockReturns);
        // 2. 전체 연도 추출(오름차순)
        Set<Integer> yearSet = new TreeSet<>();
        for (Map<Integer, Double> yearMap : yearlyDividends.values()) {
            yearSet.addAll(yearMap.keySet());
        }
        List<Integer> years = new ArrayList<>(yearSet);
        // 3. labels: 연도 리스트(문자열)
        List<String> labels = years.stream().map(String::valueOf).toList();
        // 4. series: 티커별로 연도별 합계(Double, 없는 연도는 0.0)
        Map<String, List<Double>> series = new LinkedHashMap<>();
        for (String ticker : yearlyDividends.keySet()) {
            Map<Integer, Double> yearMap = yearlyDividends.get(ticker);
            List<Double> data = new ArrayList<>();
            for (Integer year : years) {
                data.add(yearMap.getOrDefault(year, 0.0));
            }
            series.put(ticker, data);
        }
        // 5. 차트 config
        ChartData.ChartConfiguration config = configurationService.createComparisonConfiguration();
        // 6. ChartData 생성 및 반환
        return new ChartData(
                "연도별 분배금 비교",
                "bar",
                List.of(),
                series,
                config,
                labels);
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
