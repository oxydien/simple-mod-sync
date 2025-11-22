package dev.oxydien.simpleModSync.content.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.Content;
import dev.oxydien.simpleModSync.content.PackedContent;
import dev.oxydien.simpleModSync.content.PackedContentMetadata;
import dev.oxydien.simpleModSync.io.FileOperations;
import dev.oxydien.simpleModSync.log.Log;
import dev.oxydien.simpleModSync.utils.StringUtils;
import dev.oxydien.simpleModSync.utils.ZipUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PackedContentHandler extends ContentHandler<PackedContent> {
    @Override
    public PackedContent ParseJson(JsonObject contentObject, int index) {
        Content baseContent = super.ParseJson(contentObject, index);

        String directory = contentObject.has("directory") ? contentObject.get("directory").getAsString() : "";

        return new PackedContent(baseContent.getUri(), baseContent.getType(), baseContent.getName(), baseContent.getVersion(), directory);
    }

    @Override
    public String GetFileExtension() {
        return "json";
    }

    @Override
    public String GetFileName(PackedContent contentObject) {
        return String.format("sms_%s-%s.%s", StringUtils.sanitize(contentObject.getName()), StringUtils.sanitize(contentObject.getVersion()), this.GetFileExtension());
    }

    @Override
    public String GetProjectName(PackedContent contentObject) {
        return String.format("sms_%s-", contentObject.getName());
    }

    @Override
    public Path GetDirectory(Path basePath) {
        return basePath;
    }

    @Override
    public void UpdateVersion(PackedContent contentObject, FileOperations files, int index) {
        Path dir = this.GetDirectory(SimpleModSync.getInstance().getInstanceDir());
        String safeName = StringUtils.sanitize(contentObject.getName());
        String safeVersion = StringUtils.sanitize(contentObject.getVersion());

        Path metadataPath = dir.resolve(this.GetFileName(contentObject));
        Path tempZipPath = dir.resolve(String.format("sms_%s-%s.archive.zip", safeName, safeVersion));


        // Remove older version if exists
        Path olderVersion = this.GetOlderVersion(contentObject);
        if (olderVersion != null) {
            Log.debug("UpdateVersion.PackedContentHandler", "Found older version of {}, deleting {}", safeName, olderVersion.getFileName());
            try {
                String configJsonStr = Files.readString(olderVersion);
                JsonObject jsonObject = JsonParser.parseString(configJsonStr).getAsJsonObject();
                PackedContentMetadata configData = PackedContentMetadata.FromJson(jsonObject);

                for (String file : configData.getConfig()) {
                    try {
                        Files.deleteIfExists(Path.of(file));
                    } catch (IOException e) {
                        Log.warning("UpdateVersion.PackedContentHandler.delete", "Failed to delete file, ignoring: {}", e.getMessage());
                    }
                }

                Files.deleteIfExists(olderVersion);
            } catch (IOException e) {
                Log.error("UpdateVersion.PackedContentHandler.cleanup", "Failed to read or delete old version", e);
            }
        }

        // Download new version
        try {
            Log.debug("UpdateVersion.PackedContentHandler", "Downloading {} {}", contentObject.getName(), contentObject.getVersion());
            files.DownloadFromUri(contentObject.getUri(), tempZipPath, index);

            List<Path> modifiedFiles = ZipUtils.ExtractZipFile(tempZipPath, dir.resolve(StringUtils.sanitizeDirectory(contentObject.getDirectory())));
            List<String> modifiedFilesAsString = modifiedFiles.stream().map(Path::toString).toList();
            PackedContentMetadata metadata = new PackedContentMetadata(modifiedFilesAsString);
            Files.writeString(metadataPath, metadata.toJsonString());

            Files.deleteIfExists(tempZipPath);
        } catch (IOException e) {
            Log.error("UpdateVersion.PackedContentHandler.download", "Failed to download or extract file ", contentObject.getName(), e);
        }
    }
}
