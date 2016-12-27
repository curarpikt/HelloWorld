package com.chanapp.chanjet.customer.vo;

import java.util.ArrayList;
import java.util.List;

public class RowSet {

    private long total;

    private List<Row> items = new ArrayList<Row>();

    public void add(Row row) {
        items.add(row);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<Row> getItems() {
        return items;
    }

    public void setItems(List<Row> items) {
        this.items = items;
    }

}
