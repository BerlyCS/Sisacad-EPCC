// backend/src/main/java/com/application/sisacadepcc/config/ExcelConfig.java
package com.application.sisacadepcc.config;

import com.application.sisacadepcc.service.ExcelScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelConfig {

    @Bean
    public CommandLineRunner loadExcelData(ExcelScheduleService excelScheduleService) {
        return args -> {
            System.out.println("=== Loading Excel Schedule Data ===");
            // Esto forzará la carga de datos al inicio
            excelScheduleService.printAllSchedules(); // Para debug
            System.out.println("=== Excel Data Loaded ===");
        };
    }
}
