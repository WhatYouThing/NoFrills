package nofrills.config;

public class SettingDouble extends SettingGeneric {

    public SettingDouble(double defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingDouble(double defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public double value() {
        return this.get().getAsDouble();
    }

    public float valueFloat() {
        return (float) this.value();
    }
}