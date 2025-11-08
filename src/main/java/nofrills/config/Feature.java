package nofrills.config;

import com.google.gson.JsonObject;

public class Feature {
    public String key;
    private int hash = 0;
    private boolean value = false;

    public Feature(String key) {
        this.key = key;
        this.value = this.isActive();
    }

    public String key() {
        return this.key;
    }

    public void update() {
        if (this.hash != Config.getHash()) {
            if (Config.get().has(this.key)) {
                JsonObject data = Config.get().get(this.key).getAsJsonObject();
                this.value = data.has("enabled") && data.get("enabled").getAsBoolean();
            } else {
                this.value = false;
            }
            this.hash = Config.getHash();
        }
    }

    public boolean isActive() {
        this.update();
        return this.value;
    }

    public void setActive(boolean toggle) {
        if (!Config.get().has(this.key)) {
            Config.get().add(this.key, new JsonObject());
        }
        this.value = toggle;
        Config.get().get(this.key).getAsJsonObject().addProperty("enabled", this.value);
        Config.computeHash();
    }
}