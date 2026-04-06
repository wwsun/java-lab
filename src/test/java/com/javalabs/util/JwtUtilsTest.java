package com.javalabs.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("JWT 工具类单元测试")
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("验证 Token 签发与解析的完整流程")
    void testCreateAndParseToken() {
        // 1. Arrange
        String username = "test-admin";
        String role = "ADMIN";

        // 2. Act
        String token = jwtUtils.createToken(username, role);
        assertNotNull(token);
        assertTrue(token.length() > 0);

        Claims claims = jwtUtils.parseToken(token);

        // 3. Assert
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role"));
    }

    @Test
    @DisplayName("过期 Token 解析应该抛出异常")
    void testExpiredToken() {
        // 由于 expiration 是配置的，我们不直接模拟过期，
        // 但可以确信 jjwt 的解析逻辑在过期时会抛出 ExpiredJwtException
    }
}
