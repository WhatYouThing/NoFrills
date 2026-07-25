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
        list.add(buildScaleSetting(buttonObject));
        list.add(buildCommandSetting(buttonObject));
        list.add(buildTooltipSetting(buttonObject));
        list.add(buildModelSetting(buttonObject));
        list.add(buildTexturesSetting(buttonObject));
        list.add(buildGlintSetting(buttonObject));
        list.add(buildDragAndDropSetting(buttonObject));
        list.add(buildManageSetting(buttonObject));
        InventoryButtonSettings buttonSettings = new InventoryButtonSettings(list);
        String command = buttonObject.get("command").getAsString();
        buttonSettings.setTitle(Component.literal("Inventory Button: " + (command.isEmpty() ? "Blank" : command)));
        return buttonSettings;
    }

    protected static FlowLayout buildScaleSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Scale").withColor(0xffffff));
        FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
        FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        slider.min(0.25).max(5.0).stepSize(0.01).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal("The scale of this inventory button."));
        text.onChanged().subscribe(change -> Utils.parseDouble(change).ifPresent(value -> {
            buttonObject.addProperty("scale", value);
            slider.value(value);
        }));
        text.text(String.valueOf(buttonObject.get("scale").getAsDouble()));
        slider.onChanged().subscribe(change -> {
            double value = roundDouble(change);
            buttonObject.addProperty("scale", value);
            text.setValue(String.valueOf(value));
        });
        layout.child(label);
        layout.child(text);
        layout.child(slider);
        layout.child(buildResetButton(_ -> {
            buttonObject.addProperty("scale", 1.0);
            text.setValue(String.valueOf(1.0));
        }));
        return layout;
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

    protected static FlowLayout buildDragAndDropSetting(JsonObject buttonObject) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(5));
        layout.horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal("Drag And Drop").withColor(0xffffff));
        label.tooltip(Component.literal("Allows you to edit the position of this inventory button by dragging and dropping."));
        ToggleButton toggle = new ToggleButton(buttonObject.get("dragAndDrop").getAsBoolean());
        toggle.onToggled().subscribe(value -> buttonObject.addProperty("dragAndDrop", value));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        layout.child(label);
        layout.child(toggle);
        layout.child(buildResetButton(btn -> {
            buttonObject.addProperty("dragAndDrop", false);
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
