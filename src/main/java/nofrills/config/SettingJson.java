package nofrills.config;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class SettingJson extends SettingGeneric {

    public SettingJson(JsonObject defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingJson(JsonObject defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public JsonObject value() {
        return this.get().getAsJsonObject();
    }

    public void edit(Consumer<JsonObject> lambda) {
        JsonObject value = this.value();
        lambda.accept(value);
        this.set(value, false);
    }
}