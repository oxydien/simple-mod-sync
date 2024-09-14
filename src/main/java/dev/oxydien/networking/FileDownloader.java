package dev.oxydien.networking;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileDownloader {
    public static String downloadString(String uriString) throws IOException, URISyntaxException {
        URL url = new URI(uriString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String jsonString = reader.lines().collect(Collectors.joining("\n"));

        reader.close();
        inputStream.close();
        connection.disconnect();

        return jsonString;
    }

    public static String downloadFile(String uriString, String outputFileName) throws IOException, URISyntaxException {
        URL url = new URI(uriString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();

        Path outputPath = Paths.get(outputFileName);
        Files.copy(inputStream, outputPath);

        inputStream.close();
        connection.disconnect();

        return outputPath.toString();
    }

    public interface ProgressCallback {
        void onProgress(int percentage);
    }

    public static void downloadFileWithProgress(String uriString, String outputFileName, ProgressCallback callback) throws IOException, URISyntaxException {
        URL url = new URI(uriString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        long fileSize = connection.getContentLengthLong();
        InputStream inputStream = connection.getInputStream();

        Path outputPath = Paths.get(outputFileName);
        OutputStream outputStream = Files.newOutputStream(outputPath);

        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;
        int lastReportedProgress = -1;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;

            if (fileSize > 0) {  // Only calculate progress if file size is known
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

        if (lastReportedProgress < 100) {
            callback.onProgress(100);
        }
    }

    public static boolean fileExists(String path) {
        Path filePath = Paths.get(path);
        return Files.exists(filePath);
    }
}
