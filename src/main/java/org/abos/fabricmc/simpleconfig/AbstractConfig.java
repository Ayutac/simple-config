package org.abos.fabricmc.simpleconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class to build a config from. Handles initializing of rules as well as saving and loading the config.
 */
public abstract class AbstractConfig implements Iterable<ConfigProperty<?, ? extends GameRules.Rule<?>>> {

    public final static Logger LOGGER = LoggerFactory.getLogger("simpleconfig");

    /**
     * Call this method only once and from your {@link net.fabricmc.api.ModInitializer}.
     */
    public void initialize() {
        for (ConfigProperty<?, ?> property : this) {
            if (property.isWithGameRule()) {
                property.registerRule();
            }
        }
    }

    /**
     * Saves the current in-game config to the specified file.
     *
     * @param file  The file to save to. Should be a real file (not a directory) and writeable.
     * @param world Needed to get the most recent game rule values. Can be <code>null</code>, then the cached
     *              values will be used.
     * @throws IllegalStateException If an unknown subclass of {@link ConfigProperty} was attempted to be saved.
     *                               In this case you need to override this method in your subclass.
     */
    public void saveTo(Path file, @Nullable World world) throws IllegalStateException {
        if (Files.isDirectory(file)) {
            LOGGER.warn("Please don't offer a directory as a config file!");
            return;
        }
        JsonObject config = new JsonObject();
        for (ConfigProperty<?, ?> property : this) {
            if (property instanceof IntConfigProperty intProperty) {
                config.addProperty(intProperty.getName(), intProperty.getValue(world));
            } else if (property instanceof BooleanConfigProperty booleanProperty) {
                config.addProperty(booleanProperty.getName(), booleanProperty.getValue());
            } else {
                throw new IllegalStateException("Attempted to save unknown config property type " + property.getClass().getName() + "!");
            }
        }
        try {
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(config));
        } catch (IOException e) {
            LOGGER.warn("Config file couldn't be written!");
        }
    }

    /**
     * Loads the config from the specified file.
     *
     * @param file   The file to save to. Should be a real file (not a directory) and writeable.
     * @param server Needed to set the game rule values. Can be <code>null</code>, then the values will only be cached
     *               but not be available for the game rules.
     * @throws IllegalStateException If an unknown subclass of {@link ConfigProperty} was attempted to be loaded.
     *                               In this case you need to override this method in your subclass.
     */
    public void loadFrom(Path file, @Nullable MinecraftServer server) throws IllegalStateException {
        if (!Files.isReadable(file)) {
            LOGGER.warn("No readable config file found!");
            return;
        }
        JsonObject config;
        try {
            config = new Gson().fromJson(Files.readString(file), JsonObject.class);
        } catch (IOException e) {
            LOGGER.warn("Config file couldn't be read!");
            return;
        }
        for (ConfigProperty<?, ?> property : this) {
            JsonElement value;
            if (property instanceof IntConfigProperty intProperty) {
                value = config.get(intProperty.getName());
                if (value == null) {
                    LOGGER.warn("Missing config value for " + intProperty.getName() + ", default will be used!");
                    continue;
                }
                intProperty.setValue(value.getAsInt(), server);
            } else if (property instanceof BooleanConfigProperty booleanProperty) {
                value = config.get(booleanProperty.getName());
                if (value == null) {
                    LOGGER.warn("Missing config value for " + booleanProperty.getName() + ", default will be used!");
                    continue;
                }
                booleanProperty.setValue(value.getAsBoolean(), server);
            } else {
                throw new IllegalStateException("Attempted to load unknown config property type " + property.getClass().getName() + "!");
            }
        }
    }

}
