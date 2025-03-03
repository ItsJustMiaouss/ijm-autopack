package com.itsjustmiaouss.manifest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManifestService {

    private final String hostUrl;

    public ManifestService(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public List<ManifestFile> getManifestFiles() {
        List<ManifestFile> manifestFiles = new ArrayList<>();

        JSONObject manifest;
        try {
            manifest = fetchRemoteManifest();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Todo: Validate the JSONArray
        JSONArray files = manifest.getJSONArray("files");

        for (int i = 0; i < files.length(); i++) {
            JSONObject o = files.getJSONObject(i);
            System.out.printf("Fetched file %s from the manifest.%n", o.getString("name"));
            manifestFiles.add(new ManifestFile(o.getString("name"), o.getString("checksum"), o.getString("uri")));
        }

        return manifestFiles;
    }

    private JSONObject fetchRemoteManifest() throws IOException, URISyntaxException {
        URL url = new URI(hostUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return new JSONObject(response.toString());
    }
}
