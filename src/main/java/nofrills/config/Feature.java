package nofrills.config;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Feature {
    public static final CopyOnWriteArrayList<Feature> withFlags = new CopyOnWriteArrayList<>();
    public String key;
    private int hash = 0;
    private boolean value = false;
    private List<Flags> flags;

    public Feature(String key) {
        this.key = key;
        this.value = this.isActive();
        this.flags = List.of();
    }

    public Feature(String key, Flags... featureFlags) {
        this(key);
        this.flags = List.of(featureFlags);
        withFlags.add(this);
    }

    public String key() {
        return this.key;
    }

    public void update() {
        if (this.hash != Config.getHash()) {
            if (Config.get().has(this.key)) {
                JsonObject data = Config.get().get(this.key).getAsJsonObject();
                this.value = data.has("enabled") && data.get("enabled").getAsBoolean();
            } else {
                this.value = false;
            }
            this.hash = Config.getHash();
        }
    }

    public boolean isActive() {
        this.update();
        return this.value;
    }

    public void setActive(boolean toggle) {
        if (!Config.get().has(this.key)) {
            Config.get().add(this.key, new JsonObject());
        }
        this.value = toggle;
        Config.get().get(this.key).getAsJsonObject().addProperty("enabled", this.value);
        Config.computeHash();
    }

    public List<Flags> getFlags() {
        return this.flags;
    }

    public boolean hasFlag(Flags flag) {
        return this.getFlags().contains(flag);
    }

    public enum Flags {
        UsePricingAPI,
        UseElectionAPI,
        UseNonPlaceableAPI,
        UseMuseumAPI,
        UseItemTexturesAPI
    }
}