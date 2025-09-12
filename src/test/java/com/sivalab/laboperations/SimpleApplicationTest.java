package com.sivalab.laboperations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class SimpleApplicationTest {

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully with H2 database
    }
}
