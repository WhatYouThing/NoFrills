package nofrills.hud;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nofrills.features.misc.AutoSave;
import nofrills.hud.elements.Armor;
import nofrills.misc.RenderColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static nofrills.Main.mc;

public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {
    private static final List<String> helpLines = List.of(
            "NoFrills HUD Editor",
            "Left click element to toggle visibility",
            "Hold left click to drag element (Shift to snap)",
            "Right click element to view its settings"
    );

    public HudEditorScreen() {
        super(Component.literal(""));
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
            if (element instanceof Armor armorElement) {
                armorElement.updateArmor();
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        List<HudElement> activeElements = HudManager.getElements().stream().filter(HudElement::isActive).toList();
        for (HudElement element : activeElements) {
            element.updatePosition();
        }
        context.fill((int) (context.guiWidth() * 0.5), 0, (int) (context.guiWidth() * 0.5 + 1), context.guiHeight(), RenderColor.WHITE.argb());
        context.fill(0, (int) (context.guiHeight() * 0.5), context.guiWidth(), (int) (context.guiHeight() * 0.5 + 1), RenderColor.WHITE.argb());
        super.extractRenderState(context, mouseX, mouseY, delta);
        int center = context.guiWidth() / 2;
        for (int i = 0; i < helpLines.size(); i++) {
            context.centeredText(mc.font, helpLines.get(i), center, 10 * (i + 1), RenderColor.WHITE.argb());
        }
        for (HudElement element : activeElements) {
            if (element.isInBoundingBox(mouseX, mouseY)) {
                int x = element.x(), y = element.y(), w = element.width(), h = element.height();
                int halfW = (int) (w * 0.5), halfH = (int) (h * 0.5);
                context.outline(x, y, w, h, RenderColor.WHITE.argb());
                context.fill(x + halfW, y, x + halfW + 1, y + 1, 0xff000000);
                context.fill(x + halfW, y + h - 1, x + halfW + 1, y + h, 0xff000000);
                context.fill(x, y + halfH, x + 1, y + halfH + 1, 0xff000000);
                context.fill(x + w - 1, y + halfH, x + w, y + halfH + 1, 0xff000000);
                break;
            }
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
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