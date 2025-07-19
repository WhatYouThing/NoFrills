package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class DrainMessage {
    public static final Feature instance = new Feature("drainMessage");

    public static final SettingString message = new SettingString("/pc Used {mana} mana on {players} players.", "message", instance.key());
    public static final SettingBool hide = new SettingBool(false, "hide", instance.key());

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            String msg = event.getPlainMessage();
            if (msg.startsWith("Used Extreme Focus!")) {
                String mana = msg.replace("Used Extreme Focus! (", "").replace(" Mana)", "");
                int players = 0;
                for (Entity ent : Utils.getEntities()) {
                    if (ent instanceof PlayerEntity player && player != mc.player) {
                        if (Utils.isPlayer(player) && !player.isInvisible() && player.distanceTo(mc.player) <= 5) {
                            players++;
                        }
                    }
                }
                if (!message.value().isEmpty()) {
                    Utils.sendMessage(message.value().replace("{mana}", mana).replace("{players}", "" + players));
                }
                if (hide.value()) {
                    event.cancel();
                }
            }
            if (hide.value()) {
                if (msg.startsWith("You now have") && msg.contains("Damage Resistance for 5 seconds")) {
                    event.cancel();
                }
                if (msg.equals("Your Extreme Focus has worn off.")) {
                    event.cancel();
                }
            }
        }
    }
}
