package portfolio.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioRequestTest {

    @Test
    void shouldCreatePortfolioRequestWithInitialAmount() {
        // Given
        List<String> tickers = List.of("AAPL", "MSFT");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        boolean includeDividends = true;
        double initialAmount = 10000.0;

        // When
        PortfolioRequest request = new PortfolioRequest(tickers, startDate, endDate, includeDividends, initialAmount);

        // Then
        assertEquals(tickers, request.getTickers());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertEquals(includeDividends, request.isIncludeDividends());
        assertEquals(initialAmount, request.getInitialAmount());
    }

    @Test
    void shouldSetAndGetInitialAmount() {
        // Given
        PortfolioRequest request = new PortfolioRequest();
        double initialAmount = 5000.0;

        // When
        request.setInitialAmount(initialAmount);

        // Then
        assertEquals(initialAmount, request.getInitialAmount());
    }

    @Test
    void shouldHaveDefaultInitialAmountOfZero() {
        // Given & When
        PortfolioRequest request = new PortfolioRequest();

        // Then
        assertEquals(0.0, request.getInitialAmount());
    }
}
