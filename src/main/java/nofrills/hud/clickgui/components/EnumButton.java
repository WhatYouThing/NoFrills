package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import nofrills.hud.clickgui.Settings;

public class EnumButton<T extends Enum<T>> extends ButtonComponent {
    private final T[] values;
    private final T defaultValue;
    private final EventStream<TextBoxComponent.OnChanged> changedEvents = TextBoxComponent.OnChanged.newStream();
    private T value;

    public EnumButton(String value, T defaultValue, Class<T> values) {
        super(net.minecraft.text.Text.empty(), buttonComponent -> {
        });
        this.values = values.getEnumConstants();
        this.defaultValue = defaultValue;
        this.renderer(Settings.buttonRenderer);
        this.setValue(value);
        this.onPress(button -> {
            for (int i = 0; i < this.values.length; i++) {
                if (this.values[i].equals(this.value)) {
                    T newValue = i == this.values.length - 1 ? this.values[0] : this.values[i + 1];
                    this.setValue(newValue);
                    changedEvents.sink().onChanged(newValue.name());
                    return;
                }
            }
            this.setValue(this.defaultValue);
            changedEvents.sink().onChanged(this.defaultValue.name());
        });
    }

    public void setValue(String value) {
        this.setValue(this.asValue(value));
    }

    public void setValue(T value) {
        this.value = value;
        this.setMessage(net.minecraft.text.Text.of(value.name()));
        this.changedEvents.sink().onChanged(this.value.name());
    }

    public T asValue(String name) {
        for (T value : this.values) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return this.defaultValue;
    }

    public EventSource<TextBoxComponent.OnChanged> onChanged() {
        return changedEvents.source();
    }
}
