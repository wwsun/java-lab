package com.javalabs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成、解析和验证 JSON Web Token
 */
@Component
public class JwtUtils {

    // 提示：生产环境下应从配置文件读取此密钥
    // HS256 算法要求密钥长度至少为 256 位（32 字节）
    private static final String SECRET_STRING = "java-labs-learning-secret-key-2024-for-jwt-256bit";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    
    // 过期时间：24 小时
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

    /**
     * 根据用户名生成 Token
     *
     * @param username 用户名
     * @param role 用户角色
     * @return 生成的 JWT 字符串
     */
    public String createToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中获取载荷 (Claims)
     *
     * @param token JWT 字符串
     * @return 解析后的 Claims 对象
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 Token 是否过期
     *
     * @param claims 载荷对象
     * @return true 如果已过期，false 否则
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }
}
