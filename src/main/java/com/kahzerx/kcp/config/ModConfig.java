package com.kahzerx.kcp.config;

public class ModConfig {
    private final boolean enabled;
    private final int port;

    public ModConfig(boolean enabled, int port) {
        this.enabled = enabled;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "enabled=" + enabled + "\nport=" + port;
    }
}
