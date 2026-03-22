package com.historicconquest.server.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyLoaderUtil {
    public static PrivateKey loadPrivateKey() throws Exception {
        InputStream is = KeyLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream("keys/private.pem");

        if (is == null) {
            throw new RuntimeException("private.pem not found in resources/keys");
        }

        String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static PublicKey loadPublicKey() throws Exception {
        InputStream is = KeyLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream("keys/public.pem");

        if (is == null) {
            throw new RuntimeException("private.pem not found in resources/keys");
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
