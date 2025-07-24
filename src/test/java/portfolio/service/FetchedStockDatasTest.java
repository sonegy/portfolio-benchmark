package portfolio.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import portfolio.api.ChartResponse;
import portfolio.model.FetchedStockDatas;

class FetchedStockDatasTest {
    @Test
    void 생성자_정상_생성_및_데이터_초기화() {
        // given: 최소한의 ChartResponse mock 데이터 생성
        ChartResponse chart = new ChartResponse();
        ChartResponse.Chart chartInner = new ChartResponse.Chart();
        ChartResponse.Result result = new ChartResponse.Result();
        result.setTimestamp(List.of(1000L, 2000L));
        ChartResponse.Indicators indicators = new ChartResponse.Indicators();
        ChartResponse.Quote quote = new ChartResponse.Quote();
        quote.setClose(List.of(10.0, 20.0));
        indicators.setQuote(List.of(quote));
        result.setIndicators(indicators);
        chartInner.setResult(List.of(result));
        chart.setChart(chartInner);

        Map<String, ChartResponse> stockData = new HashMap<>();
        stockData.put("AAPL", chart);
        ChartResponse index = chart;

        // when
        FetchedStockDatas fetched = new FetchedStockDatas(stockData, index);

        // then
        assertEquals(1, fetched.getStockHistories().size());
        assertEquals(List.of(10.0, 20.0), fetched.getStockHistories().get("AAPL").prices());
        assertEquals(List.of(1000L, 2000L), fetched.getStockHistories().get("AAPL").timestamps());
        assertEquals(List.of(10.0, 20.0), fetched.getIndexPrices());
        assertEquals(List.of(1000L, 2000L), fetched.getIndexTimestamps());
    }

    @Test
    void 서로_다른_시작일_예외_발생() {
        // given: 서로 다른 시작일을 가진 ChartResponse 2개 생성 (LocalDate → epoch second)
        var zone = java.time.ZoneId.systemDefault();
        long ts1 = java.time.LocalDate.of(2023, 1, 1).atStartOfDay(zone).toEpochSecond();
        long ts2 = java.time.LocalDate.of(2024, 1, 1).atStartOfDay(zone).toEpochSecond();

        ChartResponse chart1 = new ChartResponse();
        ChartResponse.Chart chartInner1 = new ChartResponse.Chart();
        ChartResponse.Result result1 = new ChartResponse.Result();
        result1.setTimestamp(List.of(ts1, ts1 + 86400)); // 2023-01-01, 2023-01-02
        ChartResponse.Indicators indicators1 = new ChartResponse.Indicators();
        ChartResponse.Quote quote1 = new ChartResponse.Quote();
        quote1.setClose(List.of(10.0, 20.0));
        indicators1.setQuote(List.of(quote1));
        result1.setIndicators(indicators1);
        chartInner1.setResult(List.of(result1));
        chart1.setChart(chartInner1);

        ChartResponse chart2 = new ChartResponse();
        ChartResponse.Chart chartInner2 = new ChartResponse.Chart();
        ChartResponse.Result result2 = new ChartResponse.Result();
        result2.setTimestamp(List.of(ts2, ts2 + 86400)); // 2024-01-01, 2024-01-02
        ChartResponse.Indicators indicators2 = new ChartResponse.Indicators();
        ChartResponse.Quote quote2 = new ChartResponse.Quote();
        quote2.setClose(List.of(30.0, 40.0));
        indicators2.setQuote(List.of(quote2));
        result2.setIndicators(indicators2);
        chartInner2.setResult(List.of(result2));
        chart2.setChart(chartInner2);

        Map<String, ChartResponse> stockData = new HashMap<>();
        stockData.put("AAPL", chart1);
        stockData.put("MSFT", chart2);
        ChartResponse index = chart1;

        // when & then: 예외 발생 검증
        assertThrows(IllegalArgumentException.class, () -> {
            new FetchedStockDatas(stockData, index);
        });
    }
}
