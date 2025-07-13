package nofrills.config;

import com.google.gson.JsonObject;
import nofrills.misc.RenderColor;

public class SettingColor {
    private final String key;
    private final String parent;
    private final RenderColor defaultValue;

    public SettingColor(RenderColor defaultValue, String key, String parentKey) {
        this.defaultValue = defaultValue;
        this.key = key;
        this.parent = parentKey;
    }

    public RenderColor value() {
        if (Config.get().has(this.parent)) {
            JsonObject data = Config.get().getAsJsonObject(this.parent);
            if (data.has(this.key)) {
                return RenderColor.fromArgb(data.get(this.key).getAsInt());
            }
        }
        return this.defaultValue;
    }

    public void set(RenderColor value) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        Config.get().get(this.parent).getAsJsonObject().addProperty(this.key, value.argb);
    }

    public void reset() {
        this.set(this.defaultValue);
    }
}
