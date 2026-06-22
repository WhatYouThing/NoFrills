package nofrills.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nofrills.misc.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

import static nofrills.Main.LOGGER;

/**
 * Generic class which allows for JSON data files to be stored in the mod's config folder.
 */
public class DataFile {
    private final Path path;
    private JsonObject data = new JsonObject();
    private boolean readFailed = false;

    public DataFile(String filename) {
        this.path = Config.getFolderPath().resolve(filename);
        try {
            if (Files.isReadable(this.path)) {
                this.data = JsonParser.parseString(Files.readString(this.path)).getAsJsonObject();
                LOGGER.info("NoFrills data file loaded: {}", this.path.getFileName());
            }
        } catch (Exception exception) {
            LOGGER.error("Unable to load NoFrills data file!", exception);
            this.readFailed = true;
        }
    }

    public void saveBlocking() {
        if (this.readFailed) {
            LOGGER.warn("Prevented save of NoFrills data file {}, the file contents could not be loaded.", this.path.getFileName());
            return;
        }
        try {
            Utils.atomicWrite(this.path, this.data);
            LOGGER.info("NoFrills data file saved: {}", this.path.getFileName());
        } catch (Exception exception) {
            LOGGER.error("Unable to save NoFrills data file!", exception);
        }
    }

    public void save() {
        Thread.startVirtualThread(this::saveBlocking);
    }

    public JsonObject get() {
        return this.data;
    }
}
