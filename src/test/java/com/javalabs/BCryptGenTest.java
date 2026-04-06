package com.javalabs;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenTest {
    @Test
    public void gen() {
        System.out.println(new BCryptPasswordEncoder().encode("password123"));
    }
}
