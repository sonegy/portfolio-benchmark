package portfolio.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockReturnDataAmountTest {

    @Test
    void shouldSetAndGetAmountChanges() {
        // Given
        List<Double> amountChanges = List.of(10000.0, 11000.0, 10500.0, 12000.0);

        // When
        StockReturnData stockData = StockReturnData.builder()
            .amountChanges(amountChanges)
            .build();

        // Then
        assertEquals(amountChanges, stockData.getAmountChanges());
    }

    @Test
    void shouldCreateStockReturnDataWithAmountChanges() {
        // Given
        String ticker = "AAPL";
        double priceReturn = 0.15;
        double totalReturn = 0.18;
        double cagr = 0.12;
        List<Double> amountChanges = List.of(10000.0, 11500.0, 11800.0);

        // When
        StockReturnData stockData = StockReturnData.builder()
            .ticker(ticker)
            .priceReturn(priceReturn)
            .totalReturn(totalReturn)
            .cagr(cagr)
            .volatility(0.0)
            .amountChanges(amountChanges)
            .build();

        // Then
        assertEquals(ticker, stockData.getTicker());
        assertEquals(priceReturn, stockData.getPriceReturn());
        assertEquals(totalReturn, stockData.getTotalReturn());
        assertEquals(cagr, stockData.getCagr());
        assertEquals(amountChanges, stockData.getAmountChanges());
    }

    @Test
    void shouldHaveNullAmountChangesByDefault() {
        // Given & When
        StockReturnData stockData = StockReturnData.builder().build();

        // Then
        assertNull(stockData.getAmountChanges());
    }

    @Test
    void shouldSetAndGetMaxDrawdown() {
        // Given
        double maxDrawdown = -0.25;
        StockReturnData stockData = StockReturnData.builder().maxDrawdown(maxDrawdown).build();

        // When
        

        // Then
        assertEquals(maxDrawdown, stockData.getMaxDrawdown());
    }

    @Test
    void shouldCreateStockReturnDataWithMaxDrawdown() {
        // Given
        String ticker = "AAPL";
        double priceReturn = 0.15;
        double totalReturn = 0.18;
        double cagr = 0.12;
        double volatility = 0.2;
        double maxDrawdown = -0.3;

        // When
        StockReturnData stockData = StockReturnData.builder()
            .ticker(ticker)
            .priceReturn(priceReturn)
            .totalReturn(totalReturn)
            .cagr(cagr)
            .volatility(volatility)
            .maxDrawdown(maxDrawdown)
            .build();

        // Then
        assertEquals(maxDrawdown, stockData.getMaxDrawdown());
    }
}
