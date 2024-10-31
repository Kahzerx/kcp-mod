package com.kahzerx.kcp.kcp;

import com.kahzerx.kcp.utils.OSUtils;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class KCPExecutor {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Process process;
    private final String binDir = "bin";
    public static int localClientPort = 0;
    public static boolean proxyReady = false;

    public void runClient(String remoteHost, int remotePort, int localPort) {
        stop();
        File clientFile = this.getClientBinFileString(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + this.binDir);
        if (clientFile == null) {
            LOGGER.error("Unable to get a valid client binary.");
            return;
        }
        if (!clientFile.canExecute()) {
            boolean executable = clientFile.setExecutable(true);
            if (!executable) {
                LOGGER.error("Unable to set this client binary executable.");
                return;
            }
        }
        LOGGER.info("Got a valid client!");
        String binPath = FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + this.binDir;
        localClientPort = localPort;
        Thread t = new Thread(() -> new KCPExecutor().runBinWithConfig(
                new String[]{
                        !OSUtils.getOSName().equalsIgnoreCase("windows") ? String.format("./%s", clientFile.getAbsolutePath()) : clientFile.getAbsolutePath(),
                        "-r", String.format("%s:%d", remoteHost, remotePort),
                        "-l", String.format(":%d", localPort),
                        "-mode", "fast3"
                }
        ));
        t.setName("KCP Client Process");
        t.start();
    }

    public void runServer() {
        stop();
        File serverFile = this.getServerBinFileString(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + binDir);
        if (serverFile == null) {
            LOGGER.error("Unable to get a valid server binary.");
            return;
        }
        if (!serverFile.canExecute()) {
            boolean executable = serverFile.setExecutable(true);
            if (!executable) {
                LOGGER.error("Unable to set this server binary executable.");
                return;
            }
        }
        String configDir = "config";
        File configFile = new File(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + configDir + File.separator + "kcp.json");
        if (!configFile.exists()) {
            LOGGER.error("Config file not found...");
            return;
        }
        LOGGER.info("Got a valid server!");
        Thread t = new Thread(() -> new KCPExecutor().runBinWithConfig(
                new String[]{
                        !OSUtils.getOSName().equalsIgnoreCase("windows") ? String.format("./%s", serverFile.getAbsolutePath()) : serverFile.getAbsolutePath(),
                        "-c", configFile.getAbsolutePath(),
                }
        ));
        t.setName("KCP Server Process");
        t.start();
    }

    private File getClientBinFileString(String binPath) {
        File binDir = new File(binPath);
        if (binDir.isDirectory()) {
            File[] files = binDir.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isFile() && f.getName().startsWith("client")) {
                    return f;
                }
            }
        }
        return null;
    }

    private File getServerBinFileString(String binPath) {
        File binDir = new File(binPath);
        if (binDir.isDirectory()) {
            File[] files = binDir.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isFile() && f.getName().startsWith("server")) {
                    return f;
                }
            }
        }
        return null;
    }

    public static void waitForKCP() {
        while (!proxyReady) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runBinWithConfig(String[] command) {
        try {
            process = new ProcessBuilder(command).redirectErrorStream(true).start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream())) {
            int c;
            StringBuilder stringBuilder = new StringBuilder();
            while ((c = inputStreamReader.read()) >= 0) {
                char ch = (char) c;
                if (ch == '\n') {
                    String line = stringBuilder.toString();
                    if (line.endsWith("key derivation done")) {
                        proxyReady = true;
                    }
                    LOGGER.info(line);
                    stringBuilder.setLength(0);
                } else {
                    stringBuilder.append(ch);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        proxyReady = false;
        Thread.currentThread().interrupt();
    }

    public static void stop() {
        if (process != null && process.isAlive()) {
            proxyReady = false;
            process.destroyForcibly();
        }
    }
}
