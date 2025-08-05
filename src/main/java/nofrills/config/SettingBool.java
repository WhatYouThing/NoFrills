package nofrills.config;

import com.google.gson.JsonObject;

public class SettingBool {
    private final String key;
    private final String parent;
    private final boolean defaultValue;

    public SettingBool(boolean defaultValue, String key, String parentKey) {
        this.defaultValue = defaultValue;
        this.key = key;
        this.parent = parentKey;
    }

    public boolean value() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) {
                return data.get(this.key).getAsBoolean();
            }
        }
        return this.defaultValue;
    }

    public void set(boolean value) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, value);
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
