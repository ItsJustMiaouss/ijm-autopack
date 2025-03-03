package com.itsjustmiaouss.file;

import com.itsjustmiaouss.file.cache.CacheManager;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {

    private final CacheManager cacheManager;

    public FileManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Delete the existing mods (mods that have the same ModId as the files presents in the cache folder)
     */
    private void deleteDuplicatedMods() {
        try (Stream<Path> cacheFilePaths = Files.walk(cacheManager.getCachePath()).filter(FilesHelper::isValidMod);
             Stream<Path> modsPaths = Files.walk(cacheManager.getGamePath().resolve("mods"), 1)) {

            // Get the mods ids of the cached mods files
            Map<String, String> cacheModsIds = cacheFilePaths
                    .filter(FilesHelper::isValidMod)
                    .collect(Collectors.toMap(FilesHelper::getIdFromMetadata, path -> (
                            Optional.ofNullable(FilesHelper.getChecksum(path)).orElse("?")
                    )));

            // Delete the mods with the same ModId but a different checksum
            modsPaths
                    .filter(Files::isRegularFile)
                    .filter(path -> cacheModsIds.containsKey(FilesHelper.getIdFromMetadata(path)))
                    .filter(path -> !cacheModsIds.containsValue(FilesHelper.getChecksum(path)))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            System.out.printf("Deleted mod %s because it need to be updated.%n", path.getFileName());
                        } catch (IOException e) {
                            System.err.printf("Failed to delete the mod %s!%n", path.getFileName());
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete duplicated mods!");
        }
    }

    public void copyCacheToGameDir() throws IOException {
        deleteDuplicatedMods();

        FileUtils.copyDirectory(cacheManager.getCachePath().toFile(), cacheManager.getGamePath().toFile());
        System.out.println("Copied cache folder to .minecraft!");
    }

}
