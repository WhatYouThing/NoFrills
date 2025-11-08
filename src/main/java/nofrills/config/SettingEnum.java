package nofrills.config;

public class SettingEnum<T extends Enum<T>> extends SettingGeneric {
    public final T[] values;
    private T current;

    public SettingEnum(T defaultValue, Class<T> values, String key, String parentKey) {
        super(defaultValue, key, parentKey);
        this.values = values.getEnumConstants();
        this.current = this.toConstant(this.get().getAsString());
    }

    public SettingEnum(T defaultValue, Class<T> values, String key, Feature instance) {
        this(defaultValue, values, key, instance.key());
    }

    public T toConstant(String value) {
        for (T constant : this.values) {
            if (constant.name().equals(value)) {
                return constant;
            }
        }
        return this.values[0];
    }

    public T value() {
        String value = this.get().getAsString();
        if (!this.current.name().equals(value)) {
            this.current = this.toConstant(value);
        }
        return this.current;
    }
}