package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.events.*;
import nofrills.misc.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class DungeonSolvers {
    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );
    private static final List<BlockPos> sharpshooterList = new ArrayList<>();
    private static final Box sharpshooterTarget = Box.enclosing(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
    private static final Box sharpshooterArea = new Box(63.2, 127, 35.8, 63.8, 128, 35.2);
    private static final EntityCache dungeonKeys = new EntityCache();
    private static final EntityCache spiritBows = new EntityCache();
    private static final List<SpawningTerracotta> terracottas = new ArrayList<>();
    private static final BlockPos sadanPit = new BlockPos(-9, 67, 66);
    public static boolean isInTerminal = false;
    private static BlockPos sharpshooterNext = null;
    private static boolean isTerminalBuilt = false;
    private static int melodyTicks = 0;
    private static int gyroTicks = 0;

    private static boolean checkStackColor(ItemStack stack, DyeColor color, String colorName) {
        Item item = stack.getItem();
        if (Formatting.strip(stack.getName().getString()).trim().isEmpty()) {
            return false;
        }
        if (stack.getItem().toString().startsWith("minecraft:" + colorName)) {
            return true;
        }
        return switch (color) {
            case BLACK -> item.equals(Items.INK_SAC);
            case BLUE -> item.equals(Items.LAPIS_LAZULI);
            case BROWN -> item.equals(Items.COCOA_BEANS);
            case WHITE -> item.equals(Items.BONE_MEAL);
            default -> false;
        };
    }

    private static ItemStack stackWithCount(int count) {
        ItemStack stack = count > 0 ? Items.LIME_CONCRETE.getDefaultStack() : Items.BLUE_CONCRETE.getDefaultStack();
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(" "));
        stack.setCount(Math.abs(count));
        return stack;
    }

    private static boolean isSharpshooterActive() {
        return mc.player != null && sharpshooterArea.intersects(mc.player.getBoundingBox());
    }

    private static BlockPos findSharpshooterTarget() {
        for (double x = sharpshooterTarget.minX; x <= sharpshooterTarget.maxX; x++) {
            for (double y = sharpshooterTarget.minY; y <= sharpshooterTarget.maxY; y++) {
                for (double z = sharpshooterTarget.minZ; z <= sharpshooterTarget.maxZ; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                    if (mc.world.getBlockState(pos).equals(Blocks.EMERALD_BLOCK.getDefaultState())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    @EventHandler
    private static void onScreenOpen(ScreenOpenEvent event) {
        isTerminalBuilt = false;
        isInTerminal = false;

        if (Utils.isInDungeons() && event.screen.getTitle().getString().equals("Click the button on time!")) {
            isInTerminal = true;
            if (Config.melodyAnnounce()) {
                if (melodyTicks == 0 && !Config.melodyMessage().isEmpty()) {
                    Utils.sendMessage(Config.melodyMessage());
                    melodyTicks = 100;
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isOnDungeonFloor("6")) {
            if (Config.gyroTimer() && gyroTicks > 0) {
                Utils.showTitleCustom("GYRO: " + Utils.formatDecimal(gyroTicks / 20.0f) + "s", 1, 25, 2.5f, 0xffff00);
            }
        }
        if (Utils.isOnDungeonFloor("7")) {
            if (melodyTicks > 0) {
                melodyTicks--;
            }
            if (Config.solveDevices()) {
                if (isSharpshooterActive()) {
                    sharpshooterNext = findSharpshooterTarget();
                } else if (!sharpshooterList.isEmpty() || sharpshooterNext != null) {
                    sharpshooterNext = null;
                    sharpshooterList.clear();
                }
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (Config.solveTerminals() && Utils.isInDungeons() && !isTerminalBuilt) {
            isTerminalBuilt = event.isFinal;
            List<Slot> orderSlots = new ArrayList<>();
            List<Slot> colorSlots = new ArrayList<>();
            for (Slot slot : event.handler.slots) {
                ItemStack stack = event.inventory.getStack(slot.id);
                if (!stack.isEmpty()) {
                    if (event.title.startsWith("Correct all the panes!")) {
                        isInTerminal = true;
                        if (stack.getItem().equals(Items.RED_STAINED_GLASS_PANE)) {
                            Utils.setSpoofed(slot, SlotOptions.first);
                            Utils.setDisabled(slot, false);
                        } else {
                            Utils.setSpoofed(slot, SlotOptions.background);
                            Utils.setDisabled(slot, true);
                        }
                    }
                    if (event.title.startsWith("Click in order!")) {
                        isInTerminal = true;
                        if (stack.getItem().equals(Items.RED_STAINED_GLASS_PANE) && event.isFinal) {
                            orderSlots.add(slot);
                        } else {
                            Utils.setSpoofed(slot, SlotOptions.background);
                            Utils.setDisabled(slot, true);
                        }
                    }
                    if (event.title.startsWith("What starts with:") && event.title.endsWith("?")) {
                        isInTerminal = true;
                        String character = String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)).toLowerCase();
                        String name = Formatting.strip(stack.getName().getString()).toLowerCase().trim();
                        if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(stack)) {
                            Utils.setSpoofed(slot, SlotOptions.first);
                            Utils.setDisabled(slot, false);
                        } else {
                            Utils.setSpoofed(slot, SlotOptions.background);
                            Utils.setDisabled(slot, true);
                        }
                    }
                    if (event.title.startsWith("Select all the") && event.title.endsWith("items!")) {
                        isInTerminal = true;
                        String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                        String colorName = color.equals("SILVER") ? "light_gray" : color.toLowerCase().replace(" ", "_");
                        for (DyeColor dye : DyeColor.values()) {
                            if (dye.getId().equals(colorName)) {
                                if (!Utils.hasGlint(stack) && checkStackColor(stack, dye, colorName)) {
                                    Utils.setSpoofed(slot, SlotOptions.first);
                                    Utils.setDisabled(slot, false);
                                } else {
                                    Utils.setSpoofed(slot, SlotOptions.background);
                                    Utils.setDisabled(slot, true);
                                }
                                break;
                            }
                        }
                    }
                    if (event.title.startsWith("Change all to same color!")) {
                        isInTerminal = true;
                        if (colorsOrder.contains(stack.getItem()) && !colorSlots.contains(slot)) {
                            colorSlots.add(slot);
                        }
                        Utils.setSpoofed(slot, SlotOptions.background);
                        Utils.setDisabled(slot, true);
                    }
                }
            }
            if (!orderSlots.isEmpty()) {
                orderSlots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                Slot first = orderSlots.getFirst();
                Utils.setSpoofed(first, SlotOptions.first);
                Utils.setDisabled(first, false);
                if (orderSlots.size() > 1) {
                    Slot second = orderSlots.get(1);
                    Utils.setSpoofed(second, SlotOptions.second);
                    Utils.setDisabled(second, true);
                }
            }
            if (!colorSlots.isEmpty() && colorSlots.size() >= 9) {
                int[] colorCounts = {0, 0, 0, 0, 0};
                for (Slot slot : colorSlots) {
                    int index = colorsOrder.indexOf(slot.getStack().getItem());
                    colorCounts[index] += 1;
                }
                int mostCommon = -1, highestCommon = 0;
                for (int i = 0; i < 5; i++) {
                    if (colorCounts[i] > highestCommon) {
                        highestCommon = colorCounts[i];
                        mostCommon = i;
                    }
                }
                for (Slot slot : colorSlots) {
                    int index = colorsOrder.indexOf(slot.getStack().getItem());
                    int target = Math.negateExact(mostCommon - index);
                    if (Math.abs(target) > 2) {
                        int offset = Math.abs(target) == 4 ? 3 : 1;
                        target = Math.negateExact(target) + (target > 0 ? offset : -offset);
                    }
                    if (target == 0) {
                        Utils.setSpoofed(slot, SlotOptions.background);
                        Utils.setDisabled(slot, true);
                    } else {
                        Utils.setSpoofed(slot, stackWithCount(target));
                        Utils.setDisabled(slot, false);
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (Config.gyroTimer() && Utils.isOnDungeonFloor("6") && SkyblockData.getLines().stream().anyMatch(line -> line.endsWith("sadan"))) {
            if (event.newState.getBlock() instanceof FlowerPotBlock) { // EVERY POTTED FLOWER HAS ITS OWN BLOCK ID AAAAAAAAHHH
                if (terracottas.stream().noneMatch(terra -> terra.pos.equals(event.pos))) {
                    terracottas.add(new SpawningTerracotta(event.pos, Utils.isOnDungeonFloor("M6") ? 240 : 300));
                }
            }
            if (event.pos.equals(sadanPit) && event.newState.isAir() && event.oldState.getBlock().equals(Blocks.GRAY_STAINED_GLASS)) {
                gyroTicks = 195;
            }
        }
        if (Config.solveDevices() && Utils.isOnDungeonFloor("7")) {
            if (sharpshooterTarget.contains(event.pos.toCenterPos()) && isSharpshooterActive()) {
                BlockState terracotta = Blocks.BLUE_TERRACOTTA.getDefaultState();
                BlockState emerald = Blocks.EMERALD_BLOCK.getDefaultState();
                if (event.oldState.equals(emerald) && event.newState.equals(terracotta)) {
                    if (sharpshooterNext != null) {
                        sharpshooterList.add(sharpshooterNext);
                    }
                }
                if (event.oldState.equals(terracotta) && event.newState.equals(emerald)) {
                    sharpshooterNext = findSharpshooterTarget();
                }
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (Utils.isInDungeons()) {
            if (Config.wishReminder() && Config.dungeonClass().equals("Healer") && event.messagePlain.equals("⚠ Maxor is enraged! ⚠")) {
                Utils.showTitleCustom("WISH!", 40, -20, 4.0f, 0x00ff00);
                Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
            }
            if (Config.dungeonClass().equals("Mage")) {
                if (Config.campReminder() && event.messagePlain.equals("[BOSS] The Watcher: Let's see how you can handle this.")) {
                    Utils.showTitleCustom("CAMP BLOOD!", 40, -20, 4.0f, 0xff4646);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
                if (Config.ragAxeReminder() && Utils.isOnDungeonFloor("M5") && event.messagePlain.equals("[BOSS] Livid: I can now turn those Spirits into shadows of myself, identical to their creator.")) {
                    Utils.showTitleCustom("RAG!", 40, -20, 4.0f, 0xffff00);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
            }
            if (Config.gyroTimer()) {
                if (event.messagePlain.equals("[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!")) {
                    gyroTicks = 267;
                }
                if (event.messagePlain.equals("[BOSS] Sadan: ENOUGH!")) {
                    terracottas.clear();
                }
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Utils.isInDungeons()) {
            if (Config.keyHighlight()) {
                if (event.namePlain.equals("Wither Key") || event.namePlain.equals("Blood Key")) {
                    dungeonKeys.add(event.entity);
                }
            }
            if (Config.spiritHighlight()) {
                if (event.namePlain.equals("Spirit Bow")) {
                    spiritBows.add(event.entity);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (!sharpshooterList.isEmpty()) {
            for (BlockPos pos : sharpshooterList) {
                event.drawFilled(Box.enclosing(pos, pos), true, RenderColor.fromHex(0xff0000, 1.0f));
            }
        }
        if (sharpshooterNext != null) {
            event.drawFilled(Box.enclosing(sharpshooterNext, sharpshooterNext), true, RenderColor.fromHex(0x00ff00, 1.0f));
        }
        if (!dungeonKeys.empty()) {
            for (Entity ent : dungeonKeys.get()) {
                event.drawFilledWithBeam(Box.of(ent.getPos().add(0, 1.5, 0), 1, 1, 1), 256, true, RenderColor.fromColor(Config.keyColor()));
            }
        }
        if (!spiritBows.empty()) {
            for (Entity ent : spiritBows.get()) {
                BlockPos ground = Utils.findGround(ent.getBlockPos(), 4);
                Vec3d pos = ent.getPos();
                Vec3d posAdjust = new Vec3d(pos.x, ground.up(1).getY() + 1, pos.z);
                event.drawFilled(Box.of(posAdjust, 0.8, 1.75, 0.8), true, RenderColor.fromColor(Config.spiritColor()));
            }
        }
        if (!terracottas.isEmpty()) {
            List<SpawningTerracotta> terras = new ArrayList<>(terracottas);
            for (SpawningTerracotta terra : terras) {
                if (terra.pos != null) {
                    event.drawText(terra.pos.toCenterPos(), Text.of(Utils.formatDecimal(terra.ticks / 20.0f) + "s"), 0.035f, true, RenderColor.fromHex(0xffff00));
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (Config.hideMageBeam() && Utils.isInDungeons() && event.type.equals(ParticleTypes.FIREWORK)) {
            event.cancel();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        gyroTicks = 0;
        melodyTicks = 0;
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Config.gyroTimer() && Utils.isOnDungeonFloor("6")) {
            if (gyroTicks > 0) {
                gyroTicks--;
            }
            List<SpawningTerracotta> terras = new ArrayList<>(terracottas);
            for (SpawningTerracotta terra : terras) {
                terra.ticks -= 1;
            }
            terracottas.removeIf(terra -> terra.ticks <= 0);
        }
    }

    private static class SpawningTerracotta {
        public BlockPos pos;
        public int ticks;

        public SpawningTerracotta(BlockPos pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }
    }
}
