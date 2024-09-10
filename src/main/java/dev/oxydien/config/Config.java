package dev.oxydien.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.oxydien.SimpleModSync;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.Optional;

public class Config {
    private String path;
    private String downloadUrl;
    private String downloadDestination;
    public static Config instance;


    public Config(String path, @Nullable String downloadDestination) {
        this.path = path;
        this.downloadUrl = "";
        this.downloadDestination = downloadDestination;
        this.load();
        this.save();
        instance = this;

        SimpleModSync.LOGGER.info("Config file loaded");
    }

    public String getPath() {
        return path;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        this.save();
    }

    public String getDownloadDestination() {
        return downloadDestination;
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
            SimpleModSync.LOGGER.warn("Config file not found, creating a default one", e);
            return;
        } catch (IOException e) {
            SimpleModSync.LOGGER.error("Failed to read config file", e);
            return;
        }

        // Parse json
        JsonElement jsonElement = JsonParser.parseString(content.toString());

        this.downloadUrl = jsonElement.getAsJsonObject().get("download_url").getAsString();
        this.downloadDestination = jsonElement.getAsJsonObject().get("download_destination").getAsString();
    }

    // Serialize to json file
    public void save() {
        // Create json
        JsonObject json = new JsonObject();
        json.addProperty("download_url", this.downloadUrl);
        json.addProperty("download_destination", this.downloadDestination);

        // Write to json file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getPath()))) {
            bw.write(json.toString());
        } catch (IOException e) {
            SimpleModSync.LOGGER.error("Failed to write config file", e);
        }
    }
}
