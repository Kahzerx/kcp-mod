package com.kahzerx.kcp;

import com.kahzerx.kcp.kcp.KCPDownloader;
import com.kahzerx.kcp.kcp.KCPExecutor;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KCPClientMod implements ClientModInitializer {
    private final Logger LOGGER = LogManager.getLogger();
    public static String SRV_QUERY = "_minecraft._udp.%s";

    @SuppressWarnings("unused")
    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(KCPExecutor::stop));
        boolean downloaded = new KCPDownloader().downloadClient();
        if (!downloaded) {
            this.LOGGER.error("KCP download failed");
        }
    }
}
