package portfolio.api;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class ChartResponse implements Serializable {
    @Serial
    public static final long serialVersionUID = 1593576000L;

    private Chart chart;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @ToString
    public static class Chart {
        private List<Result> result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @ToString
    public static class Result implements Serializable {

        private Meta meta;
        private List<Long> timestamp;
        private Events events;
        private Indicators indicators;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @ToString
    public static class Meta implements Serializable {
        private String currency;
        private String symbol;
        private String exchangeName;
        private String fullExchangeName;
        private String instrumentType;
        private long firstTradeDate;
        private long regularMarketTime;
        private boolean hasPrePostMarketData;
        private int gmtoffset;
        private String timezone;
        private String exchangeTimezoneName;
        private double regularMarketPrice;
        private double fiftyTwoWeekHigh;
        private double fiftyTwoWeekLow;
        private double regularMarketDayHigh;
        private double regularMarketDayLow;
        private long regularMarketVolume;
        private String longName;
        private String shortName;
        private double chartPreviousClose;
        private int priceHint;
        private CurrentTradingPeriod currentTradingPeriod;
        private String dataGranularity;
        private String range;
        private List<String> validRanges;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Events implements Serializable {
        private Map<String, Dividend> dividends;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Dividend implements Serializable {
        private double amount;
        private long date;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class CurrentTradingPeriod implements Serializable {
        private TradingPeriod pre;
        private TradingPeriod regular;
        private TradingPeriod post;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class TradingPeriod implements Serializable {
        private String timezone;
        private long start;
        private long end;
        private int gmtoffset;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Indicators implements Serializable {
        private List<Quote> quote;
        private List<AdjClose> adjclose;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Quote implements Serializable {
        private List<Long> volume;
        private List<Double> low;
        private List<Double> high;
        private List<Double> open;
        private List<Double> close;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class AdjClose implements Serializable {
        private List<Double> adjclose;
    }
}