package nofrills.hud;

import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import nofrills.config.*;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public abstract class HudElement extends DraggableContainer<FlowLayout> {
    public final MutableComponent elementLabel;
    public final Feature instance;
    public final SettingBool added;
    public final SettingDouble xPos;
    public final SettingDouble yPos;
    public final SettingBool hideTablist;
    public final SettingBool hideF3;
    public final SettingDouble scale;
    public final SettingBool useBackground;
    public final SettingColor background;
    public final SettingInt gridPrecision;
    public final Identifier identifier;
    public final Surface disabledSurface = Surface.flat(0x55ff0000);
    public MutableComponent elementDesc = Component.empty();
    public FlowLayout layout;
    public HudSettings options;
    public boolean toggling = false;
    public Category category = Category.Misc;

    protected HudElement(FlowLayout layout, Feature instance, String label) {
        super(Sizing.content(), Sizing.content(), layout);
        this.elementLabel = Component.literal(label);
        this.instance = instance;
        this.added = new SettingBool(false, "added", instance);
        this.xPos = new SettingDouble(0.5, "x", instance);
        this.yPos = new SettingDouble(0.5, "y", instance);
        this.hideTablist = new SettingBool(false, "hideInTablist", instance);
        this.hideF3 = new SettingBool(false, "hideInF3", instance);
        this.scale = new SettingDouble(1.0, "scale", instance);
        this.useBackground = new SettingBool(false, "useBackground", instance);
        this.background = new SettingColor(RenderColor.fromArgb(0x40000000), "background", instance);
        this.gridPrecision = new SettingInt(5, "gridPrecision", instance);
        this.identifier = Identifier.fromNamespaceAndPath("nofrills", Utils.toLower(label.replaceAll(" ", "_")));
        this.positioning(Positioning.absolute(0, 0));
        this.layout = layout;
        this.layout.sizing(Sizing.content(), Sizing.content());
        this.layout.allowOverflow(true);
        this.foreheadSize(0);
        this.allowOverflow(true);
        this.child(this.layout);
    }

    protected HudElement(Feature instance, String label) {
        this(UIContainers.horizontalFlow(Sizing.content(), Sizing.content()), instance, label);
    }

    @Override
    protected void drawChildren(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends UIComponent> children) {
        try {
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        float scale = this.scale.valueFloat();
        if (scale != 1.0f) {
            context.pose().pushMatrix();
            this.applyScaling(context, scale);
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            context.pose().popMatrix();
        } else {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (this.isAdded()) {
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.toggling = true;
                return true;
            }
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                mc.setScreen(this.options);
                return true;
            }
        }
        return super.onMouseDown(click, doubled);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        if (this.isAdded()) {
            if (click.hasShiftDown()) {
                Window window = mc.getWindow();
                double newX = Math.clamp(mc.mouseHandler.getScaledXPos(window) - this.width() * 0.5, 0, window.getGuiScaledWidth() - this.width());
                double newY = Math.clamp(mc.mouseHandler.getScaledYPos(window) - this.height() * 0.5, 0, window.getGuiScaledHeight() - this.height());
                int precision = this.gridPrecision.value();
                this.xOffset = Math.min(newX - (newX % precision), newX);
                this.yOffset = Math.min(newY - (newY % precision), newY);
                this.savePosition(this.xOffset, this.yOffset);
                return true;
            } else {
                boolean result = super.onMouseDrag(click, deltaX, deltaY);
                this.savePosition(this.xOffset, this.yOffset);
                return result;
            }
        }
        return false;
    }

    @Override
    public UIComponent childAt(int x, int y) {
        if (this.isInBoundingBox(x, y)) { // gets rid of the forehead
            return this;
        }
        return super.childAt(x, y);
    }

    @Override
    public int height() {
        float scale = this.scale.valueFloat();
        return scale != 1.0 ? (int) Math.ceil(super.height() * scale) : super.height();
    }

    @Override
    public int width() {
        float scale = this.scale.valueFloat();
        return scale != 1.0 ? (int) Math.ceil(super.width() * scale) : super.width();
    }

    public void applyScaling(OwoUIGraphics context, float scale) {
        context.pose().translate((float) (this.xOffset - this.xOffset * scale), (float) (this.yOffset - this.yOffset * scale));
        context.pose().scale(scale, scale);
    }

    public boolean isAdded() {
        return this.added.value();
    }

    public boolean isActive() {
        return this.instance.isActive() && this.isAdded();
    }

    public Surface getBackground() {
        if (this.useBackground.value()) {
            return Surface.flat(this.background.value().argb);
        }
        return Surface.BLANK;
    }

    public boolean shouldRender() {
        if (!this.isAdded()) return false;
        boolean active = this.instance.isActive();
        this.layout.surface(active ? this.getBackground() : this.disabledSurface);
        if (HudManager.isEditingHud()) {
            return true;
        }
        if (this.hideTablist.value() && mc.options.keyPlayerList.isDown()) {
            return false;
        }
        if (this.hideF3.value() && mc.debugEntries.isOverlayVisible()) {
            return false;
        }
        this.updatePosition();
        return active;
    }

    public HudSettings getBaseSettings() {
        return this.getBaseSettings(new ArrayList<>());
    }

    public HudSettings getBaseSettings(List<FlowLayout> extra) {
        List<FlowLayout> list = new ArrayList<>(extra);
        list.add(new Settings.Toggle("Hide In Tablist", this.hideTablist, "Automatically hide this element while the tablist is visible."));
        list.add(new Settings.Toggle("Hide In F3", this.hideF3, "Automatically hide this element while the F3 screen is visible."));
        list.add(new Settings.SliderDouble("Scale", 0.25, 5.0, 0.01, this.scale, "The scale multiplier of this element."));
        list.add(new Settings.Toggle("Use Background", this.useBackground, "Draw a background for this element."));
        list.add(new Settings.ColorPicker("Background", this.background, "The color of the background."));
        list.add(new Settings.SliderInt("Grid Precision", 1, 20, 1, this.gridPrecision, "The precision of the grid snapping calculation while dragging this element around."));
        HudSettings settings = new HudSettings(list);
        settings.setTitle(this.elementLabel);
        return settings;
    }

    public void setDesc(String description) {
        this.elementDesc = Component.literal(description);
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isEditingHud() {
        return HudManager.isEditingHud();
    }

    public void updatePosition() {
        Window window = mc.getWindow();
        this.updatePosition(this.xPos.value() * window.getGuiScaledWidth(), this.yPos.value() * window.getGuiScaledHeight());
    }

    public void updatePosition(double x, double y) {
        Window window = mc.getWindow();
        this.xOffset = Math.clamp(x, 0, Math.clamp(window.getGuiScaledWidth() - this.width(), 0, window.getGuiScaledWidth()));
        this.yOffset = Math.clamp(y, 0, Math.clamp(window.getGuiScaledHeight() - this.height(), 0, window.getGuiScaledHeight()));
        this.updateX(0);
        this.updateY(0);
    }

    public void savePosition(double x, double y) {
        Window window = mc.getWindow();
        this.xPos.set(x / window.getGuiScaledWidth());
        this.yPos.set(y / window.getGuiScaledHeight());
    }

    public boolean isInSnapDistance(MouseButtonEvent click) {
        double mouseX = this.x() + click.x();
        double mouseY = this.y() + click.y();
        int precision = this.gridPrecision.value();
        return mouseX >= this.x() - precision && mouseX <= this.x() + this.width() + precision
                && mouseY >= this.y() - precision && mouseY <= this.y() + this.height() + precision;
    }

    public double snapDelta(double delta, double offset) {
        double newOffset = offset + delta;
        double snapOffset = Math.min(newOffset - (newOffset % this.gridPrecision.value()), newOffset);
        return offset - snapOffset;
    }

    public void toggle() {
        this.instance.setActive(!this.instance.isActive());
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public enum Category {
        Info,
        Dungeons,
        Kuudra,
        Slayer,
        Fishing,
        Mining,
        Misc
    }
}