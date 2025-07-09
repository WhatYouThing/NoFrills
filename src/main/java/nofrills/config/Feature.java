package nofrills.config;

import com.google.gson.JsonObject;

public class Feature {
    public static boolean isActive() {
        if (Config.get().has(getKey())) {
            JsonObject data = Config.get().getAsJsonObject(getKey());
            return data.has("enabled") && data.get("enabled").getAsBoolean();
        }
        return false;
    }

    public static void setActive(boolean toggle) {
        if (!Config.get().has(getKey())) {
            Config.get().add(getKey(), new JsonObject());
        }
        Config.get().get(getKey()).getAsJsonObject().addProperty("enabled", toggle);
    }

    public static void toggle() {
        setActive(!isActive());
    }

    // override with the feature's config key
    public static String getKey() {
        return "";
    }
}
