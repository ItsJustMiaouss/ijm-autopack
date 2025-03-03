package com.itsjustmiaouss.manifest;

import com.itsjustmiaouss.file.FilesHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * This represents a file fetched for the server (manifest).
 * <p>
 * This file doesn't exist on the client computer until {@link ManifestFile#download} is called.
 */
public class ManifestFile {

    private final Path path;
    private final String checksum;
    private final URL url;

    public ManifestFile(String path, String checksum, String url) {
        this.path = Path.of(path); // todo: check if file and not folder
        this.checksum = checksum;

        try {
            this.url = new URI(url).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            System.err.printf("URL is malformed for the file %s%n", path);
            throw new RuntimeException(e);
        }
    }

    public Path getPath() {
        return path;
    }

    public URL getUrl() {
        return url;
    }

    public String getChecksum() {
        return checksum;
    }

    public boolean shouldFileBeReplaced(Path localFile) {
        return !checksum.equals(FilesHelper.getChecksum(localFile));
    }

    public void download(Path dest) {
        try {
            HttpURLConnection conn = (HttpURLConnection) getUrl().openConnection();
            conn.setRequestMethod("GET");

            try (InputStream inputStream = conn.getInputStream()) {
                Path parentDirectories = dest.resolve(getPath().getParent());
                if (!Files.exists(parentDirectories)) Files.createDirectories(parentDirectories);

                Files.copy(inputStream, dest.resolve(getPath()), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.printf("Downloaded file %s.%n", getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
