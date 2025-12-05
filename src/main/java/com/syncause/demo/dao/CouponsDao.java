package com.syncause.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CouponsDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getCouponByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, code);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }
}
