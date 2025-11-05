package nofrills.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class SettingGeneric {
    private final String key;
    private final String parent;
    private final JsonElement defaultValue;
    private JsonElement value;
    private int hash = 0;

    public SettingGeneric(Object defaultValue, String key, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = this.parse(defaultValue);
        this.value = this.get();
    }

    public SettingGeneric(Object defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public JsonElement parse(Object value) {
        return switch (value) {
            case Boolean bool -> new JsonPrimitive(bool);
            case String string -> new JsonPrimitive(string);
            case Number number -> new JsonPrimitive(number);
            case RenderColor color -> new JsonPrimitive(color.argb);
            case JsonObject jsonObject -> jsonObject;
            case Enum<?> enumValue -> new JsonPrimitive(enumValue.name());
            default ->
                    throw new IllegalStateException(Utils.format("Unexpected value ({}) in {} {} setting class!", value, this.key, this.parent));
        };
    }

    public void update() {
        if (this.value == null || this.hash != Config.getHash()) {
            if (Config.get().has(this.parent)) {
                JsonObject data = Config.get().getAsJsonObject(this.parent);
                this.value = data.has(this.key) ? data.get(this.key) : this.defaultValue;
            } else {
                this.value = this.defaultValue;
            }
            this.hash = Config.getHash();
        }
    }

    public void save() {
        this.set(this.value);
    }

    public JsonElement get() {
        this.update();
        return this.value;
    }

    public void set(JsonElement value) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        this.value = value;
        Config.get().get(this.parent).getAsJsonObject().add(this.key, this.value);
        Config.computeHash();
    }

    public void set(Object value) {
        this.set(this.parse(value));
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}