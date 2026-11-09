package nofrills.hud.clickgui;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.components.KeybindButton;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.settings.CustomHeight;
import nofrills.hud.clickgui.settings.Description;
import nofrills.hud.clickgui.settings.InnerSettings;
import nofrills.hud.clickgui.settings.Separator;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import static nofrills.Main.mc;

public class SettingsScreen extends BaseOwoScreen<FlowLayout> {
    public static final ButtonComponent.Renderer buttonRenderer = (context, button, delta) -> {
        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
    };
    public static final ButtonComponent.Renderer buttonRendererWhite = (context, button, delta) -> {
        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xffffffff);
    };
    public static final Function<String, List<String>> soundSuggestionProvider = s ->
            mc.getSoundManager().getAvailableSounds().stream()
                    .map(i -> (s.contains(":") ? i.toString() : i.getPath()).toLowerCase(Locale.ROOT))
                    .filter(i -> i.startsWith(Utils.toLower(s)))
                    .sorted()
                    .toList();
    public List<FlowLayout> settings;
    public Component title = Component.empty();
    public ScrollContainer<FlowLayout> scroll;

    public SettingsScreen(List<FlowLayout> settings) {
        this.settings = settings;
        for (FlowLayout setting : this.settings) {
            if (setting instanceof InnerSettings inner) {
                inner.previousScreen = this;
            }
        }
    }

    public SettingsScreen(FlowLayout... settings) {
        this(List.of(settings));
    }

    public static ButtonComponent buildResetButton(Consumer<ButtonComponent> onPress) {
        ButtonComponent button = UIComponents.button(Component.literal("Reset").withColor(0xffffff), onPress);
        button.positioning(Positioning.relative(100, 0));
        button.renderer(buttonRendererWhite);
        return button;
    }

    protected static int getSettingsHeight(List<UIComponent> children) {
        int height = 0;
        for (UIComponent child : children) {
            int childHeight = switch (child) {
                case Description description -> 10 + ((PlainLabel) description.children().getLast()).getTextHeight();
                case Separator ignored -> 20;
                case CustomHeight customHeight -> customHeight.heightOverride;
                default -> 30;
            };
            height += childHeight;
        }
        return (int) Math.clamp(height, 30, mc.getWindow().getGuiScaledHeight() * 0.8);
    }

    protected static boolean isBinding(List<FlowLayout> settings, int button) {
        for (FlowLayout setting : settings) {
            for (UIComponent child : setting.children()) {
                if (findKeybindButton(child, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean findKeybindButton(UIComponent child, int button) {
        if (child instanceof KeybindButton keybind) {
            if (keybind.isBinding) {
                keybind.bind(button);
                return true;
            }
        } else if (child instanceof FlowLayout layout) {
            for (UIComponent layoutChild : layout.children()) {
                if (findKeybindButton(layoutChild, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (isBinding(this.settings, input.key())) {
            return true;
        }
        if (input.key() == InputConstants.KEY_PAGEUP || input.key() == InputConstants.KEY_PAGEDOWN) {
            this.scroll.onMouseScroll(0, 0, input.key() == InputConstants.KEY_PAGEUP ? 4 : -4);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll.onMouseScroll(0, 0, verticalAmount * 2);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (isBinding(this.settings, click.button())) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        FlowLayout parent = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        parent.padding(Insets.of(5));
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout settings = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        settings.surface(Surface.flat(0xaa000000)).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        int width = 300;
        List<FlowLayout> optionsMutable = new ArrayList<>(this.settings);
        for (FlowLayout option : optionsMutable) {
            option.horizontalSizing(Sizing.fixed(width));
            settings.child(option);
        }
        this.scroll = UIContainers.verticalScroll(Sizing.content(), Sizing.fixed(getSettingsHeight(settings.children())), settings);
        this.scroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        PlainLabel label = new PlainLabel(this.title);
        label.color(textColor).horizontalTextAlignment(HorizontalAlignment.CENTER).verticalTextAlignment(VerticalAlignment.CENTER);
        ParentUIComponent header = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        parent.child(header);
        parent.child(this.scroll);
        root.child(parent);
    }

    @Override
    public void onClose() {
        mc.gui.setScreen(new ClickGui());
    }

    public SettingsScreen setTitle(Component title) {
        this.title = title;
        return this;
    }
}
