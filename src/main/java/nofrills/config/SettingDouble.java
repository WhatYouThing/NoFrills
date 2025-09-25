package nofrills.config;

import com.google.gson.JsonObject;

public class SettingDouble {
    private final String key;
    private final String parent;
    private final double defaultValue;

    public SettingDouble(double defaultValue, String key, String parentKey) {
        this.defaultValue = defaultValue;
        this.key = key;
        this.parent = parentKey;
    }

    public SettingDouble(double defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public double value() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) {
                return data.get(this.key).getAsDouble();
            }
        }
        return this.defaultValue;
    }

    public float valueFloat() {
        return (float) this.value();
    }

    public void set(double value) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, value);
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}