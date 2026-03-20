package com.historicconquest.historicconquest.util;

import com.historicconquest.historicconquest.Constant;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyLoaderUtils {
    public static PublicKey loadPublicKey() throws Exception {
        InputStream is = KeyLoaderUtils.class.getResourceAsStream(Constant.PATH + "keys/public.pem");
        if (is == null) {
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
