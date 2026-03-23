package io.github.mcalgovisualizations.config;

import io.github.mcalgovisualizations.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;

import java.net.URL;
import java.nio.file.Path;

public final class WorldConfig {
    public static InstanceContainer createMainInstance() {
        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();
        URL worldResource = Main.class.getClassLoader().getResource("world");

        if (worldResource != null) {
            try {
                Path worldPath = Path.of(worldResource.toURI());
                container.setChunkLoader(new AnvilLoader(worldPath));
                System.out.println("World loaded from resources: " + worldPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Could not find the 'world' folder in resources!");
        }
        return container;
    }
}