package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

public class DungeonReminders {
    public static final Feature instance = new Feature("dungeonReminders");

    public static final SettingBool wish = new SettingBool(false, "wish", instance.key());
    public static final SettingBool bloodCamp = new SettingBool(false, "bloodCamp", instance.key());
    public static final SettingBool rag = new SettingBool(false, "rag", instance.key());

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (wish.value() && SkyblockData.dungeonClass.equals("Healer") && event.messagePlain.equals("⚠ Maxor is enraged! ⚠")) {
                Utils.showTitleCustom("WISH!", 40, -20, 4.0f, RenderColor.fromHex(0x00ff00));
                Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
            }
            if (SkyblockData.dungeonClass.equals("Mage")) {
                if (bloodCamp.value() && event.messagePlain.equals("[BOSS] The Watcher: Let's see how you can handle this.")) {
                    Utils.showTitleCustom("CAMP BLOOD!", 40, -20, 4.0f, RenderColor.fromHex(0xff4646));
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
                if (rag.value() && Utils.isOnDungeonFloor("M5") && event.messagePlain.equals("[BOSS] Livid: I can now turn those Spirits into shadows of myself, identical to their creator.")) {
                    Utils.showTitleCustom("RAG!", 40, -20, 4.0f, RenderColor.fromHex(0xffff00));
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
            }
        }
    }
}
