package nofrills.config;

public class SettingKeybind extends SettingInt { // just extend since keycodes are also integers
    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        super(defaultValue, key, instance);
    }

    public int key() {
        return this.value();
    }
}
