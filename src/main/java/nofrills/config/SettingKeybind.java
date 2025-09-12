package nofrills.config;

public class SettingKeybind extends SettingInt {
    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public int key() {
        return this.value();
    }
}