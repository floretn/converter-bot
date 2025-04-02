package ru.mephi.cb.services;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import ru.mephi.cb.beans.exceptions.ConverterBotRuntimeException;

import java.io.*;
import java.net.URL;

import static ru.mephi.cb.beans.constants.Constants.PATH_TO_TEMP_DIRECTORY;

/**
 * Сервис по загрузке файлов, отправленных пользователями.
 * @author Софронов И.Е.
 */
@Service
public class UploadService {

    private static long counter = Long.MIN_VALUE;

    public File uploadFile(Document document, String botToken) {
        File fileDocx;
        try {
            URL download = getUrl(document, botToken);
            fileDocx = createNewTempFile(document.getFileName());
            try (InputStream in = download.openStream();
                 OutputStream writer = new FileOutputStream(fileDocx)) {
                byte[] buffer = new byte[1024];
                int c;
                while ((c = in.read(buffer)) > 0) {
                    writer.write(buffer, 0, c);
                }
                writer.flush();
            }
        } catch (IOException ioe) {
            throw new ConverterBotRuntimeException(ioe);
        }
        return fileDocx;
    }

    private static URL getUrl(Document document, String botToken) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + document.getFileId());
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = br.readLine();
        JSONObject jResult = new JSONObject(res);
        JSONObject jPath = jResult.getJSONObject("result");
        String filePath = jPath.getString("file_path");
        return new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath);
    }

    private synchronized File createNewTempFile(String fileName) throws IOException {
        File tempDirectory = new File(PATH_TO_TEMP_DIRECTORY);
        if (!tempDirectory.exists()) {
            if (!tempDirectory.mkdir()) {
                throw new ConverterBotRuntimeException("Не могу создать директорию временных файлов " +
                        tempDirectory.getAbsolutePath());
            }
        }
        String directoryPath = PATH_TO_TEMP_DIRECTORY + "/temp" + counter;
        File directory = new File(directoryPath);
        while (directory.exists()) {
            incrementCounter();
            directoryPath = PATH_TO_TEMP_DIRECTORY + "/temp" + counter;
            directory = new File(directoryPath);
        }
        if (!directory.mkdir()) {
            throw new ConverterBotRuntimeException("Не могу создать директорию " + directory.getAbsolutePath());
        }
        File file = new File(directoryPath + "/" + fileName);
        if (!file.createNewFile()) {
            throw new ConverterBotRuntimeException("Не могу создать файл " + file.getAbsolutePath());
        }
        incrementCounter();
        return file;
    }

    private synchronized void incrementCounter() {
        if (counter == Long.MAX_VALUE) {
            counter = Long.MIN_VALUE;
            return;
        }
        counter++;
    }
}
