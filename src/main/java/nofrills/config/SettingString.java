package nofrills.config;

import com.google.gson.JsonObject;

public class SettingString {
    private final String key;
    private final String parent;
    private final String defaultValue;
    private String value;

    public SettingString(String defaultValue, String key, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = defaultValue;
        this.value = this.load();
    }

    public SettingString(String defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    private String load() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) return data.get(this.key).getAsString();
        }
        return this.defaultValue;
    }

    private void save() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, this.value);
    }

    public String value() {
        return this.value;
    }

    public void set(String value) {
        this.value = value;
        this.save();
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
