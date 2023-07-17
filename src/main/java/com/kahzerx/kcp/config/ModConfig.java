package com.kahzerx.kcp.config;

public record ModConfig(boolean enabled, int port) {
    @Override
    public String toString() {
        return "enabled=" + enabled + "\nport=" + port;
    }
}
