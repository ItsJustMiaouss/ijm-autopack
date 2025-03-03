package com.itsjustmiaouss.file.cache;

import com.itsjustmiaouss.manifest.ManifestFile;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class CacheManager {

    private final Path gamePath;
    private final Path cachePath;

    public CacheManager(Path gamePath, Path cachePath) {
        this.gamePath = gamePath;
        this.cachePath = cachePath;
    }

    public Path getGamePath() {
        return gamePath;
    }

    public Path getCachePath() {
        return cachePath;
    }

    public void initializeCacheDirectory() throws IOException {
        if (!Files.exists(cachePath)) {
            Files.createDirectory(cachePath);
        }
    }

    /**
     * Delete the non-existent files of the manifest from the cache.
     * @param manifestFiles
     * @throws IOException
     */
    private void cleanupCache(List<ManifestFile> manifestFiles) throws IOException {
        List<Path> absoluteCacheFile = manifestFiles.stream().map(manifestFile -> cachePath.resolve(manifestFile.getPath())).toList();

        try (Stream<Path> cachePaths = Files.walk(cachePath)) {
            cachePaths
                    .filter(path -> !absoluteCacheFile.contains(path))
                    .filter(Files::isRegularFile)
                    .forEach(CacheManager::deleteCacheFile);
        }
    }

    private static void deleteCacheFile(Path path) {
        try {
            FileUtils.forceDelete(path.toFile());
            System.out.printf("Deleted cache file %s%n", path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Download the files missing files from the manifest and delete the old ones
     * @param manifestFiles
     * @throws IOException
     */
    public void updateCacheFiles(List<ManifestFile> manifestFiles) throws IOException {
        cleanupCache(manifestFiles);

        for (ManifestFile manifestFile : manifestFiles) {
            Path resolve = cachePath.resolve(manifestFile.getPath());

            // Delete the file if the checksum is different
            if (Files.exists(resolve) && manifestFile.shouldFileBeReplaced(resolve)) {
                System.out.printf("Removed %s because the checksum different.%n", manifestFile.getPath());
                Files.deleteIfExists(resolve);
            }

            // Download all the missing files from the manifest
            if(!Files.exists(resolve)) {
                System.out.printf("File %s not found. Download it from the server...%n", manifestFile.getPath());
                manifestFile.download(cachePath);
            }
        }
    }
}
