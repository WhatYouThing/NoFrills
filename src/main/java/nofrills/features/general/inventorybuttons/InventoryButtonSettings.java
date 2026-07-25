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
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class InventoryButtonSettings extends Settings {

    public InventoryButtonSettings(List<FlowLayout> settings) {
        super(settings);
    }

    public static InventoryButtonSettings of(JsonObject buttonObject) {
        List<FlowLayout> list = new ArrayList<>();
        FlowLayout keepSquareLayout = buildKeepSquareSetting(buttonObject);
        list.add(keepSquareLayout);
        list.addAll(buildScaleSettings(buttonObject, (ToggleButton) keepSquareLayout.children().get(1)));
        list.add(buildCommandSetting(buttonObject));
        list.add(buildTooltipSetting(buttonObject));
        list.add(buildModelSetting(buttonObject));
        list.add(buildTexturesSetting(buttonObject));
        list.add(buildGlintSetting(buttonObject));
        list.add(buildManageSetting(buttonObject));
        InventoryButtonSettings buttonSettings = new InventoryButtonSettings(list);
        String command = buttonObject.get("command").getAsString();
        buttonSettings.setTitle(Component.literal("Inventory Button: " + (command.isEmpty() ? "Blank" : command)));
        return buttonSettings;
    }

    protected static FlowLayout buildKeepSquareSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);

        PlainLabel keepSquare = new PlainLabel(Component.literal("Keep Square"));
        ToggleButton keepSquareButton = new ToggleButton(buttonObject.get("keepSquare").getAsBoolean());
        keepSquare.tooltip(Component.literal("Keep aspect ratio locked to square."));
        keepSquare.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));

        keepSquareButton.onToggled().subscribe(change -> buttonObject.addProperty("keepSquare", change));
        layout.child(keepSquare);
        layout.child(keepSquareButton);

        return layout;
    }

    protected static List<FlowLayout> buildScaleSettings(JsonObject buttonObject, ToggleButton keepSquareButton) {
        FlowLayout layoutX = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        FlowLayout layoutY = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layoutX.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        layoutY.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);

        PlainLabel labelX = new PlainLabel(Component.literal("Scale X").withColor(0xffffff));
        PlainLabel labelY = new PlainLabel(Component.literal("Scale Y").withColor(0xffffff));
        FlatTextbox textX = new FlatTextbox(Sizing.fixed(50));
        FlatTextbox textY = new FlatTextbox(Sizing.fixed(50));
        FlatSlider sliderX = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        FlatSlider sliderY = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        sliderX.min(0.25).max(5.0).stepSize(0.01).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
        sliderY.min(0.25).max(5.0).stepSize(0.01).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));

        labelX.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        labelY.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        labelX.tooltip(Component.literal("The horizontal scale of this inventory button."));
        labelY.tooltip(Component.literal("The vertical scale of this inventory button."));

        textX.onChanged().subscribe(change -> Utils.parseDouble(change).ifPresent(value -> {
            buttonObject.addProperty("scaleX", value);
            sliderX.value(value);
            if (keepSquareButton.getToggle()) {
                buttonObject.addProperty("scaleY", value);
                sliderY.value(value);
            }
        }));
        textY.onChanged().subscribe(change -> Utils.parseDouble(change).ifPresent(value -> {
            buttonObject.addProperty("scaleY", value);
            sliderY.value(value);
            if (keepSquareButton.getToggle()) {
                buttonObject.addProperty("scaleX", value);
                sliderX.value(value);
            }
        }));

        textX.text(String.valueOf(buttonObject.get("scaleX").getAsDouble()));
        textY.text(String.valueOf(buttonObject.get("scaleY").getAsDouble()));

        sliderX.onChanged().subscribe(change -> {
            double value = roundDouble(change);
            buttonObject.addProperty("scaleX", value);
            textX.setValue(String.valueOf(value));
            if (keepSquareButton.getToggle()) {
                buttonObject.addProperty("scaleY", value);
                textY.setValue(String.valueOf(value));
            }
        });
        sliderY.onChanged().subscribe(change -> {
            double value = roundDouble(change);
            buttonObject.addProperty("scaleY", value);
            textY.setValue(String.valueOf(value));
            if (keepSquareButton.getToggle()) {
                buttonObject.addProperty("scaleX", value);
                textX.setValue(String.valueOf(value));
            }
        });

        layoutX.child(labelX);
        layoutX.child(textX);
        layoutX.child(sliderX);
        layoutX.child(buildResetButton(_ -> {
            buttonObject.addProperty("scaleX", 1.0);
            textX.setValue(String.valueOf(1.0));
            if (keepSquareButton.getToggle()) {
                buttonObject.addProperty("scaleY", 1.0);
                textY.setValue(String.valueOf(1.0));
            }
        }));
        layoutY.child(labelY);
        layoutY.child(textY);
        layoutY.child(sliderY);
        layoutY.child(buildResetButton(_ -> {
            buttonObject.addProperty("scaleY", 1.0);
            textY.setValue(String.valueOf(1.0));
            if (keepSquareButton.getToggle()) {
                buttonObject.addProperty("scaleX", 1.0);
                textX.setValue(String.valueOf(1.0));
            }
        }));

        return List.of(layoutX, layoutY);
    }

    protected static FlowLayout buildCommandSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Command").withColor(0xffffff));
        FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal("The message/command to send when clicking this inventory button."));
        text.onChanged().subscribe(change -> buttonObject.addProperty("command", change));
        text.text(buttonObject.get("command").getAsString());
        layout.child(label);
        layout.child(text);
        layout.child(buildResetButton(btn -> {
            buttonObject.addProperty("command", "");
            text.setValue("");
        }));
        return layout;
    }

    protected static FlowLayout buildTooltipSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Tooltip").withColor(0xffffff));
        FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal("The tooltip to display when hovering over this inventory button."));
        text.onChanged().subscribe(change -> buttonObject.addProperty("tooltip", change));
        text.text(buttonObject.get("tooltip").getAsString());
        layout.child(label);
        layout.child(text);
        layout.child(buildResetButton(btn -> {
            buttonObject.addProperty("tooltip", "");
            text.setValue("");
        }));
        return layout;
    }

    protected static FlowLayout buildModelSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Item Model").withColor(0xffffff));
        FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal("The identifier of the item to display on top of this inventory button.\nExamples: tnt, diamond_sword, purple_dye, ender_chest, bone."));
        text.onChanged().subscribe(change -> buttonObject.addProperty("model", change));
        text.text(buttonObject.get("model").getAsString());
        layout.child(label);
        layout.child(text);
        layout.child(buildResetButton(btn -> {
            buttonObject.addProperty("model", "");
            text.setValue("");
        }));
        return layout;
    }

    protected static FlowLayout buildTexturesSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Textures").withColor(0xffffff));
        FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal("The custom textures payload to display. Only works if the item model is a player head.\n\nUsage: You can copy the head textures of any item using the Inventory Buttons Copy Textures keybind.\nPaste the copied head textures into this input to apply them to the player head model."));
        text.onChanged().subscribe(change -> buttonObject.addProperty("textures", change));
        text.text(buttonObject.get("textures").getAsString());
        layout.child(label);
        layout.child(text);
        layout.child(buildResetButton(btn -> {
            buttonObject.addProperty("textures", "");
            text.setValue("");
        }));
        return layout;
    }

    protected static FlowLayout buildGlintSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5));
        layout.horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Glint").withColor(0xffffff));
        label.tooltip(Component.literal("Applies the glint effect to the item model displayed on this inventory button."));
        ToggleButton toggle = new ToggleButton(buttonObject.get("glint").getAsBoolean());
        toggle.onToggled().subscribe(value -> buttonObject.addProperty("glint", value));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        layout.child(label);
        layout.child(toggle);
        layout.child(buildResetButton(btn -> {
            buttonObject.addProperty("glint", false);
            toggle.setToggle(false);
        }));
        return layout;
    }

    protected static FlowLayout buildManageSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Manage").withColor(0xffffff));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        ButtonComponent button = UIComponents.button(Component.literal("Delete").withColor(0xffffff), btn -> {
            if (InventoryButtons.data.value().has("buttons")) {
                InventoryButtons.data.value().get("buttons").getAsJsonArray().remove(buttonObject);
                mc.setScreen(new InventoryScreen(mc.player));
            }
        });
        button.renderer(buttonRendererWhite);
        button.tooltip(Component.literal("Deletes this inventory button."));
        layout.child(label);
        layout.child(button);
        return layout;
    }

    @Override
    public void onClose() {
        mc.setScreen(new InventoryScreen(mc.player));
    }
}
