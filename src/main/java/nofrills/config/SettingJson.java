package nofrills.config;

import com.google.gson.JsonObject;

public class SettingJson {
    private final String key;
    private final String parent;
    private final JsonObject defaultValue;
    private final JsonObject value;

    public SettingJson(JsonObject defaultValue, String key, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = defaultValue;
        this.value = this.load();
    }

    public SettingJson(JsonObject defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    private JsonObject load() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        JsonObject data = Config.get().getAsJsonObject(this.parent);
        if (!data.has(this.key)) {
            data.add(this.key, this.defaultValue);
        }
        return data.get(this.key).getAsJsonObject();
    }

    // returns a pointer for the json data, any changes made are automatically saved
    public JsonObject value() {
        return this.value;
    }
}
