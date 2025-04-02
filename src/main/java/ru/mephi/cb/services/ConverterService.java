package ru.mephi.cb.services;

import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mephi.cb.beans.Application;
import ru.mephi.cb.bot.ConverterBot;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

import static ru.mephi.cb.beans.constants.Constants.*;

/**
 * Сервис, осуществляющий конвертацию файлов.
 * @author Софронов И.Е.
 */
@Service
@Slf4j
public class ConverterService {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    @Autowired
    private ConverterBot converterBot;
    @Autowired
    private UploadService uploadService;

    public synchronized void processApplication(Application application) {
        threadPool.execute(() -> {
            try {
                log.info("Файл {} от пользователя {} принят на обработку", application.getDocumentDocx().getFileName(), application.getUsername());

                Document document = new Document();
                log.info("Загружаю файл {} от пользователя {} с сервера Телеграм",
                        application.getDocumentDocx().getFileName(),
                        application.getUsername());
                File fileDocx = uploadService.uploadFile(application.getDocumentDocx(), converterBot.getBotToken());
                log.info("Файл {} от пользователя {} успешно загружен с сервера Телеграм",
                        application.getDocumentDocx().getFileName(),
                        application.getUsername());

                log.info("Начинаю конвертацию файла {} от пользователя {}", application.getDocumentDocx().getFileName(), application.getUsername());
                document.loadFromFile((fileDocx).getAbsolutePath());
                File filePdf = new File(fileDocx.getAbsolutePath().
                        substring(0, fileDocx.getAbsolutePath().lastIndexOf(".")) + PDF_PREFIX);
                try {
                    if (!filePdf.createNewFile()) {
                        log.error("Не могу создать файл {}. Прерываю обработку", filePdf.getAbsolutePath());
                        sendErrorMessage(application);
                        deleteTempFiles(fileDocx, filePdf);
                        return;
                    }
                } catch (IOException e) {
                    try {
                        sendErrorMessage(application);
                    } catch (TelegramApiException telegramApiException) {
                        log.error(LOG_ERROR_MESSAGE_CANT_SEND_MESSAGE + "{}", application.getUsername(), telegramApiException);
                    }
                    log.error("Не могу создать файл {}. Прерываю обработку", filePdf.getAbsolutePath(), e);
                    deleteTempFiles(fileDocx, filePdf);
                    return;
                } catch (TelegramApiException e) {
                    log.error(LOG_ERROR_MESSAGE_CANT_SEND_MESSAGE + "{}", application.getUsername(), e);
                }
                document.saveToFile(filePdf.getAbsolutePath(), FileFormat.PDF);
                log.info("Файл {} от пользователя {} успешно сконвертирован, отправляю файл пользователю, удаляю временные файлы", application.getDocumentDocx().getFileName(), application.getUsername());
                converterBot.sendDocument(filePdf, application.getChatId());
                deleteTempFiles(fileDocx, filePdf);
                log.info("Обработка файла {} от пользователя {} завершена", application.getDocumentDocx().getFileName(), application.getUsername());
            } catch (Exception e) {
                try {
                    sendErrorMessage(application);
                } catch (TelegramApiException telegramApiException) {
                    e.addSuppressed(telegramApiException);
                    log.error("", e);
                }
            }
        });
    }

    private void sendErrorMessage(Application application) throws TelegramApiException {
        converterBot.execute(new SendMessage(application.getChatId() + "", "Внутренняя ошибка!"));
    }

    private void deleteTempFiles(File fileDocx, File filePdf) {
        File tempDirectory = fileDocx.getParentFile();
        try {
            if (!fileDocx.delete()) {
                log.error("Не могу удалить временный файл {}", fileDocx);
            }
        } catch (SecurityException exception) {
            log.error("", exception);
        }
        try {
            if (!filePdf.delete()) {
                log.error("Не могу удалить временный файл {}", filePdf);
            }
        } catch (SecurityException exception) {
            log.error("", exception);
        }
        try {
            if (!tempDirectory.delete()) {
                log.error("Не могу удалить временную директорию {}", tempDirectory);
            }
        } catch (Exception exception) {
            log.error("", exception);
        }
    }
}
