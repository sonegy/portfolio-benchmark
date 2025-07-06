package portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import portfolio.api.StockFetcher;

@Configuration
public class ApiConfiguration {

    @Bean
    public StockFetcher stockFetcher(RestClient restClient, @Value("${stock.api.url}") String stockApiUrl) {
        return new StockFetcher(restClient, stockApiUrl);
    }
}
