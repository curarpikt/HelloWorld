package com.chanapp.chanjet.customer.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;

public class Row extends HashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = -8936873304860953471L;

    public static final String ENTITY_NAME = "entityName";

    public Row() {
    }

    public String getString(String key) {
        return (String) this.get(key);
    }

    public Integer getInt(String key) {
        if (this.get(key) != null) {
            return Integer.parseInt(this.get(key).toString());
        }
        return null;
    }

    public double getDouble(String key) {
        return (double) this.get(key);
    }

    public Long getLong(String key) {
        if (this.get(key) != null) {
            return Long.parseLong(this.get(key).toString());
        }
        return null;
    }

    public BigDecimal getBigDecimal(String key) {
        return (BigDecimal) this.get(key);
    }

}
