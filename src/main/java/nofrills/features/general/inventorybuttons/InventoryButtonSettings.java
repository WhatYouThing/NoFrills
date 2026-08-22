package nofrills.features.general.inventorybuttons;

import com.google.gson.JsonObject;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.MutableReference;
import nofrills.misc.RenderColor;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class InventoryButtonSettings extends Settings {
    private final Screen previous;

    public InventoryButtonSettings(List<FlowLayout> settings, Screen previous) {
        this.previous = previous;
        super(settings);
    }

    public static InventoryButtonSettings of(InventoryButtonWidget widget, Screen previous) {
        JsonObject buttonObject = widget.buttonObject;
        List<FlowLayout> list = new ArrayList<>();
        list.add(buildUniformSetting(buttonObject));
        list.addAll(buildScaleSettings(buttonObject));
        list.add(buildCommandSetting(buttonObject));
        list.add(buildTooltipSetting(buttonObject));
        list.add(buildModelSetting(buttonObject));
        list.add(buildItemIdSetting(buttonObject));
        list.add(buildCustomModelSetting(buttonObject));
        list.add(buildTexturesSetting(buttonObject));
        list.add(buildGlintSetting(buttonObject));
        list.add(buildInventoryOnlySetting(buttonObject));
        list.add(buildSnapPositionSetting(buttonObject));
        list.add(buildStyleSetting(buttonObject, widget));
        list.add(buildBackgroundColorSetting(buttonObject, widget));
        list.add(buildBorderColorSetting(buttonObject, widget));
        list.add(buildBorderHoverColorSetting(buttonObject, widget));
        list.add(buildManageSetting(buttonObject, previous));
        InventoryButtonSettings buttonSettings = new InventoryButtonSettings(list, previous);
        String command = buttonObject.get("command").getAsString();
        buttonSettings.setTitle(Component.literal("Inventory Button: " + (command.isEmpty() ? "Blank" : command)));
        return buttonSettings;
    }

    protected static FlowLayout buildUniformSetting(JsonObject buttonObject) {
        return new Toggle(
                "Uniform Scale",
                buttonObject.get("uniform").getAsBoolean(),
                false,
                "Keeps the button scale locked to 1:1.",
                b -> buttonObject.addProperty("uniform", b)
        );
    }

    protected static List<FlowLayout> buildScaleSettings(JsonObject buttonObject) {
        MutableReference<FlowLayout> layoutX = new MutableReference<>(null);
        MutableReference<FlowLayout> layoutY = new MutableReference<>(null);
        layoutX.set(new SliderDouble(
                "Scale X",
                0.5,
                5.0,
                0.01,
                buttonObject.get("scaleX").getAsDouble(),
                1.0,
                "The horizontal scale of this inventory button.",
                d -> {
                    buttonObject.addProperty("scaleX", d);
                    if (buttonObject.get("uniform").getAsBoolean() && layoutY.get() != null) {
                        layoutY.get().children().stream()
                                .filter(child -> child instanceof FlatSlider)
                                .findFirst()
                                .ifPresent(slider -> ((FlatSlider) slider).value(d));
                    }
                })
        );
        layoutY.set(new SliderDouble(
                "Scale Y",
                0.5,
                5.0,
                0.01,
                buttonObject.get("scaleY").getAsDouble(),
                1.0,
                "The vertical scale of this inventory button.",
                d -> {
                    buttonObject.addProperty("scaleY", d);
                    if (buttonObject.get("uniform").getAsBoolean() && layoutX.get() != null) {
                        layoutX.get().children().stream()
                                .filter(child -> child instanceof FlatSlider)
                                .findFirst()
                                .ifPresent(slider -> ((FlatSlider) slider).value(d));
                    }
                })
        );
        return List.of(layoutX.get(), layoutY.get());
    }

    protected static FlowLayout buildCommandSetting(JsonObject buttonObject) {
        return new TextInput(
                "Command",
                buttonObject.get("command").getAsString(),
                "",
                "The message/command to send when clicking this inventory button.",
                s -> buttonObject.addProperty("command", s)
        );
    }

    protected static FlowLayout buildTooltipSetting(JsonObject buttonObject) {
        return new TextInput(
                "Tooltip",
                buttonObject.get("tooltip").getAsString(),
                "",
                "The tooltip to display when hovering over this inventory button.",
                s -> buttonObject.addProperty("tooltip", s)
        );
    }

    protected static FlowLayout buildModelSetting(JsonObject buttonObject) {
        return new TextInput(
                "Item Model",
                buttonObject.get("model").getAsString(),
                "",
                "The identifier of the item to display on top of this inventory button.\nExamples: tnt, diamond_sword, purple_dye, ender_chest, bone.",
                s -> buttonObject.addProperty("model", s)
        );
    }

    protected static FlowLayout buildItemIdSetting(JsonObject buttonObject) {
        return new TextInput(
                "Item ID",
                buttonObject.get("itemId").getAsString(),
                "",
                "The Skyblock ID of the item displayed on top of this inventory button.\nThis option has no effect on the item itself and it exists purely for resource pack compatibility.",
                s -> buttonObject.addProperty("itemId", s)
        );
    }

    protected static FlowLayout buildCustomModelSetting(JsonObject buttonObject) {
        return new TextInput(
                "Custom Model",
                buttonObject.get("customModel").getAsString(),
                "",
                "The custom texture identifier to display. This option can be used to import textures from the official Skyblock resource pack.",
                s -> buttonObject.addProperty("customModel", s)
        );
    }

    protected static FlowLayout buildTexturesSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Head Textures").withColor(0xffffff));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal("Allows you to apply custom head textures to this button. Only works if the item model is player_head."));
        ButtonComponent copyButton = UIComponents.button(Component.literal("Copy").withColor(0xffffff), _ ->
                mc.keyboardHandler.setClipboard(buttonObject.get("textures").getAsString())
        );
        copyButton.renderer(buttonRenderer);
        copyButton.tooltip(Component.literal("Copies the current head textures payload.")).margins(Insets.of(0, 0, 0, 5));
        ButtonComponent pasteButton = UIComponents.button(Component.literal("Paste").withColor(0xffffff), _ ->
                buttonObject.addProperty("textures", mc.keyboardHandler.getClipboard())
        );
        pasteButton.renderer(buttonRenderer);
        pasteButton.tooltip(Component.literal("Pastes the head textures payload.")).margins(Insets.of(0, 0, 0, 5));
        layout.child(label);
        layout.child(copyButton);
        layout.child(pasteButton);
        layout.child(buildResetButton(_ -> buttonObject.addProperty("textures", "")));
        return layout;
    }

    protected static FlowLayout buildGlintSetting(JsonObject buttonObject) {
        return new Toggle(
                "Glint",
                buttonObject.get("glint").getAsBoolean(),
                false,
                "Applies the glint effect to the item model displayed on this inventory button.",
                b -> buttonObject.addProperty("glint", b)
        );
    }

    protected static FlowLayout buildInventoryOnlySetting(JsonObject buttonObject) {
        return new Toggle(
                "Inventory Only",
                buttonObject.get("inventoryOnly").getAsBoolean(),
                false,
                "Hides this inventory button outside of the player inventory screen.",
                b -> buttonObject.addProperty("inventoryOnly", b)
        );
    }

    protected static FlowLayout buildSnapPositionSetting(JsonObject buttonObject) {
        return new Toggle(
                "Snap Position",
                buttonObject.get("snapPosition").getAsBoolean(),
                true,
                "Prevents this button from appearing on top of GUIs by snapping it to the top/bottom of the GUI when needed.",
                b -> buttonObject.addProperty("snapPosition", b)
        );
    }

    protected static FlowLayout buildStyleSetting(JsonObject buttonObject, InventoryButtonWidget widget) {
        return new EnumToggle<>(
                "Style",
                widget.buttonStyle,
                InventoryButtonStyle.Vanilla,
                InventoryButtonStyle.class,
                "The rendering style of this inventory button.",
                s -> buttonObject.addProperty("style", s.name())
        );
    }

    protected static FlowLayout buildBackgroundColorSetting(JsonObject buttonObject, InventoryButtonWidget widget) {
        return new ColorPicker(
                "Background Color",
                widget.buttonColorBackground,
                RenderColor.GRAY.withAlpha(0.33f),
                "The background color of this button. Only applies with the \"Color\" style.",
                c -> buttonObject.addProperty("colorBackground", c.getArgb())
        );
    }

    protected static FlowLayout buildBorderColorSetting(JsonObject buttonObject, InventoryButtonWidget widget) {
        return new ColorPicker(
                "Border Color",
                widget.buttonColorBorder,
                RenderColor.NF_BLUE,
                "The border color of this button. Only applies with the \"Color\" style.",
                c -> buttonObject.addProperty("colorBorder", c.getArgb())
        );
    }

    protected static FlowLayout buildBorderHoverColorSetting(JsonObject buttonObject, InventoryButtonWidget widget) {
        return new ColorPicker(
                "Border Hover Color",
                widget.buttonColorBorderHover,
                RenderColor.WHITE,
                "The border color of this button while hovered over. Only applies with the \"Color\" style.",
                c -> buttonObject.addProperty("colorBorderHover", c.getArgb())
        );
    }

    protected static FlowLayout buildManageSetting(JsonObject buttonObject, Screen previous) {
        return new BigButton("Delete Button", _ -> {
            InventoryButtons.data.value().get("buttons").getAsJsonArray().remove(buttonObject);
            mc.setScreen(previous);
        });
    }

    @Override
    public void onClose() {
        mc.setScreen(this.previous);
    }
}
