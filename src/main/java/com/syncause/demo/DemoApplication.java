package com.syncause.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.reflect.Proxy;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private com.syncause.demo.dao.SysConfigDao sysConfigDao;
    @Autowired
    private com.syncause.demo.dao.MocksDao mocksDao;
    @Autowired
    private com.syncause.demo.dao.CouponsDao couponsDao;
    private final ObjectMapper mapper = new ObjectMapper();
    private BankService bankService;

    // On application start, load environment configuration from the database
    @PostConstruct
    public void init() {
        // Load Locale (Scenario 5)
        String localeStr = sysConfigDao.getConfigValue("LOCALE");

        Locale.setDefault(localeStr != null ? new Locale(localeStr) : Locale.US);

        // Load dynamic proxy (Scenario 6)
        String balStr = mocksDao.getMockJson("BANK");

        double mockBal = balStr != null ? Double.parseDouble(balStr) : 0.0;
        bankService = (BankService) Proxy.newProxyInstance(
                DemoApplication.class.getClassLoader(),
                new Class[] { BankService.class },
                (proxy, method, args) -> mockBal // Always return the negative value from DB
        );
    }

    // --- Scenario 1: Multi-condition validation (The Composite Logic Trap) ---
    // Example request: /api/apply-coupon?code=SUMMER_2024&category=FOOD&amount=100
    @GetMapping("/apply-coupon")
    public String applyCoupon(
            @RequestParam String code,
            @RequestParam String category,
            @RequestParam double amount) {

        Map<String, Object> coupon = couponsDao.getCouponByCode(code);

        if (coupon == null)
            return "Error: Coupon code not found";
        String dbStatus = (String) coupon.get("STATUS");
        String dbCategory = (String) coupon.get("CATEGORY");
        Double minAmount = (Double) coupon.get("MIN_AMOUNT");
        Date expiryDate = (Date) coupon.get("EXPIRY_DATE");

        boolean isStatusActive = "ACTIVE".equals(dbStatus);

        boolean isNotExpired = expiryDate.toLocalDate().isAfter(LocalDate.now());

        boolean isAmountValid = amount >= minAmount;

        boolean isCategoryMatch = category.equals(dbCategory);

        if (isStatusActive && isNotExpired && isAmountValid && isCategoryMatch) {
            return "SUCCESS: Coupon Applied!";
        }

        return "FAILURE: Invalid Coupon (Conditions not met)";
    }

    // --- Scenario 2: Order ID ---
    @GetMapping("/create-order")
    public String createOrder(@RequestParam String userId) {
        String prefix = sysConfigDao.getConfigValue("ORDER_PREFIX");

        String oid = prefix + "_" + userId;
        if (Pattern.matches("^[A-Z0-9]+_\\d+$", oid))
            return "✅ Order Created: " + oid;
        throw new RuntimeException("Format Error: " + oid);
    }

    // --- Scenario 3: Login ---
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String userId) throws Exception {
        String json = mocksDao.getMockJson("LOGIN");

        Map map = mapper.readValue(json, Map.class);
        Boolean banned = (Boolean) map.get("isBanned");
        if (banned != null && banned)
            return "🚫 Blocked";
        return "⚠️ Login Success (Should be Blocked!)";
    }

    // --- Scenario 4: VIP ---
    static class User {
        boolean VIP;
        
        public boolean isVIP() {
            return VIP;
        }
        
        public void setVIP(boolean VIP) {
            this.VIP = VIP;
        }
    }

    @GetMapping("/check-vip")
    public String checkVip() throws Exception {
        String json = mocksDao.getMockJson("VIP");

        User u = mapper.readValue(json, User.class);
        return u.isVIP() ? "✅ Welcome VIP" : "❌ Access Denied";
    }

    // --- Scenario 5: Report ---
    @GetMapping("/generate-report")
    public String generateReport(@RequestParam double amount) {
        String res = String.format("%.2f", amount);

        if (res.contains(","))
            throw new RuntimeException("Invalid Format: " + res);
        return "✅ Report: " + res;
    }

    // --- Scenario 6: Bank ---
    interface BankService {
        double checkBalance();
    }

    @GetMapping("/bank-transfer")
    public String bankTransfer() {
        double bal = bankService.checkBalance();
        return bal > 0 ? "✅ Transfer OK" : "❌ Insufficient Funds: " + bal;
    }
}