package com.company.sales;

import com.company.sales.infrastructure.adapters.in.LeadCliController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SalesApplicationTests {

    @MockitoBean
    private LeadCliController cliController;

    @Test
    void contextLoads() {
    }

}