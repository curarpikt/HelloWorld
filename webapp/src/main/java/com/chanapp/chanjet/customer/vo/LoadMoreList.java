package com.chanapp.chanjet.customer.vo;

import java.util.ArrayList;
import java.util.List;

public class LoadMoreList {

    private boolean hasMore;

    private List<Row> items = new ArrayList<Row>();

    public void addItem(Row item) {
        this.items.add(item);
    }

    public List<Row> getItems() {
        return items;
    }

    public void setItems(List<Row> items) {
        this.items = items;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

}
