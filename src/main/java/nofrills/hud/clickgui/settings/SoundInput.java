package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingSound;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.FlatAutofillTextbox;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SoundInput extends InnerSettings {

    public SoundInput(String name, String currentSound, String defaultSound, double currentVolume, double defaultVolume, double currentPitch, double defaultPitch, String tooltip, Consumer<String> soundCallback, Consumer<Double> volumeCallback, Consumer<Double> pitchCallback) {
        List<FlowLayout> list = new ArrayList<>();
        FlowLayout soundLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        soundLayout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Sound").withColor(0xffffff));
        FlatTextbox text = new FlatAutofillTextbox(Sizing.fixed(200), SettingsScreen.soundSuggestionProvider);
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal(tooltip));
        text.onChanged().subscribe(soundCallback::accept);
        text.text(currentSound);
        soundLayout.child(label);
        soundLayout.child(text);
        soundLayout.child(SettingsScreen.buildResetButton(_ -> {
            soundCallback.accept(defaultSound);
            text.setValue(defaultSound);
        }));
        SliderDouble volumeSlider = new SliderDouble(
                "Volume",
                0.0,
                1.0,
                0.01,
                currentVolume,
                defaultVolume,
                "The volume of the sound.",
                volumeCallback
        );
        SliderDouble pitchSlider = new SliderDouble(
                "Pitch",
                0.0,
                2.0,
                0.01,
                currentPitch,
                defaultPitch,
                "The pitch of the sound.",
                pitchCallback
        );
        list.add(new BigButton("Play Sound", _ ->
                Utils.playSound(text.getValue(), (float) volumeSlider.slider.value(), (float) pitchSlider.slider.value()))
        );
        list.add(soundLayout);
        list.add(volumeSlider);
        list.add(pitchSlider);
        super(name, list, "Edit Sound", tooltip);
    }

    public SoundInput(String name, SettingSound setting, String tooltip) {
        this(
                name,
                setting.sound().value(),
                setting.sound().getDefault().getAsString(),
                setting.volume().value(),
                setting.volume().getDefault().getAsDouble(),
                setting.pitch().value(),
                setting.pitch().getDefault().getAsDouble(),
                tooltip,
                s -> setting.sound().set(s),
                d -> setting.volume().set(d),
                d -> setting.pitch().set(d)
        );
    }
}
