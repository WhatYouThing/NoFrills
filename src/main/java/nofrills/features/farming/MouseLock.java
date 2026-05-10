package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class MouseLock {
    public static final Feature instance = new Feature("mouseLock");

    public static SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "keybind", instance);
    public static SettingBool rebind = new SettingBool(false, "rebind", instance);
    public static SettingKeybind breakKeyActive = new SettingKeybind(GLFW.GLFW_KEY_SPACE, "breakKeyActive", instance);
    public static SettingKeybind jumpKeyActive = new SettingKeybind(GLFW.GLFW_MOUSE_BUTTON_1, "jumpKeyActive", instance);
    public static SettingKeybind breakKeyInactive = new SettingKeybind(GLFW.GLFW_MOUSE_BUTTON_1, "breakKeyInactive", instance);
    public static SettingKeybind jumpKeyInactive = new SettingKeybind(GLFW.GLFW_KEY_SPACE, "jumpKeyInactive", instance);

    public static boolean locked = false;

    private static void bindOption(KeyBinding option, SettingKeybind key) {
        if (!key.bound()) return;
        option.setBoundKey(key.asInputConstant());
        option.setPressed(false);
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.isKey(event.key) && mc.currentScreen == null && Utils.isInGarden()) {
            if (event.action == GLFW.GLFW_PRESS) {
                locked = !locked;
                Utils.info(locked ? "§aMouse lock activated." : "§cMouse lock deactivated.");
                if (rebind.value()) {
                    bindOption(mc.options.attackKey, locked ? breakKeyActive : breakKeyInactive);
                    bindOption(mc.options.jumpKey, locked ? jumpKeyActive : jumpKeyInactive);
                    KeyBinding.updateKeysByCode();
                }
            }
            event.cancel();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            locked = false;
            if (rebind.value()) {
                bindOption(mc.options.attackKey, breakKeyInactive);
                bindOption(mc.options.jumpKey, jumpKeyInactive);
                KeyBinding.updateKeysByCode();
            }
        }
    }
}
