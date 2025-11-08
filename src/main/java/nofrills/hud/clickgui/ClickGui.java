package nofrills.hud.clickgui;

import com.google.common.collect.Lists;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.features.dungeons.*;
import nofrills.features.farming.GlowingMushroom;
import nofrills.features.farming.PlotBorders;
import nofrills.features.farming.SpaceFarmer;
import nofrills.features.fishing.CapTracker;
import nofrills.features.fishing.MuteDrake;
import nofrills.features.fishing.RareAnnounce;
import nofrills.features.fishing.RareGlow;
import nofrills.features.general.*;
import nofrills.features.hunting.*;
import nofrills.features.kuudra.*;
import nofrills.features.mining.*;
import nofrills.features.misc.*;
import nofrills.features.slayer.*;
import nofrills.features.solvers.*;
import nofrills.features.tweaks.*;
import nofrills.hud.HudEditorScreen;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public List<Category> categories;
    public ScrollContainer<FlowLayout> mainScroll;
    public int mouseX = 0;
    public int mouseY = 0;

    private boolean matchSearch(String text, String search) {
        return Utils.toLower(text).replaceAll(" ", "").contains(Utils.toLower(search).replaceAll(" ", ""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_LEFT && keyCode != GLFW.GLFW_KEY_RIGHT && keyCode != GLFW.GLFW_KEY_PAGE_DOWN && keyCode != GLFW.GLFW_KEY_PAGE_UP) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            for (Category category : this.categories) {
                for (Module module : category.features) {
                    if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                        return category.scroll.onMouseScroll(0, 0, keyCode == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
                    }
                }
            }
            return this.mainScroll.onMouseScroll(0, 0, keyCode == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Category category : this.categories) {
            for (Module module : category.features) {
                if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                    return category.scroll.onMouseScroll(0, 0, verticalAmount * 2);
                }
            }
        }
        return this.mainScroll.onMouseScroll(0, 0, verticalAmount * 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int height = context.getScaledWindowHeight();
        context.drawTextWithShadow(this.textRenderer, "Left click a feature to toggle", 1, height - 20, RenderColor.white.argb);
        context.drawTextWithShadow(this.textRenderer, "Right click a feature open its settings", 1, height - 10, RenderColor.white.argb);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        this.categories = Lists.newArrayList(
                new Category("General", List.of(
                        new Module("Auto Sprint", AutoSprint.instance, "Essentially Toggle Sprint, but always active.", new Settings(List.of(
                                new Settings.Toggle("Water Check", AutoSprint.waterCheck, "Prevents Auto Sprint from working while you are in water.")
                        ))),
                        new Module("Slot Binding", SlotBinding.instance, "Bind your hotbar slots to your inventory slots, similarly to NEU's slot binding.", new Settings(List.of(
                                new Settings.Separator("Usage"),
                                new Settings.Description("Using Binds", "Shift + Left click on either of the bound slots to swap items between them."),
                                new Settings.Description("Adding Binds", "Hover over a slot, press the keybind, move your cursor to another slot, and release the keybind."),
                                new Settings.Description("Deleting Binds", "Pressing and releasing the keybind over a slot will clear any binds that it has."),
                                new Settings.Separator("Settings"),
                                new Settings.Keybind("Keybind", SlotBinding.keybind, "The keybind used for creating and removing slot binding combinations."),
                                new Settings.Toggle("Show Lines", SlotBinding.lines, "Draw lines between the slot you're hovering over, and any slots bound to it."),
                                new Settings.Toggle("Show Borders", SlotBinding.borders, "Draw borders around any slot that is bound to the slot you're hovering over."),
                                new Settings.SliderDouble("Line Width", 0.1, 5.0, 0.1, SlotBinding.lineWidth, "The width of the lines"),
                                new Settings.ColorPicker("Binding Color", true, SlotBinding.binding, "The color used to display the bind you are currently creating."),
                                new Settings.ColorPicker("Bound Color", true, SlotBinding.bound, "The color used to display existing slot binds.")
                        ))),
                        new Module("Price Tooltips", PriceTooltips.instance, "Adds pricing information to item tooltips. Requires connectivity to the NoFrills API.", new Settings(List.of(
                                new Settings.Toggle("Lowest BIN", PriceTooltips.auction, "Adds the Lowest BIN price to applicable items."),
                                new Settings.Toggle("Bazaar", PriceTooltips.bazaar, "Adds the Bazaar insta-buy and insta-sell prices to applicable items."),
                                new Settings.Toggle("NPC Sell", PriceTooltips.npc, "Adds the NPC sell price to applicable items."),
                                new Settings.Toggle("Motes Sell", PriceTooltips.mote, "Adds the Motes sell price to applicable items."),
                                new Settings.SliderInt("Grubber Stacks", 0, 5, 1, PriceTooltips.burgers, "The amount of McGrubber's Burgers you've eaten, used to calculate the bonus Motes value.")
                        ))),
                        new Module("Wardrobe Keybinds", WardrobeKeybinds.instance, "Adds hotkeys to the Skyblock Wardrobe.", new Settings(List.of(
                                new Settings.Dropdown<>("Keybind Style", WardrobeKeybinds.style, "The style of keybinds you want to use.\n\nSimple: Uses the 1-9 keyboard keys.\nHotbar: Uses your hotbar slot keybinds from the Minecraft controls screen.\nCustom: Uses the custom keys which you can define below."),
                                new Settings.Toggle("No Unequip", WardrobeKeybinds.noUnequip, "Prevents you from being able to unequip your armor set with a keybind."),
                                new Settings.Toggle("Sound Effect", WardrobeKeybinds.sound, "Plays a sound effect upon using a keybind."),
                                new Settings.Keybind("Custom Slot 1", WardrobeKeybinds.custom1, "Your custom keybind for the 1st wardrobe slot."),
                                new Settings.Keybind("Custom Slot 2", WardrobeKeybinds.custom2, "Your custom keybind for the 2nd wardrobe slot."),
                                new Settings.Keybind("Custom Slot 3", WardrobeKeybinds.custom3, "Your custom keybind for the 3rd wardrobe slot."),
                                new Settings.Keybind("Custom Slot 4", WardrobeKeybinds.custom4, "Your custom keybind for the 4th wardrobe slot."),
                                new Settings.Keybind("Custom Slot 5", WardrobeKeybinds.custom5, "Your custom keybind for the 5th wardrobe slot."),
                                new Settings.Keybind("Custom Slot 6", WardrobeKeybinds.custom6, "Your custom keybind for the 6th wardrobe slot."),
                                new Settings.Keybind("Custom Slot 7", WardrobeKeybinds.custom7, "Your custom keybind for the 7th wardrobe slot."),
                                new Settings.Keybind("Custom Slot 8", WardrobeKeybinds.custom8, "Your custom keybind for the 8th wardrobe slot."),
                                new Settings.Keybind("Custom Slot 9", WardrobeKeybinds.custom9, "Your custom keybind for the 9th wardrobe slot.")
                        ))),
                        new Module("Chat Waypoints", ChatWaypoints.instance, "Automatically creates waypoints for coordinates sent in Party/Global chat.", new Settings(List.of(
                                new Settings.Separator("Party Chat"),
                                new Settings.Toggle("Create Waypoints", ChatWaypoints.partyWaypoints, "Enables creating waypoints for coordinates sent by party members."),
                                new Settings.Toggle("Clear On Arrive", ChatWaypoints.partyClear, "Automatically removes the party waypoint once you get close."),
                                new Settings.SliderInt("Duration", 1, 600, 1, ChatWaypoints.partyDuration, "The duration (in seconds) that party waypoints should be rendered for."),
                                new Settings.ColorPicker("Color", true, ChatWaypoints.partyColor, "The color used for the party waypoints."),
                                new Settings.Separator("All Chat"),
                                new Settings.Toggle("Create Waypoints", ChatWaypoints.allWaypoints, "Enables creating waypoints for coordinates sent by players in the all chat."),
                                new Settings.Toggle("Clear On Arrive", ChatWaypoints.allClear, "Automatically remove the all chat waypoint once you get close."),
                                new Settings.SliderInt("Duration", 1, 600, 1, ChatWaypoints.allDuration, "The duration (in seconds) that all chat waypoints should be rendered for."),
                                new Settings.ColorPicker("Color", true, ChatWaypoints.allColor, "The color used for the all chat waypoints.")
                        ))),
                        new Module("Etherwarp Overlay", EtherwarpOverlay.instance, "Highlights the block you're targeting with the Ether Transmission ability.", new Settings(List.of(
                                new Settings.Dropdown<>("Highlight Style", EtherwarpOverlay.highlightStyle, "The style of the highlight"),
                                new Settings.Separator("Colors"),
                                new Settings.ColorPicker("Correct Fill", true, EtherwarpOverlay.fillCorrect, "The fill color used when your Etherwarp target is considered valid."),
                                new Settings.ColorPicker("Wrong Fill", true, EtherwarpOverlay.fillWrong, "The fill color used when your Etherwarp target is considered invalid."),
                                new Settings.ColorPicker("Correct Outline", true, EtherwarpOverlay.outlineCorrect, "The outline color used when your Etherwarp target is considered valid."),
                                new Settings.ColorPicker("Wrong Outline", true, EtherwarpOverlay.outlineWrong, "The outline color used when your Etherwarp target is considered invalid.")
                        ))),
                        new Module("Fullbright", Fullbright.instance, "You know him, you love him.", new Settings(List.of(
                                new Settings.Dropdown<>("Mode", Fullbright.mode, "The lighting mode used by fullbright."),
                                new Settings.Toggle("No Effect", Fullbright.noEffect, "Removes the Night Vision effect while active. Ignored if you use the Potion mode.")
                        ))),
                        new Module("No Render", NoRender.instance, "Prevent various things from appearing.", new Settings(List.of(
                                new Settings.Toggle("Explosions", NoRender.explosions, "Prevents the server from spawning any explosion particles."),
                                new Settings.Toggle("Empty Tooltips", NoRender.emptyTooltips, "Disables slots that contain items with blank tooltips."),
                                new Settings.Toggle("Fire Overlay", NoRender.fireOverlay, "Removes the fire overlay."),
                                new Settings.Toggle("Break Particles", NoRender.breakParticles, "Removes the particles that appear when breaking blocks."),
                                new Settings.Toggle("Boss Bar", NoRender.bossBar, "Hides the boss health bar that appears at the top of the screen."),
                                new Settings.Toggle("Effect Display", NoRender.effectDisplay, "Removes the potion effect display from the inventory and the top right of the screen."),
                                new Settings.Toggle("Dead Entities", NoRender.deadEntities, "Hides entities that are in their death animation, and their health bars (if applicable)."),
                                new Settings.Toggle("Dead Poof", NoRender.deadPoof, "Tries to hide the death \"poof\" particles that appear after a dead entity is deleted."),
                                new Settings.Toggle("Lightning", NoRender.lightning, "Hides lightning strikes."),
                                new Settings.Toggle("Falling Blocks", NoRender.fallingBlocks, "Hides falling block entities such as sand."),
                                new Settings.Toggle("Mage Beam", NoRender.mageBeam, "Prevents the server from spawning the Mage Beam particles in Dungeons."),
                                new Settings.Toggle("Tree Bits", NoRender.treeBits, "Hides the flying wood and leaves blocks that appear when chopping trees on the Galatea."),
                                new Settings.Toggle("Nausea", NoRender.nausea, "Prevents the nausea screen wobble and/or green overlay from rendering."),
                                new Settings.Toggle("Vignette", NoRender.vignette, "Prevents the dark vignette overlay from rendering."),
                                new Settings.Toggle("Exp Orbs", NoRender.expOrbs, "Prevents experience orbs from rendering.")
                        ))),
                        new Module("Pearl Refill", PearlRefill.instance, "Easily refill your Ender Pearls from your sacks with a keybind.", new Settings(List.of(
                                new Settings.Keybind("Keybind", PearlRefill.keybind, "The key that activates the feature.")
                        ))),
                        new Module("Party Commands", PartyCommands.instance, "Provide various commands to your party members.", new Settings(List.of(
                                new Settings.Description("Usage", "Run the \"/nf partyCommands\" command to see more information."),
                                new Settings.TextInput("Prefixes", PartyCommands.prefixes, "List of valid prefixes for these commands, separated by space."),
                                new Settings.Toggle("Self Commands", PartyCommands.self, "Allows you to trigger your own party commands and grants you whitelisted status, not recommended."),
                                new Settings.Dropdown<>("Warp", PartyCommands.warp, "Allows party members to warp themselves into your lobby on demand.\n\nCommand: !warp"),
                                new Settings.Dropdown<>("Party Transfer", PartyCommands.transfer, "Allows party members to promote themselves to party leader on demand.\n\nCommand: !pt"),
                                new Settings.Dropdown<>("All Invite", PartyCommands.allinv, "Allows party members to toggle the All Invite party setting on demand.\n\nCommand: !allinv"),
                                new Settings.Dropdown<>("Downtime", PartyCommands.downtime, "Allows party members to schedule a downtime reminder for the end of your Kuudra/Dungeons run.\nThis command will also pause Auto Requeue if you have it enabled.\n\nCommand: !dt"),
                                new Settings.Dropdown<>("Instance Queue", PartyCommands.queue, Utils.format("Allows party members to queue for any instance on demand.\n\nCommand List: {}", PartyCommands.listInstancesFormatted())),
                                new Settings.Dropdown<>("Coords", PartyCommands.coords, "Allows party members to get your coordinates on demand.\n\nCommand: !coords")
                        ))),
                        new Module("Viewmodel", Viewmodel.instance, "Easily customize the appearance of your held item.", new Settings(List.of(
                                new Settings.Toggle("No Haste", Viewmodel.noHaste, "Prevents Haste and Mining Fatigue from affecting your swing speed."),
                                new Settings.Toggle("No Equip Animation", Viewmodel.noEquip, "Removes the item swapping animation."),
                                new Settings.Toggle("No Bow Swing", Viewmodel.noBowSwing, "Removes the swing animation for all bows."),
                                new Settings.Toggle("Apply To Hand", Viewmodel.applyToHand, "Applies the viewmodel changes to the empty hand."),
                                new Settings.SliderInt("Swing Speed", 0, 20, 1, Viewmodel.speed, "Apply a custom swing speed. Set to 0 to disable."),
                                new Settings.SliderDouble("Offset X", -2, 2, 0.01, Viewmodel.offsetX, "The X axis offset position of your held item."),
                                new Settings.SliderDouble("Offset Y", -2, 2, 0.01, Viewmodel.offsetY, "The Y axis offset position of your held item."),
                                new Settings.SliderDouble("Offset Z", -2, 2, 0.01, Viewmodel.offsetZ, "The Z axis offset position of your held item."),
                                new Settings.SliderDouble("Scale X", 0, 5, 0.01, Viewmodel.scaleX, "The X axis scale of your held item."),
                                new Settings.SliderDouble("Scale Y", 0, 5, 0.01, Viewmodel.scaleY, "The Y axis scale of your held item."),
                                new Settings.SliderDouble("Scale Z", 0, 5, 0.01, Viewmodel.scaleZ, "The Z axis scale of your held item."),
                                new Settings.SliderDouble("Rotation X", -180, 180, 0.5, Viewmodel.rotX, "The X axis rotation of your held item."),
                                new Settings.SliderDouble("Rotation Y", -180, 180, 0.5, Viewmodel.rotY, "The Y axis rotation of your held item."),
                                new Settings.SliderDouble("Rotation Z", -180, 180, 0.5, Viewmodel.rotZ, "The Z axis rotation of your held item."),
                                new Settings.SliderDouble("Swing X", 0, 2, 0.01, Viewmodel.swingX, "The X multiplier for swing animation offset."),
                                new Settings.SliderDouble("Swing Y", 0, 2, 0.01, Viewmodel.swingY, "The Y multiplier for swing animation offset."),
                                new Settings.SliderDouble("Swing Z", 0, 2, 0.01, Viewmodel.swingZ, "The Z multiplier for swing animation offset.")
                        ))),
                        new Module("Custom Keybinds", CustomKeybinds.instance, "Create keybinds that run a custom command when pressed.", CustomKeybinds.buildSettings())
                )),
                new Category("Tweaks", List.of(
                        new Module("No Loading Screen", NoLoadingScreen.instance, "Fully removes the loading terrain screen that appears when switching islands."),
                        new Module("Middle Click Override", MiddleClickOverride.instance, "Replaces left clicks with middle clicks in applicable GUIs, making navigation smoother."),
                        new Module("No Front Perspective", NoFrontPerspective.instance, "Removes the front facing camera perspective."),
                        new Module("No Ability Place", NoAbilityPlace.instance, "Prevents block items with abilities from being placeable client side, such as the Egglocator.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", NoAbilityPlace.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", NoAbilityPlace.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Anti Swim", AntiSwim.instance, "Prevents the crawling and the swimming animations from activating.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", AntiSwim.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", AntiSwim.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Enchant Fix", EnchantFix.instance, "Fixes client side issues with certain vanilla enchants.\n\n- Fixes Efficiency being lag and ping dependent\n- Fixes Aqua Affinity not working", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", EnchantFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", EnchantFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Item Count Fix", ItemCountFix.instance, "Prevents the game from hiding item counts for unstackable items."),
                        new Module("Middle Click Fix", MiddleClickFix.instance, "Allows the middle mouse button to work just as it does on 1.8.9."),
                        new Module("No Drop Swing", NoDropSwing.instance, "Don't swing your hand while dropping items.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", NoDropSwing.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", NoDropSwing.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("No Pearl Cooldown", NoPearlCooldown.instance, "Removes the visual cooldown from Ender Pearls.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", NoPearlCooldown.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", NoPearlCooldown.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Old Sneak", OldSneak.instance, "Restores the old sneaking eye height and hitbox size.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", OldSneak.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", OldSneak.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Riding Camera Fix", RidingCameraFix.instance, "Removes the floaty camera movement effect while riding entities.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", RidingCameraFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", RidingCameraFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Snow Fix", SnowFix.instance, "Adjusts snow layer collision to reduce lagbacks.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", SnowFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", SnowFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Animation Fix", AnimationFix.instance, "Fixes the ancient bug where certain animations can play twice, such as unsneaking.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", AnimationFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", AnimationFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Sneak Lag Fix", SneakLagFix.instance, "Fixes lag backs that occur when you sneak while walking.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", SneakLagFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", SneakLagFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Double Use Fix", DoubleUseFix.instance, "Fixes Blaze Daggers and Fishing Rods being able to activate twice at once.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", DoubleUseFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", DoubleUseFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Old Safewalk", OldSafewalk.instance, "Allows you to walk onto short blocks (such as carpet) while sneaking.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", OldSafewalk.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", OldSafewalk.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Disconnect Fix", DisconnectFix.instance, "Patches any known kick/disconnect issues.\n\n- Fixes the rare disconnects that occur while doing Tarantula slayer."),
                        new Module("Break Reset Fix", BreakResetFix.instance, "Fixes item updates resetting your block breaking progress.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", BreakResetFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", BreakResetFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("No Confirm Screen", NoConfirmScreen.instance, "Removes the \"Confirm Command Execution\" screen and allows the command to run anyways.")
                )),
                new Category("Misc", List.of(
                        new Module("Tooltip Scale", TooltipScale.instance, "Customize the scale of tooltips.", new Settings(List.of(
                                new Settings.SliderDouble("Scale", 0.0, 4.0, 0.01, TooltipScale.scale, "The scale multiplier.")
                        ))),
                        new Module("Recipe Lookup", RecipeLookup.instance, "Search up recipes for the hovered item with a keybind.", new Settings(List.of(
                                new Settings.Keybind("Keybind", RecipeLookup.keybind, "The key that activates the feature.")
                        ))),
                        new Module("Update Checker", UpdateChecker.instance, "Checks if a NoFrills update is available the first time you join any world/server."),
                        new Module("Hotbar Swap", HotbarSwap.instance, "A simple alternative to slot binding with no configuration needed.", new Settings(List.of(
                                new Settings.Separator("Usage"),
                                new Settings.Description("Swapping", "Left ctrl + Left click on an item in your inventory to swap it with the hotbar slot directly below it."),
                                new Settings.Separator("Settings"),
                                new Settings.SliderInt("Last Override", 1, 9, 1, HotbarSwap.override, "Specify a replacement hotbar slot in cases where you swap with the 9th (unused) hotbar slot.")
                        ))),
                        new Module("Auto Requeue", AutoRequeue.instance, "Automatically starts a new Dungeons/Kuudra run once finished.", new Settings(List.of(
                                new Settings.SliderInt("Delay", 20, 400, 5, AutoRequeue.delay, "The delay (in ticks) until the new run is started."),
                                new Settings.Toggle("Terror Check", AutoRequeue.terrorCheck, "Waits until everyone has switched off their Terror armor to start requeuing. Only applies in Kuudra."),
                                new Settings.Keybind("Pause Keybind", AutoRequeue.pauseBind, "A keybind that allows you to manually pause Auto Requeue on demand.")
                        ))),
                        new Module("Party Finder", PartyFinder.instance, "Various features for your monkey finding adventures.", new Settings(List.of(
                                new Settings.Toggle("Buttons", PartyFinder.buttons, "Adds various buttons in chat whenever anyone joins your party, such as kick or copy name.")
                        ))),
                        new Module("Command Tooltip", CommandTooltip.instance, "Reveals the command that the hovered chat message would run when clicked."),
                        new Module("Auto Save", AutoSave.instance, "Automatically saves your settings after closing the settings/HUD editor screen."),
                        new Module("Unfocused Tweaks", UnfocusedTweaks.instance, "Various optimizations for when you are tabbed out of Minecraft.", new Settings(List.of(
                                new Settings.Toggle("Skip World Render", UnfocusedTweaks.noWorldRender, "Skips world rendering while unfocused which greatly reduces usage."),
                                new Settings.Toggle("Mute Sounds", UnfocusedTweaks.muteSounds, "Mutes the in-game sound while unfocused."),
                                new Settings.Toggle("No Vanilla Limit", UnfocusedTweaks.noVanilla, "Fully disables the vanilla \"Reduce FPS\" option."),
                                new Settings.SliderInt("FPS Limit", 0, 200, 1, UnfocusedTweaks.fpsLimit, "The max FPS the game will render at while unfocused. Set to 0 to disable.")
                        ))),
                        new Module("Page Keybinds", PageKeybinds.instance, "Adds next/previous page keybinds to applicable Skyblock GUIs.", new Settings(List.of(
                                new Settings.Keybind("Next Page", PageKeybinds.next, "The keybind to go to the next page of the GUI."),
                                new Settings.Keybind("Previous Page", PageKeybinds.previous, "The keybind to go to the previous page of the GUI.")
                        ))),
                        new Module("Force Nametag", ForceNametag.instance, "Makes player nametags always visible, even if they are invisible and/or sneaking.")
                )),
                new Category("Solvers", List.of(
                        new Module("Experimentation Table", ExperimentSolver.instance, "Solves the Experimentation Table mini-games and prevents wrong clicks.", new Settings(List.of(
                                new Settings.Toggle("Chronomatron", ExperimentSolver.chronomatron, "Reveals the solution in Chronomatron."),
                                new Settings.Toggle("Ultrasequencer", ExperimentSolver.ultrasequencer, "Reveals the solution in Ultrasequencer."),
                                new Settings.Toggle("Superpairs", ExperimentSolver.superpairs, "Reveals uncovered rewards in Superpairs and highlights matchable/matched pairs.")
                        ))),
                        new Module("Calendar Date", CalendarDate.instance, "Calculates the exact starting dates of events in the calendar."),
                        new Module("Spooky Chests", SpookyChests.instance, "Highlights nearby trick or treat chests during the Spooky Festival.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, SpookyChests.color, "The color of the spooky chest highlight.")
                        ))),
                        new Module("Diana Solver", DianaSolver.instance, "Guesses Diana burrow positions when using your spade. Also highlights nearby burrows.", new Settings(List.of(
                                new Settings.Separator("Burrows"),
                                new Settings.Toggle("Guess Tracer", DianaSolver.guessTracer, "Draws a tracer towards the guessed burrow."),
                                new Settings.ColorPicker("Guess Color", true, DianaSolver.guessColor, "The color of the guessed burrow beacon."),
                                new Settings.ColorPicker("Tracer Color", true, DianaSolver.guessTracerColor, "The color of the guessed burrow tracer."),
                                new Settings.ColorPicker("Treasure Color", true, DianaSolver.treasureColor, "The color of the treasure burrow beacon."),
                                new Settings.ColorPicker("Enemy Color", true, DianaSolver.enemyColor, "The color of the enemy burrow beacon."),
                                new Settings.ColorPicker("Start Color", true, DianaSolver.startColor, "The color of the start burrow beacon."),
                                new Settings.Separator("Warps"),
                                new Settings.Keybind("Warp Keybind", DianaSolver.warpKey, "The keybind to warp to the location closest to the guessed burrow."),
                                new Settings.Toggle("Hub Warp", DianaSolver.hubToggle, "Consider Hub a valid warp location when using the Warp Keybind."),
                                new Settings.Toggle("Stonks Warp", DianaSolver.stonksToggle, "Consider Stonks Auction as a valid warp location when using the Warp Keybind."),
                                new Settings.Toggle("Museum Warp", DianaSolver.museumToggle, "Consider Museum as a valid warp location when using the Warp Keybind."),
                                new Settings.Toggle("Castle Warp", DianaSolver.castleToggle, "Consider Castle as a valid warp location when using the Warp Keybind."),
                                new Settings.Toggle("Wizard Tower Warp", DianaSolver.wizardToggle, "Consider Wizard Tower as a valid warp location when using the Warp Keybind."),
                                new Settings.Toggle("Dark Auction Warp", DianaSolver.daToggle, "Consider Dark Auction as a valid warp location when using the Warp Keybind."),
                                new Settings.Toggle("Crypt Warp", DianaSolver.cryptToggle, "Consider Crypt as a valid warp location when using the Warp Keybind.")
                        ))),
                        new Module("Hoppity Solver", HoppitySolver.instance, "Guesses the position of the egg when using the Egglocator.", new Settings(List.of(
                                new Settings.Toggle("Guess Tracer", HoppitySolver.guessTracer, "Draws a tracer towards the guessed egg."),
                                new Settings.ColorPicker("Guess Color", true, HoppitySolver.guessColor, "The color of the guessed egg beacon."),
                                new Settings.ColorPicker("Tracer Color", true, HoppitySolver.guessTracerColor, "The color of the guessed egg tracer.")
                        ))),
                        new Module("Moonglade Beacon", BeaconTuningSolver.instance, "Solves the beacon tuning mini-game on Galatea.")
                )),
                new Category("Fishing", List.of(
                        new Module("Cap Tracker", CapTracker.instance, "Tracks the sea creature cap. Mostly for barn fishing.", new Settings(List.of(
                                new Settings.SliderInt("Target", 1, 60, 1, CapTracker.target, "The amount of sea creatures to consider as the limit."),
                                new Settings.Toggle("Show Title", CapTracker.title, "Shows a title on screen once the cap is reached."),
                                new Settings.Toggle("Play Sound", CapTracker.sound, "Plays a sound effect once the cap is reached."),
                                new Settings.Toggle("Send Message", CapTracker.sendMsg, "Sends a specific message once the cap is reached."),
                                new Settings.TextInput("Message", CapTracker.msg, "The message to send."),
                                new Settings.SliderInt("Kill Delay", 5, 120, 1, CapTracker.delay, "The delay (in seconds) until the cap starts being tracked again after it is reached.")
                        ))),
                        new Module("Mute Drake", MuteDrake.instance, "Prevents the Reindrake from blowing up your ears with gifts."),
                        new Module("Rare Glow", RareGlow.instance, "Applies a glow effect to nearby rare/profitable sea creatures.", new Settings(List.of(
                                new Settings.ColorPicker("Color", false, RareGlow.color, "The color of the glow.")
                        ))),
                        new Module("Rare Alert", RareAnnounce.instance, "Alerts you, and/or your party when you catch a rare sea creature.", new Settings(List.of(
                                new Settings.Toggle("Show Title", RareAnnounce.title, "Shows a title on screen with the name of the sea creature."),
                                new Settings.Toggle("Play Sound", RareAnnounce.sound, "Plays a sound effect once you catch a rare sea creature."),
                                new Settings.Toggle("Replace Message", RareAnnounce.replace, "Replaces the catch message of rare sea creatures with colored versions."),
                                new Settings.Toggle("Send Message", RareAnnounce.sendMsg, "Sends a specific message once you catch a rare sea creature."),
                                new Settings.TextInput("Message", RareAnnounce.msg, "The message to send. Replaces {spawnmsg} with the catch message, and {name} with the sea creature name.")
                        )))
                )),
                new Category("Hunting", List.of(
                        new Module("Invisibug Highlight", InvisibugHighlight.instance, "Highlights nearby Invisibugs on the Galatea.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, InvisibugHighlight.color, "The color of the Invisibug highlight.")
                        ))),
                        new Module("Cinderbat Highlight", CinderbatHighlight.instance, "Highlights the annoying bats on the Crimson Isle.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, CinderbatHighlight.color, "The color of the Cinderbat highlight.")
                        ))),
                        new Module("Fusion Keybinds", FusionKeybinds.instance, "Adds handy keybinds to the Fusion Machine.", new Settings(List.of(
                                new Settings.Keybind("Repeat Previous", FusionKeybinds.repeat, "The keybind to repeat the previous fusion."),
                                new Settings.Keybind("Confirm Fusion", FusionKeybinds.confirm, "The keybind to confirm a fusion."),
                                new Settings.Keybind("Cancel Fusion", FusionKeybinds.cancel, "The keybind to cancel a fusion.")
                        ))),
                        new Module("Lasso Alert", LassoAlert.instance, "Plays a sound effect once you can reel in with your lasso."),
                        new Module("Instant Fog", InstantFog.instance, "Makes the thick underwater fog disappear instantly."),
                        new Module("Shard Tracker", ShardTracker.instance, "Tracks obtained shards for you and displays information with a HUD element.", ShardTracker.buildSettings()),
                        new Module("Huntaxe Lock", HuntaxeLock.instance, "Requires you to double right click with your Huntaxe to be able to open the GUI.")
                )),
                new Category("Dungeons", List.of(
                        new Module("Device Solvers", DeviceSolvers.instance, "Solvers for various F7/M7 devices.", new Settings(List.of(
                                new Settings.Toggle("Sharpshooter", DeviceSolvers.sharpshooter, "Highlights hit targets as red while doing the 4th device.")
                        ))),
                        new Module("Starred Mob Highlight", StarredMobHighlight.instance, "High performance starred mob highlights.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, StarredMobHighlight.color, "The color of the starred mob highlight.")
                        ))),
                        new Module("Miniboss Highlight", MinibossHighlight.instance, "Highlights minibosses.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, MinibossHighlight.color, "The color of the miniboss highlight.")
                        ))),
                        new Module("Key Highlight", KeyHighlight.instance, "Highlights nearby Wither and Blood keys.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, KeyHighlight.color, "The color of the key highlight.")
                        ))),
                        new Module("Spirit Bow Highlight", SpiritBowHighlight.instance, "Highlights the Spirit Bow in the F4/M4 boss fight.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, SpiritBowHighlight.color, "The color of the Spirit Bow highlight.")
                        ))),
                        new Module("Reminders", DungeonReminders.instance, "Various class specific Dungeons reminders.", new Settings(List.of(
                                new Settings.Toggle("Wish", DungeonReminders.wish, "Reminds you to wish as Healer when Maxor enrages in F7/M7."),
                                new Settings.Toggle("Blood Camp", DungeonReminders.bloodCamp, "Reminds you to start camping the blood room as Mage."),
                                new Settings.Toggle("M5 Rag", DungeonReminders.rag, "Reminds you to use your Ragnarock in the M5 boss room as Mage.")
                        ))),
                        new Module("Leap Overlay", LeapOverlay.instance, "Renders a custom overlay in place of the Spirit Leap menu.", new Settings(List.of(
                                new Settings.Toggle("Send Message", LeapOverlay.send, "Sends a message once you leap to a teammate."),
                                new Settings.TextInput("Leap Message", LeapOverlay.message, "The message to send. Replaces {name} with the name of the player."),
                                new Settings.SliderDouble("Text Scale", 1.0, 4.0, 1.0, LeapOverlay.scale, "The scale of the text on the overlay."),
                                new Settings.ColorPicker("Healer Color", false, LeapOverlay.healer, "The color used for Healer on the overlay."),
                                new Settings.ColorPicker("Mage Color", false, LeapOverlay.mage, "The color used for Mage on the overlay."),
                                new Settings.ColorPicker("Bers Color", false, LeapOverlay.bers, "The color used for Berserker on the overlay."),
                                new Settings.ColorPicker("Arch Color", false, LeapOverlay.arch, "The color used for Archer on the overlay."),
                                new Settings.ColorPicker("Tank Color", false, LeapOverlay.tank, "The color used for Tank on the overlay.")
                        ))),
                        new Module("Terminal Solvers", TerminalSolvers.instance, "Solves terminals and prevents wrong clicks in F7/M7. Also hides item tooltips in every terminal.", new Settings(List.of(
                                new Settings.Toggle("Solve Panes", TerminalSolvers.panes, "Solves the \"Correct all panes\" terminal."),
                                new Settings.Toggle("Solve In Order", TerminalSolvers.inOrder, "Solves the \"Click in order\" Among Us task."),
                                new Settings.Toggle("Solve Starts With", TerminalSolvers.startsWith, "Solves the \"What starts with\" terminal."),
                                new Settings.Toggle("Solve Select", TerminalSolvers.select, "Solves the \"Select all\" terminal."),
                                new Settings.Toggle("Solve Colors", TerminalSolvers.colors, "Solves the \"Change all to same color\" terminal.")
                        ))),
                        new Module("Terracotta Timers", TerracottaTimer.instance, "Renders timers on screen and for every dead terracotta in F6/M6."),
                        new Module("Wither Dragons", WitherDragons.instance, "Features for the last phase of M7.", new Settings(List.of(
                                new Settings.Toggle("Spawn Alert", WitherDragons.alert, "Alerts you when a dragon is about to spawn.\nThis option also calculates the priority on the initial spawn based on your selected class."),
                                new Settings.SliderDouble("Split Power", 0, 32, 0.1, WitherDragons.power, "The required Power blessing level to consider a split possible.\nLeaving this option at 0 is recommended for party finder teams."),
                                new Settings.SliderDouble("Easy Power", 0, 32, 0.1, WitherDragons.powerEasy, "The required Power blessing level to consider a split possible, as long as one of the dragons is Purple."),
                                new Settings.Toggle("Dragon Glow", WitherDragons.glow, "Applies a glow effect to each dragon."),
                                new Settings.Toggle("Kill Areas", WitherDragons.boxes, "Renders the kill areas of every alive dragon."),
                                new Settings.Toggle("Tracers", WitherDragons.tracers, "Draws tracer lines to spawning dragons."),
                                new Settings.Toggle("Stack Waypoints", WitherDragons.stack, "Renders waypoints for stacking your Last Breath arrows."),
                                new Settings.Dropdown<>("Waypoint Type", WitherDragons.stackType, "The type of the arrow stack waypoints.\n\nSimple: Highlights the exact spawn position of a spawning dragon.\nAdvanced: Highlights each individual hitbox of a spawning dragon."),
                                new Settings.Toggle("Spawn Timer", WitherDragons.timer, "Renders timers for exactly when a dragon should finish spawning."),
                                new Settings.Toggle("Dragon Health", WitherDragons.health, "Renders the exact health of the dragons.")
                        ))),
                        new Module("Secret Bat Highlight", SecretBatHighlight.instance, "Applies a glow effect to secret bats.", new Settings(List.of(
                                new Settings.ColorPicker("Color", false, SecretBatHighlight.color, "The color of the secret bat glow.")
                        ))),
                        new Module("Livid Solver", LividSolver.instance, "Finds and highlights the correct Livid in F5/M5.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, LividSolver.color, "The color of the correct Livid outline.")
                        ))),
                        new Module("Prince Message", PrinceMessage.instance, "Sends a message when you gain bonus score from the Prince Shard.", new Settings(List.of(
                                new Settings.TextInput("Message", PrinceMessage.msg, "The message to send.")
                        ))),
                        new Module("Mimic Message", MimicMessage.instance, "Sends a message once you kill the Mimic. Should work even if it's instantly killed.", new Settings(List.of(
                                new Settings.TextInput("Message", MimicMessage.msg, "The message to send.")
                        ))),
                        new Module("Spirit Bear Timer", SpiritBearTimer.instance, "Renders a timer on screen for when the Spirit Bear is going to spawn in F4/M4."),
                        new Module("Secret Chime", SecretChime.instance, "Plays sounds upon collecting specific secrets.", new Settings(List.of(
                                new Settings.Toggle("Items", SecretChime.itemsToggle, "Play a chime upon picking up a secret item."),
                                new Settings.TextInput("Items Sound", SecretChime.itemsSound, "The identifier of the sound to play."),
                                new Settings.SliderDouble("Items Volume", 0.0, 5.0, 0.1, SecretChime.itemsVolume, "The volume of the sound."),
                                new Settings.SliderDouble("Items Pitch", 0.0, 2.0, 0.05, SecretChime.itemsPitch, "The pitch of the sound."),
                                new Settings.Toggle("Chests", SecretChime.chestToggle, "Play a chime upon opening a secret chest."),
                                new Settings.TextInput("Chests Sound", SecretChime.chestSound, "The identifier of the sound to play."),
                                new Settings.SliderDouble("Chests Volume", 0.0, 5.0, 0.1, SecretChime.chestVolume, "The volume of the sound."),
                                new Settings.SliderDouble("Chests Pitch", 0.0, 2.0, 0.05, SecretChime.chestPitch, "The pitch of the sound."),
                                new Settings.Toggle("Essence", SecretChime.essenceToggle, "Play a chime upon collecting a Wither Essence secret."),
                                new Settings.TextInput("Essence Sound", SecretChime.essenceSound, "The identifier of the sound to play."),
                                new Settings.SliderDouble("Essence Volume", 0.0, 5.0, 0.1, SecretChime.essenceVolume, "The volume of the sound."),
                                new Settings.SliderDouble("Essence Pitch", 0.0, 2.0, 0.05, SecretChime.essencePitch, "The pitch of the sound."),
                                new Settings.Toggle("Bats", SecretChime.batToggle, "Play a chime upon killing a secret bat."),
                                new Settings.TextInput("Bats Sound", SecretChime.batSound, "The identifier of the sound to play."),
                                new Settings.SliderDouble("Bats Volume", 0.0, 5.0, 0.1, SecretChime.batVolume, "The volume of the sound."),
                                new Settings.SliderDouble("Bats Pitch", 0.0, 2.0, 0.05, SecretChime.batPitch, "The pitch of the sound."),
                                new Settings.Toggle("Levers", SecretChime.leverToggle, "Play a chime upon interacting with a lever."),
                                new Settings.TextInput("Levers Sound", SecretChime.leverSound, "The identifier of the sound to play."),
                                new Settings.SliderDouble("Levers Volume", 0.0, 5.0, 0.1, SecretChime.leverVolume, "The volume of the sound."),
                                new Settings.SliderDouble("Levers Pitch", 0.0, 2.0, 0.05, SecretChime.leverPitch, "The pitch of the sound.")
                        ))),
                        new Module("Melody Message", MelodyMessage.instance, "Send start and progress messages when you get the Melody terminal in F7/M7.", new Settings(List.of(
                                new Settings.TextInput("Message", MelodyMessage.msg, "The message to send when the terminal is opened."),
                                new Settings.Toggle("Send Progress", MelodyMessage.progress, "Send messages when you make progress in the terminal."),
                                new Settings.TextInput("% Message", MelodyMessage.progressMsg, "The message to send when you make progress.\nReplaces {percent} with your progress percentage (25%/50%/75%).")
                        ))),
                        new Module("Quick Close", QuickClose.instance, "Quickly close Dungeon secret and/or loot chests by pressing any of the movement keys (WASD)."),
                        new Module("Chest Value", DungeonChestValue.instance, "Calculates the value of your Dungeons loot. Requires connectivity to the NoFrills API.", new Settings(List.of(
                                new Settings.ColorPicker("Background", true, DungeonChestValue.background, "The color of the background of the value text.")
                        )))
                )),
                new Category("Kuudra", List.of(
                        new Module("Drain Message", DrainMessage.instance, "Send a message when you drain your mana using an End Stone Sword.", new Settings(List.of(
                                new Settings.TextInput("Message", DrainMessage.message, "The message to send.\nReplaces {mana} with the mana used, and {players} with the amount of affected players."),
                                new Settings.Toggle("Hide Ability Messages", DrainMessage.hide, "Hides the chat messages that appear after using an End Stone Sword.")
                        ))),
                        new Module("Fresh Timer", FreshTimer.instance, "Shows a timer on screen for the Fresh Tools essence shop ability.", new Settings(List.of(
                                new Settings.Toggle("Send Message", FreshTimer.send, "Send a message once Fresh Tools activates."),
                                new Settings.TextInput("Message", FreshTimer.message, "The message to send.")
                        ))),
                        new Module("Kuudra Health", KuudraHealth.instance, "Shows Kuudra's exact health on screen.", new Settings(List.of(
                                new Settings.Toggle("Show DPS", KuudraHealth.dps, "Calculates your team's DPS. Only applies in the last phase of Infernal tier."),
                                new Settings.ColorPicker("Color", false, KuudraHealth.color, "The color of the text.")
                        ))),
                        new Module("Kuudra Hitbox", KuudraHitbox.instance, "Renders a hitbox for Kuudra.", new Settings(List.of(
                                new Settings.Toggle("Through Walls", KuudraHitbox.walls, "Makes the hitbox render through walls, because even YouTube ranks can get away with ESP in Kuudra."),
                                new Settings.ColorPicker("Color", true, KuudraHitbox.color, "The color of the hitbox.")
                        ))),
                        new Module("Waypoints", KuudraWaypoints.instance, "Renders various waypoints in Kuudra.", new Settings(List.of(
                                new Settings.Toggle("Supplies", KuudraWaypoints.supply, "Renders beacons for every supply crate."),
                                new Settings.ColorPicker("Supply Color", true, KuudraWaypoints.supplyColor, "The color of the supply crate beacons."),
                                new Settings.Toggle("Drop-offs", KuudraWaypoints.drop, "Renders beacons for every available supply drop-off point."),
                                new Settings.ColorPicker("Drop-off Color", true, KuudraWaypoints.dropColor, "The color of the drop-off beacons."),
                                new Settings.Toggle("Build Piles", KuudraWaypoints.build, "Renders beacons for every unfinished Ballista build pile."),
                                new Settings.ColorPicker("Piles Color", true, KuudraWaypoints.buildColor, "The color of the build pile beacons.")
                        ))),
                        new Module("Pre Message", PreMessage.instance, "Announces if no supply spawns at your pre spot (or your next pickup spot)."),
                        new Module("Shop Cleaner", ShopCleaner.instance, "Removes useless things from the perk shop."),
                        new Module("Chest Value", KuudraChestValue.instance, "Calculates the value of your Kuudra loot. Requires connectivity to the NoFrills API.", new Settings(List.of(
                                new Settings.ColorPicker("Background", true, KuudraChestValue.background, "The color of the background of the value text."),
                                new Settings.SliderInt("Pet Bonus", 0, 20, 1, KuudraChestValue.petBonus, "The extra Crimson Essence percentage granted by your Kuudra pet.\nUsed to calculate the value of the essence with the extra perk included.")
                        )))
                )),
                new Category("Slayer", List.of(
                        new Module("Boss Highlight", BossHighlight.instance, "Highlights your slayer boss.", new Settings(List.of(
                                new Settings.ColorPicker("Fill Color", true, BossHighlight.fillColor, "The color of the filled box highlight (if applicable)."),
                                new Settings.ColorPicker("Outline Color", true, BossHighlight.outlineColor, "The color of the outline box highlight (if applicable)."),
                                new Settings.Dropdown<>("Highlight Style", BossHighlight.highlightStyle, "The style of the highlight."),
                                new Settings.Separator("Inferno Demonlord"),
                                new Settings.ColorPicker("Ashen Fill", true, BossHighlight.ashenFill, "The color of the filled box if your boss is using the Ashen attunement."),
                                new Settings.ColorPicker("Ashen Outline", true, BossHighlight.ashenOutline, "The color of the outline box if your boss is using the Ashen attunement."),
                                new Settings.ColorPicker("Spirit Fill", true, BossHighlight.spiritFill, "The color of the filled box if your boss is using the Spirit attunement."),
                                new Settings.ColorPicker("Spirit Outline", true, BossHighlight.spiritOutline, "The color of the outline if your boss is using the Spirit attunement."),
                                new Settings.ColorPicker("Auric Fill", true, BossHighlight.auricFill, "The color of the filled box if your boss is using the Auric attunement."),
                                new Settings.ColorPicker("Auric Outline", true, BossHighlight.auricOutline, "The color of the outline box if your boss is using the Auric attunement."),
                                new Settings.ColorPicker("Crystal Fill", true, BossHighlight.crystalFill, "The color of the filled box if your boss is using the Crystal attunement."),
                                new Settings.ColorPicker("Crystal Outline", true, BossHighlight.crystalOutline, "The color of the outline box if your boss is using the Crystal attunement.")
                        ))),
                        new Module("Pillar Alert", PillarAlert.instance, "Alerts you when your Blaze boss spawns a fire pillar.\nThis feature tries to prevent false flags by tracking the \"path\" that the pillars take."),
                        new Module("No Attunement Spam", NoAttunementSpam.instance, "Filters the chat messages about using the wrong attunement on the Blaze boss."),
                        new Module("Kill Timer", KillTimer.instance, "Tracks how long your slayer boss took to kill."),
                        new Module("Chalice Highlight", ChaliceHighlight.instance, "Highlights the Blood Ichor chalices spawned by the T5 Vampire.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, ChaliceHighlight.color, "The color of the chalice highlight.")
                        ))),
                        new Module("Ice Alert", IceAlert.instance, "Shows a timer for when your Vampire boss is going to cast Twinclaws."),
                        new Module("Stake Alert", StakeAlert.instance, "Shows text on screen once you can vanquish your Vampire boss with the Steak Stake."),
                        new Module("Mute Vampire", MuteVampire.instance, "Prevents the Vampire Mania/Killer Springs sounds from playing.", new Settings(List.of(
                                new Settings.Toggle("Mania", MuteVampire.mania, "Mutes the loud Mania sounds while in the Chateau."),
                                new Settings.Toggle("Killer Springs", MuteVampire.springs, "Mutes the Wither sound spam that occurs when your boss spawns a Killer Spring.")
                        ))),
                        new Module("Hits Shield Display", HitsShieldDisplay.instance, "Renders the needed hits for the Voidgloom Seraph hits shield phase.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, HitsShieldDisplay.color, "The color of the text."),
                                new Settings.SliderDouble("Scale", 0.0, 1.0, 0.01, HitsShieldDisplay.scale, "The scale of the text.")
                        ))),
                        new Module("Egg Hits Display", EggHitsDisplay.instance, "Renders the needed hits for the Tarantula Broodfather egg sack phase.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, EggHitsDisplay.color, "The color of the text."),
                                new Settings.SliderDouble("Scale", 0.0, 1.0, 0.01, EggHitsDisplay.scale, "The scale of the text.")
                        ))),
                        new Module("Beacon Tracer", BeaconTracer.instance, "Draws tracers towards the Yang Glyphs thrown by the Voidgloom Seraph.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, BeaconTracer.color, "The color of the tracer.")
                        ))),
                        new Module("Mute Enderman", MuteEnderman.instance, "Prevents the angry Enderman sounds from playing."),
                        new Module("Cocoon Alert", CocoonAlert.instance, "Alerts you when your slayer boss is cocooned by your Primordial belt.")
                )),
                new Category("Mining", List.of(
                        new Module("Ability Alert", AbilityAlert.instance, "Alerts you when your pickaxe ability cooldown is finished.\nBecomes more reliable when the pickaxe ability tablist widget is present."),
                        new Module("Corpse Highlight", CorpseHighlight.instance, "Highlights corpses in the Glacite Mineshafts.", new Settings(List.of(
                                new Settings.ColorPicker("Lapis Color", false, CorpseHighlight.lapisColor, "The color of the Lapis corpse."),
                                new Settings.ColorPicker("Mineral Color", false, CorpseHighlight.mineralColor, "The color of the Tungsten corpse."),
                                new Settings.ColorPicker("Yog Color", false, CorpseHighlight.yogColor, "The color of the Umber corpse."),
                                new Settings.ColorPicker("Vanguard Color", false, CorpseHighlight.vanguardColor, "The color of the Vanguard corpse.")
                        ))),
                        new Module("Better Sky Mall", BetterSkyMall.instance, "Compacts Sky Mall messages, and fully hides them if you are not mining.", new Settings(List.of(
                                new Settings.TextInput("Buff Whitelist", BetterSkyMall.whitelist, "Allows a buff message to always show if it contains a specific keyword.\nThe list is case insensitive, and separated by comma.")
                        ))),
                        new Module("Ghost Vision", GhostVision.instance, "Makes Ghosts easier to see in the Dwarven Mines.", new Settings(List.of(
                                new Settings.ColorPicker("Fill Color", true, GhostVision.fill, "The color of the filled box over each Ghost."),
                                new Settings.ColorPicker("Outline Color", true, GhostVision.outline, "The color of the outline box over each Ghost.")
                        ))),
                        new Module("Scatha Mining", ScathaMining.instance, "Scatha mining features.", new Settings(List.of(
                                new Settings.Toggle("Spawn Alert", ScathaMining.alert, "Alerts you when a Worm/Scatha spawns nearby."),
                                new Settings.Toggle("Cooldown", ScathaMining.cooldown, "Tracks the Worm spawn cooldown for you.")
                        ))),
                        new Module("End Node Highlight", EndNodeHighlight.instance, "Highlights Ender Nodes.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, EndNodeHighlight.color, "The color of the node highlight.")
                        ))),
                        new Module("Temple Skip", TempleSkip.instance, "Highlights a pearl skip spot for the Jungle Temple once you approach the entrance.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, TempleSkip.color, "The color of the skip highlight.")
                        )))
                )),
                new Category("Farming", List.of(
                        new Module("Space Farmer", SpaceFarmer.instance, "Allows you to farm by holding space bar, sneak and press space to activate.\nThis feature will also lock your view once you start holding space."),
                        new Module("Glowing Mushrooms", GlowingMushroom.instance, "Highlights Glowing Mushrooms.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, GlowingMushroom.color, "The color of the highlight.")
                        ))),
                        new Module("Plot Borders", PlotBorders.instance, "Renders borders for plots.", new Settings(List.of(
                                new Settings.Toggle("Infested Plots", PlotBorders.infested, "Adds borders to plots with pests in them."),
                                new Settings.ColorPicker("Infested Color", true, PlotBorders.infestedColor, "The color of the infested plot border."),
                                new Settings.Toggle("Current Plot", PlotBorders.current, "Adds a border to the plot you are in."),
                                new Settings.ColorPicker("Current Color", true, PlotBorders.currentColor, "The color of the current plot border."),
                                new Settings.Toggle("All Plots", PlotBorders.all, "Adds borders to every plot if no other border should apply."),
                                new Settings.ColorPicker("All Color", true, PlotBorders.allColor, "The color of the border for every plot.")
                        )))
                ))
        );
        this.categories.getLast().margins(Insets.of(5, 0, 3, 3));
        for (Category category : this.categories) {
            parent.child(category);
        }
        this.mainScroll = Containers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent);
        this.mainScroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        root.child(this.mainScroll);
        ButtonComponent hudEditorButton = Components.button(Text.literal("Open HUD Editor"), button -> mc.setScreen(new HudEditorScreen()));
        hudEditorButton.margins(Insets.of(0, 3, 0, 3));
        hudEditorButton.positioning(Positioning.relative(100, 100));
        hudEditorButton.renderer((context, button, delta) -> {
            context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
            context.drawBorder(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
        });
        root.child(hudEditorButton);
        FlatTextbox searchBox = new FlatTextbox(Sizing.fixed(200));
        searchBox.setSuggestion("Search...");
        searchBox.margins(Insets.of(0, 3, 0, 0));
        searchBox.positioning(Positioning.relative(50, 100));
        searchBox.onChanged().subscribe(value -> {
            if (value.isEmpty()) {
                searchBox.setSuggestion("Search...");
                for (Category category : this.categories) {
                    category.scroll.child().clearChildren();
                    for (Module module : category.features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            } else {
                searchBox.setSuggestion("");
                for (Category category : this.categories) {
                    List<Module> features = new ArrayList<>(category.features);
                    features.removeIf(feature -> {
                        if (matchSearch(feature.label.getText(), value) || matchSearch(feature.label.getTooltip(), value)) {
                            return false;
                        }
                        if (feature.options != null) {
                            for (FlowLayout setting : feature.options.settings) {
                                for (Component child : setting.children()) {
                                    if (child instanceof PlainLabel label) {
                                        if (matchSearch(label.getText(), value) || matchSearch(label.getTooltip(), value)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    });
                    category.scroll.child().clearChildren();
                    for (Module module : features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            }
        });
        root.child(searchBox);
    }

    @Override
    public void close() {
        if (AutoSave.instance.isActive()) AutoSave.save();
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }
        super.close();
    }
}
