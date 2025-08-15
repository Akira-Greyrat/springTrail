package com.demo.bootmybatis.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类 - 兼容Java 17+
 * 使用新版本的jjwt库 (0.11.5)
 */
@Component
public class JwtUtil {

    // 从配置文件读取，如果没有则使用默认值
    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24小时
    private Long expiration;

    private SecretKey getSigningKey() {
        // 对于HS256算法，密钥必须至少256位（32字节）
        // 如果配置的secret长度不够，则使用随机生成的安全密钥
        if (secret.length() < 32) {
            System.out.println("Warning: Configured secret is too short, using auto-generated secure key");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        // 确保密钥长度至少32字节
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 如果不够32字节，填充到32字节
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            return Keys.hmacShaKeyFor(paddedKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据用户信息生成token
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 根据用户信息和额外声明生成token
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        return createToken(extraClaims, username);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 获取token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取特定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 获取token中的所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String usernameFromToken = getUsernameFromToken(token);
            return (usernameFromToken.equals(username) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证token（仅检查签名和过期时间）
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取自定义声明
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claims.get(claimName);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Failed to get claim from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + expiration));
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey())
                    .compact();
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Failed to refresh token: " + e.getMessage());
            return null;
        }
    }
}