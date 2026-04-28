// FILE 26: DateTimeUtil.java
package com.eduplatform.certificate.util;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    public static long getDaysUntilExpiration(LocalDateTime expiresAt) {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
    }

    public static boolean isExpired(LocalDateTime expiresAt) {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public static LocalDateTime addYears(LocalDateTime date, int years) {
        return date.plusYears(years);
    }
}