package com.historicconquest.server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final long VALIDITY_TIME = 2 * 60 * 60 * 1000; // 2 hours
    private static final PrivateKey privateKey;
    private static final PublicKey publicKey;


    static {
        try {
            privateKey = KeyLoader.loadPrivateKey();
            publicKey = KeyLoader.loadPublicKey();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load keys", e);
        }
    }


    public static String generateToken(String playerId, String roomCode) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + VALIDITY_TIME);

        return Jwts.builder()
                   .setSubject(playerId)
                   .claim("roomCode", roomCode)
                   .setIssuedAt(now)
                   .setExpiration(expiry)
                   .signWith(privateKey, SignatureAlgorithm.RS256)
                   .compact();
    }

    public static Map<String, String> verifyToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                                .setSigningKey(publicKey)
                                .build()
                                .parseClaimsJws(token)
                                .getBody();

            Map<String, String> result = new HashMap<>();
            result.put("playerId", claims.getSubject());
            result.put("roomCode", claims.get("roomCode", String.class));
            return result;

        } catch (Exception e) {
            logger.warn("Failed to verify JWT token", e);
            throw new RuntimeException("Invalid JWT token");
        }
    }
}