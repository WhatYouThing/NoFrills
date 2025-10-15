package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import nofrills.config.*;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.ScreenOptions;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class LeapOverlay {
    public static final Feature instance = new Feature("leapOverlay");

    public static final SettingBool send = new SettingBool(false, "send", instance.key());
    public static final SettingString message = new SettingString("/pc Leaped to {name}!", "message", instance.key());
    public static final SettingDouble scale = new SettingDouble(3.0, "scale", instance.key());
    public static final SettingColor healer = new SettingColor(RenderColor.fromHex(0xecb50c), "healerColor", instance.key());
    public static final SettingColor mage = new SettingColor(RenderColor.fromHex(0x1793c4), "mageColor", instance.key());
    public static final SettingColor bers = new SettingColor(RenderColor.fromHex(0xe7413c), "bersColor", instance.key());
    public static final SettingColor arch = new SettingColor(RenderColor.fromHex(0x4a14b7), "archColor", instance.key());
    public static final SettingColor tank = new SettingColor(RenderColor.fromHex(0x768f46), "tankColor", instance.key());

    private static final String leapMenuName = "Spirit Leap";
    private static final RenderColor nameColor = RenderColor.fromHex(0xffffff);
    private static final RenderColor deadColor = RenderColor.fromHex(0xaaaaaa);

    public static boolean isLeapMenu(String title) {
        return instance.isActive() && Utils.isInDungeons() && title.equals(leapMenuName);
    }

    private static RenderColor getColor(String className) {
        return switch (className) {
            case "Healer" -> healer.value();
            case "Mage" -> mage.value();
            case "Berserk" -> bers.value();
            case "Archer" -> arch.value();
            case "Tank" -> tank.value();
            case "DEAD" -> deadColor;
            default -> nameColor;
        };
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && event.isFinal && event.title.equals(leapMenuName) && Utils.isInDungeons()) {
            List<LeapTarget> validTargets = new ArrayList<>();
            List<LeapTarget> deadTargets = new ArrayList<>();
            List<String> tabListLines = Utils.getTabListLines();
            for (Slot slot : event.handler.slots) {
                ItemStack stack = event.inventory.getStack(slot.id);
                if (!stack.isEmpty() && stack.getItem().equals(Items.PLAYER_HEAD)) {
                    List<String> lore = Utils.getLoreLines(stack);
                    if (!lore.isEmpty()) {
                        String line = lore.getFirst();
                        String name = Utils.toPlainString(stack.getName());
                        if (name.equals("Unknown Player") || line.equals("This player is offline!")) {
                            continue;
                        }
                        if (line.equals("This player is currently dead!")) {
                            deadTargets.add(new LeapTarget(-1, name, "", false, true));
                        } else if (line.equals("Click to teleport!")) {
                            for (String entry : tabListLines) {
                                if (entry.contains(name)) {
                                    for (String dungeonClass : SkyblockData.dungeonClasses) {
                                        if (entry.contains("(" + dungeonClass) && entry.endsWith(")")) {
                                            int classStart = entry.indexOf("("); // this may or may not be broken with a nick hider. too bad!
                                            int classEnd = Math.min(entry.indexOf(" ", classStart), entry.indexOf(")", classStart));
                                            validTargets.add(new LeapTarget(slot.id, name, entry.substring(classStart + 1, classEnd), false, false));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            validTargets.sort(Comparator.comparing(target -> target.dungeonClass + target.name));
            deadTargets.sort(Comparator.comparing(target -> target.name));
            List<LeapTarget> targets = new ArrayList<>();
            targets.addAll(validTargets);
            targets.addAll(deadTargets);
            if (targets.size() < 4) {
                int missing = 4 - targets.size();
                for (int i = 1; i <= missing; i++) {
                    targets.add(new LeapTarget(-1, "", "", true, false));
                }
            }
            for (LeapTarget target : targets) {
                String name = target.empty ? "Empty" : target.name;
                String dungeonClass = target.dead ? "DEAD" : target.dungeonClass;
                ((ScreenOptions) event.screen).nofrills_mod$addLeapButton(target.slotId, name, dungeonClass, getColor(dungeonClass));
            }
        }
    }

    private static class LeapTarget {
        public int slotId;
        public String name;
        public String dungeonClass;
        public boolean empty;
        public boolean dead;

        public LeapTarget(int slotId, String name, String dungeonClass, boolean empty, boolean dead) {
            this.slotId = slotId;
            this.name = name;
            this.dungeonClass = dungeonClass;
            this.empty = empty;
            this.dead = dead;
        }
    }

    public static class LeapButton implements Drawable {
        public final int slotId;
        public final Text player;
        public final Text dungeonClass;
        private final RenderColor nameColor;
        private final RenderColor classColor;
        private final float offsetX;
        private final float offsetY;
        private final int background;
        private final int backgroundHover;
        private final int border;
        public int minX = 0;
        public int minY = 0;
        public int maxX = 0;
        public int maxY = 0;
        public boolean hovered = false;

        public LeapButton(int slotId, int index, String player, String dungeonClass, RenderColor classColor) {
            this.slotId = slotId;
            this.player = Text.of(player);
            this.dungeonClass = Text.of(dungeonClass);
            this.nameColor = LeapOverlay.nameColor;
            this.classColor = classColor;
            background = ColorHelper.fromFloats(0.67f, 0.0f, 0.0f, 0.0f);
            backgroundHover = ColorHelper.fromFloats(0.67f, this.classColor.r * 0.33f, this.classColor.g * 0.33f, this.classColor.b * 0.33f);
            border = ColorHelper.fromFloats(1.0f, this.classColor.r, this.classColor.g, this.classColor.b);
            offsetX = index == 0 || index == 2 ? 0.25f : 0.55f;
            offsetY = index <= 1 ? 0.25f : 0.55f;
        }

        private int getX(DrawContext context, float xOffset) {
            return (int) Math.floor(context.getScaledWindowWidth() * xOffset);
        }

        private int getY(DrawContext context, float yOffset) {
            return (int) Math.floor(context.getScaledWindowHeight() * yOffset);
        }

        public boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            minX = getX(context, this.offsetX);
            minY = getY(context, this.offsetY);
            maxX = getX(context, this.offsetX + 0.2f);
            maxY = getY(context, this.offsetY + 0.2f);
            context.fill(minX, minY, maxX, maxY, hovered ? backgroundHover : background); // for some reason its ARGB rather than RGBA
            if (slotId != -1) {
                context.drawBorder(minX, minY, maxX - minX, maxY - minY, border);
            }
            float textScale = (float) (scale.value() / mc.options.getGuiScale().getValue());
            int textX = minX + (maxX - minX) / 2;
            int playerTextY = (int) (minY + (maxY - minY) * 0.25);
            int classTextY = (int) (minY + (maxY - minY) * 0.5);
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(textX - textX * textScale, playerTextY - playerTextY * textScale);
            context.getMatrices().scale(textScale);
            context.drawCenteredTextWithShadow(mc.textRenderer, this.player, textX, playerTextY, this.nameColor.argb);
            context.getMatrices().popMatrix();
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(textX - textX * textScale, classTextY - classTextY * textScale);
            context.getMatrices().scale(textScale);
            context.drawCenteredTextWithShadow(mc.textRenderer, this.dungeonClass, textX, classTextY, this.classColor.argb);
            context.getMatrices().popMatrix();
        }
    }
}
