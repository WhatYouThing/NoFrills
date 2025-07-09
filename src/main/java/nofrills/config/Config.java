package nofrills.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static nofrills.Main.LOGGER;

public class Config {
    public static final Path filePath = FabricLoader.getInstance().getConfigDir().resolve("NoFrills/Config.json");
    private static JsonObject data = new JsonObject();

    public static void load() {
        if (Files.exists(filePath)) {
            try {
                data = JsonParser.parseString(Files.readString(filePath)).getAsJsonObject();
            } catch (IOException exception) {
                LOGGER.error("Unable to load config file!", exception);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.writeString(filePath, data.toString());
        } catch (IOException exception) {
            LOGGER.error("Unable to save config file!", exception);
        }
    }

    public static JsonObject get() {
        return data;
    }
}
