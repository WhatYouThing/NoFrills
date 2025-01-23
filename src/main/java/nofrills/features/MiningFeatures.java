package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;
import nofrills.mixin.CreeperEntityAccessor;

import static nofrills.Main.mc;

public class MiningFeatures {
    private static final RenderColor cheeseSpotColor = RenderColor.fromHex(0xaf00ff, 0.5f);
    private static final RenderColor ghostColor = RenderColor.fromHex(0x00c8c8, 1.0f);
    private static final String uselessMessage1 = "New day! Your Sky Mall buff changed!";
    private static final String uselessMessage2 = "You can disable this messaging by toggling Sky Mall in your /hotm!";
    private static BlockPos templeCheeseSpot = null;

    private static boolean isMiningPiece(ItemStack armorPiece) {
        LoreComponent lore = armorPiece.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                String lineClean = Formatting.strip(line.getString());
                if (lineClean.startsWith("Mining Speed:") || lineClean.startsWith("Mining Fortune:") || lineClean.startsWith("Block Fortune:")) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.templeLocator) {
            if (templeCheeseSpot == null && SkyblockData.getLocation().equals(Utils.Symbols.zone + " Jungle Temple")) {
                for (Entity ent : mc.world.getEntities()) {
                    if (ent.getType() == EntityType.ARMOR_STAND && ent.hasCustomName()) {
                        String name = Formatting.strip(ent.getCustomName().getString());
                        if (name.equals("Kalhuiki Door Guardian") && ent.distanceTo(mc.player) <= 8.0) {
                            BlockPos ground = Utils.findGround(ent.getBlockPos(), 4);
                            if (mc.world.getBlockState(ground).getBlock() == Blocks.STONE_BRICKS) {
                                templeCheeseSpot = ground.up(1).add(61, -48, 18);
                                String coords = templeCheeseSpot.getX() + " " + templeCheeseSpot.getY() + " " + templeCheeseSpot.getZ();
                                Utils.info("§5Jungle Temple Cheese Spot: " + coords);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (Config.ghostVision) {
            if (SkyblockData.getLocation().equals(Utils.Symbols.zone + " The Mist")) {
                for (Entity ent : mc.world.getEntities()) {
                    if (ent.getType() == EntityType.CREEPER && !Rendering.Entities.isDrawingFilled(ent)) {
                        CreeperEntity creeper = (CreeperEntity) ent;
                        if (creeper.getMaxHealth() >= 1000.0f) {
                            creeper.setInvisible(true);
                            creeper.getDataTracker().set(((CreeperEntityAccessor) creeper).getChargedFlag(), false);
                            Rendering.Entities.drawFilled(ent, true, ghostColor);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onJoin(ServerJoinEvent event) {
        templeCheeseSpot = null;
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (Config.templeLocator && templeCheeseSpot != null) {
            Rendering.drawFilled(event.matrices, event.consumer, event.camera, Box.enclosing(templeCheeseSpot, templeCheeseSpot), true, cheeseSpotColor);
        }
    }

    @EventHandler
    public static void onChat(ChatMsgEvent event) {
        if (Config.betterSkymall) {
            if (event.messagePlain.equalsIgnoreCase(uselessMessage1) || event.messagePlain.equalsIgnoreCase(uselessMessage2)) {
                event.cancel();
            }
            if (event.messagePlain.startsWith("New buff: ")) {
                for (ItemStack armor : mc.player.getArmorItems()) {
                    if (isMiningPiece(armor)) {
                        Utils.info("§bNew Sky Mall Buff: " + event.messagePlain.replace("New buff:", "").trim());
                        break;
                    }
                }
                event.cancel();
            }
        }
    }
}
