package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class DungeonSolvers {
    private static final ItemStack backgroundStack = Utils.setStackName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    private static final ItemStack firstStack = Utils.setStackName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    private static final ItemStack secondStack = Utils.setStackName(Items.BLUE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "9Click next.");
    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );
    private static final List<Dragon> dragons = List.of( // box coordinates taken from odin's WitherDragonEnum xqcL
            new Dragon("Red", 3, 3, RenderColor.fromHex(0xff0000), 0, new Box(14.5, 13, 45.5, 39.5, 28, 70.5)),
            new Dragon("Orange", 1, 5, RenderColor.fromHex(0xffaa00), 14, new Box(72, 8, 47, 102, 28, 77)),
            new Dragon("Blue", 4, 2, RenderColor.fromHex(0x55ffff), 0, new Box(71.5, 16, 82.5, 96.5, 26, 107.5)),
            new Dragon("Purple", 5, 1, RenderColor.fromHex(0xaa00aa), 0, new Box(45.5, 13, 113.5, 68.5, 23, 136.5)),
            new Dragon("Green", 2, 4, RenderColor.fromHex(0x00ff00), 14, new Box(7, 8, 80, 37, 28, 110))
    );
    private static final List<Dragon> spawnedDragons = new ArrayList<>();
    private static final List<BlockPos> sharpshooterList = new ArrayList<>();
    private static final Box sharpshooterTarget = Box.enclosing(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
    private static final Box sharpshooterArea = new Box(63.2, 127, 35.8, 63.8, 128, 35.2);
    private static final List<Entity> dungeonKeys = new ArrayList<>();
    private static final List<Entity> spiritBows = new ArrayList<>();
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final String wishMsg = "⚠ Maxor is enraged! ⚠";
    private static final String campMsg = "[BOSS] The Watcher: Let's see how you can handle this.";
    private static final String ragAxeMsg = "[BOSS] Livid: I can now turn those Spirits into shadows of myself, identical to their creator.";
    private static final String sadanEnterMsg = "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!";
    private static final String sadanLastMsg = "[BOSS] Sadan: You did it. I understand now, you have earned my respect.";
    public static boolean isInTerminal = false;
    private static boolean dragonSplitDone = false;
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

    private static boolean isDragonPhase() {
        if (mc.player != null) {
            Vec3d pos = mc.player.getPos();
            return Utils.isOnDungeonFloor("M7") && pos.getX() > 0 && pos.getY() < 50 && pos.getZ() > 0;
        }
        return false;
    }

    private static boolean isArcherTeam() {
        return switch (Config.dungeonClass) {
            case "Archer", "Tank" -> true;
            default -> false;
        };
    }

    private static double getPowerLevel() {
        double total = 0;
        for (String line : Utils.getFooterLines()) {
            if (line.startsWith("Blessing of Power")) {
                total += Utils.parseRoman(line.replace("Blessing of Power", "").trim());
            }
            if (line.startsWith("Blessing of Time")) {
                total += 0.5 * Utils.parseRoman(line.replace("Blessing of Time", "").trim());
            }
        }
        return total;
    }

    private static List<Dragon> getSpawnedDragons() {
        return new ArrayList<>(spawnedDragons);
    }

    private static boolean isDragonParticle(ParticleS2CPacket packet) {
        return packet.getParameters().getType().equals(ParticleTypes.FLAME) && packet.getCount() == 20
                && packet.getY() == 19 && packet.getOffsetX() == 2.0f && packet.getOffsetY() == 3.0f
                && packet.getOffsetZ() == 2.0f && packet.getSpeed() == 0.0f && packet.getX() % 1 == 0.0
                && packet.getZ() % 1 == 0.0;
    }

    private static boolean isDragonSpawned(Dragon dragon) {
        for (Dragon drag : getSpawnedDragons()) {
            if (dragon.name.equals(drag.name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean doesDragonExist(Entity dragon) {
        for (Dragon drag : getSpawnedDragons()) {
            if (drag.entity != null && drag.entity.getUuidAsString().equals(dragon.getUuidAsString())) {
                return true;
            }
        }
        return false;
    }

    private static Dragon getHigherPriority(Dragon first, Dragon second, boolean archerTeam) {
        if (archerTeam) {
            return first.archPriority > second.archPriority ? first : second;
        } else {
            return first.bersPriority > second.bersPriority ? first : second;
        }
    }

    private static void announceDragonSpawn(Dragon drag, boolean split) {
        Utils.showTitleCustom(drag.name.toUpperCase() + " IS SPAWNING!", 60, -20, 4.0f, drag.color.hex);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
        if (split) {
            Utils.infoRaw(Text.literal(drag.name + " is your priority dragon.").setStyle(Style.EMPTY.withColor(drag.color.hex)));
        } else {
            Utils.infoRaw(Text.literal(drag.name + " is spawning.").setStyle(Style.EMPTY.withColor(drag.color.hex)));
        }
    }

    @EventHandler
    private static void onScreenOpen(ScreenOpenEvent event) {
        isTerminalBuilt = false;
        isInTerminal = false;

        if (Utils.isInDungeons() && event.screen.getTitle().getString().equals("Click the button on time!")) {
            isInTerminal = true;
            if (Config.melodyAnnounce) {
                if (melodyTicks == 0 && !Config.melodyMessage.isEmpty()) {
                    Utils.sendMessage(Config.melodyMessage);
                    melodyTicks = 100;
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isOnDungeonFloor("6")) {
            if (gyroTicks > 0) {
                Utils.showTitleCustom("GYRO: " + decimalFormat.format(gyroTicks / 20.0f) + "s", 1, 25, 2.5f, 0xffff00);
            }
        }
        if (Utils.isOnDungeonFloor("7")) {
            if (melodyTicks > 0) {
                melodyTicks--;
            }
            if (Config.solveDevices) {
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
        if (Config.solveTerminals && Utils.isInDungeons() && !isTerminalBuilt) {
            isTerminalBuilt = event.isFinal;
            List<Slot> orderSlots = new ArrayList<>();
            List<Slot> colorSlots = new ArrayList<>();
            for (Slot slot : event.handler.slots) {
                ItemStack stack = event.inventory.getStack(slot.id);
                if (!stack.isEmpty()) {
                    if (event.title.startsWith("Correct all the panes!")) {
                        isInTerminal = true;
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (event.title.startsWith("Click in order!")) {
                        isInTerminal = true;
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE && event.isFinal) {
                            orderSlots.add(slot);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (event.title.startsWith("What starts with:") && event.title.endsWith("?")) {
                        isInTerminal = true;
                        String character = String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)).toLowerCase();
                        String name = Formatting.strip(stack.getName().getString()).toLowerCase().trim();
                        if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(stack)) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (event.title.startsWith("Select all the") && event.title.endsWith("items!")) {
                        isInTerminal = true;
                        String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                        String colorName = color.equals("SILVER") ? "light_gray" : color.toLowerCase().replace(" ", "_");
                        for (DyeColor dye : DyeColor.values()) {
                            if (dye.getName().equals(colorName)) {
                                if (!Utils.hasGlint(stack) && checkStackColor(stack, dye, colorName)) {
                                    Utils.setSpoofed(event.screen, slot, firstStack);
                                    Utils.setDisabled(event.screen, slot, false);
                                } else {
                                    Utils.setSpoofed(event.screen, slot, backgroundStack);
                                    Utils.setDisabled(event.screen, slot, true);
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
                        Utils.setSpoofed(event.screen, slot, backgroundStack);
                        Utils.setDisabled(event.screen, slot, true);
                    }
                }
            }
            if (!orderSlots.isEmpty()) {
                orderSlots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                Slot first = orderSlots.getFirst();
                Utils.setSpoofed(event.screen, first, firstStack);
                Utils.setDisabled(event.screen, first, false);
                if (orderSlots.size() > 1) {
                    Slot second = orderSlots.get(1);
                    Utils.setSpoofed(event.screen, second, secondStack);
                    Utils.setDisabled(event.screen, second, true);
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
                    if (target == 0) {
                        Utils.setSpoofed(event.screen, slot, backgroundStack);
                        Utils.setDisabled(event.screen, slot, true);
                    } else {
                        Utils.setSpoofed(event.screen, slot, stackWithCount(target));
                        Utils.setDisabled(event.screen, slot, false);
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (Config.gyroTimer && Utils.isOnDungeonFloor("6") && Config.dungeonClass.equals("Mage")) {
            Block newBlock = event.newState.getBlock();
            if (gyroTicks == 0 && event.oldState.isAir() && (newBlock.equals(Blocks.SKELETON_WALL_SKULL) || newBlock.equals(Blocks.PLAYER_HEAD))) {
                if (mc.world.getBlockState(event.pos.down(1)).getBlock().equals(Blocks.BROWN_TERRACOTTA) && Utils.horizontalDistance(mc.player.getPos(), event.pos.toCenterPos()) <= 4.5) {
                    gyroTicks = Utils.isOnDungeonFloor("M6") ? 80 : 100;
                }
            }
        }
        if (Config.solveDevices && Utils.isOnDungeonFloor("7")) {
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
            if (Config.wishReminder && Config.dungeonClass.equals("Healer") && event.messagePlain.equals(wishMsg)) {
                Utils.showTitleCustom("WISH!", 40, -20, 4.0f, 0x00ff00);
                Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
            }
            if (Config.dungeonClass.equals("Mage")) {
                if (Config.campReminder && event.messagePlain.equals(campMsg)) {
                    Utils.showTitleCustom("CAMP BLOOD!", 40, -20, 4.0f, 0xff4646);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
                if (Config.ragAxeReminder && Utils.isOnDungeonFloor("M5") && event.messagePlain.equals(ragAxeMsg)) {
                    Utils.showTitleCustom("RAG AXE!", 40, -20, 4.0f, 0xffff00);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
                if (Config.gyroTimer && event.messagePlain.equals(sadanEnterMsg)) {
                    gyroTicks = 270;
                }
                if (Config.gyroTimer && event.messagePlain.equals(sadanLastMsg)) {
                    gyroTicks = 220;
                }
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Utils.isInDungeons()) {
            if (Config.keyHighlight && !dungeonKeys.contains(event.entity)) {
                if (event.namePlain.equals("Wither Key") || event.namePlain.equals("Blood Key")) {
                    dungeonKeys.add(event.entity);
                }
            }
            if (Config.spiritHighlight && !spiritBows.contains(event.entity)) {
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
        if (!dungeonKeys.isEmpty()) {
            List<Entity> keys = new ArrayList<>(dungeonKeys);
            for (Entity ent : keys) {
                if (ent.isAlive()) {
                    event.drawFilled(Box.of(ent.getPos().add(0, 1.5, 0), 0.9, 1.25, 0.9), false, RenderColor.fromColor(Config.keyColor));
                } else {
                    dungeonKeys.remove(ent);
                }
            }
        }
        if (!spiritBows.isEmpty()) {
            List<Entity> bows = new ArrayList<>(spiritBows);
            for (Entity ent : bows) {
                if (ent.isAlive()) {
                    BlockPos ground = Utils.findGround(ent.getBlockPos(), 4);
                    Vec3d pos = ent.getPos();
                    Vec3d posAdjust = new Vec3d(pos.x, ground.up(1).getY() + 1, pos.z);
                    event.drawFilled(Box.of(posAdjust, 0.8, 1.75, 0.8), true, RenderColor.fromColor(Config.spiritColor));
                } else {
                    spiritBows.remove(ent);
                }
            }
        }
        if (!spawnedDragons.isEmpty()) {
            for (Dragon drag : getSpawnedDragons()) {
                if (Config.dragBoxes) {
                    event.drawOutline(drag.area, false, drag.color);
                }
                if (Config.dragTimer && !drag.spawned) {
                    event.drawText(drag.area.getCenter(), Text.of(decimalFormat.format(drag.spawnTicks / 20.0f) + "s"), 0.2f, true, drag.color);
                }
                if (Config.dragHealth && drag.entity != null) {
                    Vec3d pos = drag.entity.getLerpedPos(event.tickCounter.getTickDelta(true)); // should make the text move smoothly with the dragons
                    event.drawText(pos, Text.of(decimalFormat.format(drag.health * 0.000001) + "M"), 0.2f, true, drag.color);
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (Config.hideMageBeam && Utils.isInDungeons() && event.type.equals(ParticleTypes.FIREWORK)) {
            event.cancel();
        }
        if (isDragonPhase() && isDragonParticle(event.packet)) {
            Vec3d pos = new Vec3d(event.packet.getX(), event.packet.getY(), event.packet.getZ());
            for (Dragon drag : dragons) {
                if (!isDragonSpawned(drag) && drag.area.contains(pos) && pos.y >= drag.spawnMinY) {
                    spawnedDragons.add(drag.copy());
                    if (!dragonSplitDone && spawnedDragons.size() == 2) {
                        if (Config.dragAlert) {
                            double power = getPowerLevel();
                            Dragon first = spawnedDragons.getFirst();
                            Dragon second = spawnedDragons.getLast();
                            boolean purple = first.name.equals("Purple") || second.name.equals("Purple");
                            if ((power >= Config.dragSkipEasy && purple) || power >= Config.dragSkip) {
                                announceDragonSpawn(getHigherPriority(first, second, isArcherTeam()), true);
                            } else { // no split
                                announceDragonSpawn(getHigherPriority(first, second, true), true);
                            }
                            dragonSplitDone = true;
                        }
                    } else {
                        if (Config.dragAlert) {
                            Utils.info(drag.name + " is spawning");
                            announceDragonSpawn(drag, false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        dragonSplitDone = false;
        spawnedDragons.clear();
        gyroTicks = 0;
        melodyTicks = 0;
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (event.entity instanceof EnderDragonEntity dragonEntity && isDragonPhase()) {
            float health = dragonEntity.getHealth();
            for (Dragon drag : getSpawnedDragons()) {
                if (drag.spawning && !doesDragonExist(event.entity) && drag.area.contains(event.entity.getPos())) {
                    Utils.info(drag.name + " dragon entity spawned");
                    drag.entity = event.entity;
                    drag.health = health;
                    drag.spawning = false;
                    drag.spawned = true;
                    if (Config.dragGlow) {
                        Rendering.Entities.drawGlow(event.entity, true, drag.color);
                    }
                } else if (drag.spawned && event.entity.getUuidAsString().equals(drag.entity.getUuidAsString())) {
                    if (health > 0.0f) {
                        drag.health = health;
                    } else {
                        Utils.info(drag.name + " dragon entity killed and removed");
                        spawnedDragons.removeIf(dragon -> dragon.name.equals(drag.name));
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Config.gyroTimer && gyroTicks > 0) {
            gyroTicks--;
        }
        for (Dragon drag : getSpawnedDragons()) {
            if (drag.entity == null && drag.spawnTicks > 0) {
                drag.spawnTicks--;
            }
        }
    }

    private static class Dragon {
        public String name;
        public int archPriority;
        public int bersPriority;
        public RenderColor color;
        public double spawnMinY;
        public Box area;
        public Entity entity = null;
        public int spawnTicks = 100;
        public boolean spawned = false;
        public boolean spawning = true;
        public float health = 0.0f;

        public Dragon(String name, int archPriority, int bersPriority, RenderColor color, double spawnMinY, Box area) {
            this.name = name;
            this.archPriority = archPriority;
            this.bersPriority = bersPriority;
            this.color = color;
            this.spawnMinY = spawnMinY;
            this.area = area;
        }

        public Dragon copy() {
            return new Dragon(this.name, this.archPriority, this.bersPriority, this.color, this.spawnMinY, this.area);
        }
    }
}
