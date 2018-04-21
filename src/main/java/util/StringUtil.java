package util;

import java.util.UUID;

public class StringUtil {

    /**
     * @return String which contains only letters and digits.
     */
    public static String generateRandomAlphaNumericString() {

        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
