package nofrills.config;

public final class SettingEnum<T extends Enum<T>> extends SettingGeneric {
    public final Class<T> values;
    public final T[] constants;
    private T current;

    public SettingEnum(T defaultValue, Class<T> values, String key, String parentKey) {
        super(defaultValue, key, parentKey);
        this.values = values;
        this.constants = values.getEnumConstants();
        this.current = this.toConstant(this.get().getAsString());
    }

    public SettingEnum(T defaultValue, Class<T> values, String key, Feature instance) {
        this(defaultValue, values, key, instance.key());
    }

    public T toConstant(String value) {
        for (T constant : this.constants) {
            if (constant.name().equals(value)) {
                return constant;
            }
        }
        return this.constants[0];
    }

    public T value() {
        String value = this.get().getAsString();
        if (!this.current.name().equals(value)) {
            this.current = this.toConstant(value);
        }
        return this.current;
    }

    public T defaultValue() {
        return this.toConstant(this.getDefault().getAsString());
    }
}