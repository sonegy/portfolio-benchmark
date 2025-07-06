package portfolio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import portfolio.Main;

class MainTest {
    @Test
    void hello_shouldReturnHelloWorld() {
        assertEquals("Hello, World!", Main.hello());
    }
} 