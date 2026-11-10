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

    public static SettingKeybind keybind = new SettingKeybind(InputConstants.UNKNOWN, "keybind", instance);
    public static SettingBool rebind = new SettingBool(false, "rebind", instance);
    public static SettingKeybind breakKeyActive = new SettingKeybind("key.keyboard.space", "breakKeyActive", instance);
    public static SettingKeybind jumpKeyActive = new SettingKeybind("key.mouse.left", "jumpKeyActive", instance);
    public static SettingKeybind breakKeyInactive = new SettingKeybind("key.mouse.left", "breakKeyInactive", instance);
    public static SettingKeybind jumpKeyInactive = new SettingKeybind("key.keyboard.space", "jumpKeyInactive", instance);

    public static boolean locked = false;

    private static void bindOption(KeyMapping option, SettingKeybind key) {
        if (!key.bound()) return;
        option.setKey(key.value());
        option.setDown(false);
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && event.isKey(keybind) && mc.gui.screen() == null && Utils.isInGarden()) {
            event.consume(() -> {
                locked = !locked;
                Utils.info(locked ? "§aMouse lock activated." : "§cMouse lock deactivated.");
                if (rebind.value()) {
                    bindOption(mc.options.keyAttack, locked ? breakKeyActive : breakKeyInactive);
                    bindOption(mc.options.keyJump, locked ? jumpKeyActive : jumpKeyInactive);
                    KeyMapping.resetMapping();
                }
            });
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
