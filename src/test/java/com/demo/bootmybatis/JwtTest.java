package com.demo.bootmybatis;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT测试类 - 使用新版本的jjwt库 (0.11.5)
 * 解决了Java 17兼容性问题
 */
public class JwtTest {

    // 使用更安全的密钥生成方式
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    @Test
    public void createJwtToken() {
        // 创建payload数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "12345");
        claims.put("username", "testuser");
        claims.put("role", "USER");

        // 创建JWT token
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)  // 使用新的签名方法
                .compact();

        System.out.println("Generated JWT Token: " + token);
        
        // 验证token
        validateJwtToken(token);
    }

    @Test
    public void validateJwtToken() {
        // 先创建一个token
        String token = createToken();
        validateJwtToken(token);
    }

    private String createToken() {
        return Jwts.builder()
                .setSubject("testuser")
                .claim("userId", "12345")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    private void validateJwtToken(String token) {
        try {
            // 解析和验证JWT token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // 使用新的解析器API
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("Token validation successful!");
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("User ID: " + claims.get("userId"));
            System.out.println("Role: " + claims.get("role"));
            System.out.println("Issued At: " + claims.getIssuedAt());
            System.out.println("Expires At: " + claims.getExpiration());

        } catch (JwtException e) {
            System.err.println("Token validation failed: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testExpiredToken() {
        // 创建一个已过期的token
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1秒前过期
                .signWith(SECRET_KEY)
                .compact();

        System.out.println("Testing expired token...");

        try {
            validateJwtToken(expiredToken);
        } catch (ExpiredJwtException e) {
            System.out.println("Expected: Token is expired - " + e.getMessage());
        }
    }

    @Test
    public void testInvalidSignature() {
        // 使用不同的密钥创建token
        SecretKey wrongKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(wrongKey)  // 使用错误的密钥
                .compact();

        System.out.println("Testing token with invalid signature...");

        try {
            validateJwtToken(tokenWithWrongSignature);
        } catch (SignatureException e) {
            System.out.println("Expected: Invalid signature - " + e.getMessage());
        }
    }
}