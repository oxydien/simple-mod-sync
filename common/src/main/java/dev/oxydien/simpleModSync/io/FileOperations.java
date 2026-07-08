package dev.oxydien.simpleModSync.io;

import dev.oxydien.simpleModSync.content.SyncSchema;
import dev.oxydien.simpleModSync.content.SyncStatus;
import dev.oxydien.simpleModSync.log.Log;
import dev.oxydien.simpleModSync.utils.NetUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperations {
    private final SyncSchema syncSchema;

    public FileOperations(SyncSchema schema) {
        this.syncSchema = schema;
    }

    public interface ProgressCallback {
        void onProgress(int percentage); // 0 -> 100
    }

    public void DownloadFromUri(String uri, Path output, int index) {
        try {
            this.downloadFileWithProgress(uri, output, (newProgress) -> {
                syncSchema.withStatus(index, (status -> {
                    status.setState(SyncStatus.SyncState.DOWNLOADING);
                    status.setDownloadProgress(newProgress / (float) 100);
                }));
            });
        } catch (Exception e) {
            Log.error("Index %d encountered an error while downloading file from: '%s'".formatted(index, uri), e);
            syncSchema.withStatus(index, (status -> {
                status.setErrorMessage(e.getMessage());
            }));
        }
    }

    public void downloadFileWithProgress(String uriString, Path outputPath, ProgressCallback callback) throws IOException, URISyntaxException {
        HttpURLConnection connection = NetUtils.setupConnectionWithRedirectsTo(uriString);

        long fileSize = connection.getContentLengthLong();
        InputStream inputStream = connection.getInputStream();

        OutputStream outputStream = Files.newOutputStream(outputPath);

        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;
        int lastReportedProgress = -1;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;

            if (fileSize > 0) {
                int progress = (int) ((totalBytesRead * 100) / fileSize);
                if (progress > lastReportedProgress) {
                    callback.onProgress(progress);
                    lastReportedProgress = progress;
                }
            }
        }

        outputStream.close();
        inputStream.close();
        connection.disconnect();

        if (connection instanceof HttpURLConnection httpURLConnection) {
            httpURLConnection.disconnect();
        }

        if (lastReportedProgress < 100) {
            callback.onProgress(100);
        }
    }
}
