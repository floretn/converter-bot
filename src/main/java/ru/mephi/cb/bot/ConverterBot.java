package ru.mephi.cb.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

/**
 * Часть бота, осуществляющая взаимодействие с сервером Телеграмма.
 * @author Софронов И.Е.
 */
@Component
@Slf4j
public class ConverterBot extends TelegramLongPollingBot {

    private final BotFacade botFacade;

    public ConverterBot(BotFacade botFacade) {
        super(botFacade.getBotToken());
        this.botFacade = botFacade;
    }

    @Override
    public String getBotUsername() {
        return botFacade.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botFacade.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.getMessage().getFrom().getIsBot()) {
            if (!update.getMessage().getChat().isUserChat()) {
                log.info("Пользователь с id {} обратился к боту из группового чата или канала, запрос отклонён.", update.getMessage().getFrom().getId());
                return;
            }
            try {
                execute(botFacade.handleUpdate(update));
            } catch (TelegramApiException e) {
                log.error("Телеграм бот не смог отправить сообщение пользователю с id {}", update.getMessage().getFrom().getId(), e);
            } catch (Exception e) {
                log.error("Внутренняя ошибка", e);
            }
        }
    }

    public void sendDocument(File filePdf, Long chatId) {
        SendDocument document = SendDocument.builder()
                .chatId(chatId + "")
                .document(new InputFile(filePdf))
                .caption("Вот ваш сконвертированный файл!!!")
                .build();
        try {
            execute(document);
        } catch (TelegramApiException e) {
            log.error("", e);
        }
    }
}
