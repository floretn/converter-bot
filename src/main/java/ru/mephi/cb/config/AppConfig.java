package ru.mephi.cb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.mephi.cb.bot.ConverterBot;

@Configuration
public class AppConfig {
    @Bean
    TelegramBotsApi telegramBotsApi(ConverterBot converterBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(converterBot);
        return telegramBotsApi;
    }
}
