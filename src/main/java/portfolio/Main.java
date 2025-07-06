package portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static String hello() {
        return "Hello, World!";
    }
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
