package nofrills.config;

import com.google.gson.JsonObject;

public class SettingBool {
    private final String key;
    private final String parent;
    private final boolean defaultValue;

    public SettingBool(String key, boolean defaultValue, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = defaultValue;
    }

    public boolean value() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            return data.has(this.key) && data.get(this.key).getAsBoolean();
        }
        return false;
    }

    public void set(boolean value) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, value);
    }
}
