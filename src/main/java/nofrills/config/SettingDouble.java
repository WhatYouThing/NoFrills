package nofrills.config;

import com.google.gson.JsonObject;

public class SettingDouble {
    private final String key;
    private final String parent;
    private final double defaultValue;
    private double value;

    public SettingDouble(double defaultValue, String key, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = defaultValue;
        this.value = this.load();
    }

    public SettingDouble(double defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    private double load() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) return data.get(this.key).getAsDouble();
        }
        return this.defaultValue;
    }

    private void save() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, this.value);
    }

    public double value() {
        return this.value;
    }

    public float valueFloat() {
        return (float) this.value;
    }

    public void set(double value) {
        this.value = value;
        this.save();
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
