package com.javalabs;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class AuthDebugTest {
    @Test
    public void verifyHash1() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$8uXn3pKOnzh8X1B86Kk.O8X.9fC6Kk.O8X.9fC6Kk.O8X.9fC6";
        boolean matches = encoder.matches("password123", hash);
        System.out.println("Hash 1 (from user prompt) matches: " + matches);
    }

    @Test
    public void verifyHash2() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$UMxy0Qkhq/L2s3DnDVce0uXTx9dDhVGziUlh.mN/DeINfaDiSp8JC";
        boolean matches = encoder.matches("password123", hash);
        System.out.println("Hash 2 (from data.sql) matches: " + matches);
    }
}
