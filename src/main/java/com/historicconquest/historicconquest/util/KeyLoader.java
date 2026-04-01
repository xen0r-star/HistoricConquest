package com.historicconquest.historicconquest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyLoader {
    private static final Logger logger = LoggerFactory.getLogger(KeyLoader.class);


    public static PublicKey loadPublicKey() throws Exception {
        InputStream is = KeyLoader.class.getResourceAsStream("/keys/public.pem");

        if (is == null) {
            logger.error("public.pem not found in classpath!");
            throw new RuntimeException("public.pem not found");
        }

        String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
