package portfolio.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class PortfolioReturnData {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StockReturnData> stockReturns;
    private StockReturnData portfolioStockReturn;

    public PortfolioReturnData(List<StockReturnData> stockReturns) {
        this.stockReturns = stockReturns;
    }
}
