package dev.oxydien.simpleModSync.io;

import dev.oxydien.simpleModSync.content.SyncSchema;
import dev.oxydien.simpleModSync.content.SyncStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

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
            syncSchema.withStatus(index, (status -> {
                status.setErrorMessage(e.getMessage());
            }));
        }
    }

    public void downloadFileWithProgress(String uriString, Path outputPath, ProgressCallback callback) throws IOException, URISyntaxException {
        // Manually follow redirects so cross-protocol redirects (e.g. http -> https
        // from URL shorteners) are handled correctly.
        int maxRedirects = 10;
        String currentUri = uriString;
        URLConnection connection = null;
        boolean resolved = false;

        for (int i = 0; i < maxRedirects; i++) {
            URL url = new URI(currentUri).toURL();
            connection = url.openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);

            if (connection instanceof HttpURLConnection httpURLConnection) {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; SimpleModSync)");
                httpURLConnection.setInstanceFollowRedirects(false);

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode >= 300 && responseCode < 400) {
                    String location = httpURLConnection.getHeaderField("Location");
                    httpURLConnection.disconnect();
                    if (location == null) {
                        throw new IOException("Redirect (HTTP " + responseCode + ") with no Location header");
                    }
                    if (!location.startsWith("http://") && !location.startsWith("https://")) {
                        URL base = new URI(currentUri).toURL();
                        location = new URL(base, location).toString();
                    }
                    currentUri = location;
                    continue;
                }
            }
            resolved = true;
            break; // non-HTTP connection or non-redirect response — proceed to download
        }

        if (!resolved) {
            throw new IOException("Too many redirects while fetching: " + uriString);
        }

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

        if (connection instanceof HttpURLConnection httpURLConnection) {
            httpURLConnection.disconnect();
        }

        if (lastReportedProgress < 100) {
            callback.onProgress(100);
        }
    }
}