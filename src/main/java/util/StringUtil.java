package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class StringUtil {

    /**
     * @return String which contains only letters and digits.
     */
    public static String generateRandomAlphaNumericString() {

        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String fromInputStreamToString(InputStream inputStream, String encoding) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, encoding))) {
            char[] charBuffer = new char[256];
            int bytesRead;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        }
        return  stringBuilder.toString();
    }
}
