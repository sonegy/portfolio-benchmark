package portfolio.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.Amount;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.ReturnRate;
import portfolio.model.StockReturnData;
import portfolio.util.DateUtils;
import portfolio.util.JsonLoggingUtils;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

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
    private static final String INDEX = "^GSPC";

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
        log.debug("analyzePortfolio request:{}", JsonLoggingUtils.toJsonPretty(request));

        // Fetch stock data
        var tickers = new ArrayList<>(request.getTickers());
        tickers.add(INDEX); // S&P 500 index

        Map<String, ChartResponse> stockData = fetchStockData(
            tickers, period1, period2, includeDividends);
        ChartResponse index = stockData.get(INDEX);
        stockData.remove(INDEX);
        List<Double> indexPrices = extractPrices(index);

        validateStockDataConsistency(stockData);
        Set<Entry<String, ChartResponse>> entrySet = stockData.entrySet();
        for (Entry<String, ChartResponse> entry : entrySet) {
            String key = entry.getKey();
            ChartResponse value = entry.getValue();
            log.debug("analyzePortfolio {} {}", key, JsonLoggingUtils.toJsonPretty(value));
        }

        // Calculate returns for each stock
        List<StockReturnData> stockReturns = calculateStockReturns(request, stockData, indexPrices);
        // Calculate and set portfolio-level metrics
        return calculatePortfolioReturnData(stockReturns, request.getWeights(), indexPrices);
    }

    private void validateRequest(PortfolioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Portfolio request cannot be null");
        }

        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            throw new IllegalArgumentException("Tickers cannot be null or empty");
        }
    }

    private void validateStockDataConsistency(Map<String, ChartResponse> stockData) {
        if (stockData.size() <= 1) {
            return;
        }

        Optional<LocalDate> latestStartDateOpt = stockData.values().stream()
                .map(this::extractTimestamps)
                .filter(timestamps -> !timestamps.isEmpty())
                .map(timestamps -> DateUtils.toLocalDate(timestamps.get(0)))
                .max(LocalDate::compareTo);

        if (latestStartDateOpt.isEmpty()) {
            return; // No data with timestamps found
        }

        LocalDate latestStartDate = latestStartDateOpt.get();

        boolean allMatch = stockData.values().stream()
                .map(this::extractTimestamps)
                .filter(timestamps -> !timestamps.isEmpty())
                .map(timestamps -> DateUtils.toLocalDate(timestamps.get(0)))
                .allMatch(latestStartDate::equals);

        if (!allMatch) {
            throw new IllegalArgumentException(
                    "Stock data has different start dates. Please align them. The latest start date is "
                            + latestStartDate + ".");
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
            Map<String, ChartResponse> stockData, List<Double> indexPrices) {
        List<StockReturnData> stockReturns = new ArrayList<>();
        List<Double> weights = request.getWeights();

        for (int i = 0; i < request.getTickers().size(); i++) {
            String ticker = request.getTickers().get(i);
            ChartResponse chartResponse = stockData.get(ticker);
            if (chartResponse != null) {
                double weight = (weights != null && i < weights.size()) ? weights.get(i)
                        : 1.0 / request.getTickers().size();
                StockReturnData stockReturn = calculateStockReturn(ticker, chartResponse, request.isIncludeDividends(),
                        request.getInitialAmount(), weight, indexPrices);
                stockReturns.add(stockReturn);
            }
        }
        return stockReturns;
    }

    private StockReturnData calculateStockReturn(String ticker, List<Double> prices, List<Long> timestamps,
            List<Dividend> dividends, double initialAmount, double weight, List<Double> indexPrices) {
        boolean includeDividends = dividends != null && !dividends.isEmpty();

        if (prices.isEmpty()) {
            log.error("{} prices is Empty", ticker);
            return StockReturnData.builder()
                    .ticker(ticker)
                    .priceReturn(0.0)
                    .totalReturn(0.0)
                    .cagr(0.0)
                    .volatility(0.0)
                    .build();
        }

        // Calculate returns
        ReturnRate priceReturn = returnCalculator.calculatePriceReturn(prices);
        ReturnRate totalReturn = includeDividends
                ? returnCalculator.calculateTotalReturn(prices, timestamps, dividends)
                : priceReturn;

        // Calculate CAGR using actual time period
        double startPrice = prices.get(0);
        double endPrice = startPrice * (includeDividends
                ? totalReturn.rate()
                : priceReturn.rate()) + startPrice;
        // prices.get(prices.size() - 1);
        log.debug("calculateStockReturn.startPrice:{} endPrice:{}", startPrice, endPrice);

        double years = calculateYearsBetweenPrices(timestamps);
        double cagr = years > 0 ? returnCalculator.calculateCAGR(startPrice, endPrice, years).rate() : 0.0;
        List<ReturnRate> periodicReturnRate = returnCalculator.calculatePeriodicReturnRates(prices, timestamps,
                dividends);
        double volatility = returnCalculator.calculateVolatility(periodicReturnRate);
        log.debug("calculateStockReturn.ticker:{} volatility:{}", ticker, volatility);

        // 누적 수익율 배당금 포함.
        List<ReturnRate> cumulativeReturns = returnCalculator.calculateCumulativeReturns(prices, timestamps, dividends);
        // 누적 수익율 배당금 미포함.
        List<ReturnRate> cumulativePriceReturns = returnCalculator.calculateCumulativeReturns(prices, timestamps,
                emptyList());

        // 최대낙폭
        List<Double> maxDrawdowns = returnCalculator.calculateMaxDrawdowns(prices);
        // log.debug("calculateStockReturn.ticker:{} maxDrawdowns:{}", ticker,
        // maxDrawdowns);

        List<Double> priceReturnsRates = cumulativePriceReturns.stream().map(ReturnRate::rate).toList();
        List<Double> indexReturnsRates = returnCalculator.calculateCumulativeReturns(indexPrices, timestamps, emptyList())
                .stream()
                .map(ReturnRate::rate)
                .toList();

        double beta = returnCalculator.calculateBeta(priceReturnsRates, indexReturnsRates);

        return StockReturnData.builder()
                .ticker(ticker)
                .priceReturn(priceReturn.rate())
                .totalReturn(totalReturn.rate())
                .cagr(cagr)
                .volatility(volatility)
                .cumulativeReturns(cumulativeReturns.stream().map(ReturnRate::rate).toList())
                .cumulativePriceReturns(cumulativePriceReturns.stream().map(ReturnRate::rate).toList())
                .prices(requireNonNullElse(prices, emptyList()))
                .timestamps(requireNonNullElse(timestamps, emptyList()))
                .dividends(dividends)
                .initialAmount(initialAmount * weight)
                .dates(extractDates(timestamps))
                .periodicReturnRates(periodicReturnRate.stream().map(ReturnRate::rate).toList())
                .maxDrawdowns(maxDrawdowns)
                .maxDrawdown(returnCalculator.calculateMaxValue(maxDrawdowns))
                // Calculate amount changes if initial amount is provided
                .amountChanges(initialAmount > 0
                        ? returnCalculator.calculateCumulativeAmounts(
                                prices, timestamps, dividends, initialAmount, weight)
                                .stream().map(Amount::amount).toList()
                        : Collections.emptyList())
                .sharpeRatio(returnCalculator.calculateSharpeRatio(periodicReturnRate))
                .beta(beta)
                .build();
    }

    private StockReturnData calculateStockReturn(String ticker, ChartResponse chartResponse, boolean includeDividends,
            double initialAmount, double weight, List<Double> indexPrices) {
        // Extract prices and timestamps from chart response
        List<Double> prices = extractPrices(chartResponse);
        List<Long> timestamps = extractTimestamps(chartResponse);
        List<Dividend> dividends = includeDividends ? extractDividends(chartResponse) : Collections.emptyList();
        if (prices.size() != indexPrices.size()) {
            throw new IllegalArgumentException("Prices and index prices must have the same size");
        }
        return calculateStockReturn(ticker, prices, timestamps, dividends, initialAmount, weight, indexPrices);
    }

    private double calculateYearsBetweenPrices(List<Long> timestamps) {
        if (timestamps == null || timestamps.size() < 2) {
            return 1; // Default to 1 year if timestamps are not available
        }

        long startTimestamp = timestamps.get(0);
        long endTimestamp = timestamps.get(timestamps.size() - 1);

        // Convert seconds to years (approximate)
        long secondsInYear = 365L * 24L * 60L * 60L;
        long yearsDifference = (endTimestamp - startTimestamp) / secondsInYear;

        return Math.max(1, yearsDifference); // At least 1 year
    }

    private List<Double> extractPrices(ChartResponse chartResponse) {
        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result == null) {
            return new ArrayList<>();
        }

        if (result.getIndicators() == null ||
                result.getIndicators().getQuote() == null ||
                result.getIndicators().getQuote().isEmpty() ||
                result.getIndicators().getQuote().get(0).getClose() == null) {
            return new ArrayList<>();
        }

        return result.getIndicators().getQuote().get(0).getClose();
    }

    private List<Long> extractTimestamps(ChartResponse chartResponse) {
        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result == null || result.getTimestamp() == null) {
            return new ArrayList<>();
        }
        return result.getTimestamp();
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

    List<LocalDate> extractDates(List<Long> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) {
            return new ArrayList<>();
        }

        return timestamps.stream().map(DateUtils::toLocalDate).toList();
    }

    StockReturnData calculatePortfolioStockReturn(List<StockReturnData> stockReturns, List<Double> weights, List<Double> indexPrices) {
        if (stockReturns == null || stockReturns.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("weights must not be null or empty");
        }
        if (stockReturns.size() != weights.size()) {
            throw new IllegalArgumentException("stockReturns and weights must have the same size");
        }

        List<Double> prices = new ArrayList<>();
        List<Long> timestamps = stockReturns.get(0).getTimestamps();

        // 각 시점별로 모든 주식의 가격×비율을 합산하여 포트폴리오 가격을 계산
        if (timestamps != null && !timestamps.isEmpty()) {
            int n = timestamps.size();
            for (int i = 0; i < n; i++) {
                double portfolioPrice = 0.0;
                for (int j = 0; j < stockReturns.size(); j++) {
                    final double firstPrice = stockReturns.get(j).getPrices().get(0);
                    double price = stockReturns.get(j).getPrices().get(i);
                    double weight = weights.get(j);
                    portfolioPrice += (price / firstPrice) * weight;
                }
                prices.add(portfolioPrice);
            }
        }

        List<Dividend> allDividends = new ArrayList<>();
        for (int i = 0; i < stockReturns.size(); i++) {
            final double firstPrice = stockReturns.get(i).getPrices().get(0);
            List<Dividend> dividends = stockReturns.get(i).getDividends();
            double weight = weights.get(i);

            allDividends.addAll(
                    dividends.stream().map(dividend -> {
                        Dividend div = new Dividend();
                        div.setAmount((dividend.getAmount() / firstPrice) * weight);
                        div.setDate(dividend.getDate());
                        return div;
                    }).toList());

        }

        // 각 ticker의 초기가격을 모두 더하면 총 초기가격
        for (int i = 0; i < stockReturns.size(); i++) {
            double initialAmount = stockReturns.get(i).getInitialAmount();
            log.debug("calculatePortfolioStockReturn.initialAmount:{}", initialAmount);
        }
        double initialAmount = stockReturns.stream().mapToDouble(v -> v.getInitialAmount()).sum();
        log.debug("calculatePortfolioStockReturn.initialAmount:{}", initialAmount);
        ;
        // prices가 비어 있으면 명확한 예외 발생
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Portfolio prices cannot be empty");
        }
        // 모든 ticker의 처의 가격
        return calculateStockReturn("Portfolio", prices, timestamps, allDividends, initialAmount, 1.0, indexPrices);
    }

    private PortfolioReturnData calculatePortfolioReturnData(List<StockReturnData> stockReturns,
            List<Double> weights, List<Double> indexPrices) {
        if (stockReturns == null || stockReturns.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        PortfolioReturnData portfolioData = new PortfolioReturnData(stockReturns);
        List<LocalDate> dates = requireNonNullElse(stockReturns.get(0).getDates(), emptyList());
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (!dates.isEmpty()) {
            startDate = dates.get(0);
            endDate = dates.get(dates.size() - 1);
        }

        portfolioData.setStartDate(startDate);
        portfolioData.setEndDate(endDate);
        portfolioData.setPortfolioStockReturn(calculatePortfolioStockReturn(stockReturns, weights, indexPrices));
        return portfolioData;
    }
}
