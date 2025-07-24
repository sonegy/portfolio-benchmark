package portfolio.service.stockdata;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import portfolio.api.ChartResponse.Dividend;
import portfolio.model.Amount;
import portfolio.model.PortfolioRequest;
import portfolio.model.StockReturnData;
import portfolio.model.ReturnRate;
import portfolio.service.ReturnCalculator;
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
        List<StockReturnData> stockReturns = new ArrayList<>();
        List<Double> weights = request.getWeights();
        var indexPrices = fetchedStockDatas.getIndexPrices();

        for (int i = 0; i < request.getTickers().size(); i++) {
            String ticker = request.getTickers().get(i);
            var stockHistories = fetchedStockDatas.getStockHistories().get(ticker);

            if (stockHistories != null) {
                double weight = (weights != null && i < weights.size()) ? weights.get(i)
                        : 1.0 / request.getTickers().size();
                StockReturnData stockReturn = calculateStockReturn(
                        ticker, stockHistories, indexPrices,
                        request.isIncludeDividends(), request.getInitialAmount(), weight);
                stockReturns.add(stockReturn);
            }
        }
        return stockReturns;
    }

    public StockReturnData calculateStockReturn(String ticker, List<Double> prices, List<Long> timestamps,
            List<Dividend> dividends, List<Double> indexPrices, double initialAmount, double weight) {
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
        List<Double> indexReturnsRates = returnCalculator
                .calculateCumulativeReturns(indexPrices, timestamps, emptyList())
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

    private StockReturnData calculateStockReturn(String ticker, StockHistories stockHistories, List<Double> indexPrices,
            boolean includeDividends,
            double initialAmount, double weight) {
        // Extract prices and timestamps from chart response
        List<Double> prices = stockHistories.prices();
        List<Long> timestamps = stockHistories.timestamps();
        List<Dividend> dividends = includeDividends ? stockHistories.dividends() : Collections.emptyList();
        if (prices.size() != indexPrices.size()) {
            throw new IllegalArgumentException("Prices and index prices must have the same size");
        }
        return calculateStockReturn(ticker, prices, timestamps, dividends, indexPrices, initialAmount, weight);
    }

}
