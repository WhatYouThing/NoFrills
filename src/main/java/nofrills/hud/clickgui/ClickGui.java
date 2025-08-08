package nofrills.hud.clickgui;

import com.google.common.collect.Lists;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.DrawContext;
import nofrills.features.dungeons.*;
import nofrills.features.farming.GlowingMushroom;
import nofrills.features.farming.SpaceFarmer;
import nofrills.features.fishing.CapTracker;
import nofrills.features.fishing.MuteDrake;
import nofrills.features.fishing.RareAnnounce;
import nofrills.features.fishing.RareGlow;
import nofrills.features.fixes.*;
import nofrills.features.general.*;
import nofrills.features.hunting.*;
import nofrills.features.keybinds.CustomKeybinds;
import nofrills.features.keybinds.PearlRefill;
import nofrills.features.keybinds.RecipeLookup;
import nofrills.features.kuudra.*;
import nofrills.features.mining.*;
import nofrills.features.slayer.*;
import nofrills.features.solvers.CalendarDate;
import nofrills.features.solvers.ExperimentSolver;
import nofrills.features.solvers.SpookyChests;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public List<Category> categories;
    public ScrollContainer<FlowLayout> mainScroll;
    public int mouseX = 0;
    public int mouseY = 0;

    private boolean matchSearch(String text, String search) {
        return text.toLowerCase().replaceAll(" ", "").contains(search.toLowerCase().replaceAll(" ", ""));
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
        int height = context.getScaledWindowHeight() - 4;
        context.drawTextWithShadow(this.textRenderer, "Left click a feature to toggle", 1, height - 24, 0xffffff);
        context.drawTextWithShadow(this.textRenderer, "Right click a feature open its settings", 1, height - 16, 0xffffff);
        context.drawTextWithShadow(this.textRenderer, "Scrolling supported in each category and the screen itself", 1, height - 8, 0xffffff);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        this.categories = Lists.newArrayList(
                new Category("General", List.of(
                        new Module("Auto Sprint", AutoSprint.instance, "Essentially Toggle Sprint, but always active.", new Settings(List.of(
                                new Settings.Toggle("Submerged Check", AutoSprint.waterCheck, "Prevents Auto Sprint from working while you are underwater.")
                        ))),
                        new Module("Update Checker", UpdateChecker.instance, "Checks if a NoFrills update is available the first time you join any world/server."),
                        new Module("Slot Binding", SlotBinding.instance, "Bind your hotbar slots to your inventory slots, similarly to NEU's slot binding.", new Settings(List.of(
                                new Settings.Separator("Usage"),
                                new Settings.Description("Using Binds", "Shift + Left click on either of the bound slots to swap items between them."),
                                new Settings.Description("Adding Binds", "Hover over a slot, press the keybind, move your cursor to another slot, and release the keybind."),
                                new Settings.Description("Deleting Binds", "Pressing and releasing the keybind over a slot will clear any binds that it has."),
                                new Settings.Separator("Settings"),
                                new Settings.Keybind("Keybind", SlotBinding.keybind, "The keybind used for creating and removing slot binding combinations."),
                                new Settings.Toggle("Show Lines", SlotBinding.lines, "Draw lines between the slot you're hovering over, and any slots bound to it."),
                                new Settings.Toggle("Show Borders", SlotBinding.borders, "Draw borders around any slot that is bound to the slot you're hovering over."),
                                new Settings.ColorPicker("Binding Color", false, SlotBinding.binding, "The color used to display the bind you are currently creating."),
                                new Settings.ColorPicker("Bound Color", false, SlotBinding.bound, "The color used to display existing slot binds.")
                        ))),
                        new Module("Price Tooltips", PriceTooltips.instance, "Adds pricing information to item tooltips. Requires connectivity to the NoFrills API.", new Settings(List.of(
                                new Settings.Toggle("Lowest BIN", PriceTooltips.auction, "Adds the Lowest BIN price to applicable items."),
                                new Settings.Toggle("Bazaar", PriceTooltips.bazaar, "Adds the Bazaar insta-buy and insta-sell prices to applicable items."),
                                new Settings.Toggle("NPC Sell", PriceTooltips.npc, "Adds the NPC sell price to applicable items."),
                                new Settings.Toggle("Motes Sell", PriceTooltips.mote, "Adds the Motes sell price to applicable items."),
                                new Settings.SliderInt("Grubber Stacks", 0, 5, 1, PriceTooltips.burgers, "The amount of McGrubber's Burgers you've eaten, used to calculate the bonus Motes value.")
                        ))),
                        new Module("Wardrobe Keybinds", WardrobeKeybinds.instance, "Adds number hotkeys (1-9) to the Skyblock Wardrobe.", new Settings(List.of(
                                new Settings.Toggle("No Unequip", WardrobeKeybinds.noUnequip, "Prevents you from being able to unequip your armor set with the keybinds."),
                                new Settings.Toggle("Sound Effect", WardrobeKeybinds.sound, "Plays a sound effect after switching your armor set with a hotkey.")
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
                                new Settings.Toggle("Lightning", NoRender.lightning, "Hides lightning strikes."),
                                new Settings.Toggle("Falling Blocks", NoRender.fallingBlocks, "Hides falling block entities such as sand."),
                                new Settings.Toggle("Mage Beam", NoRender.mageBeam, "Prevents the server from spawning the Mage Beam particles in Dungeons."),
                                new Settings.Toggle("Tree Bits", NoRender.treeBits, "Hides the flying wood and leaves blocks that appear when chopping trees on the Galatea."),
                                new Settings.Toggle("Nametag Invisibility", NoRender.nametagInvisibility, "Keeps player nametags visible, even if they are sneaking or have the invisibility effect.")
                        ))),
                        new Module("Pearl Refill", PearlRefill.instance, "Easily refill your Ender Pearls from your sacks with a keybind.", new Settings(List.of(
                                new Settings.Keybind("Keybind", PearlRefill.keybind, "The key that activates the feature.")
                        ))),
                        new Module("Recipe Lookup", RecipeLookup.instance, "Search up recipes for the hovered item with a keybind.", new Settings(List.of(
                                new Settings.Keybind("Keybind", RecipeLookup.keybind, "The key that activates the feature.")
                        ))),
                        new Module("Party Commands", PartyCommands.instance, "Provide various commands to your party members.", new Settings(List.of(
                                new Settings.Description("Usage", "Run the \"/nf partyCommands\" command to see more information."),
                                new Settings.TextInput("Prefixes", PartyCommands.prefixes, "List of valid prefixes for these commands, separated by space."),
                                new Settings.Toggle("Self Commands", PartyCommands.self, "Allows you to trigger your own party commands and grants you whitelisted status, not recommended."),
                                new Settings.Dropdown<>("Warp", PartyCommands.warp, "Allows party members to warp themselves into your lobby on demand.\n\nCommand: !warp"),
                                new Settings.Dropdown<>("Party Transfer", PartyCommands.transfer, "Allows party members to promote themselves to party leader on demand.\n\nCommand: !pt"),
                                new Settings.Dropdown<>("All Invite", PartyCommands.allinv, "Allows party members to toggle the All Invite party setting on demand.\n\nCommand: !allinv"),
                                new Settings.Dropdown<>("Downtime", PartyCommands.downtime, "Allows party members to schedule a downtime reminder for the end of your Kuudra/Dungeons run.\n\nCommand: !dt"),
                                new Settings.Dropdown<>("Instance Queue", PartyCommands.queue, Utils.format("Allows party members to queue for any instance on demand.\n\nCommand List: {}", PartyCommands.listInstancesFormatted())),
                                new Settings.Dropdown<>("Coords", PartyCommands.coords, "Allows party members to get your coordinates on demand.\n\nCommand: !coords")
                        ))),
                        new Module("Party Finder", PartyFinder.instance, "Various features for your monkey finding adventures.", new Settings(List.of(
                                new Settings.Toggle("Buttons", PartyFinder.buttons, "Adds various buttons in chat whenever anyone joins your party, such as kick or copy name.")
                        ))),
                        new Module("Viewmodel", Viewmodel.instance, "Easily customize the appearance of your held item.", new Settings(List.of(
                                new Settings.Toggle("No Haste", Viewmodel.noHaste, "Prevents Haste and Mining Fatigue from affecting your swing speed."),
                                new Settings.Toggle("No Equip Animation", Viewmodel.noEquip, "Removes the item swapping animation."),
                                new Settings.SliderInt("Swing Speed", 0, 20, 1, Viewmodel.speed, "Apply a custom swing speed. Set to 0 to disable."),
                                new Settings.SliderDouble("Offset X", -2, 2, 0.01, Viewmodel.offsetX, "The X axis offset position of your held item."),
                                new Settings.SliderDouble("Offset Y", -2, 2, 0.01, Viewmodel.offsetY, "The Y axis offset position of your held item."),
                                new Settings.SliderDouble("Offset Z", -2, 2, 0.01, Viewmodel.offsetZ, "The Z axis offset position of your held item."),
                                new Settings.SliderDouble("Scale X", 0, 5, 0.01, Viewmodel.scaleX, "The X axis scale of your held item."),
                                new Settings.SliderDouble("Scale Y", 0, 5, 0.01, Viewmodel.scaleY, "The Y axis scale of your held item."),
                                new Settings.SliderDouble("Scale Z", 0, 5, 0.01, Viewmodel.scaleZ, "The Z axis scale of your held item."),
                                new Settings.SliderDouble("Rotation X", -180, 180, 0.5, Viewmodel.rotX, "The X axis rotation of your held item."),
                                new Settings.SliderDouble("Rotation Y", -180, 180, 0.5, Viewmodel.rotY, "The Y axis rotation of your held item."),
                                new Settings.SliderDouble("Rotation Z", -180, 180, 0.5, Viewmodel.rotZ, "The Z axis rotation of your held item.")
                        ))),
                        new Module("Tooltip Scale", TooltipScale.instance, "Customize the scale of item tooltips.", new Settings(List.of(
                                new Settings.SliderDouble("Scale", 0.0, 4.0, 0.01, TooltipScale.scale, "The scale multiplier.")
                        ))),
                        new Module("Custom Keybinds", CustomKeybinds.instance, "Create keybinds that run a custom command when pressed.", CustomKeybinds.buildSettings()),
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
                        )))
                )),
                new Category("Tweaks", List.of(
                        new Module("No Loading Screen", NoLoadingScreen.instance, "Fully removes the loading terrain screen that appears when switching islands."),
                        new Module("Middle Click Override", MiddleClickOverride.instance, "Replaces left clicks with middle clicks in applicable GUI's, making navigation smoother."),
                        new Module("No Front Perspective", NoFrontPerspective.instance, "Removes the front facing camera perspective."),
                        new Module("No Ability Place", NoAbilityPlace.instance, "Prevents block items with abilities from being placeable client side, such as the Egglocator.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", NoAbilityPlace.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", NoAbilityPlace.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Anti Swim", AntiSwim.instance, "Prevents the crawling and the swimming animations from activating.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", AntiSwim.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", AntiSwim.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Efficiency Fix", EfficiencyFix.instance, "Fixes the Efficiency enchant being ping and lag dependent.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", EfficiencyFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", EfficiencyFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
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
                        new Module("Stonk Fix", StonkFix.instance, "Reverts Microsoft's client side stonking patch.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", StonkFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", StonkFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        ))),
                        new Module("Animation Fix", AnimationFix.instance, "Fixes the ancient bug where certain animations can play twice, such as unsneaking.", new Settings(List.of(
                                new Settings.Toggle("Skyblock Only", AnimationFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock."),
                                new Settings.Toggle("Old Island Only", AnimationFix.modernCheck, "Prevent the feature from activating on islands using modern Minecraft versions (such as Galatea).")
                        )))
                )),
                new Category("Solvers", List.of(
                        new Module("Experiments", ExperimentSolver.instance, "Solves the Experimentation Table mini-games and prevents wrong clicks.", new Settings(List.of(
                                new Settings.Toggle("Chronomatron", ExperimentSolver.chronomatron, "Reveals the solution in Chronomatron."),
                                new Settings.Toggle("Ultrasequencer", ExperimentSolver.ultrasequencer, "Reveals the solution in Ultrasequencer."),
                                new Settings.Toggle("Superpairs", ExperimentSolver.superpairs, "Reveals uncovered rewards in Superpairs and highlights matchable/matched pairs.")
                        ))),
                        new Module("Calendar Date", CalendarDate.instance, "Calculates the exact starting dates of events in the calendar."),
                        new Module("Spooky Chests", SpookyChests.instance, "Highlights nearby trick or treat chests during the Spooky Festival.", new Settings(List.of(
                                new Settings.ColorPicker("Color", true, SpookyChests.color, "The color of the spooky chest highlight.")
                        )))
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
                                new Settings.Keybind("Confirm Previous", FusionKeybinds.confirm, "The keybind to confirm the previous fusion."),
                                new Settings.Keybind("Cancel Previous", FusionKeybinds.cancel, "The keybind to cancel the previous fusion.")
                        ))),
                        new Module("Lasso Alert", LassoAlert.instance, "Plays a sound effect once you can reel in with your lasso."),
                        new Module("Instant Fog", InstantFog.instance, "Makes the thick underwater fog disappear instantly.")
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
                                new Settings.ColorPicker("Healer Color", false, LeapOverlay.healer, "The color used for Healer on the overlay."),
                                new Settings.ColorPicker("Mage Color", false, LeapOverlay.mage, "The color used for Mage on the overlay."),
                                new Settings.ColorPicker("Bers Color", false, LeapOverlay.bers, "The color used for Berserker on the overlay."),
                                new Settings.ColorPicker("Arch Color", false, LeapOverlay.arch, "The color used for Archer on the overlay."),
                                new Settings.ColorPicker("Tank Color", false, LeapOverlay.tank, "The color used for Tank on the overlay.")
                        ))),
                        new Module("Terminal Solvers", TerminalSolvers.instance, "Solves terminals in F7/M7.", new Settings(List.of(
                                new Settings.Toggle("Announce Melody", TerminalSolvers.melody, "Sends a message once you get the torture terminal."),
                                new Settings.TextInput("Melody Message", TerminalSolvers.melodyMsg, "The message to send.")
                        ))),
                        new Module("Terracotta Timers", TerracottaTimer.instance, "Renders timers on screen and for every dead terracotta in F6/M6."),
                        new Module("Wither Dragons", WitherDragons.instance, "Features for the last phase of M7.", new Settings(List.of(
                                new Settings.Toggle("Spawn Alert", WitherDragons.alert, "Alerts you when a dragon is about to spawn.\nThis option also calculates the priority on the initial spawn based on your selected class."),
                                new Settings.SliderDouble("Split Power", 0, 32, 0.1, WitherDragons.power, "The required Power blessing level to consider a split possible.\nLeaving this option at 0 is recommended for party finder teams."),
                                new Settings.SliderDouble("Easy Power", 0, 32, 0.1, WitherDragons.powerEasy, "The required Power blessing level to consider a split possible, as long as one of the dragons is Purple."),
                                new Settings.Toggle("Dragon Glow", WitherDragons.glow, "Applies a glow effect to each dragon."),
                                new Settings.Toggle("Kill Areas", WitherDragons.boxes, "Renders the kill areas of every alive dragon."),
                                new Settings.Toggle("Stack Waypoints", WitherDragons.stack, "Renders waypoints for stacking your Last Breath arrows."),
                                new Settings.Dropdown<>("Waypoint Type", WitherDragons.stackType, "The type of the arrow stack waypoints."),
                                new Settings.Toggle("Spawn Timer", WitherDragons.timer, "Renders timers for exactly when a dragon should finish spawning."),
                                new Settings.Toggle("Dragon Health", WitherDragons.health, "Renders the exact health of the dragons.")
                        ))),
                        new Module("Secret Bat Highlight", SecretBatHighlight.instance, "Applies a glow effect to secret bats.", new Settings(List.of(
                                new Settings.ColorPicker("Color", false, SecretBatHighlight.color, "The color of the secret bat glow.")
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
                        new Module("Show Health", KuudraHealth.instance, "Shows Kuudra's exact health on screen.", new Settings(List.of(
                                new Settings.Toggle("Show DPS", KuudraHealth.dps, "Calculates your team's DPS. Only applies in the last phase of Infernal tier."),
                                new Settings.ColorPicker("Color", false, KuudraHealth.color, "The color of the text.")
                        ))),
                        new Module("Show Hitbox", KuudraHitbox.instance, "Renders a hitbox for Kuudra.", new Settings(List.of(
                                new Settings.Toggle("Through Walls", KuudraHitbox.walls, "Allows the hitbox to render through walls, because even YouTube ranks can get away with ESP in Kuudra."),
                                new Settings.ColorPicker("Color", true, KuudraHitbox.color, "The color of the hitbox.")
                        ))),
                        new Module("Waypoints", KuudraWaypoints.instance, "Renders various waypoints in Kuudra.", new Settings(List.of(
                                new Settings.Toggle("Supplies", KuudraWaypoints.supply, "Renders beacons for every supply crate."),
                                new Settings.Toggle("Drop-offs", KuudraWaypoints.drop, "Renders beacons for every available supply drop-off point."),
                                new Settings.Toggle("Build Piles", KuudraWaypoints.build, "Renders beacons for every unfinished Ballista build pile."),
                                new Settings.ColorPicker("Supply Color", true, KuudraWaypoints.supplyColor, "The color of the supply crate beacons."),
                                new Settings.ColorPicker("Drop-off Color", true, KuudraWaypoints.dropColor, "The color of the drop-off beacons."),
                                new Settings.ColorPicker("Piles Color", true, KuudraWaypoints.buildColor, "The color of the build pile beacons.")
                        ))),
                        new Module("Pre Message", PreMessage.instance, "Announces if no supply spawns at your pre spot (or your next pickup spot).")
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
                        new Module("Inferno Demonlord", InfernoDemonlord.instance, "Various features for Blaze slayer.", new Settings(List.of(
                                new Settings.Toggle("Pillar Alert", InfernoDemonlord.pillarAlert, "Displays information about your fire pillars.\nThis feature tries to ensure that you only get alerted for your own pillars, no false flags."),
                                new Settings.Toggle("Attunement Swap Fix", InfernoDemonlord.attunementFix, "Fixes your attunement changing twice after right clicking on a block."),
                                new Settings.Toggle("No Spam", InfernoDemonlord.noSpam, "Hides the chat messages about using the wrong attunement.")
                        ))),
                        new Module("Kill Timer", KillTimer.instance, "Tracks how long your slayer boss took to kill."),
                        new Module("Riftstalker Bloodfiend", RiftstalkerBloodfiend.instance, "Various features for Vampire slayer.", new Settings(List.of(
                                new Settings.Toggle("Ice Indicator", RiftstalkerBloodfiend.ice, "Shows when to use your Holy Ice against Twinclaws."),
                                new Settings.Toggle("Steak Indicator", RiftstalkerBloodfiend.steak, "Shows text on screen once you can vanquish your boss with the Steak Stake."),
                                new Settings.Toggle("Ichor Highlight", RiftstalkerBloodfiend.ichor, "Highlights the Blood Ichors during the T5 fight."),
                                new Settings.ColorPicker("Ichor Color", true, RiftstalkerBloodfiend.ichorColor, "The color of the Blood Ichor highlight."),
                                new Settings.Toggle("Silence Mania", RiftstalkerBloodfiend.mania, "Removes the very loud Mania sound effects."),
                                new Settings.Toggle("Silence Killer Springs", RiftstalkerBloodfiend.springs, "Removes the sounds that play when your boss spawns a Killer Spring.")
                        ))),
                        new Module("Voidgloom Seraph", VoidgloomSeraph.instance, "Various features for Vampire slayer.", new Settings(List.of(
                                new Settings.Toggle("Hits Display", VoidgloomSeraph.hits, "Shows the amount of hits needed to break the hits shield of your boss.")
                        )))
                )),
                new Category("Mining", List.of(
                        new Module("Ability Alert", AbilityAlert.instance, "Alerts you when your mining ability is ready to be used again."),
                        new Module("Corpse Highlight", CorpseHighlight.instance, "Highlights corpses in the Glacite Mineshafts.", new Settings(List.of(
                                new Settings.ColorPicker("Lapis Color", false, CorpseHighlight.lapisColor, "The color of the Lapis corpse."),
                                new Settings.ColorPicker("Mineral Color", false, CorpseHighlight.mineralColor, "The color of the Mineral corpse."),
                                new Settings.ColorPicker("Yog Color", false, CorpseHighlight.yogColor, "The color of the Yog corpse."),
                                new Settings.ColorPicker("Vanguard Color", false, CorpseHighlight.vanguardColor, "The color of the Vanguard corpse.")
                        ))),
                        new Module("Better Sky Mall", BetterSkyMall.instance, "Compacts Sky Mall messages, and fully hides them if you are not mining.", new Settings(List.of(
                                new Settings.TextInput("Buff Whitelist", BetterSkyMall.whitelist, "Allows a buff message to always show if it contains a specific keyword.\nThe list is case insensitive, and separated by comma.")
                        ))),
                        new Module("Safe Pickobulus", SafePickobulus.instance, "Prevents you from being able to use Pickobulus on your private island and Garden."),
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
                        )))
                ))
        );
        for (Category category : this.categories) {
            parent.child(category);
        }
        SearchBox search = new SearchBox();
        search.input.onChanged().subscribe(value -> {
            if (value.isEmpty()) {
                for (Category category : this.categories) {
                    category.scroll.child().clearChildren();
                    for (Module module : category.features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            } else {
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
        parent.child(search);
        ScrollContainer<FlowLayout> scroll = Containers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent);
        this.mainScroll = scroll;
        root.child(scroll);
    }

    @Override
    public void close() {
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }
        super.close();
    }
}
