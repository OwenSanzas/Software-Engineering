package app.services.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class ShortIdGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    public static String generateShortId() {
        byte[] randomBytes = new byte[8];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
