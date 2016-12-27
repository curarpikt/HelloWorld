package com.chanapp.chanjet.customer.vo;

import java.util.ArrayList;
import java.util.List;

public class VORowSet<T> {

    private long total;

    private List<T> items = new ArrayList<T>();

    public void add(T row) {
        items.add(row);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

}
