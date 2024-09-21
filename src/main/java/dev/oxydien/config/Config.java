package dev.oxydien.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.oxydien.logger.Log;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class Config {
    private final String path;
    private boolean autoDownload;
    private String downloadUrl;
    private String downloadDestination;
    public static Config instance;


    public Config(String path, @Nullable String downloadDestination) {
        this.path = path;
        this.autoDownload = true;
        this.downloadUrl = "";
        this.downloadDestination = downloadDestination;
        this.load();
        this.save();
        instance = this;

        Log.Log.debug("Config file loaded");
    }

    public String getPath() {
        return this.path;
    }

    public boolean getAutoDownload() {
        return this.autoDownload;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public String getDownloadDestination(String type) {
        return this.getDownloadDestination() + "/" + type;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        this.save();
    }

    public String getDownloadDestination() {
        return this.downloadDestination;
    }

    // Deserialize from json file

    public void load() {
        // Read from json file
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(this.getPath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (FileNotFoundException e) {
            Log.Log.warn("config.load", "Config file not found, creating a default one", e);
            return;
        } catch (IOException e) {
            Log.Log.error("config.load.IOException", "Failed to read config file", e);
            return;
        }

        // Parse json
        JsonElement jsonElement = JsonParser.parseString(content.toString());

        this.autoDownload = jsonElement.getAsJsonObject().get("auto_download") == null ||
                jsonElement.getAsJsonObject().get("auto_download").getAsBoolean();

        var downloadUrl = jsonElement.getAsJsonObject().get("download_url");
        if (downloadUrl != null && !downloadUrl.getAsString().isEmpty()) {
            this.downloadUrl = downloadUrl.getAsString();
        }

        var downloadDestination = jsonElement.getAsJsonObject().get("download_destination");
        if (downloadDestination != null && downloadDestination.getAsString().isEmpty()) {
            this.downloadDestination = downloadDestination.getAsString();
        }
    }

    // Serialize to json file
    public void save() {
        // Create json
        JsonObject json = new JsonObject();
        json.addProperty("auto_download", this.autoDownload);
        json.addProperty("download_url", this.downloadUrl);
        json.addProperty("download_destination", this.downloadDestination);

        // Write to json file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getPath()))) {
            bw.write(json.toString());
        } catch (IOException e) {
            Log.Log.error("config.save.IOException", "Failed to write config file", e);
        }
    }
}
