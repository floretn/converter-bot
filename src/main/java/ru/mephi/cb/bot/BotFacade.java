package ru.mephi.cb.bot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.mephi.cb.beans.Application;
import ru.mephi.cb.services.ConverterService;

import static ru.mephi.cb.beans.constants.Constants.DOCX_PREFIX;

/**
 * Внутренняя работа бота.
 * @author Софронов И.Е.
 */
@Component
@Slf4j
public class BotFacade {

    private static final String MESSAGE = "Я - бот, который умеет конвертировать файлы WORD в PDF.\n" +
            "Просто отправьте мне файл в формате .docx и немного подождите, пока я занимаюсь его конвертацией:)";

    @Getter
    @Value("${bot.name}")
    private String botUsername;

    @Getter
    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private ConverterService converterService;

    public BotApiMethod<?> handleUpdate(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        if (message != null) {
            String username = update.getMessage().getFrom().getUserName();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            if (message.hasDocument()) {
                Document document = message.getDocument();
                String fileName = document.getFileName();
                if (!fileName.endsWith(DOCX_PREFIX)) {
                    sendMessage.setText("Файл должен быть формата docx!");
                    log.warn("Пользователь {} прислал документ неправильного формата {}", username, fileName);
                    return sendMessage;
                }
                log.info("Начинаю обработку файла {} от пользователя {}", fileName, username);
                converterService.processApplication(new Application(message.getChatId(),
                        document, message.getFrom().getUserName()));
                sendMessage.setText("Начинаю обработку файла!");
                return sendMessage;
            }
            if (message.hasText()) {
                String text = update.getMessage().getText();
                log.info("Бот начал обработку команды {} от пользователя {}", text, username);
                if ("/start".equals(text)) {
                    sendMessage.setText("Привет!\n" + MESSAGE);
                    log.info("Пользователь {} запросил команду {}", username, text);
                    return sendMessage;
                }
                sendMessage.setText("Мне не известна такая команда:(\n" + MESSAGE);
                log.info("Пользователь {} отправил неизвестную команду {}", username, text);
                return sendMessage;
            }
        }
        log.warn("Пользователь выполнил незнакомое боту действие {}", update);
        return null;
    }
}
