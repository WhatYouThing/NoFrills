package nofrills.config;

import com.google.gson.JsonObject;

public class Feature {
    private final String key;
    private boolean active;

    public Feature(String key) {
        this.key = key;
        this.active = this.load();
    }

    private boolean load() {
        if (Config.get().has(this.key)) {
            JsonObject data = Config.get().get(this.key).getAsJsonObject();
            if (data.has("enabled")) return data.get("enabled").getAsBoolean();
        }
        return false;
    }

    private void save() {
        if (!Config.get().has(this.key)) {
            Config.get().add(this.key, new JsonObject());
        }
        Config.get().get(this.key).getAsJsonObject().addProperty("enabled", this.active);
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean toggle) {
        this.active = toggle;
        this.save();
    }

    public String key() {
        return this.key;
    }
}
