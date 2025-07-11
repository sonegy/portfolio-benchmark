package portfolio.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;
import portfolio.util.DateUtils;
import portfolio.util.JsonLoggingUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PortfolioReturnService {

    private final PortfolioDataService portfolioDataService;
    private final ReturnCalculator returnCalculator;
    private final PortfolioAnalyzer portfolioAnalyzer;
    private final PeriodManager periodManager;

    public PortfolioReturnService(
            PortfolioDataService portfolioDataService,
            ReturnCalculator returnCalculator,
            PortfolioAnalyzer portfolioAnalyzer,
            PeriodManager periodManager) {
        this.portfolioDataService = portfolioDataService;
        this.returnCalculator = returnCalculator;
        this.portfolioAnalyzer = portfolioAnalyzer;
        this.periodManager = periodManager;
    }

    public PortfolioReturnData analyzePortfolio(PortfolioRequest request) {
        validateRequest(request);

        // Convert dates to timestamps
        long period1 = DateUtils.toUnixTimestamp(request.getStartDate());
        long period2 = DateUtils.toUnixTimestamp(request.getEndDate());
        boolean includeDividends = request.isIncludeDividends();

        // Fetch stock data
        Map<String, ChartResponse> stockData = fetchStockData(request.getTickers(), period1, period2, includeDividends);
        Set<Entry<String, ChartResponse>> entrySet = stockData.entrySet();
        for (Entry<String, ChartResponse> entry : entrySet) {
            String key = entry.getKey();
            ChartResponse value = entry.getValue();
            log.debug("analyzePortfolio {} {}", key, JsonLoggingUtils.toJsonPretty(value));
        }

        // Calculate returns for each stock
        List<StockReturnData> stockReturns = calculateStockReturns(request, stockData);
        for (StockReturnData stockReturnData : stockReturns) {
            log.debug("analyzePortfolio stockReturnData {}", JsonLoggingUtils.toJsonPretty(stockReturnData));
        }

        // Calculate and set portfolio-level metrics
        return calculatePortfolioReturnData(stockReturns, request.getWeights());
    }

    private void validateRequest(PortfolioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Portfolio request cannot be null");
        }

        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            throw new IllegalArgumentException("Tickers cannot be null or empty");
        }
    }

    private Map<String, ChartResponse> fetchStockData(List<String> tickers, long period1, long period2,
            boolean includeDividends) {
        CompletableFuture<Map<String, ChartResponse>> future;
        if (includeDividends) {
            future = portfolioDataService.fetchMultipleDividends(tickers, period1, period2);
        } else {
            future = portfolioDataService.fetchMultipleStocks(tickers, period1, period2);
        }
        return future.join();
    }

    private List<StockReturnData> calculateStockReturns(PortfolioRequest request,
            Map<String, ChartResponse> stockData) {
        List<StockReturnData> stockReturns = new ArrayList<>();
        for (String ticker : request.getTickers()) {
            ChartResponse chartResponse = stockData.get(ticker);
            if (chartResponse != null) {
                StockReturnData stockReturn = calculateStockReturn(ticker, chartResponse, request.isIncludeDividends());
                stockReturns.add(stockReturn);
            }
        }
        return stockReturns;
    }

    private StockReturnData calculateStockReturn(String ticker, ChartResponse chartResponse, boolean includeDividends) {
        // Extract prices from chart response
        List<Double> prices = extractPrices(chartResponse);

        if (prices.isEmpty()) {
            log.error("{} prices is Empty", ticker);
            return new StockReturnData(ticker, 0.0, 0.0, 0.0);
        }

        // Calculate returns
        double priceReturn = returnCalculator.calculatePriceReturn(prices);
        List<Dividend> dividends = extractDividends(chartResponse);
        double totalReturn = includeDividends
                ? returnCalculator.calculateTotalReturn(prices, dividends)
                : priceReturn;

        // Calculate CAGR using actual time period
        double startPrice = prices.get(0);
        double endPrice = prices.get(prices.size() - 1);
        int years = calculateYearsBetweenPrices(chartResponse);
        double cagr = years > 0 ? returnCalculator.calculateCAGR(startPrice, endPrice, years) : 0.0;

        StockReturnData stockReturnData = new StockReturnData(ticker, priceReturn, totalReturn, cagr);
        stockReturnData.setCumulativeReturns(returnCalculator.calculateCumulativeReturns(prices, dividends));
        stockReturnData.setDates(extractDates(chartResponse));
        return stockReturnData;
    }

    private int calculateYearsBetweenPrices(ChartResponse chartResponse) {
        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result == null || result.getTimestamp() == null || result.getTimestamp().size() < 2) {
            return 1; // Default to 1 year if timestamps are not available
        }

        long startTimestamp = result.getTimestamp().get(0);
        long endTimestamp = result.getTimestamp().get(result.getTimestamp().size() - 1);

        // Convert seconds to years (approximate)
        long secondsInYear = 365L * 24L * 60L * 60L;
        long yearsDifference = (endTimestamp - startTimestamp) / secondsInYear;

        return Math.max(1, (int) yearsDifference); // At least 1 year
    }

    private List<Double> extractPrices(ChartResponse chartResponse) {
        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result == null) {
            return new ArrayList<>();
        }

        if (result.getIndicators() == null ||
                result.getIndicators().getAdjclose() == null ||
                result.getIndicators().getAdjclose().isEmpty()) {
            return new ArrayList<>();
        }

        return result.getIndicators().getAdjclose().get(0).getAdjclose();
    }

    private List<ChartResponse.Dividend> extractDividends(ChartResponse chartResponse) {
        List<ChartResponse.Dividend> dividends = new ArrayList<>();

        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result != null && result.getEvents() != null && result.getEvents().getDividends() != null) {
            dividends.addAll(result.getEvents().getDividends().values());
        }

        return dividends;
    }

    private ChartResponse.Result getFirstResult(ChartResponse chartResponse) {
        if (chartResponse == null ||
                chartResponse.getChart() == null ||
                chartResponse.getChart().getResult() == null ||
                chartResponse.getChart().getResult().isEmpty()) {
            return null;
        }

        return chartResponse.getChart().getResult().get(0);
    }

    List<LocalDate> extractDates(ChartResponse chartResponse) {
        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result == null) {
            return new ArrayList<>();
        }

        if (result.getTimestamp() == null ||
                result.getTimestamp().isEmpty()) {
            return new ArrayList<>();
        }

        return result.getTimestamp().stream().map(DateUtils::toLocalDate).toList();
    }

    private PortfolioReturnData calculatePortfolioReturnData(List<StockReturnData> stockReturns,
            List<Double> weights) {
        if (stockReturns == null || stockReturns.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        PortfolioReturnData portfolioData = new PortfolioReturnData(stockReturns);
        portfolioData.setPortfolioPriceReturn(portfolioAnalyzer.calculatePortfolioPriceReturn(stockReturns, weights));
        portfolioData.setPortfolioTotalReturn(portfolioAnalyzer.calculatePortfolioTotalReturn(stockReturns, weights));
        portfolioData.setPortfolioCAGR(portfolioAnalyzer.calculatePortfolioCAGR(stockReturns, weights));
        portfolioData.setVolatility(portfolioAnalyzer.calculateVolatility(stockReturns, weights));
        portfolioData.setSharpeRatio(portfolioAnalyzer.calculateSharpeRatio(portfolioData.getPortfolioTotalReturn(), portfolioData.getVolatility()));
        return portfolioData;
    }
}
