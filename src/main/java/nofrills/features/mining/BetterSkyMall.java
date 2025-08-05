package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.Arrays;

import static nofrills.Main.mc;

public class BetterSkyMall {
    public static final Feature instance = new Feature("betterSkyMall");

    public static final SettingString whitelist = new SettingString("titanium, goblins", "whitelist", instance.key());

    private static int skyMallTicks = 0;
    private static boolean skyMallInbound = false;
    private static String skyMallBuff = "";

    private static boolean isWearingMiningPiece() {
        for (ItemStack armor : Utils.getEntityArmor(mc.player)) {
            if (Utils.hasEitherStat(armor, "Mining Speed", "Mining Fortune", "Block Fortune")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBuffWhitelisted(String buff) {
        if (!whitelist.value().isEmpty()) {
            return Arrays.stream(whitelist.value().split(",")).anyMatch(keyword -> buff.toLowerCase().contains(keyword.toLowerCase().trim()));
        }
        return false;
    }

    private static boolean isMonth(String line) {
        return line.contains("Spring") || line.contains("Summer") || line.contains("Autumn") || line.contains("Winter");
    }

    private static String getSkyblockDay() {
        return SkyblockData.getLines().stream().filter(BetterSkyMall::isMonth).findFirst().orElse("Unknown Day").trim();
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive()) {
            if (skyMallTicks > 0) {
                skyMallTicks--;
                if (skyMallTicks == 0) {
                    Utils.info("§2Sky Mall §ebuff for §b" + getSkyblockDay() + "§e: " + skyMallBuff);
                }
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (mc.player != null && instance.isActive()) {
            if (event.messagePlain.equals("New day! Your Sky Mall buff changed!")) {
                skyMallInbound = true;
                event.cancel();
            }
            if (event.messagePlain.equals("You can disable this messaging by toggling Sky Mall in your /hotm!")) {
                event.cancel();
            }
            if (event.messagePlain.startsWith("New buff: ") && skyMallInbound) {
                String message = event.messagePlain.replace("New buff:", "").trim();
                if (isWearingMiningPiece() || isBuffWhitelisted(message)) {
                    skyMallBuff = message;
                    skyMallTicks = 50;
                }
                skyMallInbound = false;
                event.cancel();
            }
        }
    }
}
