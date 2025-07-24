package portfolio.service.stockdata;

import java.util.List;

import portfolio.api.ChartResponse.Dividend;

public record StockHistories(List<Double> prices, List<Long> timestamps, List<Dividend> dividends) {
}