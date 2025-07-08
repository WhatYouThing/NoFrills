package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static nofrills.Main.Config;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        Category generalCategory = new Category("General", List.of(
                new Feature("Auto Sprint", Config.keys.autoSprint, "Essentially just Toggle Sprint, but always active.", new Settings(List.of(
                        new Settings.Toggle("Submerged Check", Config.keys.autoSprintWater, "Prevents Auto Sprint from working while you are underwater.")
                ))),
                new Feature("Update Checker", Config.keys.updateChecker, "Automatically checks if a new NoFrills release is available when joining Skyblock."),
                new Feature("No Selfie Camera", Config.keys.noSelfieCam, "Removes the front facing camera perspective."),
                new Feature("Slot Binding", Config.keys.slotBinding, "Bind your hotbar slots to your inventory slots, similarly to NEU's slot binding.", new Settings(List.of(
                        new Settings.Toggle("Show Lines", Config.keys.slotBindingLines, "Draw lines between the slot you're hovering over, and any slots bound to it."),
                        new Settings.Toggle("Show Borders", Config.keys.slotBindingBorders, "Draw borders around any slot that is bound to the slot you're hovering over."),
                        new Settings.ColorPicker("Bound Color", false, Config.keys.slotBindingColor, "The color used when drawing the lines and borders.")
                ))),
                new Feature("Ignore Background", Config.keys.ignoreBackground, "Disables slots that have items with empty names."),
                new Feature("Middle Click Override", Config.keys.middleClickOverride, "Replaces left clicks with middle clicks in applicable GUI's, making navigation smoother."),
                new Feature("Price Tooltips", Config.keys.fetchPricing, "Adds pricing information to item tooltips. Requires connectivity to the NoFrills API.", new Settings(List.of(
                        new Settings.Toggle("Auction Price", Config.keys.pricingAuction, "Adds the Lowest BIN price to applicable items."),
                        new Settings.Toggle("Bazaar Prices", Config.keys.pricingBazaar, "Adds the Bazaar insta-buy and insta-sell prices to applicable items."),
                        new Settings.Toggle("NPC Price", Config.keys.pricingNPC, "Adds the NPC sell price to applicable items."),
                        new Settings.Toggle("Motes Price", Config.keys.pricingMote, "Adds the Motes sell price to applicable items."),
                        new Settings.SliderInt("Grubber Stacks", 0, 5, 1, Config.keys.pricingMoteStacks, "The amount of McGrubber's Burgers you've eaten, used to calculate the bonus Motes value.")
                ))),
                new Feature("Wardrobe Hotkeys", Config.keys.wardrobeHotkeys, "Adds number hotkeys (1-9) to the Skyblock Wardrobe.", new Settings(List.of(
                        new Settings.Toggle("Sound Effect", Config.keys.wardrobeHotkeysSound, "Plays a sound effect after switching your armor set with a hotkey.")
                ))),
                new Feature("Chat Waypoints", Config.keys.wardrobeHotkeys, "Automatically creates waypoints for coordinates sent in Party/Global chat.", new Settings(List.of(
                        new Settings.Toggle("Party Waypoints", Config.keys.partyWaypoints, "Enables creating waypoints for coordinates sent by party members."),
                        new Settings.SliderInt("Duration", 1, 600, 1, Config.keys.partyWaypointTime, "The duration (in seconds) that party waypoints should be rendered for."),
                        new Settings.Toggle("Auto Clear", Config.keys.partyWaypoints, "Automatically removes a party waypoint once you get near."),
                        new Settings.Separator("Big Separator 123 123"),
                        new Settings.ColorPicker("Color", true, Config.keys.partyWaypointColor, "The color used for the party waypoints.")
                )))
        ));
        parent.child(generalCategory);
        root.child(Containers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent));
    }
}
