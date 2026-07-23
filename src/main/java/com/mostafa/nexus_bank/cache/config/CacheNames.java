package com.mostafa.nexus_bank.cache.config;

public final class CacheNames {

    private CacheNames() {
    }

    public static final String USERS = "users";
    public static final String ACCOUNTS = "accounts";
    public static final String ROLES = "roles";
    public static final String OTP = "otp";
    public static final String REFRESH_TOKENS = "refreshTokens";
    public static final String NOTIFICATIONS = "notifications";
    public static final String NOTIFICATION_COUNT = "notificationCount";
    public static final String JWT_BLACKLIST = "jwtBlacklist";
    public static final String TRANSACTIONS = "transactions";

    public static String userKey(Object id) {
        return "user::" + id;
    }

    public static String accountKey(Object id) {
        return "account::" + id;
    }

    public static String accountNumberKey(String accountNumber) {
        return "account::number::" + accountNumber;
    }

    public static String roleKey(Object id) {
        return "role::" + id;
    }

    public static String roleByNameKey(String name) {
        return "role::name::" + name;
    }

    public static String otpKey(String email, String purpose) {
        return "otp::" + email + "::" + purpose;
    }

    public static String notificationCountKey(Object userId) {
        return "notification::count::" + userId;
    }

    public static String jwtBlacklistKey(String token) {
        return "jwt::blacklist::" + token;
    }

    public static String failedLoginKey(String email) {
        return "login::failed::" + email;
    }
}
