package nofrills.config;

import com.google.gson.JsonObject;

public class SettingJson {
    private final String key;
    private final String parent;
    private final JsonObject defaultValue;

    public SettingJson(JsonObject defaultValue, String key, String parentKey) {
        this.defaultValue = defaultValue;
        this.key = key;
        this.parent = parentKey;
    }

    public JsonObject value() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        JsonObject data = Config.get().getAsJsonObject(this.parent);
        if (!data.has(this.key)) {
            data.add(this.key, this.defaultValue);
        }
        return data.get(this.key).getAsJsonObject();
    }

    public void reset() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().add(this.key, this.defaultValue);
    }
}
