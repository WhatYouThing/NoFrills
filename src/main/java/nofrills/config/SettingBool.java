package nofrills.config;

import com.google.gson.JsonObject;

public class SettingBool {
    private final String key;
    private final String parent;
    private final boolean defaultValue;
    private boolean value;

    public SettingBool(boolean defaultValue, String key, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = defaultValue;
        this.value = this.load();
    }

    public SettingBool(boolean defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    private boolean load() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) return data.get(this.key).getAsBoolean();
        }
        return this.defaultValue;
    }

    private void save() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, this.value);
    }

    public boolean value() {
        return this.value;
    }

    public void set(boolean value) {
        this.value = value;
        this.save();
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
