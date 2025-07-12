package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class ForagingFeatures {
    private static final List<Block> treeBlockList = List.of(
            Blocks.MANGROVE_WOOD,
            Blocks.MANGROVE_LEAVES,
            Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.AZALEA_LEAVES
    );
    private static final List<Invisibug> invisibugList = new ArrayList<>();
    private static final RenderColor invisibugColor = RenderColor.fromHex(0xff0000, 0.5f);
    private static final EntityCache cinderbatList = new EntityCache();
    private static final RenderColor cinderbatColor = RenderColor.fromHex(0x00ff00, 1.0f);

    public static boolean isTreeBlock(Entity entity) {
        if (Utils.isInArea("Galatea") && entity instanceof DisplayEntity.BlockDisplayEntity blockDisplay) {
            Block block = blockDisplay.getBlockState().getBlock();
            for (Block blacklisted : treeBlockList) {
                if (block.equals(blacklisted)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInvisibugParticle(ParticleS2CPacket packet) {
        return packet.getCount() == 1 && packet.getSpeed() == 0.0f && packet.getOffsetX() == 0.0f
                && packet.getOffsetY() == 0.0f && packet.getOffsetZ() == 0.0f
                && packet.isImportant() && packet.shouldForceSpawn();
    }

    private static boolean hasInvisibugMarker(Vec3d pos) {
        List<Entity> other = Utils.getOtherEntities(mc.player, Box.of(pos, 1, 2, 1), null);
        if (other.size() == 1 && other.getFirst() instanceof ArmorStandEntity marker) {
            return marker.isMarker() && marker.getCustomName() == null && marker.getMainHandStack().isEmpty();
        }
        return false;
    }

    private static boolean isValidBind(ItemStack stack, int key) {
        if (stack != null && !stack.isEmpty()) {
            String name = Formatting.strip(stack.getName().getString());
            return (name.equals("Repeat Previous Fusion") && key == GLFW.GLFW_KEY_SPACE) ||
                    (name.equals("Fusion") && stack.getItem().equals(Items.LIME_TERRACOTTA) && key == GLFW.GLFW_KEY_ENTER) ||
                    (name.equals("Cancel") && stack.getItem().equals(Items.RED_TERRACOTTA) && key == GLFW.GLFW_KEY_BACKSPACE);
        }
        return false;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (Config.invisibugHighlight && Utils.isInArea("Galatea") && event.type.equals(ParticleTypes.CRIT) && isInvisibugParticle(event.packet)) {
            if (hasInvisibugMarker(event.pos)) {
                for (Invisibug bug : new ArrayList<>(invisibugList)) {
                    if (bug.isNear(event.pos)) {
                        bug.add(event.pos);
                        return;
                    }
                }
                invisibugList.add(new Invisibug(event.pos));
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Config.invisibugHighlight && Utils.isInArea("Galatea")) {
            for (Invisibug bug : new ArrayList<>(invisibugList)) {
                bug.tick();
                if (bug.updateTicks == 0) {
                    invisibugList.remove(bug);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (Config.invisibugHighlight && Utils.isInArea("Galatea")) {
            for (Invisibug bug : new ArrayList<>(invisibugList)) {
                if (bug.positions.size() == 4) {
                    event.drawFilled(Box.of(bug.positions.getLast(), 1, 1, 1), false, invisibugColor);
                    event.drawText(bug.positions.getLast().add(0, 1, 0), Text.of("Invisibug"), 0.035f, false, RenderColor.fromHex(0xffffff));
                }
            }
        }
        if (Config.cinderbatHighlight && Utils.isInArea("Crimson Isle")) {
            for (Entity bat : cinderbatList.get()) {
                Vec3d pos = bat.getLerpedPos(event.tickCounter.getTickProgress(true)).add(0, 0.45, 0);
                event.drawOutline(Box.of(pos, 2.5, 2.5, 2.5), false, cinderbatColor);
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Config.lassoAlert && event.namePlain.equals("REEL") && Utils.getHeldItem().getItem().equals(Items.LEAD)) {
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0f, 1.0f);
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (Config.cinderbatHighlight && Utils.isInArea("Crimson Isle") && event.entity instanceof BatEntity bat) {
            if (bat.getHealth() > 6.0f && !cinderbatList.has(event.entity)) {
                cinderbatList.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (Config.fusionKeybinds && event.action == GLFW.GLFW_PRESS && mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.equals("Fusion Box") || title.equals("Confirm Fusion")) {
                for (Slot slot : container.getScreenHandler().slots) {
                    if (isValidBind(slot.getStack(), event.key)) {
                        mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, GLFW.GLFW_MOUSE_BUTTON_3, SlotActionType.CLONE, mc.player);
                        event.cancel();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        invisibugList.clear();
    }

    private static class Invisibug {
        public int updateTicks = 20;
        public List<Vec3d> positions = new ArrayList<>();

        public Invisibug(Vec3d initialPos) {
            this.positions.add(initialPos);
        }

        public void tick() {
            if (this.updateTicks > 0) {
                this.updateTicks -= 1;
            }
        }

        public boolean isNear(Vec3d pos) {
            return !this.positions.isEmpty() && pos.distanceTo(this.positions.getLast()) <= 0.3;
        }

        public void add(Vec3d pos) {
            if (this.positions.size() == 4) {
                this.positions.removeFirst();
            }
            this.positions.add(pos);
            this.updateTicks = 20;
        }
    }
}
