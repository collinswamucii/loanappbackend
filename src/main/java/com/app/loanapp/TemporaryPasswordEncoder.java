// For generating a hashed password.
package com.app.loanapp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TemporaryPasswordEncoder {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        System.out.println(encodedPassword);
    }
}