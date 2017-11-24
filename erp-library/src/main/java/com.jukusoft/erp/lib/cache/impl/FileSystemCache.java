package com.jukusoft.erp.lib.cache.impl;

import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.utils.FileUtils;
import com.jukusoft.erp.lib.utils.HashUtils;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileSystemCache implements ICache {

    protected String cacheDir = "";

    public FileSystemCache (String cacheDir, String cacheName, ILogging logger) {
        //create cache directory, if doesnt exists.
        if (!new File(cacheDir).exists()) {
            new File(cacheDir).mkdirs();
        }

        if (!cacheDir.endsWith("/")) {
            cacheDir += "/";
        }

        this.cacheDir = cacheDir;
    }

    @Override
    public void put(String key, JsonObject data) {
        //get filename
        String filename = this.getFileName(key);

        //generate path
        String path = this.cacheDir + filename;

        //save content
        try {
            FileUtils.writeFile(path, data.encode(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(String key) {
        //get filename
        String filename = this.getFileName(key);

        //generate path
        String path = this.cacheDir + filename;

        File file = new File(path);

        //remove file, if exists
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void removeAll() {
        //
    }

    @Override
    public boolean contains(String key) {
        //get filename
        String filename = this.getFileName(key);

        //generate path
        String path = this.cacheDir + filename;

        File file = new File(path);

        return file.exists();
    }

    @Override
    public JsonObject get(String key) {
        //get filename
        String filename = this.getFileName(key);

        //generate path
        String path = this.cacheDir + filename;

        File file = new File(path);

        if (!file.exists()) {
            return null;
        }

        try {
            String jsonStr = FileUtils.readFile(path, StandardCharsets.UTF_8);
            return new JsonObject(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getFileName (String key) {
        String hash = HashUtils.computeMD5Hash(key);
        return hash + ".tmp";
    }

    @Override
    public void cleanUp() {
        //TODO: truncate cache directory
    }

}
