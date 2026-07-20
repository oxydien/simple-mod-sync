package dev.oxydien.simpleModSync.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;

public class DownloadUtils {
    public static String downloadString(String uriString, boolean checkConstraints) throws IOException, URISyntaxException {
        HttpURLConnection connection = NetUtils.setupConnectionWithRedirectsTo(uriString, checkConstraints);

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String jsonString = reader.lines().collect(Collectors.joining("\n"));

        reader.close();
        inputStream.close();
        connection.disconnect();

        return jsonString;
    }
}