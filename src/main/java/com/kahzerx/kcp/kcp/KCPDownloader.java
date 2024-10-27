package com.kahzerx.kcp.kcp;

import com.google.gson.*;
import com.kahzerx.kcp.utils.OSUtils;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;


public class KCPDownloader {
    private final String fileStart = String.format("kcptun-%s-%s-", OSUtils.getOSName(), OSUtils.getArchName());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String binDir = "bin";

    public KCPDownloader() {}

    public boolean downloadClient() {
        return this.kcpPrep();
    }

    public boolean downloadServer() {
        if (!this.kcpPrep()) {
            return false;
        }
        new KCPExecutor().runServer();
        return true;
    }

    private boolean kcpPrep() {
        String url = this.getValidTarGZUrl();
        if (url == null) {
            LOGGER.error("Unable to find a valid KCP client...");
            return false;
        }
        LOGGER.info("Valid KCP client found! > {}", url);
        String targzDir = "compressed";
        @SuppressWarnings("unused")
        boolean createTarDir = new File(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + targzDir).mkdirs();

        @SuppressWarnings("unused")
        boolean createBinDir = new File(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + this.binDir).mkdirs();
        boolean downloaded = this.downloadCompressed(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + targzDir, url);
        if (!downloaded) {
            LOGGER.error("Error trying to download the compressed binary.");
            return false;
        }
        LOGGER.info("Downloaded kcp.tar.gz file");
        boolean unCompressed = this.unTar(FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + targzDir, FabricLoader.getInstance().getConfigDir() + File.separator + "kcp_data" + File.separator + this.binDir);
        if (!unCompressed) {
            LOGGER.error("Error on unTar kcp.tar.gz, unable to uncompress.");
            return false;
        }
        return true;
    }

    private String getValidTarGZUrl() {
        try {
            StringBuilder res = new StringBuilder();
            String RELEASE = "https://api.github.com/repos/xtaci/kcptun/releases/latest";
            HttpURLConnection conn = (HttpURLConnection) new URL(RELEASE).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            for (String line; (line = reader.readLine()) != null;) {
                res.append(line);
            }
            JsonElement json = JsonParser.parseString(res.toString());
            JsonArray assetsArray = json.getAsJsonObject().get("assets").getAsJsonArray();
            if (assetsArray == null) {
                LOGGER.error("Failed to get latest assets...");
                return null;
            }
            for (JsonElement asset : assetsArray) {
                JsonObject assetObj = asset.getAsJsonObject();
                String assetName = assetObj.get("name").getAsString();
                if (assetName.startsWith(this.fileStart)) {
                    return assetObj.get("browser_download_url").getAsString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean downloadCompressed(String path, String url) {
        emptyDir(path);

        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(path + "/kcp.tar.gz")) {
            byte[] data = new byte[2048];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOutputStream.write(data, 0, byteContent);
            }
            inputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void emptyDir(String path) {
        File compressedDir = new File(path);
        if (compressedDir.isDirectory()) {
            File[] files = compressedDir.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isDirectory()) {
                    continue;
                }
                @SuppressWarnings("unused")
                boolean deleted = f.delete();
            }
        }
    }

    private boolean unTar(String compressedDirPath, String resultDir) {
        emptyDir(resultDir);
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(compressedDirPath + "/kcp.tar.gz");
            GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fileInputStream));
            tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
            TarArchiveEntry tarArchiveEntry;
            while ((tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
                if (!tarArchiveEntry.isDirectory()) {
                    File out = new File(resultDir + File.separator + tarArchiveEntry.getName());
                    FileOutputStream fileOutputStream = new FileOutputStream(out);
                    @SuppressWarnings("unused")
                    boolean created = out.getParentFile().mkdirs();
                    IOUtils.copy(tarArchiveInputStream, fileOutputStream);
                    fileOutputStream.close();
                }
            }
            gzipInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (tarArchiveInputStream != null) {
                try {
                    tarArchiveInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
