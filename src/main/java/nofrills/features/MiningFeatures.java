package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;
import nofrills.mixin.CreeperEntityAccessor;

import java.util.Arrays;

import static nofrills.Main.mc;

public class MiningFeatures {
    private static final RenderColor ghostColor = RenderColor.fromHex(0x00c8c8, 1.0f);
    private static final String uselessMessage1 = "New day! Your Sky Mall buff changed!";
    private static final String uselessMessage2 = "You can disable this messaging by toggling Sky Mall in your /hotm!";
    private static int skyMallTicks = 0;
    private static String skyMallBuff = "";

    private static boolean isWearingMiningPiece() {
        for (ItemStack armor : mc.player.getArmorItems()) {
            LoreComponent lore = armor.getComponents().get(DataComponentTypes.LORE);
            if (lore != null) {
                for (Text line : lore.lines()) {
                    String lineClean = Formatting.strip(line.getString());
                    if (lineClean.startsWith("Mining Speed:") || lineClean.startsWith("Mining Fortune:") || lineClean.startsWith("Block Fortune:")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isBuffWhitelisted(String buff) {
        if (!Config.skymallWhitelist.isEmpty()) {
            return Arrays.stream(Config.skymallWhitelist.split(",")).anyMatch(keyword -> buff.toLowerCase().contains(keyword.toLowerCase().trim()));
        }
        return false;
    }

    private static boolean isMonth(String line) {
        return line.contains("Spring") || line.contains("Summer") || line.contains("Autumn") || line.contains("Winter");
    }

    private static String getSkyblockDay() {
        return SkyblockData.getLines().stream().filter(MiningFeatures::isMonth).findFirst().orElse("Unknown Day").trim();
    }

    private static boolean isChest(PlayerInteractBlockC2SPacket packet) {
        Block block = mc.world.getBlockState(packet.getBlockHitResult().getBlockPos()).getBlock();
        return block.equals(Blocks.CHEST) || block.equals(Blocks.TRAPPED_CHEST);
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Config.ghostVision && SkyblockData.getLocation().equals(Utils.Symbols.zone + " The Mist")) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof CreeperEntity creeper) {
                    if (!Rendering.Entities.isDrawingFilled(ent)) {
                        if (creeper.getMaxHealth() >= 1000.0f) {
                            creeper.setInvisible(true);
                            creeper.getDataTracker().set(CreeperEntityAccessor.getChargedFlag(), false);
                            Rendering.Entities.drawFilled(ent, true, ghostColor);
                        }
                    }
                }
            }
        }
        if (Config.betterSkymall) {
            if (skyMallTicks > 0) {
                skyMallTicks--;
                if (skyMallTicks == 0) {
                    Utils.info("§2Sky Mall §ebuff for §b" + getSkyblockDay() + "§e: " + skyMallBuff);
                    skyMallBuff = "";
                }
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (Config.betterSkymall && Utils.isInSkyblock()) {
            if (event.messagePlain.equalsIgnoreCase(uselessMessage1) || event.messagePlain.equalsIgnoreCase(uselessMessage2)) {
                event.cancel();
            }
            if (event.messagePlain.startsWith("New buff: ")) {
                String message = event.messagePlain.replace("New buff:", "").trim();
                if (isWearingMiningPiece() || isBuffWhitelisted(message)) {
                    skyMallBuff = message;
                    skyMallTicks = 50;
                }
                event.cancel();
            }
        }
    }
}
