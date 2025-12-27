package nofrills.hud;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.features.misc.AutoSave;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.RenderColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {
    public HudEditorScreen() {
        super(Text.of(""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.allowOverflow(false);
        for (HudElement element : HudManager.getElements()) {
            root.child(element);
        }
        HudManager.armor.updateArmor();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (HudElement element : HudManager.getElements()) {
            if (element.isAdded()) element.updatePosition();
        }
        super.render(context, mouseX, mouseY, delta);
        int center = context.getScaledWindowWidth() / 2;
        context.drawCenteredTextWithShadow(mc.textRenderer, "NoFrills HUD Editor", center, 10, RenderColor.white.argb);
        context.drawCenteredTextWithShadow(mc.textRenderer, "Left click element to hide", center, 20, RenderColor.white.argb);
        context.drawCenteredTextWithShadow(mc.textRenderer, "Right click element to view its settings", center, 30, RenderColor.white.argb);
        context.drawCenteredTextWithShadow(mc.textRenderer, "Right click screen to add/remove elements", center, 40, RenderColor.white.argb);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.uiAdapter == null) {
            return false;
        }
        boolean clicked = this.uiAdapter.mouseClicked(mouseX, mouseY, button);
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !clicked) {
            List<FlowLayout> list = new ArrayList<>();
            for (HudElement element : HudManager.getElements()) {
                FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
                layout.padding(Insets.of(5));
                PlainLabel label = new PlainLabel(element.elementLabel);
                label.tooltip(element.elementDesc);
                label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
                ToggleButton toggle = new ToggleButton(element.isAdded());
                toggle.onToggled().subscribe(element.added::set);
                layout.child(label);
                layout.child(toggle);
                list.add(layout);
            }
            HudSettings settings = new HudSettings(list);
            settings.setTitle(Text.literal("HUD Elements"));
            mc.setScreen(settings);
            return true;
        }
        return clicked;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (HudElement element : HudManager.getElements()) {
                if (element.toggling && element.isAdded()) {
                    element.toggling = false;
                    element.toggle();
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (HudElement element : HudManager.getElements()) {
            if (element.toggling && element.isAdded()) {
                element.toggling = false;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void close() {
        if (AutoSave.instance.isActive()) AutoSave.save();
        super.close();
    }
}