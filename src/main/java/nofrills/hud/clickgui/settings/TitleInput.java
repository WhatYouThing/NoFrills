package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingTitle;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TitleInput extends InnerSettings {

    public TitleInput(String name, String currentTitle, String defaultTitle, int currentFadeInTicks, int defaultFadeInTicks, int currentStayTicks, int defaultStayTicks, int currentFadeOutTicks, int defaultFadeOutTicks, boolean currentVanilla, boolean defaultVanilla, String tooltip, Consumer<String> titleCallback, Consumer<Integer> fadeInCallback, Consumer<Integer> stayCallback, Consumer<Integer> fadeOutCallback, Consumer<Boolean> vanillaCallback) {
        List<FlowLayout> list = new ArrayList<>();
        FlowLayout previewLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        previewLayout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel previewLabel = new PlainLabel(Component.literal("Text Preview").withColor(0xffffff));
        previewLabel.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        PlainLabel outputLabel = new PlainLabel(Utils.ofUserFormatted(currentTitle));
        outputLabel.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.fixed(20));
        previewLayout.child(previewLabel);
        previewLayout.child(outputLabel);
        list.add(previewLayout);
        list.add(new TextInput(
                "Title Text",
                currentTitle,
                defaultTitle,
                "The text displayed on the title. You can use the \"&\" character to insert formatting codes.",
                s -> {
                    outputLabel.text(Utils.ofUserFormatted(s));
                    titleCallback.accept(s);
                }
        ));
        list.add(new SliderInt(
                "Fade In Ticks",
                0,
                200,
                1,
                currentFadeInTicks,
                defaultFadeInTicks,
                "The amount of ticks the title takes to fade in.",
                fadeInCallback
        ));
        list.add(new SliderInt(
                "Stay Ticks",
                0,
                200,
                1,
                currentStayTicks,
                defaultStayTicks,
                "The amount of ticks the title remains on screen for.",
                stayCallback
        ));
        list.add(new SliderInt(
                "Fade Out Ticks",
                0,
                200,
                1,
                currentFadeOutTicks,
                defaultFadeOutTicks,
                "The amount of ticks the title takes to fade out.",
                fadeOutCallback
        ));
        list.add(new Toggle(
                "Vanilla Title",
                currentVanilla,
                defaultVanilla,
                "Whether vanilla tooltip rendering should be used.\nDisable this option to use custom rendering and prevent the title from being overwritten.",
                vanillaCallback
        ));
        super(name, list, "Edit Title", tooltip);
    }

    public TitleInput(String name, SettingTitle setting, String tooltip) {
        this(
                name,
                setting.title().value(),
                setting.title().getDefault().getAsString(),
                setting.fadeIn().value(),
                setting.fadeIn().getDefault().getAsInt(),
                setting.stay().value(),
                setting.stay().getDefault().getAsInt(),
                setting.fadeOut().value(),
                setting.fadeOut().getDefault().getAsInt(),
                setting.vanilla().value(),
                setting.vanilla().getDefault().getAsBoolean(),
                tooltip,
                s -> setting.title().set(s),
                i -> setting.fadeIn().set(i),
                i -> setting.stay().set(i),
                i -> setting.fadeOut().set(i),
                b -> setting.vanilla().set(b)
        );
    }
}
