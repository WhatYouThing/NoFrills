package nofrills.hud;

import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.util.Window;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingDouble;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class HudElement extends DraggableContainer<FlowLayout> {
    public final MutableText elementLabel;
    public final Feature instance;
    public final SettingBool added;
    public final SettingDouble xPos;
    public final SettingDouble yPos;
    public final SettingBool useBackground;
    public final SettingColor background;
    public final Identifier identifier;
    public final Surface disabledSurface = Surface.flat(0x55ff0000);
    public MutableText elementDesc = Text.empty();
    public FlowLayout layout;
    public HudSettings options;
    public boolean toggling = false;

    public HudElement(FlowLayout layout, Feature instance, String label) {
        super(Sizing.content(), Sizing.content(), layout);
        this.elementLabel = Text.literal(label);
        this.instance = instance;
        this.added = new SettingBool(false, "added", instance);
        this.xPos = new SettingDouble(0.5, "x", instance);
        this.yPos = new SettingDouble(0.5, "y", instance);
        this.useBackground = new SettingBool(false, "useBackground", instance);
        this.background = new SettingColor(RenderColor.fromArgb(0x40000000), "background", instance);
        this.identifier = Identifier.of("nofrills", Utils.toLower(label.replaceAll(" ", "-")));
        this.positioning(Positioning.absolute(0, 0));
        this.layout = layout;
        this.layout.sizing(Sizing.content(), Sizing.content());
        this.layout.allowOverflow(true);
        this.foreheadSize(0);
        this.allowOverflow(true);
        this.child(this.layout);
        HudManager.addNew(this);
    }

    public HudElement(Feature instance, String label) {
        this(Containers.horizontalFlow(Sizing.content(), Sizing.content()), instance, label);
    }

    @Override
    protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {
        try {
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.isAdded()) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.toggling = true;
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                mc.setScreen(this.options);
                return true;
            }
        }
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (this.isAdded()) {
            boolean result = super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
            Window window = mc.getWindow();
            this.savePosition(this.xOffset / window.getScaledWidth(), this.yOffset / window.getScaledHeight());
            return result;
        }
        return false;
    }

    @Override
    public Component childAt(int x, int y) {
        if (this.isInBoundingBox(x, y)) { // gets rid of the forehead
            return this;
        }
        return super.childAt(x, y);
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
        if (!this.isAdded()) {
            return false;
        }
        boolean active = this.instance.isActive();
        this.layout.surface(active ? this.getBackground() : this.disabledSurface);
        return active || HudManager.isEditingHud();
    }

    public HudSettings getBaseSettings() {
        return this.getBaseSettings(new ArrayList<>());
    }

    public HudSettings getBaseSettings(List<FlowLayout> extra) {
        List<FlowLayout> list = new ArrayList<>(extra);
        list.add(new Settings.Toggle("Use Background", this.useBackground, "Draw a background for this element."));
        list.add(new Settings.ColorPicker("Background", true, this.background, "The color of the background."));
        HudSettings settings = new HudSettings(list);
        settings.setTitle(this.elementLabel);
        return settings;
    }

    public void setDesc(String description) {
        this.elementDesc = Text.literal(description);
    }

    public boolean isEditingHud() {
        return HudManager.isEditingHud();
    }

    public void updatePosition() {
        Window window = mc.getWindow();
        int width = window.getScaledWidth(), height = window.getScaledHeight();
        this.xOffset = Math.clamp(this.xPos.value() * width, 0, Math.clamp(width - this.width, 0, width));
        this.yOffset = Math.clamp(this.yPos.value() * height, 0, Math.clamp(height - this.height, 0, height));
        this.updateX(0);
        this.updateY(0);
    }

    public void savePosition(double x, double y) {
        this.xPos.set(x);
        this.yPos.set(y);
    }

    public void toggle() {
        this.instance.setActive(!this.instance.isActive());
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }
}