package ru.mephi.cb.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Document;

/**
 * Класс для хранения заявки на обработку.
 * @author Софронов И.Е.
 */
@Data
@AllArgsConstructor
public class Application {
    private long chatId;
    private Document documentDocx;
    private String username;
}
