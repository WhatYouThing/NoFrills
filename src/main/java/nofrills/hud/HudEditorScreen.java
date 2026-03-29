package nofrills.hud;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nofrills.features.misc.AutoSave;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.RenderColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {
    public HudEditorScreen() {
        super(Component.nullToEmpty(""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.allowOverflow(false);
        for (HudElement element : HudManager.getElements()) {
            if (element.isAdded()) {
                root.child(element);
            }
        }
        HudManager.armor.updateArmor();
    }

    @Override
    public void drawComponentTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        for (HudElement element : HudManager.getElements()) {
            if (element.isAdded()) element.updatePosition();
        }
        super.drawComponentTooltip(context, mouseX, mouseY, delta);
        int center = context.guiWidth() / 2;
        context.centeredText(mc.font, "NoFrills HUD Editor", center, 10, RenderColor.white.argb);
        context.centeredText(mc.font, "Left click element to hide", center, 20, RenderColor.white.argb);
        context.centeredText(mc.font, "Right click element to view its settings", center, 30, RenderColor.white.argb);
        context.centeredText(mc.font, "Right click screen to add/remove elements", center, 40, RenderColor.white.argb);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.uiAdapter == null) {
            return false;
        }
        boolean clicked = this.uiAdapter.mouseClicked(click, doubled);
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !clicked) {
            List<FlowLayout> list = new ArrayList<>();
            List<HudElement> elementList = new ArrayList<>(HudManager.getElements());
            elementList.sort(Comparator.comparing(element -> element.elementLabel.getString()));
            for (HudElement element : elementList) {
                FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
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
            settings.setTitle(Component.literal("HUD Elements"));
            mc.setScreen(settings);
            return true;
        }
        return clicked;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (HudElement element : HudManager.getElements()) {
                if (element.toggling && element.isAdded()) {
                    element.toggling = false;
                    element.toggle();
                    return true;
                }
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        for (HudElement element : HudManager.getElements()) {
            if (element.toggling && element.isAdded()) {
                element.toggling = false;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public void onClose() {
        if (AutoSave.instance.isActive()) AutoSave.save();
        super.onClose();
    }
}