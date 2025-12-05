package com.syncause.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MocksDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getMockJson(String type) {
        String sql = "SELECT json FROM mocks WHERE type=?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, type);
        } catch (Exception e) {
            return null;
        }
    }
}
