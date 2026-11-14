package nofrills.config;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.sdl.SDLMouse;

public class SettingKeybind extends SettingGeneric {
    private InputConstants.Key key = InputConstants.UNKNOWN;

    public SettingKeybind(InputConstants.Key defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(InputConstants.Key defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public SettingKeybind(String defaultValue, String key, String parentKey) {
        this(Utils.getInputKey(defaultValue), key, parentKey);
    }

    public SettingKeybind(String defaultValue, String key, Feature instance) {
        this(Utils.getInputKey(defaultValue), key, instance.key());
    }

    public InputConstants.Key value() {
        JsonElement current = this.get();
        if (!this.key.getName().equals(current.getAsString())) {
            this.key = Utils.getInputKey(current.getAsString());
        }
        return this.key;
    }

    public boolean bound() {
        return !this.value().equals(InputConstants.UNKNOWN);
    }

    public boolean isKey(int key) {
        return this.bound() && this.value().getValue() == key;
    }

    public boolean isKey(InputEvent event) {
        return event.isKey(this);
    }

    public boolean isMouse() {
        return this.value().getType().equals(InputConstants.Type.MOUSE);
    }

    public boolean isDown() {
        if (this.isMouse()) {
            return this.bound() && (SDLMouse.SDL_GetMouseState(null, null) & (1 << (this.value().getValue() - 1))) > 0;
        }
        return this.bound() && InputConstants.isKeyDown(this.value().getValue());
    }
}