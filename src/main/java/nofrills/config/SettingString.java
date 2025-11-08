package nofrills.config;

public class SettingString extends SettingGeneric {

    public SettingString(String defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingString(String defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public String value() {
        return this.get().getAsString();
    }
}