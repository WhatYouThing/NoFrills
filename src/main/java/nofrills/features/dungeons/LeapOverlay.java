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
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.ScreenOptions;
import nofrills.misc.Utils;

import java.util.*;

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
    public static final RenderColor nameColor = RenderColor.fromHex(0xffffff);
    public static final RenderColor deadColor = RenderColor.fromHex(0xaaaaaa);

    private static final String leapMenuName = "Spirit Leap";

    public static boolean isLeapMenu(String title) {
        return instance.isActive() && Utils.isInDungeons() && title.equals(leapMenuName);
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && event.isFinal && event.title.equals(leapMenuName) && Utils.isInDungeons()) {
            List<LeapTarget> targets = new ArrayList<>();
            HashMap<String, Integer> alive = new HashMap<>(4);
            for (Slot slot : Utils.getContainerSlots(event.handler)) {
                ItemStack stack = slot.getStack();
                if (!stack.getItem().equals(Items.PLAYER_HEAD)) continue;
                List<String> lore = Utils.getLoreLines(stack);
                String name = Utils.toPlain(stack.getName());
                String dungeonClass = DungeonUtil.getPlayerClass(name);
                if (!lore.isEmpty() && !dungeonClass.isEmpty() && lore.getFirst().equals("Click to teleport!")) {
                    alive.put(name, slot.id);
                }
            }
            for (Map.Entry<String, String> entry : DungeonUtil.getClassCache().entrySet()) {
                int slotId = alive.getOrDefault(entry.getKey(), -1);
                targets.add(new LeapTarget(slotId, entry.getKey(), entry.getValue(), slotId == -1));
            }
            targets.sort(Comparator.comparing(target -> target.dungeonClass + target.name));
            if (targets.size() < 4) {
                int missing = 4 - targets.size();
                for (int i = 1; i <= missing; i++) {
                    targets.add(LeapTarget.empty());
                }
            }
            for (LeapTarget target : targets) {
                ((ScreenOptions) event.screen).nofrills_mod$addLeapButton(target);
            }
        }
    }

    public static class LeapTarget {
        public int slotId;
        public String name;
        public String dungeonClass;
        public boolean dead;

        public LeapTarget(int slotId, String name, String dungeonClass, boolean dead) {
            this.slotId = slotId;
            this.name = name;
            this.dungeonClass = dungeonClass;
            this.dead = dead;
        }

        public static LeapTarget empty() {
            return new LeapTarget(-1, "", "Empty", false);
        }
    }

    public static class LeapButton implements Drawable {
        public final int slotId;
        public final Text player;
        public final Text dungeonClass;
        public final boolean dead;
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

        public LeapButton(LeapTarget target, int index) {
            this.slotId = target.slotId;
            this.player = Text.literal(target.name);
            this.dungeonClass = Text.literal(target.dungeonClass);
            this.dead = target.dead;
            this.nameColor = LeapOverlay.nameColor;
            this.classColor = switch (target.dungeonClass) {
                case "Healer" -> healer.value();
                case "Mage" -> mage.value();
                case "Berserk" -> bers.value();
                case "Archer" -> arch.value();
                case "Tank" -> tank.value();
                case "Empty" -> deadColor;
                default -> nameColor;
            };
            this.background = ColorHelper.fromFloats(0.67f, 0.0f, 0.0f, 0.0f);
            this.backgroundHover = ColorHelper.fromFloats(0.67f, this.classColor.r * 0.33f, this.classColor.g * 0.33f, this.classColor.b * 0.33f);
            this.border = ColorHelper.fromFloats(1.0f, this.classColor.r, this.classColor.g, this.classColor.b);
            this.offsetX = index == 0 || index == 2 ? 0.25f : 0.55f;
            this.offsetY = index <= 1 ? 0.25f : 0.55f;
        }

        private int getX(DrawContext context, float xOffset) {
            return (int) Math.floor(context.getScaledWindowWidth() * xOffset);
        }

        private int getY(DrawContext context, float yOffset) {
            return (int) Math.floor(context.getScaledWindowHeight() * yOffset);
        }

        public boolean isHovered(double mouseX, double mouseY) {
            return this.slotId != -1 && mouseX >= this.minX && mouseX <= this.maxX && mouseY >= this.minY && mouseY <= this.maxY;
        }

        public void drawText(DrawContext context, Text text, int x, int y, float scale, RenderColor color) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(x - x * scale, y - y * scale);
            context.getMatrices().scale(scale);
            context.drawCenteredTextWithShadow(mc.textRenderer, text, x, y, color.argb);
            context.getMatrices().popMatrix();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.minX = getX(context, this.offsetX);
            this.minY = getY(context, this.offsetY);
            this.maxX = getX(context, this.offsetX + 0.2f);
            this.maxY = getY(context, this.offsetY + 0.2f);
            context.fill(this.minX, this.minY, this.maxX, this.maxY, this.isHovered(mouseX, mouseY) ? this.backgroundHover : this.background);
            if (this.slotId != -1)
                context.drawBorder(this.minX, this.minY, this.maxX - this.minX, this.maxY - this.minY, this.border);
            float textScale = (float) (scale.value() / mc.options.getGuiScale().getValue());
            int textX = this.minX + (this.maxX - this.minX) / 2;
            int playerTextY = (int) (this.minY + (this.maxY - this.minY) * 0.25);
            int classTextY = (int) (this.minY + (this.maxY - this.minY) * 0.5);
            int deadTextY = (int) (this.minY + (this.maxY - this.minY) * 0.75);
            this.drawText(context, this.player, textX, playerTextY, textScale, this.nameColor);
            this.drawText(context, this.dungeonClass, textX, classTextY, textScale, this.classColor);
            if (this.dead) this.drawText(context, Text.literal("DEAD"), textX, deadTextY, textScale, deadColor);
        }
    }
}
