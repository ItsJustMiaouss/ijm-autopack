package com.itsjustmiaouss.file;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FilesHelper {

    /**
     * Get the MD5 checksum of a file.
     * @param filePath Input file
     * @return MD5 Checksum
     */
    public static @Nullable String getChecksum(Path filePath) {
        if (!isValidFile(filePath)) return null;

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] bytes = new byte[2048];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                digest.update(bytes, 0, bytesRead);
            }
        } catch (IOException e) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    /**
     * @param file Path to a file
     * @return If the specified path is a valid file path.
     */
    public static boolean isValidFile(Path file) {
        return Files.exists(file, LinkOption.NOFOLLOW_LINKS) && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * @param file Path to a file
     * @return If the specified file path is a valid mod.
     */
    public static boolean isValidMod(Path file) {
        return isValidFile(file) && file.toFile().getName().endsWith(".jar");
    }

    @Deprecated
    public static boolean isValidConfig(Path file) {
        return isValidFile(file) && file.endsWith(".minecraft/config");
    }

    /**
     * @param filePath Mod (jar) file
     * @return The mod ID contained in fabric.mod.json. Work obviously only with Fabric mods.
     */
    public static @Nullable String getIdFromMetadata(Path filePath) {
        if (!isValidMod(filePath)) return null;

        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            ZipEntry entry = zipFile.getEntry("fabric.mod.json");
            if (entry == null) return null;

            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                JSONObject json = new JSONObject(new JSONTokener(inputStream));
                return json.optString("id", null);
            }
        } catch (IOException e) {
            return null;
        }
    }
}
