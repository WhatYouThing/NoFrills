package nofrills.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import nofrills.misc.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

import static nofrills.Main.LOGGER;

public class Config {
    private static final Path folderPath = FabricLoader.getInstance().getConfigDir().resolve("NoFrills");
    private static final Path filePath = folderPath.resolve("Configuration.json");
    private static JsonObject data = new JsonObject();
    private static int hash = 0;

    public static Path getFolderPath() {
        return folderPath;
    }

    public static void load() {
        if (Files.exists(filePath)) {
            try {
                data = JsonParser.parseString(Files.readString(filePath)).getAsJsonObject();
            } catch (Exception exception) {
                LOGGER.error("Unable to load NoFrills config file!", exception);
            }
        } else {
            save();
        }
        computeHash();
    }

    public static void save() {
        try {
            Utils.atomicWrite(filePath, data.toString());
        } catch (Exception exception) {
            LOGGER.error("Unable to save NoFrills config file!", exception);
        }
    }

    public static void saveAsync() {
        new Thread(Config::save).start();
    }

    public static int getHash() {
        return hash;
    }

    public static void computeHash() {
        hash = data.hashCode();
    }

    public static JsonObject get() {
        return data;
    }
}