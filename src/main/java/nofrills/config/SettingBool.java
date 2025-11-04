package nofrills.config;

public class SettingBool extends SettingGeneric {
    public SettingBool(boolean defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingBool(boolean defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public boolean value() {
        return this.get().getAsBoolean();
    }
}