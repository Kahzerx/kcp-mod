package com.kahzerx.kcp.protocol;

public enum Protocols {
    TCP("TCP"),
    KCP("KCP");

    private final String value;

    Protocols(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
