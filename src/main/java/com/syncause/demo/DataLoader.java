package com.syncause.demo;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DataLoader {

    // Force file mode and enable mixed mode to avoid lock conflicts
    private static final String DB_URL = "jdbc:h2:file:./demo_db;AUTO_SERVER=TRUE";

    public static void main(String[] args) throws Exception {
        System.out.println(">>> 🛠️  DATABASE INITIALIZER TOOL");

        // 1. Decrypt configuration
        Scanner scanner = new Scanner(System.in);
        String key = scanner.nextLine().trim();

        Map<String, String> config = decrypt(key);
        System.out.println(">>> ✅ Config Decrypted.");

        // 2. Initialize database
        try (Connection conn = DriverManager.getConnection(DB_URL, "sa", "")) {

            // --- Table 1: sys_config (maps config_key, config_value) ---
            System.out.println("... Resetting Table: sys_config");
            conn.createStatement().execute("DROP TABLE IF EXISTS sys_config");
            conn.createStatement()
                    .execute("CREATE TABLE sys_config (config_key VARCHAR(50), config_value VARCHAR(50))");

            // Insert data: 'LOCALE' and 'ORDER_PREFIX' are hardcoded as keys
            // So Spring Boot queries WHERE config_key='LOCALE' will match
            insert(conn, "INSERT INTO sys_config (config_key, config_value) VALUES (?, ?)", "LOCALE",
                    config.get("sys_locale"));
            insert(conn, "INSERT INTO sys_config (config_key, config_value) VALUES (?, ?)", "ORDER_PREFIX",
                    config.get("order_prefix"));

            // --- Table 2: upstream_mock (maps user_id, json_response) ---
            System.out.println("... Resetting Table: upstream_mock");
            conn.createStatement().execute("DROP TABLE IF EXISTS upstream_mock");
            conn.createStatement()
                    .execute("CREATE TABLE upstream_mock (user_id VARCHAR(50), json_response VARCHAR(255))");

            insert(conn, "INSERT INTO upstream_mock (user_id, json_response) VALUES (?, ?)", "u_992",
                    config.get("login_json"));

            // --- Table 3: mocks (kept for other scenarios) ---
            System.out.println("... Resetting Table: mocks");
            conn.createStatement().execute("DROP TABLE IF EXISTS mocks");
            conn.createStatement().execute("CREATE TABLE mocks (type VARCHAR(50), json VARCHAR(255))");

            insert(conn, "INSERT INTO mocks (type, json) VALUES (?, ?)", "BANK", config.get("bank_balance"));
            insert(conn, "INSERT INTO mocks (type, json) VALUES (?, ?)", "LOGIN", config.get("login_json"));
            insert(conn, "INSERT INTO mocks (type, json) VALUES (?, ?)", "VIP", config.get("vip_json"));

            // --- Table 3: coupons ---
            System.out.println("... Resetting Table: coupons");
            conn.createStatement().execute("DROP TABLE IF EXISTS coupons");
            conn.createStatement().execute(
                    "CREATE TABLE coupons (" +
                            "code VARCHAR(50), " +
                            "status VARCHAR(20), " +
                            "category VARCHAR(50), " +
                            "min_amount DOUBLE, " +
                            "expiry_date DATE)");
            insert(conn,
                    "INSERT INTO coupons (code, status, category, min_amount, expiry_date) VALUES (?, ?, ?, ?, ?)",
                    "SUMMER_2024",
                    config.get("coupon_status"),
                    config.get("coupon_category"),
                    Double.parseDouble(config.get("coupon_min_amount")),
                    java.sql.Date.valueOf(config.get("coupon_expiry_date")));

            System.out.println(">>> 🚀 Database initialized at: " + new File("demo_db.mv.db").getAbsolutePath());
            System.out.println(">>> You can now start the Spring Boot App.");
        }
    }

    private static void insert(Connection conn, String sql, Object... args) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++)
                ps.setObject(i + 1, args[i]);
            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new RuntimeException("Insert failed: " + sql);
        }
    }

    private static Map<String, String> decrypt(String key) throws Exception {
        byte[] enc = Files.readAllBytes(new File("config.enc").toPath());
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
        String raw = new String(cipher.doFinal(enc));

        Map<String, String> map = new HashMap<>();
        for (String line : raw.split("\n")) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2)
                map.put(parts[0].trim(), parts[1]);
        }
        return map;
    }
}