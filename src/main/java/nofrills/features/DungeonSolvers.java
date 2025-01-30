package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

public class DungeonSolvers {
    private static final ItemStack backgroundStack = Utils.setStackName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    private static final ItemStack firstStack = Utils.setStackName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    private static final ItemStack secondStack = Utils.setStackName(Items.BLUE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "9Click next.");
    private static final Item[] colorsOrder = {
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
    };
    private static final List<ArrowAlignPart> arrowAlignSteps = new ArrayList<>();
    private static final List<Entity> dungeonKeys = new ArrayList<>();
    private static final List<Entity> spiritBows = new ArrayList<>();
    public static boolean isInTerminal = false;
    private static boolean isTerminalBuilt = false;
    private static int melodyTicks = 0;

    public static boolean checkStackColor(ItemStack stack, DyeColor color, String colorName) {
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

    // for WIP arrow align solver
    public static BlockPos applyRotationOffset(BlockPos pos, int rotation) {
        return switch (rotation) {
            case 0 -> pos.add(0, 1, -1);
            case 1 -> pos.add(0, 0, -1);
            case 2 -> pos.add(0, -1, -1);
            case 3 -> pos.add(0, -1, 0);
            case 4 -> pos.add(0, -1, 1);
            case 5 -> pos.add(0, 0, 1);
            case 6 -> pos.add(0, 1, 1);
            case 7 -> pos.add(0, 1, 0);
            default -> pos;
        };
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
        if (melodyTicks > 0) {
            melodyTicks--;
        }
    }

    @EventHandler
    public static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (Config.solveTerminals && Utils.isInDungeons() && !isTerminalBuilt) {
            String title = event.screen.getTitle().getString();
            GenericContainerScreenHandler handler = event.screen.getScreenHandler();
            Inventory inventory = handler.getInventory();
            isTerminalBuilt = event.isFinal;
            List<Slot> orderSlots = new ArrayList<>();
            for (Slot slot : handler.slots) {
                ItemStack stack = inventory.getStack(slot.id);
                if (!stack.isEmpty()) {
                    if (title.startsWith("Correct all the panes!")) {
                        isInTerminal = true;
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (title.startsWith("Click in order!")) {
                        isInTerminal = true;
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE && event.isFinal) {
                            orderSlots.add(slot);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (title.startsWith("What starts with:") && title.endsWith("?")) {
                        isInTerminal = true;
                        String character = String.valueOf(title.charAt(title.indexOf("'") + 1)).toLowerCase();
                        String name = Formatting.strip(stack.getName().getString()).toLowerCase().trim();
                        if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(stack)) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (title.startsWith("Select all the") && title.endsWith("items!")) {
                        isInTerminal = true;
                        String color = title.replace("Select all the", "").replace("items!", "").trim();
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
                    if (title.startsWith("Change all to same color!")) {
                        isInTerminal = true;
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
    public static void onRender(WorldRenderEvent event) {
        if (Utils.isInDungeons()) {
            if (!dungeonKeys.isEmpty()) {
                List<Entity> keys = new ArrayList<>(dungeonKeys);
                for (Entity ent : keys) {
                    if (ent.isAlive()) {
                        BlockPos ground = Utils.findGround(ent.getBlockPos(), 4);
                        Vec3d pos = ent.getPos();
                        Vec3d posAdjust = new Vec3d(pos.x, ground.up(1).getY() + 1, pos.z);
                        event.drawFilled(Box.of(posAdjust, 1, 1.5, 1), false, RenderColor.fromColor(Config.keyColor));
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

    static class ArrowAlignPart {
        public BlockPos pos;
        public int clicks;

        public ArrowAlignPart(BlockPos pos, int clicks) {
            this.pos = pos;
            this.clicks = clicks;
        }
    }
}
