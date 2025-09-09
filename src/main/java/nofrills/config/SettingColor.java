package nofrills.config;

import com.google.gson.JsonObject;
import nofrills.misc.RenderColor;

public class SettingColor {
    private final String key;
    private final String parent;
    private final RenderColor defaultValue;
    private RenderColor value;

    public SettingColor(RenderColor defaultValue, String key, String parentKey) {
        this.key = key;
        this.parent = parentKey;
        this.defaultValue = defaultValue;
        this.value = this.load();
    }

    public SettingColor(RenderColor defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    private RenderColor load() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) return RenderColor.fromArgb(data.get(this.key).getAsInt());
        }
        return this.defaultValue;
    }

    private void save() {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, this.value.argb);
    }

    public RenderColor value() {
        return this.value;
    }

    public void set(RenderColor value) {
        this.value = value;
        this.save();
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
