package com.itsjustmiaouss;

import com.itsjustmiaouss.file.FileManager;
import com.itsjustmiaouss.file.cache.CacheManager;
import com.itsjustmiaouss.manifest.ManifestFile;
import com.itsjustmiaouss.manifest.ManifestService;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class IJMAutopack {

    private final ManifestService manifestService;
    private final CacheManager cacheManager;

    public IJMAutopack(String gameDir, String hostUrl, String cacheFolderName) {
        Path gamePath = Path.of(gameDir);
        Path cachePath = gamePath.resolve(cacheFolderName);

        manifestService = new ManifestService(hostUrl);
        cacheManager = new CacheManager(gamePath, cachePath);
    }

    public void run() throws IOException {
        cacheManager.initializeCacheDirectory();
        List<ManifestFile> manifestFiles = manifestService.getManifestFiles();

        cacheManager.updateCacheFiles(manifestFiles);

        new FileManager(cacheManager).copyCacheToGameDir();
        System.out.println("Done!");
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addRequiredOption("gamedir", "gamedir", true, "The path of the game directory. Use $INST_MC_DIR.");
        options.addRequiredOption("host", "host", true, "Define the host URL containing the manifest.");
        return options;
    }

    private static CommandLine parseCommandLine(String[] args, Options options) {
        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("Command line syntax:", options);
            System.exit(1);
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("IJM's Autopack - Created by ItsJustMiaouss (https://github.com/ItsJustMiaouss/ijm-autopack).");

        Options options = createOptions();
        CommandLine cmd = parseCommandLine(args, options);

        IJMAutopack app = new IJMAutopack(
                cmd.getOptionValue("gamedir"),
                cmd.getOptionValue("host"),
                ".ijmautopack"
        );

        app.run();
    }
}