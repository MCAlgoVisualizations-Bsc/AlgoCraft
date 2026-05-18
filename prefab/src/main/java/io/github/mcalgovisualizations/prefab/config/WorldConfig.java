package io.github.mcalgovisualizations.prefab.config;

import io.github.mcalgovisualizations.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Stream;

public final class WorldConfig {
    private static final String WORLD_PATH_ENV = "WORLD_PATH";
    private static final Path DEFAULT_CONTAINER_WORLD_PATH = Path.of("/app/world");

    private static final String BUNDLED_WORLD_NAME = "world2";

    private WorldConfig() {
    }

    public static InstanceContainer createMainInstance() {
        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();
        Path worldPath = resolveWorldPath();

        if (worldPath == null) {
            throw new IllegalStateException("No usable world found. Checked WORLD_PATH, /app/world and bundled resources/world.");
        }

        container.setChunkLoader(new AnvilLoader(worldPath));
        System.out.println("World loaded from: " + worldPath.toAbsolutePath());
        return container;
    }

    private static Path resolveWorldPath() {
        Path configuredPath = resolveConfiguredPath();
        if (isValidWorld(configuredPath)) {
            return configuredPath;
        }

        if (isBootstrapCandidate(configuredPath) && bootstrapFromBundledWorld(configuredPath) && isValidWorld(configuredPath)) {
            System.out.println("Bootstrapped world into: " + configuredPath.toAbsolutePath());
            return configuredPath;
        }

        if (configuredPath != null && Files.exists(configuredPath) && !isValidWorld(configuredPath)) {
            System.err.println("Configured world path exists but is not a valid world: " + configuredPath.toAbsolutePath());
        }

        Path bundledDiskWorld = resolveBundledWorldOnDisk();
        if (isValidWorld(bundledDiskWorld)) {
            return bundledDiskWorld;
        }

        return null;
    }

    private static Path resolveConfiguredPath() {
        String envValue = System.getenv(WORLD_PATH_ENV);
        if (envValue != null && !envValue.isBlank()) {
            return Path.of(envValue.trim());
        }
        return DEFAULT_CONTAINER_WORLD_PATH;
    }

    private static Path resolveBundledWorldOnDisk() {
        URL worldResource = Main.class.getClassLoader().getResource(BUNDLED_WORLD_NAME);
        if (worldResource == null) {
            return null;
        }

        try {
            URI uri = worldResource.toURI();
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return Path.of(uri);
            }
        } catch (Exception e) {
            System.err.println("Failed to resolve bundled world path on disk: " + e.getMessage());
        }

        return null;
    }

    private static boolean bootstrapFromBundledWorld(Path target) {
        URL worldResource = Main.class.getClassLoader().getResource(BUNDLED_WORLD_NAME);
        if (worldResource == null) {
            System.err.println("Bundled resources/world not found, cannot bootstrap world volume.");
            return false;
        }

        try {
            URI uri = worldResource.toURI();
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                copyDirectory(Path.of(uri), target);
                return true;
            }
            if ("jar".equalsIgnoreCase(uri.getScheme())) {
                copyDirectoryFromJar(uri, target);
                return true;
            }

            System.err.println("Unsupported world resource URI scheme: " + uri.getScheme());
            return false;
        } catch (Exception e) {
            System.err.println("Failed to bootstrap world from bundled resources: " + e.getMessage());
            return false;
        }
    }

    private static void copyDirectoryFromJar(URI jarUri, Path target) throws IOException {
        FileSystem jarFs = null;
        boolean created = false;

        try {
            try {
                jarFs = FileSystems.newFileSystem(jarUri, Map.of());
                created = true;
            } catch (FileSystemAlreadyExistsException ignored) {
                jarFs = FileSystems.getFileSystem(jarUri);
            }

            copyDirectory(jarFs.getPath("/" + BUNDLED_WORLD_NAME), target);
        } finally {
            if (created && jarFs != null) {
                jarFs.close();
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target);

        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path relative = source.relativize(path);
                    Path destination = target.resolve(relative.toString());

                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed copying " + path + " to " + target, e);
                }
            });
        }
    }

    private static boolean isValidWorld(Path worldPath) {
        if (worldPath == null || !Files.isDirectory(worldPath)) {
            return false;
        }

        return Files.exists(worldPath.resolve("level.dat")) && Files.isDirectory(worldPath.resolve("region"));
    }

    private static boolean isBootstrapCandidate(Path worldPath) {
        if (worldPath == null) {
            return false;
        }

        if (Files.notExists(worldPath)) {
            return true;
        }

        if (!Files.isDirectory(worldPath)) {
            return false;
        }

        try (Stream<Path> stream = Files.list(worldPath)) {
            return stream.findAny().isEmpty();
        } catch (IOException ignored) {
            return false;
        }
    }
}