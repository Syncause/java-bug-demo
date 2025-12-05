package com.syncause.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.sql.Date;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private com.syncause.demo.dao.CouponsDao couponsDao;

    @GetMapping("/coupon-data")
    public Map<String, Object> getCouponData() {
        return couponsDao.getCouponByCode("SUMMER_2024");
    }

    @GetMapping("/debug-coupon")
    public String debugCoupon(
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

        StringBuilder sb = new StringBuilder();
        sb.append("Input category: '").append(category).append("'\n");
        sb.append("DB category: '").append(dbCategory).append("'\n");
        sb.append("Normalized input: '").append(normalizeString(category)).append("'\n");
        sb.append("Normalized DB: '").append(normalizeString(dbCategory)).append("'\n");
        sb.append("Categories equal: ").append(normalizeString(category).equals(normalizeString(dbCategory))).append("\n");
        sb.append("Status active: ").append("ACTIVE".equals(dbStatus)).append("\n");
        sb.append("Not expired: ").append(!LocalDate.now().isAfter(expiryDate.toLocalDate())).append("\n");
        sb.append("Amount valid: ").append(amount >= minAmount).append("\n");
        sb.append("Min amount: ").append(minAmount).append("\n");
        sb.append("Current amount: ").append(amount).append("\n");

        return sb.toString();
    }

    /**
     * Normalizes a string by removing all non-printable characters and trimming whitespace
     */
    private String normalizeString(String str) {
        if (str == null) {
            return null;
        }
        // Remove all non-printable characters and trim
        return str.replaceAll("[^\\p{Print}]", "").trim();
    }
}