package portfolio.model;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import portfolio.api.ChartResponse.Dividend;
import lombok.AllArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockReturnData {
    /** 종목 티커(예: AAPL, MSFT) */
    private String ticker;
    /** 해당 종목의 가격 시계열 데이터 */
    private List<Double> prices;
    /** 각 가격에 대응하는 타임스탬프(Unix time, ms) */
    private List<Long> timestamps;
    /** 해당 종목의 배당 시계열 데이터 */
    private List<Dividend> dividends;
    private double initialAmount;
    /** 단순 가격 수익률 */
    private double priceReturn;
    /** 배당 등 모든 요소를 포함한 총수익률 */
    private double totalReturn;
    /**
     * 연평균 복리수익률(CAGR)
     * 분배율(가중치)이 적용될 수 있음
     */
    private double cagr;
    /**
     * 누적 수익률 시계열
     * 분배율(가중치)이 적용될 수 있음
     */
    private List<Double> cumulativeReturns;
    /**
     * 누적 슈익율 시계열 
     * 분배율이 적용되지 않음.
     */
    private List<Double> cumulativePriceReturns;
    /** 가격 데이터에 대응하는 날짜 리스트 */
    private List<LocalDate> dates;
    /**
     * 구간별 금액 변화(리밸런싱 등으로 인한 변화)
     * 분배율(가중치)이 적용될 수 있음
     */
    private List<Double> amountChanges;
    /**
     * 기간별 수익률 목록 (예: 0.1, 0.2, 0.1, -0.1)
     * 각 기간별 수익률을 나타냄
     * 분배율(가중치)이 적용될 수 있음
     */
    private List<Double> periodicReturnRates;
    /**
     * 변동성(표준편차 등으로 계산)
     * 분배율(가중치)이 적용될 수 있음
     */
    private double volatility;
    /** 최대 낙폭(MDD, Max Drawdown) */
    private List<Double> maxDrawdowns;
    private double maxDrawdown;
    private double sharpeRatio;
    private double beta;

    // Lombok이 getter, builder, 생성자 자동 생성
}
