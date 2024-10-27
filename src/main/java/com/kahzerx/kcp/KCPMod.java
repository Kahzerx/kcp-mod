package com.kahzerx.kcp;

import com.kahzerx.kcp.config.ModConfig;
import com.kahzerx.kcp.kcp.KCPExecutor;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;

public class KCPMod implements DedicatedServerModInitializer {
    public static ModConfig config;
    private final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("unused")
    @Override
    public void onInitializeServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(KCPExecutor::stop));
        Path configDir = FabricLoader.getInstance().getConfigDir();
        if (configDir.isAbsolute()) {
            return;
        }
        File modConfig = new File(configDir + File.separator + "kcp.conf");
        if (!modConfig.isFile()) {
            try {
                boolean created = modConfig.createNewFile();
                boolean writable = modConfig.setWritable(true);
                FileWriter fw = new FileWriter(modConfig);
                ModConfig cfg = new ModConfig(false, 25577);
                fw.write(cfg.toString());
                fw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(modConfig));
            StringBuilder sb = new StringBuilder();
            String configs;
            while ((configs = br.readLine()) != null) {
                sb.append(configs).append("\n");
            }
            String[] kvs = sb.toString().split("\n");
            int port = 0;
            boolean enabled = false;
            for (String kv : kvs) {
                String[] keyValues = kv.trim().split("=");
                if (keyValues.length < 2) {
                    continue;
                }
                if (keyValues[0].strip().equalsIgnoreCase("port")) {
                    String value = keyValues[1];
                    port = Integer.parseInt(value);
                }
                if (keyValues[0].strip().equalsIgnoreCase("enabled")) {
                    String value = keyValues[1];
                    enabled = Boolean.parseBoolean(value);
                }
            }
            if (enabled && (port <= 0 || port >= 65536)) {
                LOGGER.error("Invalid port!");
                throw new RuntimeException();
            }
            config = new ModConfig(enabled, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
