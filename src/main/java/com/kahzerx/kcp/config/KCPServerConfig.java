package com.kahzerx.kcp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kahzerx.kcp.KCPMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class KCPServerConfig {
    @SuppressWarnings("unused")
    public void createKCPConfig(int tcpServerPort) {
        File kcpConfig = new File(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + "config" + File.separator + "kcp.json");
        boolean make = kcpConfig.getParentFile().mkdirs();
        try {
            boolean created = kcpConfig.createNewFile();
            boolean writable = kcpConfig.setWritable(true);
            HashMap<String, String> defaultConfig = new HashMap<>();
            defaultConfig.put("listen", String.format(":%d", KCPMod.config.port()));
            defaultConfig.put("target", String.format("127.0.0.1:%d", tcpServerPort));
            defaultConfig.put("mode", "fast3");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonObject = gson.toJson(defaultConfig);
            FileWriter fw = new FileWriter(kcpConfig);
            fw.write(jsonObject);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
