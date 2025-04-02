package ru.mephi.cb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в приложение.
 * @author Софронов И.Е.
 */
@SpringBootApplication
@Slf4j
public class ConverterBotApplication {
    public static void main(String[] args) {
        log.info("Запускаю приложение!");
        SpringApplication.run(ConverterBotApplication.class, args);
    }
}
