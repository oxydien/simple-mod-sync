package dev.oxydien.simpleModSync.utils;

import dev.oxydien.simpleModSync.config.RepositoryConstraints;
import dev.oxydien.simpleModSync.log.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class NetUtils {
    private static final int MAX_REDIRECTS = 10;
    private static final int CONNECTION_TIMEOUT = 10_000; // 10s
    private static final int READ_TIMEOUT = 30_000;       // 30s

    /*
     * Creates basic `HttpURLConnection` with default parameters
     */
    public static HttpURLConnection setupConnectionTo(String uriString, boolean checkConstraints) throws IOException, URISyntaxException {
        URL url = new URI(uriString).toURL();

        if (checkConstraints) {
            var allowedDomains = RepositoryConstraints.AllowedDomains();
            var isAllowed = allowedDomains.stream()
                    .anyMatch(d -> url.getHost().endsWith(d)); // using `endsWith` to allow subdomains (ex: cdn.modrinth.com, mediafilez.forgecdn.net)

            if (!isAllowed)
                throw new URISyntaxException(uriString, "Domain not allowed. Check mods github FAQ.");
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; SimpleModSync)");
        connection.setInstanceFollowRedirects(false);
        return connection;
    }

    public static HttpURLConnection setupConnectionWithRedirectsTo(String uriString) throws IOException, URISyntaxException {
        return setupConnectionWithRedirectsTo(uriString, true);
    }

    /*
     * Creates basic `HttpURLConnection` with default parameters.
     * Manually follows redirects so cross-protocol redirects (e.g. http -> https
     * from URL shorteners) are handled correctly.
     */
    public static HttpURLConnection setupConnectionWithRedirectsTo(String uriString, boolean checkConstraints) throws IOException, URISyntaxException {
        String currentUri = uriString;
        HttpURLConnection connection = null;
        boolean resolved = false;

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            connection = NetUtils.setupConnectionTo(currentUri, checkConstraints);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 300 && responseCode < 400) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();

                if (location == null)
                    throw new IOException("Redirect (HTTP " + responseCode + ") with no Location header");

                if (!location.startsWith("http://") && !location.startsWith("https://")) {
                    URL base = new URI(currentUri).toURL();
                    location = new URL(base, location).toString();
                }

                Log.debug("Encountered a HTTP redirect", responseCode, "FROM", currentUri, "TO", location);

                currentUri = location;
                continue;
            }
            resolved = true;
            break; // non-redirect response - proceed to download
        }

        if (!resolved)
            throw new IOException("Too many redirects while fetching: " + uriString);

        return connection;
    }
}
