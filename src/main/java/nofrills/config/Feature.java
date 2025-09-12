package nofrills.config;

import com.google.gson.JsonObject;

public record Feature(String key) {

    public boolean isActive() {
        if (Config.get().has(this.key)) {
            JsonObject data = Config.get().get(this.key).getAsJsonObject();
            if (data.has("enabled")) {
                return data.get("enabled").getAsBoolean();
            }
        }
        return false;
    }

    public void setActive(boolean toggle) {
        if (!Config.get().has(this.key)) {
            Config.get().add(this.key, new JsonObject());
        }
        Config.get().get(this.key).getAsJsonObject().addProperty("enabled", toggle);
    }
}