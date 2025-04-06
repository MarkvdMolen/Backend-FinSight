package com.finsight.UtilClasses;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public class HashUtil {

    public static String generateTransactionHash(String account, String recipient, String description, BigDecimal amount, LocalDate date) {
        String raw = String.format("%s|%s|%s|%s|%s",
                account.trim(),
                recipient.trim(),
                description.trim(),
                amount.toPlainString(),
                date.toString());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }
}
