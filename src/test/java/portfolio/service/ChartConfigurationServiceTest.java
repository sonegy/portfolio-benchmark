package portfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import portfolio.model.ChartData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChartConfigurationServiceTest {

    private ChartConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        configurationService = new ChartConfigurationService();
    }

    @Test
    void shouldCreateDefaultTimeSeriesConfiguration() {
        // When
        ChartData.ChartConfiguration config = configurationService.createTimeSeriesConfiguration();

        // Then
        assertNotNull(config);
        assertEquals("Date", config.getXAxisLabel());
        assertEquals("Cumulative Return", config.getYAxisLabel());
        assertTrue(config.isShowLegend());
        assertFalse(config.getColors().isEmpty());
    }

    @Test
    void shouldCreateDefaultComparisonConfiguration() {
        // When
        ChartData.ChartConfiguration config = configurationService.createComparisonConfiguration();

        // Then
        assertNotNull(config);
        assertEquals("Stocks", config.getXAxisLabel());
        assertEquals("Return (%)", config.getYAxisLabel());
        assertTrue(config.isShowLegend());
        assertFalse(config.getColors().isEmpty());
    }

    @Test
    void shouldCreateCustomConfiguration() {
        // Given
        String xLabel = "Custom X";
        String yLabel = "Custom Y";
        Map<String, String> customColors = Map.of("AAPL", "#FF0000", "MSFT", "#00FF00");

        // When
        ChartData.ChartConfiguration config = configurationService.createCustomConfiguration(
            xLabel, yLabel, customColors, false
        );

        // Then
        assertNotNull(config);
        assertEquals(xLabel, config.getXAxisLabel());
        assertEquals(yLabel, config.getYAxisLabel());
        assertFalse(config.isShowLegend());
        assertEquals(customColors, config.getColors());
    }

    @Test
    void shouldGetColorPalette() {
        // When
        Map<String, String> colors = configurationService.getColorPalette();

        // Then
        assertNotNull(colors);
        assertFalse(colors.isEmpty());
        assertTrue(colors.containsKey("AAPL"));
        assertTrue(colors.containsKey("MSFT"));
    }

    @Test
    void shouldGetColorForTicker() {
        // When
        String appleColor = configurationService.getColorForTicker("AAPL");
        String unknownColor = configurationService.getColorForTicker("UNKNOWN");

        // Then
        assertNotNull(appleColor);
        assertNotNull(unknownColor);
        assertNotEquals(appleColor, unknownColor);
    }
}
