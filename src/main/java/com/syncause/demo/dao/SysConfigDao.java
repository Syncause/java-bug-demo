package com.syncause.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SysConfigDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getConfigValue(String key) {
        String sql = "SELECT config_value FROM sys_config WHERE config_key=?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, key);
        } catch (Exception e) {
            return null;
        }
    }
}
