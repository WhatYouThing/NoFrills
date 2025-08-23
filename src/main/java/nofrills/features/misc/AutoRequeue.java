package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class AutoRequeue {
    public static final Feature instance = new Feature("autoRequeue");

    public static final SettingInt delay = new SettingInt(100, "delay", instance.key());
    public static final SettingBool terrorCheck = new SettingBool(false, "terrorCheck", instance.key());
    public static final SettingKeybind pauseBind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "pauseBind", instance.key());

    public static boolean paused = false;
    public static int ticks = 0;

    private static boolean isAnyoneInTerror() {
        if (Utils.isInKuudra()) {
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof PlayerEntity player && Utils.isPlayer(player)) {
                    for (ItemStack stack : Utils.getEntityArmor(player)) {
                        if (Utils.getSkyblockId(stack).contains("TERROR")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void setPaused() {
        if (!paused) {
            Utils.info("§aAuto Requeue paused for the current instance.");
            paused = true;
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !paused && SkyblockData.isInstanceOver()) {
            if (terrorCheck.value() && isAnyoneInTerror()) {
                return;
            }
            if (ticks != -1) {
                if (ticks == 0) {
                    Utils.infoFormat("§aAutomatically requeuing in {} seconds.", Utils.formatDecimal(delay.value() / 20.0f));
                }
                ticks++;
                if (ticks >= delay.value()) {
                    Utils.sendMessage("/instancerequeue");
                    ticks = -1;
                }
            }
        }
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.currentScreen == null && event.key == pauseBind.value() && event.modifiers == 0 && SkyblockData.isInInstance()) {
            if (event.action == GLFW.GLFW_PRESS) {
                setPaused();
            }
            event.cancel();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        paused = false;
        ticks = 0;
    }
}
