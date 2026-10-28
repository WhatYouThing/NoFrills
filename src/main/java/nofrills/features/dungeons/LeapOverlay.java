package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.events.ScreenCloseEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static nofrills.Main.mc;

@EventListener
public class LeapOverlay {
    public static final Feature instance = new Feature("leapOverlay");

    public static final SettingDouble scale = new SettingDouble(3.0, "scale", instance.key());
    public static final SettingKeybind firstKey = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "firstKey", instance);
    public static final SettingKeybind secondKey = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "secondKey", instance);
    public static final SettingKeybind thirdKey = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "thirdKey", instance);
    public static final SettingKeybind fourthKey = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "fourthKey", instance);
    public static final RenderColor nameColor = RenderColor.fromHex(0xffffff);
    public static final RenderColor deadColor = RenderColor.fromHex(0xaaaaaa);

    private static final List<LeapButton> leapButtons = new ArrayList<>();

    public static boolean isLeapMenu(String title) {
        return instance.isActive() && title.equals("Spirit Leap") && Utils.isInDungeons();
    }

    public static List<LeapButton> getLeapButtons() {
        return leapButtons;
    }

    @EventHandler
    private static void onScreenOpen(ScreenOpenEvent event) {
        if (isLeapMenu(event.screen.getTitle().getString())) {
            String self = mc.player.getName().getString();
            List<DungeonUtil.Teammate> teammates = DungeonUtil.getAliveTeammates(true);
            List<LeapTarget> targets = new ArrayList<>();
            for (Map.Entry<String, String> entry : DungeonUtil.getClassCache().entrySet()) {
                String name = entry.getKey();
                String dungeonClass = entry.getValue();
                if (!name.equalsIgnoreCase(self)) {
                    targets.add(new LeapTarget(name, dungeonClass, teammates.stream().noneMatch(teammate -> teammate.name().equals(name))));
                }
            }
            targets.sort(Comparator.comparing(target -> target.dungeonClass + target.name));
            if (targets.size() < 4) {
                int missing = 4 - targets.size();
                for (int i = 1; i <= missing; i++) {
                    targets.add(LeapTarget.empty());
                }
            }
            leapButtons.clear();
            for (LeapTarget target : targets) {
                leapButtons.add(new LeapOverlay.LeapButton(target, leapButtons.size()));
            }
        }
    }

    @EventHandler
    private static void onScreenClose(ScreenCloseEvent event) {
        leapButtons.clear();
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (mc.gui.screen() != null && isLeapMenu(mc.gui.screen().getTitle().getString())) {
            List<SettingKeybind> leapKeys = List.of(firstKey, secondKey, thirdKey, fourthKey);
            for (int i = 0; i < leapKeys.size(); i++) {
                if (leapKeys.get(i).isKey(event.key) && leapButtons.size() >= i + 1) {
                    leapButtons.get(i).click(((AbstractContainerScreen<?>) mc.gui.screen()).getMenu());
                    event.cancel();
                    return;
                }
            }
        }
    }

    public static class LeapTarget {
        public String name;
        public String dungeonClass;
        public boolean dead;

        public LeapTarget(String name, String dungeonClass, boolean dead) {
            this.name = name;
            this.dungeonClass = dungeonClass;
            this.dead = dead;
        }

        public static LeapTarget empty() {
            return new LeapTarget("", "Empty", false);
        }
    }

    public static class LeapButton implements Renderable {
        public final Component player;
        public final Component dungeonClass;
        public final boolean dead;
        private final RenderColor nameColor;
        private final RenderColor classColor;
        private final float offsetX;
        private final float offsetY;
        private final RenderColor background;
        private final RenderColor backgroundHover;
        private final RenderColor border;
        public int minX = 0;
        public int minY = 0;
        public int maxX = 0;
        public int maxY = 0;

        public LeapButton(LeapTarget target, int index) {
            this.player = Component.literal(target.name);
            this.dungeonClass = Component.literal(target.dungeonClass);
            this.dead = target.dead;
            this.nameColor = LeapOverlay.nameColor;
            this.classColor = target.dungeonClass.equals("Empty") ? deadColor : ClassColors.getForClass(target.dungeonClass).orElse(nameColor);
            this.background = RenderColor.fromFloat(0.0f, 0.0f, 0.0f, 0.67f);
            this.backgroundHover = RenderColor.fromFloat(this.classColor.r * 0.33f, this.classColor.g * 0.33f, this.classColor.b * 0.33f, 0.67f);
            this.border = RenderColor.fromFloat(this.classColor.r, this.classColor.g, this.classColor.b, 1.0f);
            this.offsetX = index == 0 || index == 2 ? 0.25f : 0.55f;
            this.offsetY = index <= 1 ? 0.25f : 0.55f;
        }

        private int getX(GuiGraphicsExtractor context, float xOffset) {
            return (int) Math.floor(context.guiWidth() * xOffset);
        }

        private int getY(GuiGraphicsExtractor context, float yOffset) {
            return (int) Math.floor(context.guiHeight() * yOffset);
        }

        public boolean isHovered(double mouseX, double mouseY) {
            return !this.player.getString().isEmpty() && mouseX >= this.minX && mouseX <= this.maxX && mouseY >= this.minY && mouseY <= this.maxY;
        }

        public void click(AbstractContainerMenu handler) {
            for (Slot slot : Utils.getContainerSlots(handler)) {
                ItemStack stack = slot.getItem();
                if (!stack.getItem().equals(Items.PLAYER_HEAD)) continue;
                List<String> lore = Utils.getLoreLines(stack);
                if (!lore.isEmpty() && Utils.toPlain(stack.getHoverName()).equals(this.player.getString()) && lore.getFirst().equals("Click to teleport!")) {
                    Utils.click(handler.containerId, slot.index, 0, ContainerInput.PICKUP);
                    Utils.infoRaw(Utils.formatText("Leaping to {}.", this.player.getString()).withStyle(ChatFormatting.GREEN));
                    return;
                }
            }
            Utils.infoRaw(Utils.formatText("Could not leap to {}, teleport button not found.", this.player.getString()).withStyle(ChatFormatting.RED));
        }

        public void drawText(GuiGraphicsExtractor context, Component text, int x, int y, float scale, RenderColor color) {
            context.pose().pushMatrix();
            context.pose().translate(x - x * scale, y - y * scale);
            context.pose().scale(scale);
            context.centeredText(mc.font, text, x, y, color.argb);
            context.pose().popMatrix();
        }

        @Override
        public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            this.minX = getX(context, this.offsetX);
            this.minY = getY(context, this.offsetY);
            this.maxX = getX(context, this.offsetX + 0.2f);
            this.maxY = getY(context, this.offsetY + 0.2f);
            context.fill(minX, minY, maxX, maxY, this.isHovered(mouseX, mouseY) ? backgroundHover.argb : background.argb);
            if (!this.player.getString().isEmpty()) {
                Rendering.drawBorder(context, minX, minY, maxX - minX, maxY - minY, border);
            }
            float textScale = (float) (scale.value() / mc.options.guiScale().get());
            int textX = this.minX + (this.maxX - this.minX) / 2;
            int playerTextY = (int) (this.minY + (this.maxY - this.minY) * 0.25);
            int classTextY = (int) (this.minY + (this.maxY - this.minY) * 0.5);
            int deadTextY = (int) (this.minY + (this.maxY - this.minY) * 0.75);
            this.drawText(context, this.player, textX, playerTextY, textScale, this.nameColor);
            this.drawText(context, this.dungeonClass, textX, classTextY, textScale, this.classColor);
            if (this.dead) this.drawText(context, Component.literal("DEAD"), textX, deadTextY, textScale, deadColor);
        }
    }
}
