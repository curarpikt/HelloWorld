package com.chanapp.chanjet.customer.cache;

import java.util.ArrayList;
import java.util.Map;

public class CustomerLayout {
    private ArrayList<Map<String, Object>> customerView = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> customerEdit = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> contactView = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> customerManager = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> contactManager = new ArrayList<Map<String, Object>>();

    public ArrayList<Map<String, Object>> getCustomerView() {
        return customerView;
    }

    public void setCustomerView(ArrayList<Map<String, Object>> customerView) {
        this.customerView = customerView;
    }

    public ArrayList<Map<String, Object>> getCustomerEdit() {
        return customerEdit;
    }

    public void setCustomerEdit(ArrayList<Map<String, Object>> customerEdit) {
        this.customerEdit = customerEdit;
    }

    public ArrayList<Map<String, Object>> getContactView() {
        return contactView;
    }

    public void setContactView(ArrayList<Map<String, Object>> contactView) {
        this.contactView = contactView;
    }

    public ArrayList<Map<String, Object>> getCustomerManager() {
        return customerManager;
    }

    public void setCustomerManager(ArrayList<Map<String, Object>> customerManager) {
        this.customerManager = customerManager;
    }

    public ArrayList<Map<String, Object>> getContactManager() {
        return contactManager;
    }

    public void setContactManager(ArrayList<Map<String, Object>> contactManager) {
        this.contactManager = contactManager;
    }

}
