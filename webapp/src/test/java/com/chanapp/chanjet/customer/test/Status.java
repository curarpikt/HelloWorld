package com.chanapp.chanjet.customer.test;

public enum Status {
    OK(200, "OK"), NO_CONTENT(204, "No Content"), BAD_REQUEST(400, "Bad Request"), UNAUTHORIZED(401,
            "Unauthorized"), FORBIDDEN(403, "Forbidden");

    private int code;
    private String reason;

    private Status(int statusCode, final String reasonPhrase) {
        this.code = statusCode;
        this.reason = reasonPhrase;
    }

    public int getStatusCode() {
        return code;
    }

    public String getReasonPhrase() {
        return toString();
    }

    @Override
    public String toString() {
        return reason;
    }
}
