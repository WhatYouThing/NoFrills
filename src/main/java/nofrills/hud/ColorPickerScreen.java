package nofrills.hud;

import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import nofrills.config.SettingColor;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class ColorPickerScreen extends Settings {
    private final Screen previous;

    public ColorPickerScreen(List<FlowLayout> settings, Screen previous) {
        super(settings);
        this.previous = previous;
    }

    public static ColorPickerScreen build(SettingColor setting, Screen previous) {
        List<FlowLayout> list = new ArrayList<>();

        FlowLayout colorSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        colorSection.padding(Insets.of(5));

        BoxComponent colorDisplay = UIComponents.box(Sizing.fixed(290), Sizing.fixed(20));
        colorDisplay.color(Color.ofArgb(setting.value().argb)).fill(true);
        colorSection.child(colorDisplay);

        FlowLayout argbSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        argbSection.horizontalAlignment(HorizontalAlignment.LEFT).padding(Insets.of(5));
        FlatTextbox argbInput = new FlatTextbox(Sizing.fixed(100));
        argbInput.text("0x" + Integer.toHexString(setting.value().argb));
        argbSection.child(addLabel("ARGB"));
        argbSection.child(argbInput);

        FlowLayout redSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        redSection.horizontalAlignment(HorizontalAlignment.LEFT).padding(Insets.of(5));
        FlatTextbox redInput = new FlatTextbox(Sizing.fixed(40));
        redInput.text(String.valueOf((int) (setting.value().r * 255)));
        FlatSlider redSlider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        redSlider.min(0).max(255).stepSize(1).horizontalSizing(Sizing.fixed(150)).verticalSizing(Sizing.fixed(20));
        redSlider.value((int) (setting.value().r * 255));
        redSection.child(addLabel("Red"));
        redSection.child(redInput);
        redSection.child(redSlider);

        FlowLayout greenSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        greenSection.horizontalAlignment(HorizontalAlignment.LEFT).padding(Insets.of(5));
        FlatTextbox greenInput = new FlatTextbox(Sizing.fixed(40));
        greenInput.text(String.valueOf((int) (setting.value().g * 255)));
        FlatSlider greenSlider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        greenSlider.min(0).max(255).stepSize(1).horizontalSizing(Sizing.fixed(150)).verticalSizing(Sizing.fixed(20));
        greenSlider.value((int) (setting.value().g * 255));
        greenSection.child(addLabel("Green"));
        greenSection.child(greenInput);
        greenSection.child(greenSlider);

        FlowLayout blueSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        blueSection.horizontalAlignment(HorizontalAlignment.LEFT).padding(Insets.of(5));
        FlatTextbox blueInput = new FlatTextbox(Sizing.fixed(40));
        blueInput.text(String.valueOf((int) (setting.value().b * 255)));
        FlatSlider blueSlider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        blueSlider.min(0).max(255).stepSize(1).horizontalSizing(Sizing.fixed(150)).verticalSizing(Sizing.fixed(20));
        blueSlider.value((int) (setting.value().b * 255));
        blueSection.child(addLabel("Blue"));
        blueSection.child(blueInput);
        blueSection.child(blueSlider);

        FlowLayout alphaSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        alphaSection.horizontalAlignment(HorizontalAlignment.LEFT).padding(Insets.of(5));
        FlatTextbox alphaInput = new FlatTextbox(Sizing.fixed(40));
        alphaInput.text(String.valueOf((int) (setting.value().a * 255)));
        FlatSlider alphaSlider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        alphaSlider.min(0).max(255).stepSize(1).horizontalSizing(Sizing.fixed(150)).verticalSizing(Sizing.fixed(20));
        alphaSlider.value((int) (setting.value().a * 255));
        alphaSection.child(addLabel("Alpha"));
        alphaSection.child(alphaInput);
        alphaSection.child(alphaSlider);

        Runnable syncValues = () -> {
            RenderColor color = setting.value();
            int red = (int) (color.r * 255);
            int green = (int) (color.g * 255);
            int blue = (int) (color.b * 255);
            int alpha = (int) (color.a * 255);
            colorDisplay.color(Color.ofArgb(color.argb)).fill(true);
            argbInput.setText("0x" + Integer.toHexString(color.argb));
            redInput.setText(String.valueOf(red));
            redSlider.value(red);
            greenInput.setText(String.valueOf(green));
            greenSlider.value(green);
            blueInput.setText(String.valueOf(blue));
            blueSlider.value(blue);
            alphaInput.setText(String.valueOf(alpha));
            alphaSlider.value(alpha);
        };

        FlowLayout buttonSection = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(30));
        buttonSection.horizontalAlignment(HorizontalAlignment.LEFT).padding(Insets.of(5));
        ButtonComponent backButton = UIComponents.button(Text.literal("Back"), (btn) -> mc.setScreen(previous));
        backButton.margins(Insets.right(5));
        backButton.renderer(Settings.buttonRenderer);
        ButtonComponent copyButton = UIComponents.button(Text.literal("Copy Color"), (btn) ->
                mc.keyboard.setClipboard("0x" + Integer.toHexString(setting.value().argb))
        );
        copyButton.margins(Insets.right(5));
        copyButton.renderer(Settings.buttonRenderer);
        ButtonComponent pasteButton = UIComponents.button(Text.literal("Paste Color"), (btn) -> {
            Utils.parseHex(mc.keyboard.getClipboard()).ifPresent(integer -> setting.set(RenderColor.fromArgb(integer)));
            syncValues.run();
        });
        pasteButton.renderer(Settings.buttonRenderer);
        buttonSection.child(backButton);
        buttonSection.child(copyButton);
        buttonSection.child(pasteButton);

        argbInput.onChanged().subscribe((value) -> {
            Utils.parseHex(value).ifPresent(integer -> setting.set(RenderColor.fromArgb(integer)));
            syncValues.run();
        });

        redInput.onChanged().subscribe((value) -> {
            Utils.parseInt(value).ifPresent(integer -> setting.set(setting.value().withRed(integer / 255.0f)));
            syncValues.run();
        });
        redSlider.onChanged().subscribe((value) -> {
            setting.set(setting.value().withRed((int) value / 255.0f));
            syncValues.run();
        });

        greenInput.onChanged().subscribe((value) -> {
            Utils.parseInt(value).ifPresent(integer -> setting.set(setting.value().withGreen(integer / 255.0f)));
            syncValues.run();
        });
        greenSlider.onChanged().subscribe((value) -> {
            setting.set(setting.value().withGreen((int) value / 255.0f));
            syncValues.run();
        });

        blueInput.onChanged().subscribe((value) -> {
            Utils.parseInt(value).ifPresent(integer -> setting.set(setting.value().withBlue(integer / 255.0f)));
            syncValues.run();
        });
        blueSlider.onChanged().subscribe((value) -> {
            setting.set(setting.value().withBlue((int) value / 255.0f));
            syncValues.run();
        });

        alphaInput.onChanged().subscribe((value) -> {
            Utils.parseInt(value).ifPresent(integer -> setting.set(setting.value().withAlpha(integer / 255.0f)));
            syncValues.run();
        });
        alphaSlider.onChanged().subscribe((value) -> {
            setting.set(setting.value().withAlpha((int) value / 255.0f));
            syncValues.run();
        });

        list.add(colorSection);
        list.add(argbSection);
        list.add(redSection);
        list.add(greenSection);
        list.add(blueSection);
        list.add(alphaSection);
        list.add(buttonSection);

        return new ColorPickerScreen(list, previous);
    }

    public static FlowLayout addLabel(String text) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.fixed(40), Sizing.content());
        PlainLabel label = new PlainLabel(Text.literal(text));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).sizing(Sizing.content(), Sizing.fixed(20));
        layout.child(label);
        return layout;
    }

    @Override
    public void close() {
        mc.setScreen(this.previous);
    }
}
