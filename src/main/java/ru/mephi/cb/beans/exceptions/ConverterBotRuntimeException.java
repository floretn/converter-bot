package ru.mephi.cb.beans.exceptions;

/**
 * Класс локального исключения.
 * @author Софронов И.Е.
 */
public class ConverterBotRuntimeException extends RuntimeException {
    public ConverterBotRuntimeException(String message) {
        super(message);
    }

    public ConverterBotRuntimeException(Exception exception) {
        super(exception);
    }
}
