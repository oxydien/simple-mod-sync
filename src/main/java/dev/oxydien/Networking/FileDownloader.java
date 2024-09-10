package dev.oxydien.Networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileDownloader {
    public static String downloadString(String urlString) throws IOException {
        URL url = new URL(urlString);
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

    public static String downloadFile(String urlString, String outputFileName) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();

        Path outputPath = Paths.get(outputFileName);
        Files.copy(inputStream, outputPath);

        inputStream.close();
        connection.disconnect();

        return outputPath.toString();
    }

    public static boolean fileExists(String path) {
        Path filePath = Paths.get(path);
        return Files.exists(filePath);
    }
}
