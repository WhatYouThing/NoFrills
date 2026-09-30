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
import nofrills.hud.clickgui.Settings;
import nofrills.hud.elements.Armor;
import nofrills.misc.RenderColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

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
        for (HudElement element : HudManager.getElements()) {
            if (element.isAdded()) {
                element.updatePosition();
            }
        }
        context.fill((int) (context.guiWidth() * 0.5), 0, (int) (context.guiWidth() * 0.5 + 1), context.guiHeight(), RenderColor.WHITE.getArgb());
        context.fill(0, (int) (context.guiHeight() * 0.5), context.guiWidth(), (int) (context.guiHeight() * 0.5 + 1), RenderColor.WHITE.getArgb());
        super.extractRenderState(context, mouseX, mouseY, delta);
        int center = context.guiWidth() / 2;
        for (int i = 0; i < helpLines.size(); i++) {
            context.centeredText(mc.font, helpLines.get(i), center, 10 * (i + 1), RenderColor.WHITE.argb);
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        if (this.uiAdapter == null) {
            return false;
        }
        boolean clicked = this.uiAdapter.mouseClicked(click, doubled);
        if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT && !clicked) {
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
            mc.gui.setScreen(settings);
            return true;
        }
        return clicked;
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