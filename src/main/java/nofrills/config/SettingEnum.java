package nofrills.config;

import com.google.gson.JsonObject;

public class SettingEnum<T extends Enum<T>> {
    public final T[] values;
    private final String key;
    private final String parent;
    private final T defaultValue;

    public SettingEnum(T defaultValue, Class<T> values, String key, String parentKey) {
        this.defaultValue = defaultValue;
        this.values = values.getEnumConstants();
        this.key = key;
        this.parent = parentKey;
    }

    public T value() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) {
                String value = data.get(this.key).getAsString();
                for (T constant : values) {
                    if (constant.name().equals(value)) {
                        return constant;
                    }
                }
            }
        }
        return this.defaultValue;
    }

    public void set(T value) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, String.valueOf(value));
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
