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
    public static String downloadString(String uriString) throws IOException, URISyntaxException {
        int maxRedirects = 10;
        String currentUri = uriString;

        for (int i = 0; i < maxRedirects; i++) {
            URL url = new URI(currentUri).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            connection.setRequestMethod("GET");
            // setting the User-Agent header for CDNs that care about bots
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; SimpleModSync)");
            connection.setInstanceFollowRedirects(false); // we handle redirects manually

            int responseCode = connection.getResponseCode();

            if (responseCode >= 300 && responseCode < 400) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null) {
                    throw new IOException("Redirect (HTTP " + responseCode + ") with no Location header");
                }
                // Handle relative redirect URLs
                if (!location.startsWith("http://") && !location.startsWith("https://")) {
                    location = new URI(currentUri).resolve(location).toString();
                }
                currentUri = location;
                continue;
            }

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String jsonString = reader.lines().collect(Collectors.joining("\n"));

            reader.close();
            inputStream.close();
            connection.disconnect();

            return jsonString;
        }

        throw new IOException("Too many redirects while fetching: " + uriString);
    }
}