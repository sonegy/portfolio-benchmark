package portfolio.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse;
import portfolio.api.ChartResponse.Dividend;
import portfolio.model.PortfolioRequest;
import portfolio.model.PortfolioReturnData;
import portfolio.model.StockReturnData;
import portfolio.service.stockdata.FetchedStockDatas;
import portfolio.service.stockdata.StockReturnCalculator;
import portfolio.util.DateUtils;
import portfolio.util.JsonLoggingUtils;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PortfolioReturnService {

    private final PortfolioDataService portfolioDataService;
    private final StockReturnCalculator stockReturnCalculator;

    public PortfolioReturnService(
            PortfolioDataService portfolioDataService,
            StockReturnCalculator stockReturnCalculator) {
        this.portfolioDataService = portfolioDataService;
        this.stockReturnCalculator = stockReturnCalculator;
    }

    private void validateRequest(PortfolioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Portfolio request cannot be null");
        }

        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            throw new IllegalArgumentException("Tickers cannot be null or empty");
        }
    }

    public PortfolioReturnData analyzePortfolio(PortfolioRequest request) {
        validateRequest(request);

        // Convert dates to timestamps
        long period1 = DateUtils.toUnixTimestamp(request.getStartDate());
        long period2 = DateUtils.toUnixTimestamp(request.getEndDate());
        boolean includeDividends = request.isIncludeDividends();
        log.debug("analyzePortfolio request:{}", JsonLoggingUtils.toJsonPretty(request));

        // Fetch stock data
        FetchedStockDatas fetchedStockDatas = fetchStockData(request.getTickers(), period1, period2, includeDividends);

        // Calculate returns for each stock
        List<StockReturnData> stockReturns = stockReturnCalculator.calculateStockReturns(request, fetchedStockDatas);
        // Calculate and set portfolio-level metrics
        return calculatePortfolioReturnData(stockReturns, request.getWeights(), fetchedStockDatas.getIndexPrices());
    }

    private FetchedStockDatas fetchStockData(List<String> tickers, long period1, long period2,
            boolean includeDividends) {
        // index ticker
        final String INDEX = "^GSPC";

        var requestTickers = new ArrayList<>(tickers);
        requestTickers.add(INDEX);

        CompletableFuture<Map<String, ChartResponse>> future;
        if (includeDividends) {
            future = portfolioDataService.fetchMultipleDividends(requestTickers, period1, period2);
        } else {
            future = portfolioDataService.fetchMultipleStocks(requestTickers, period1, period2);
        }
        var result = future.join();
        if (!result.containsKey(INDEX)) {
            throw new IllegalArgumentException("Index data not found");
        }
        var indexChartResponse = result.remove(INDEX);
        return new FetchedStockDatas(result, indexChartResponse);
    }

    public StockReturnData calculatePortfolioStockReturn(List<StockReturnData> stockReturns, List<Double> weights,
            List<Double> indexPrices) {
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
        return stockReturnCalculator.calculateStockReturn("Portfolio", prices, timestamps, allDividends, indexPrices, initialAmount, 1.0);
    }

    private PortfolioReturnData calculatePortfolioReturnData(List<StockReturnData> stockReturns,
            List<Double> weights,
            List<Double> indexPrices) {
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
