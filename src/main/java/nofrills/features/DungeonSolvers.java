package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class DungeonSolvers {
    private static final ItemStack backgroundStack = Utils.setStackName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    private static final ItemStack firstStack = Utils.setStackName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    private static final ItemStack secondStack = Utils.setStackName(Items.BLUE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "9Click next.");
    private static final List<Item> colorsOrder = List.of(new Item[]{
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
    });
    private static final List<BlockPos> sharpshooterList = new ArrayList<>();
    private static final Box sharpshooterTarget = Box.enclosing(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
    private static final Box sharpshooterArea = Box.enclosing(new BlockPos(63, 127, 35), new BlockPos(63, 128, 35));
    private static final List<Entity> dungeonKeys = new ArrayList<>();
    private static final List<Entity> spiritBows = new ArrayList<>();
    public static boolean isInTerminal = false;
    private static BlockPos sharpshooterNext = null;
    private static boolean isTerminalBuilt = false;
    private static int melodyTicks = 0;

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

    // for WIP arrow align solver
    private static BlockPos getRotationOffset(BlockPos pos, int rotation) {
        return switch (rotation) {
            case 1 -> pos.add(0, 0, -1);
            case 3 -> pos.add(0, -1, 0);
            case 5 -> pos.add(0, 0, 1);
            case 7 -> pos.add(0, 1, 0);
            default -> pos;
        };
    }

    private static boolean isSharpshooterActive() {
        return !mc.world.getOtherEntities(null, sharpshooterArea, ent -> ent.getType() == EntityType.PLAYER).isEmpty();
    }

    @EventHandler
    public static void onScreenOpen(ScreenOpenEvent event) {
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
    public static void onTick(WorldTickEvent event) {
        if (Utils.isInDungeons()) {
            if (melodyTicks > 0) {
                melodyTicks--;
            }
            if (sharpshooterNext != null || !sharpshooterList.isEmpty()) {
                if (!isSharpshooterActive()) {
                    sharpshooterNext = null;
                    sharpshooterList.clear();
                }
            }
        }
    }

    @EventHandler
    public static void onSlotUpdate(ScreenSlotUpdateEvent event) {
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
    public static void onChat(ChatMsgEvent event) {
        if (Config.wishReminder && Utils.isInDungeons() && Config.dungeonClass.equals("Healer")) {
            if (event.messagePlain.equals("⚠ Maxor is enraged! ⚠")) {
                Utils.showTitle("§a§lWISH!", "", 5, 40, 5);
                Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
            }
        }
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
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
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (Config.solveDevices && Utils.isInDungeons()) {
            if (sharpshooterTarget.contains(event.packet.getPos().toCenterPos()) && isSharpshooterActive()) {
                if (event.packet.getState() == Blocks.EMERALD_BLOCK.getDefaultState()) {
                    sharpshooterNext = event.packet.getPos();
                }
                if (event.packet.getState() == Blocks.BLUE_TERRACOTTA.getDefaultState()) {
                    sharpshooterList.add(event.packet.getPos());
                }
            }
        }
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (sharpshooterNext != null) {
            event.drawFilled(Box.enclosing(sharpshooterNext, sharpshooterNext), true, RenderColor.fromHex(0x00ff00, 1.0f));
        }
        if (!sharpshooterList.isEmpty()) {
            for (BlockPos pos : sharpshooterList) {
                event.drawFilled(Box.enclosing(pos, pos), true, RenderColor.fromHex(0xff0000, 1.0f));
            }
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
    }
}
