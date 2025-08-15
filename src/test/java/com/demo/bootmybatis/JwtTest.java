package com.demo.bootmybatis;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT测试类 - 使用新版本的jjwt库 (兼容0.12.x版本)
 * 解决了Java 17兼容性问题和密钥安全性要求
 */
public class JwtTest {

    // 使用更安全的密钥生成方式 - 符合HS256算法要求（>=256位）
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    
    // 测试用的固定密钥（至少32字节）
    private static final String TEST_SECRET = "myTestSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm";
    private static final SecretKey FIXED_SECRET_KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));

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

    @Test
    public void testFixedSecretKey() {
        System.out.println("Testing with fixed secret key...");
        
        // 使用固定密钥创建token
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", "67890")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(FIXED_SECRET_KEY)  // 使用固定密钥
                .compact();

        System.out.println("Generated JWT Token with fixed key: " + token);
        
        // 验证token
        validateJwtTokenWithKey(token, FIXED_SECRET_KEY);
    }

    private void validateJwtTokenWithKey(String token, SecretKey key) {
        try {
            // 解析和验证JWT token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)  // 使用指定的密钥
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("Token validation with fixed key successful!");
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
}