package nofrills.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nofrills.misc.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

import static nofrills.Main.LOGGER;

/**
 * Generic class which allows for JSON data files to be stored in the mod's config folder.
 * Non-blocking load and save by default.
 */
public class DataFile {
    private final Path path;
    private JsonObject data = new JsonObject();

    public DataFile(String filename) {
        this.path = Config.getFolderPath().resolve(filename);
        Thread.startVirtualThread(() -> {
            if (Files.exists(this.path)) {
                try {
                    this.data = JsonParser.parseString(Files.readString(this.path)).getAsJsonObject();
                } catch (Exception exception) {
                    LOGGER.error("Unable to load NoFrills data file", exception);
                }
            }
        });
    }

    public void saveBlocking() {
        try {
            Utils.atomicWrite(this.path, this.data);
        } catch (Exception exception) {
            LOGGER.error("Unable to save NoFrills data file", exception);
        }
    }

    public void save() {
        Thread.startVirtualThread(this::saveBlocking);
    }

    public JsonObject get() {
        return this.data;
    }
}
