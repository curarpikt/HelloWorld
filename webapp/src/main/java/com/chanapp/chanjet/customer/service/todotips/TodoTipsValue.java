package com.chanapp.chanjet.customer.service.todotips;

public class TodoTipsValue {

    private Long id;
    private String localId;
    private String todoTips;
    private Long sortBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getTodoTips() {
        return todoTips;
    }

    public void setTodoTips(String todoTips) {
        this.todoTips = todoTips;
    }

    public Long getSortBy() {
        return sortBy;
    }

    public void setSortBy(Long sortBy) {
        this.sortBy = sortBy;
    }

}
