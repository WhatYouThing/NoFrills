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
    private final boolean loadFinished;
    private JsonObject data = new JsonObject();
    private int hash = 0;
    private boolean loadFailed = false;

    protected DataFile(String filename) {
        this.path = Config.getFolderPath().resolve(filename);
        try {
            if (Files.isReadable(this.path)) {
                this.data = JsonParser.parseString(Files.readString(this.path)).getAsJsonObject();
                this.hash = this.data.hashCode();
            }
        } catch (Exception exception) {
            LOGGER.error("Unable to load NoFrills data file!", exception);
            this.loadFailed = true;
        }
        this.loadFinished = true;
    }

    public void saveBlocking() {
        if (this.loadFailed || !this.loadFinished) {
            LOGGER.warn("Prevented save of NoFrills data file {} due to file load failure.", this.path.getFileName());
            return;
        }
        try {
            int hash = this.data.hashCode();
            if (hash != this.hash) {
                Utils.atomicWrite(this.path, this.data);
                this.hash = hash;
            }
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

    public void set(JsonObject newData) {
        this.data = newData;
    }
}
