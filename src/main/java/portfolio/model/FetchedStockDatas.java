package portfolio.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import portfolio.api.ChartResponse;
import portfolio.util.DateUtils;

@Slf4j
@Getter
public class FetchedStockDatas {
    private final Map<String, StockHistories> stockHistories;
    private final List<Long> indexTimestamps;
    private final List<Double> indexPrices;

    public FetchedStockDatas(Map<String, ChartResponse> stockData, ChartResponse index) {
        this.stockHistories = new LinkedHashMap<>();
        stockData.forEach((ticker, chartResponse) -> {
            var prices = extractPrices(chartResponse);
            var timestamps = extractTimestamps(chartResponse);
            var dividends = extractDividends(chartResponse);
            this.stockHistories.put(ticker, new StockHistories(prices, timestamps, dividends));
        });
        this.indexTimestamps = extractTimestamps(index);
        this.indexPrices = extractPrices(index);

        validateStockDataConsistency();
    }

    private void validateStockDataConsistency() {
        if (stockHistories.keySet().size() <= 1) {
            return;
        }

        stockHistories.values().forEach(stockHistories -> {
            if (stockHistories.timestamps() == null || stockHistories.timestamps().isEmpty()) {
                throw new IllegalArgumentException("Stock data has no timestamps. Please align them.");
            }
        });

        Optional<LocalDate> latestStartDateOpt = stockHistories.values().stream()
                .map(value -> value.timestamps())
                .filter(timestamps -> !timestamps.isEmpty())
                .map(timestamps -> DateUtils.toLocalDate(timestamps.get(0)))
                .max(LocalDate::compareTo);

        log.debug("Latest start date: {}", latestStartDateOpt);

        if (latestStartDateOpt.isEmpty()) {
            return; // No data with timestamps found
        }

        LocalDate latestStartDate = latestStartDateOpt.get();

        boolean allMatch = stockHistories.values().stream()
                .map(value -> value.timestamps())
                .filter(timestamps -> !timestamps.isEmpty())
                .map(timestamps -> DateUtils.toLocalDate(timestamps.get(0)))
                .allMatch(latestStartDate::equals);

        log.debug("All match: {}", allMatch);

        if (!allMatch) {
            throw new IllegalArgumentException(
                    "Stock data has different start dates. Please align them. The latest start date is "
                            + latestStartDate + ".");
        }
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

    List<Double> extractPrices(ChartResponse chartResponse) {
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

    List<Long> extractTimestamps(ChartResponse chartResponse) {
        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result == null || result.getTimestamp() == null) {
            return new ArrayList<>();
        }
        return result.getTimestamp();
    }

    List<ChartResponse.Dividend> extractDividends(ChartResponse chartResponse) {
        List<ChartResponse.Dividend> dividends = new ArrayList<>();

        ChartResponse.Result result = getFirstResult(chartResponse);
        if (result != null && result.getEvents() != null && result.getEvents().getDividends() != null) {
            dividends.addAll(result.getEvents().getDividends().values());
        }

        return dividends;
    }
}