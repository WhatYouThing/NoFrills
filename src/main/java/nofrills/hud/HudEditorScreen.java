package nofrills.hud;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nofrills.features.misc.AutoSave;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.elements.Armor;
import nofrills.misc.RenderColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static nofrills.Main.mc;

public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {
    private static final List<String> helpLines = List.of(
            "NoFrills HUD Editor",
            "Left click element to toggle visibility",
            "Hold left click to drag element (Shift to snap)",
            "Right click element to view its settings",
            "Right click screen to add/remove elements"
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
        context.fill((int) (context.guiWidth() * 0.5), 0, (int) (context.guiWidth() * 0.5 + 1), context.guiHeight(), RenderColor.WHITE.getArgb());
        context.fill(0, (int) (context.guiHeight() * 0.5), context.guiWidth(), (int) (context.guiHeight() * 0.5 + 1), RenderColor.WHITE.getArgb());
        super.extractRenderState(context, mouseX, mouseY, delta);
        int center = context.guiWidth() / 2;
        for (int i = 0; i < helpLines.size(); i++) {
            context.centeredText(mc.font, helpLines.get(i), center, 10 * (i + 1), RenderColor.WHITE.argb);
        }
        for (HudElement element : activeElements) {
            if (element.isInBoundingBox(mouseX, mouseY)) {
                int x = element.x(), y = element.y(), w = element.width(), h = element.height();
                int halfW = (int) (w * 0.5), halfH = (int) (h * 0.5);
                context.outline(x, y, w, h, RenderColor.WHITE.getArgb());
                context.fill(x + halfW, y, x + halfW + 1, y + 1, 0xff000000);
                context.fill(x + halfW, y + h - 1, x + halfW + 1, y + h, 0xff000000);
                context.fill(x, y + halfH, x + 1, y + halfH + 1, 0xff000000);
                context.fill(x + w - 1, y + halfH, x + w, y + halfH + 1, 0xff000000);
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        if (this.uiAdapter == null) {
            return false;
        }
        boolean clicked = this.uiAdapter.mouseClicked(click, doubled);
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !clicked) {
            List<FlowLayout> list = new ArrayList<>();
            HashMap<HudElement.Category, List<HudElement>> categories = new HashMap<>();
            for (HudElement element : HudManager.getElements()) {
                if (!categories.containsKey(element.getCategory())) {
                    categories.put(element.getCategory(), new ArrayList<>());
                }
                categories.get(element.getCategory()).add(element);
            }
            for (HudElement.Category category : HudElement.Category.values()) {
                List<HudElement> elements = categories.getOrDefault(category, new ArrayList<>());
                if (elements.isEmpty()) {
                    continue;
                }
                list.add(new Settings.Separator(category.name()));
                elements.sort(Comparator.comparing(element -> element.elementLabel.getString()));
                for (HudElement element : elements) {
                    list.add(new Settings.Toggle(
                            element.elementLabel.getString(),
                            element.isAdded(),
                            false,
                            element.elementDesc.getString(),
                            value -> {
                                if (value && !element.instance.isActive()) {
                                    element.instance.setActive(true);
                                }
                                element.added.set(value);
                            })
                    );
                }
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