package portfolio.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import portfolio.api.ChartResponse.Dividend;
import portfolio.model.Amount;
import portfolio.model.FetchedStockDatas;
import portfolio.model.PortfolioRequest;
import portfolio.model.StockReturnData;
import portfolio.model.ReturnRate;
import portfolio.model.StockHistories;
import portfolio.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReturnCalculator {
    private final ReturnCalculator returnCalculator;

    double calculateYearsBetweenPrices(List<Long> timestamps) {
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

    List<LocalDate> extractDates(List<Long> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) {
            return new ArrayList<>();
        }

        return timestamps.stream().map(DateUtils::toLocalDate).toList();
    }

    public List<StockReturnData> calculateStockReturns(PortfolioRequest request,
            FetchedStockDatas fetchedStockDatas) {
        final boolean includeDividends = request.isIncludeDividends();
        final List<String> tickers = request.getTickers();
        final double initialAmount = request.getInitialAmount();
        final List<StockReturnData> stockReturns = new ArrayList<>();
        final List<Double> weights = request.getWeights();
        final List<Double> indexPrices = fetchedStockDatas.getIndexPrices();
        final Map<String, StockHistories> stockHistoriesMap = fetchedStockDatas.getStockHistories();

        for (int i = 0; i < tickers.size(); i++) {
            String ticker = tickers.get(i);
            var stockHistories = stockHistoriesMap.get(ticker);

            if (stockHistories != null) {
                double weight = (weights != null && i < weights.size()) ? weights.get(i)
                        : 1.0 / tickers.size();
                StockReturnData stockReturn = calculateStockReturn(
                        includeDividends, ticker, stockHistories, indexPrices,
                        initialAmount, weight);
                stockReturns.add(stockReturn);
            }
        }
        return stockReturns;
    }

    public StockReturnData calculateStockReturn(
            boolean includeDividends, String ticker, List<Double> prices, List<Long> timestamps,
            List<Dividend> dividends, List<Double> indexPrices, double initialAmount, double weight) {
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
        ReturnRate totalReturn = returnCalculator.calculateTotalReturn(prices, timestamps, dividends);

        // Calculate CAGR using actual time period
        double startPrice = prices.get(0);
        double endPrice = startPrice * priceReturn.rate() + startPrice;
        // prices.get(prices.size() - 1);
        log.debug("calculateStockReturn.startPrice:{} endPrice:{}", startPrice, endPrice);

        double years = calculateYearsBetweenPrices(timestamps);
        double cagr = years > 0 ? returnCalculator.calculateCAGR(startPrice, endPrice, years).rate() : 0.0;
        List<ReturnRate> periodicReturnRate = returnCalculator.calculatePeriodicReturnRates(prices, timestamps);
        double volatility = returnCalculator.calculateVolatility(periodicReturnRate);
        log.debug("calculateStockReturn.ticker:{} volatility:{}", ticker, volatility);

        // 누적 수익율 배당금 포함.
        List<ReturnRate> totalReturns = returnCalculator.calculateCumulativeReturns(prices, timestamps, dividends);
        // 누적 수익율 배당금 미포함.
        List<ReturnRate> priceReturns = returnCalculator.calculateCumulativeReturns(prices, timestamps, List.of());

        // 배당금 재투자 가능.
        List<ReturnRate> cumulativeReturns = includeDividends ? totalReturns : priceReturns;

        // 최대낙폭
        List<Double> maxDrawdowns = returnCalculator.calculateMaxDrawdowns(prices);
        // log.debug("calculateStockReturn.ticker:{} maxDrawdowns:{}", ticker,
        // maxDrawdowns);

        List<Double> priceReturnsRates = priceReturns.stream().map(ReturnRate::rate).toList();
        List<Double> indexReturnsRates = returnCalculator
                .calculateCumulativeReturns(indexPrices, timestamps, List.of())
                .stream()
                .map(ReturnRate::rate)
                .toList();

        double beta = returnCalculator.calculateBeta(priceReturnsRates, indexReturnsRates);

        List<Amount> calculateCumulativeAmounts = returnCalculator.calculateCumulativeAmounts(
                includeDividends,
                prices, timestamps,
                dividends,
                initialAmount, weight);

        return StockReturnData.builder()
                .ticker(ticker)
                .priceReturn(priceReturn.rate())
                .totalReturn(totalReturn.rate())
                .cagr(cagr)
                .volatility(volatility)
                .cumulativeReturns(cumulativeReturns.stream().map(ReturnRate::rate).toList())
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
                        ? calculateCumulativeAmounts.stream().map(Amount::amount).toList()
                        : List.of())
                .amountDividens(initialAmount > 0
                        ? calculateCumulativeAmounts.stream().map(Amount::cash).toList()
                        : List.of())
                .sharpeRatio(returnCalculator.calculateSharpeRatio(periodicReturnRate))
                .beta(beta)
                .build();
    }

    private StockReturnData calculateStockReturn(
            boolean includeDividends, String ticker, StockHistories stockHistories, List<Double> indexPrices,
            double initialAmount, double weight) {
        // Extract prices and timestamps from chart response
        List<Double> prices = stockHistories.prices();
        List<Long> timestamps = stockHistories.timestamps();
        List<Dividend> dividends = stockHistories.dividends();
        if (prices.size() != indexPrices.size()) {
            throw new IllegalArgumentException("Prices and index prices must have the same size");
        }
        return calculateStockReturn(includeDividends, ticker, prices, timestamps, dividends, indexPrices, initialAmount,
                weight);
    }

}
