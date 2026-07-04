package dev.oxydien.simpleModSync.config;

import com.google.gson.*;
import dev.oxydien.simpleModSync.log.Log;

import java.io.*;
import java.nio.file.Path;

public class Config {
    public static Config instance;

    private final Path path;
    private final ConfigValues values;

    public Config(Path configFilePath) {
        this.path = configFilePath;

        this.values = ConfigValues.createDefault();

        this.load();
        this.save();

        instance = this;

        Log.debug("Config file loaded");
    }

    public Path getPath() {
        return this.path;
    }

    //region I/O
    // Deserialize from json file
    public void load() {
        // Read from json file
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(this.getPath().toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (FileNotFoundException e) {
            Log.warning("config.load", "Config file not found, creating a default one", e);
            return;
        } catch (IOException e) {
            Log.error("config.load.IOException", "Failed to read config file", e);
        }

        // Parse json
        var values = this.values;
        JsonElement jsonElement = JsonParser.parseString(content.toString());
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var jsonObject = jsonElement.getAsJsonObject();
            values.readFrom(jsonObject);
        }
    }

    // Serialize to json file
    public void save() {
        // Create json
        JsonObject json = new JsonObject();
        this.values.writeTo(json);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        var content = gson.toJson(json);

        // Write to json file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getPath().toFile()))) {
            bw.write(content);
        } catch (IOException e) {
            Log.error("config.save.IOException", "Failed to write config file", e);
        }
    }
    //endregion

    //region Data
    public boolean getSyncOnStartup() {
        return this.values.getSyncConfig().getSyncOnStartup();
    }
    public void setSyncOnStartup(boolean allowed) {
        this.values.getSyncConfig().setSyncOnStartup(allowed);
        this.save();
    }

    public String getSchemaFileUrl() {
        return this.values.getSyncConfig().getSyncSchemaFileUrl();
    }
    public void setSchemaFileUrl(String schemaFileUrl) {
        this.values.getSyncConfig().setSyncSchemaFileUrl(schemaFileUrl);
        this.save();
    }

    public boolean hasVisitedInitScreen() {
        return this.values.getSeenConfig().hasVisitedInitScreen();
    }
    public void setHasVisitedInitScreen(boolean seenInitScreen) {
        this.values.getSeenConfig().setHasVisitedInitScreen(seenInitScreen);
        this.save();
    }

    public boolean isSyncInProgressPopupsAllowed() {
        return this.values.getSeenConfig().isSyncInProgressPopupsAllowed();
    }
    public void setAllowSyncInProgressPopups(boolean allowed) {
        this.values.getSeenConfig().setAllowSyncInProgressPopups(allowed);
        this.save();
    }

    public boolean isSyncGameNeedsRestartAllowed() {
        return this.values.getSeenConfig().isSyncGameNeedsRestartAllowed();
    }
    public void setAllowGameNeedsRestartPopups(boolean allowed) {
        this.values.getSeenConfig().setAllowGameNeedsRestartPopups(allowed);
        this.save();
    }
    //endregion
}