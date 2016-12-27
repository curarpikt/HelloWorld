package com.chanapp.chanjet.customer.http;

import java.util.List;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private Map<String, List<String>> headerFields;
    private byte[] bytes;
    private String string;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

}