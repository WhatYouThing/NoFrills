package nofrills.features.farming;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

@EventListener
public class MouseLock {
    public static final Feature instance = new Feature("mouseLock");

    public static SettingKeybind keybind = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "keybind", instance);
    public static SettingBool rebind = new SettingBool(false, "rebind", instance);
    public static SettingKeybind breakKeyActive = new SettingKeybind(InputConstants.KEY_SPACE, "breakKeyActive", instance);
    public static SettingKeybind jumpKeyActive = new SettingKeybind(InputConstants.MOUSE_BUTTON_LEFT, "jumpKeyActive", instance);
    public static SettingKeybind breakKeyInactive = new SettingKeybind(InputConstants.MOUSE_BUTTON_LEFT, "breakKeyInactive", instance);
    public static SettingKeybind jumpKeyInactive = new SettingKeybind(InputConstants.KEY_SPACE, "jumpKeyInactive", instance);

    public static boolean locked = false;

    private static void bindOption(KeyMapping option, SettingKeybind key) {
        if (!key.bound()) return;
        option.setKey(key.asInputConstant());
        option.setDown(false);
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.isKey(event.key) && mc.gui.screen() == null && Utils.isInGarden()) {
            if (event.action == InputConstants.PRESS) {
                locked = !locked;
                Utils.info(locked ? "§aMouse lock activated." : "§cMouse lock deactivated.");
                if (rebind.value()) {
                    bindOption(mc.options.keyAttack, locked ? breakKeyActive : breakKeyInactive);
                    bindOption(mc.options.keyJump, locked ? jumpKeyActive : jumpKeyInactive);
                    KeyMapping.resetMapping();
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
                bindOption(mc.options.keyAttack, breakKeyInactive);
                bindOption(mc.options.keyJump, jumpKeyInactive);
                KeyMapping.resetMapping();
            }
        }
    }
}
