package com.kahzerx.kcp.protocol;

public enum Protocols {
    TCP("TCP"),
    KCP("KCP");

    private final String value;
    public String toString() {
        return value;
    }

    Protocols(String value) {
        this.value = value;
    }
}
