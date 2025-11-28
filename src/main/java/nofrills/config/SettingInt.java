package nofrills.config;

public class SettingInt extends SettingGeneric {

    public SettingInt(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingInt(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public int value() {
        return this.get().getAsInt();
    }
}