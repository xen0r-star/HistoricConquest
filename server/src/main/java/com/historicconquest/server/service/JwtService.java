package com.historicconquest.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {
    private static final String SECRET_KEY = "I LOVE EATING PIGS, THEY'RE SO DELICIOUS";
    private static final long VALIDITY_TIME = 2 * 60 * 60 * 1000; // 2 hours


    public static String generateToken(String playerId, String roomCode) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + VALIDITY_TIME);

        return Jwts.builder()
                .setSubject(playerId)
                .claim("roomCode", roomCode)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Map<String, String> verifyToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Map<String, String> result = new HashMap<>();
            result.put("playerId", claims.getSubject());
            result.put("roomCode", claims.get("roomCode", String.class));
            return result;

        } catch (Exception e) {
            return null;
        }
    }
}