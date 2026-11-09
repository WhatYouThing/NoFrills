package nofrills.hud.clickgui;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import nofrills.features.dungeons.*;
import nofrills.features.dungeons.devicesolvers.ArrowAlignSolver;
import nofrills.features.dungeons.devicesolvers.DeviceSolvers;
import nofrills.features.dungeons.devicesolvers.SharpshooterSolver;
import nofrills.features.dungeons.devicesolvers.SimonSaysSolver;
import nofrills.features.farming.*;
import nofrills.features.fishing.RadarSolver;
import nofrills.features.fishing.RareAnnounce;
import nofrills.features.fishing.RareHighlight;
import nofrills.features.general.*;
import nofrills.features.general.inventorybuttons.InventoryButtons;
import nofrills.features.general.partycommands.PartyCommands;
import nofrills.features.hunting.*;
import nofrills.features.kuudra.*;
import nofrills.features.mining.*;
import nofrills.features.misc.*;
import nofrills.features.slayer.*;
import nofrills.features.solvers.*;
import nofrills.features.tweaks.*;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.settings.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public static final AtomicReference<Category> lastCategory = new AtomicReference<>();
    public List<Category> categories;
    public ScrollContainer<FlowLayout> categoriesParent;
    public ScrollContainer<FlowLayout> modulesParent;
    public FlatTextbox searchBox;
    public int mouseX = 0;
    public int mouseY = 0;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int height = context.guiHeight();
        context.text(this.font, "Left click a feature to toggle", 1, height - 30, RenderColor.WHITE.argb());
        context.text(this.font, "Right click a feature open its settings", 1, height - 20, RenderColor.WHITE.argb());
        context.text(this.font, "Scrolling supported in each category and the screen itself", 1, height - 10, RenderColor.WHITE.argb());
    }

    @Override
    protected void build(FlowLayout root) {
        this.categories = Lists.newArrayList(
                new Category("General", List.of(
                        new Module("Auto Sprint", AutoSprint.instance, "Essentially Toggle Sprint, but always active.", new SettingsScreen(List.of(
                                new Toggle("Water Check", AutoSprint.waterCheck, "Prevents Auto Sprint from working while you are in water.")
                        ))),
                        new Module("Slot Binding", SlotBinding.instance, "Bind your hotbar slots to your inventory slots, similarly to NEU's slot binding.", SlotBinding.buildSettings()),
                        new Module("Price Tooltips", PriceTooltips.instance, "Adds pricing information to item tooltips. Requires connectivity to the NoFrills API.", new SettingsScreen(List.of(
                                new Toggle("Lowest BIN", PriceTooltips.auction, "Adds the Lowest BIN price to applicable items."),
                                new Toggle("Bazaar", PriceTooltips.bazaar, "Adds the Bazaar insta-buy and insta-sell prices to applicable items."),
                                new Toggle("NPC Sell", PriceTooltips.npc, "Adds the NPC sell price to applicable items."),
                                new Toggle("Motes Sell", PriceTooltips.mote, "Adds the Motes sell price to applicable items."),
                                new SliderInt("Burger Count", 0, 5, 1, PriceTooltips.burgers, "The amount of McGrubber's Burgers you've eaten, used to calculate the bonus Motes value."),
                                new Toggle("Price Paid", PriceTooltips.pricePaid, "Tracks and displays the amount you've paid for a specific item in a BIN auction."),
                                new Toggle("Craft Cost", PriceTooltips.craftCost, "Tries to calculate the lowest raw and/or insta-buy craft cost of an item.\nRecipes you view using /recipe will be cached automatically to provide craft costs.")
                        ))),
                        new Module("Wardrobe Keybinds", WardrobeKeybinds.instance, "Adds hotkeys to the Armor and the Equipment wardrobes.", new SettingsScreen(List.of(
                                new EnumToggle<>("Keybind Style", WardrobeKeybinds.style, "The style of keybinds you want to use.\n\nSimple: Uses the 1-9 keyboard keys.\nHotbar: Uses your hotbar slot keybinds from the Minecraft controls screen.\nCustom: Uses the custom keys which you can define below."),
                                new Toggle("No Unequip", WardrobeKeybinds.noUnequip, "Prevents you from being able to unequip your armor set with a keybind."),
                                new Toggle("Sound Effect", WardrobeKeybinds.playSound, "Plays a sound effect upon using a keybind."),
                                new SoundInput("Sound", WardrobeKeybinds.sound, "The sound to play on set switch."),
                                new Keybind("Custom Slot 1", WardrobeKeybinds.custom1, "Your custom keybind for the 1st wardrobe slot."),
                                new Keybind("Custom Slot 2", WardrobeKeybinds.custom2, "Your custom keybind for the 2nd wardrobe slot."),
                                new Keybind("Custom Slot 3", WardrobeKeybinds.custom3, "Your custom keybind for the 3rd wardrobe slot."),
                                new Keybind("Custom Slot 4", WardrobeKeybinds.custom4, "Your custom keybind for the 4th wardrobe slot."),
                                new Keybind("Custom Slot 5", WardrobeKeybinds.custom5, "Your custom keybind for the 5th wardrobe slot."),
                                new Keybind("Custom Slot 6", WardrobeKeybinds.custom6, "Your custom keybind for the 6th wardrobe slot."),
                                new Keybind("Custom Slot 7", WardrobeKeybinds.custom7, "Your custom keybind for the 7th wardrobe slot."),
                                new Keybind("Custom Slot 8", WardrobeKeybinds.custom8, "Your custom keybind for the 8th wardrobe slot."),
                                new Keybind("Custom Slot 9", WardrobeKeybinds.custom9, "Your custom keybind for the 9th wardrobe slot.")
                        ))),
                        new Module("Chat Waypoints", ChatWaypoints.instance, "Automatically creates waypoints for coordinates sent in Party/Global chat.", new SettingsScreen(List.of(
                                new Separator("Generic"),
                                new Toggle("Show Distance", ChatWaypoints.showDistance, "Shows distance to all waypoints."),
                                new Separator("Party Chat"),
                                new Toggle("Create Waypoints", ChatWaypoints.partyWaypoints, "Enables creating waypoints for coordinates sent by party members."),
                                new Toggle("Clear On Arrive", ChatWaypoints.partyClear, "Automatically removes the party waypoint once you get close."),
                                new SliderInt("Duration", 1, 600, 1, ChatWaypoints.partyDuration, "The duration (in seconds) that party waypoints should be rendered for."),
                                new ColorPicker("Color", ChatWaypoints.partyColor, "The color used for the party waypoints."),
                                new Separator("All Chat"),
                                new Toggle("Create Waypoints", ChatWaypoints.allWaypoints, "Enables creating waypoints for coordinates sent by players in the all chat."),
                                new Toggle("Clear On Arrive", ChatWaypoints.allClear, "Automatically remove the all chat waypoint once you get close."),
                                new SliderInt("Duration", 1, 600, 1, ChatWaypoints.allDuration, "The duration (in seconds) that all chat waypoints should be rendered for."),
                                new ColorPicker("Color", ChatWaypoints.allColor, "The color used for the all chat waypoints.")
                        ))),
                        new Module("Etherwarp Overlay", EtherwarpOverlay.instance, "Highlights the block you are targeting with the Ether Transmission ability.", new SettingsScreen(
                                new Separator("Sound"),
                                new Toggle("Warp Sound", EtherwarpOverlay.doSound, "Plays a custom sound effect upon teleporting."),
                                new Toggle("Pingless Sound", EtherwarpOverlay.pinglessSound, "If enabled, the custom sound will play as soon as you click.\nMakes teleporting more responsive on high ping, but may produce false positives."),
                                new SoundInput("Sound", EtherwarpOverlay.sound, "The sound to play upon teleporting."),
                                new Toggle("Cancel Sound", EtherwarpOverlay.cancelSound, "Prevents the intended Ether Transmission sound effect from playing."),
                                new Separator("Highlight"),
                                new EnumToggle<>("Highlight Style", EtherwarpOverlay.highlightStyle, "The style of the highlight"),
                                new ColorPicker("Correct Fill", EtherwarpOverlay.fillCorrect, "The fill color used when your Etherwarp target is considered valid."),
                                new ColorPicker("Wrong Fill", EtherwarpOverlay.fillWrong, "The fill color used when your Etherwarp target is considered invalid."),
                                new ColorPicker("Correct Outline", EtherwarpOverlay.outlineCorrect, "The outline color used when your Etherwarp target is considered valid."),
                                new ColorPicker("Wrong Outline", EtherwarpOverlay.outlineWrong, "The outline color used when your Etherwarp target is considered invalid.")
                        )),
                        new Module("Fullbright", Fullbright.instance, "You know him, you love him.", new SettingsScreen(List.of(
                                new EnumToggle<>("Mode", Fullbright.mode, "The lighting mode.\n\nAmbient: Increases dimension ambient light, most reliable.\nGamma: Increases the Minecraft brightness setting to a high value.\nPotion: Permanently applies the Night Vision potion effect to your player."),
                                new Toggle("No Effect", Fullbright.noEffect, "Removes the Night Vision effect while active. Ignored if you use the Potion mode.")
                        ))),
                        new Module("No Render", NoRender.instance, "Prevent various things from appearing.", new SettingsScreen(List.of(
                                new Toggle("Explosions", NoRender.explosions, "Prevents the server from spawning any explosion particles."),
                                new Toggle("Empty Tooltips", NoRender.emptyTooltips, "Disables slots that contain items with blank tooltips."),
                                new Toggle("Fire Overlay", NoRender.fireOverlay, "Removes the fire overlay."),
                                new Toggle("Break Particles", NoRender.breakParticles, "Removes the particles that appear when breaking blocks."),
                                new Toggle("Boss Bar", NoRender.bossBar, "Hides the boss health bar that appears at the top of the screen."),
                                new Toggle("Armor Bar", NoRender.armorBar, "Removes the armor bar from your hotbar."),
                                new Toggle("Food Bar", NoRender.foodBar, "Removes the food bar from your hotbar."),
                                new EnumToggle<>("Health Bar", NoRender.healthBar, "Where the health bar shouldn't be shown.\n\nNowhere: Don't hide the health bar.\nOutside rift: Hide the healthbar everywhere except the Rift.\nEverywhere: Hide the health bar in all areas."),
                                new Toggle("Fog", NoRender.fog, "Hides terrain and ambient fog."),
                                new Toggle("Effect Display", NoRender.effectDisplay, "Removes the potion effect display from the inventory and the top right of the screen."),
                                new Toggle("Recipe Book", NoRender.recipeBook, "Removes the recipe book button from the inventory."),
                                new Toggle("Selected Item Name", NoRender.selectedItemName, "Hides the selected item name text which appears above the hotbar."),
                                new Toggle("Dead Entities", NoRender.deadEntities, "Hides entities that are in their death animation, and their health bars (if applicable)."),
                                new Toggle("Dead Poof", NoRender.deadPoof, "Tries to hide the death \"poof\" particles that appear after a dead entity is deleted."),
                                new Toggle("Lightning", NoRender.lightning, "Hides lightning strikes."),
                                new Toggle("Falling Blocks", NoRender.fallingBlocks, "Hides falling block entities such as sand."),
                                new Toggle("Entity Fire", NoRender.entityFire, "Hides the fire effect that appears on burning mobs."),
                                new Toggle("Mage Beam", NoRender.mageBeam, "Prevents the server from spawning the Mage Beam particles in Dungeons."),
                                new Toggle("Ice Spray", NoRender.iceSpray, "Prevents the server from spawning the Ice Spray Wand particles."),
                                new Toggle("Powder Coating", NoRender.powderCoating, "Prevents the server from spawning Divan's Powder Coating particles on your armor."),
                                new Toggle("Soulweaver Skulls", NoRender.soulweaverSkulls, "Hides the floating skulls that appear while wearing the Soulweaver Gloves in Dungeons."),
                                new Toggle("Guided Sheep", NoRender.guidedSheep, "Hides the Guided Sheep Mage ability in Dungeons."),
                                new Toggle("Bone Plating", NoRender.bonePlating, "Hides the Bone Plating Archer ability (floating bone meal item) in Dungeons."),
                                new Toggle("Healer Fairy", NoRender.healerFairy, "Hides the Healer flying Fairy in Dungeons."),
                                new Toggle("Tree Bits", NoRender.treeBits, "Hides the flying wood and leaves blocks that appear when chopping trees on the Galatea."),
                                new Toggle("Cocoon Animation", NoRender.cocoonAnimation, "Hides the floating cocoon animation that appears in place of cocooned mobs."),
                                new Toggle("Nausea", NoRender.nausea, "Prevents the nausea screen wobble and/or green overlay from rendering."),
                                new EnumToggle<>("Vignette", NoRender.vignette, "The type of vignette overlay to hide.\n\nNone: Don't hide the vignette.\nAmbient: Hides the dark vignette that appears when in darkness.\nDanger: Hides the red (world border) vignette.\nBoth: Always hides the vignette."),
                                new Toggle("Exp Orbs", NoRender.expOrbs, "Prevents experience orbs from rendering."),
                                new Toggle("Stuck Arrows", NoRender.stuckArrows, "Prevents arrows stuck to entities from rendering.")
                        ))),
                        new Module("Party Commands", PartyCommands.instance, "Provide various commands to your party members.", new SettingsScreen(List.of(
                                new Description("Usage", "Run the \"/nf partyCommands\" command to see more information."),
                                new TextInput("Prefixes", PartyCommands.prefixes, "List of valid prefixes for these commands, separated by space."),
                                new Toggle("Self Commands", PartyCommands.self, "Allows you to trigger your own party commands and grants you whitelisted status, not recommended."),
                                new SliderInt("Grace Period", 0, 60, 1, PartyCommands.gracePeriod, "Prevents recent Party Finder joins from running commands for a specific amount of time, in seconds.\nSet to 0 to disable."),
                                new Toggle("Grace Auto Kick", PartyCommands.graceAutoKick, "Automatically kicks a player from the party if they attempt to run a command during the grace period."),
                                new EnumToggle<>("Warp", PartyCommands.warp, "Allows party members to warp themselves into your lobby on demand.\n\nCommands: !warp | !w"),
                                new EnumToggle<>("Party Transfer", PartyCommands.transfer, "Allows party members to promote themselves (or another player) to party leader on demand.\n\nCommands: !ptme | !pt"),
                                new EnumToggle<>("All Invite", PartyCommands.allinv, "Allows party members to toggle the All Invite party setting on demand.\n\nCommand: !allinv"),
                                new Toggle("Downtime", PartyCommands.downtime, "Allows party members to schedule a downtime reminder for the end of your Kuudra/Dungeons run.\n\nCommand: !dt"),
                                new EnumToggle<>("Instance Queue", PartyCommands.queue, Utils.format("Allows party members to queue for any instance on demand.\n\nCommand List: {}", PartyCommands.listInstancesFormatted())),
                                new EnumToggle<>("Coords", PartyCommands.coords, "Allows party members to get your coordinates on demand.\n\nCommand: !coords"),
                                new EnumToggle<>("Kick", PartyCommands.kick, "Allows party members to kick specific players on demand.\n\nCommands: !kick | !k")
                        ))),
                        new Module("Viewmodel", Viewmodel.instance, "Easily customize the appearance of your held item.", new SettingsScreen(List.of(
                                new Toggle("No Haste", Viewmodel.noHaste, "Prevents Haste and Mining Fatigue from affecting your swing speed."),
                                new Toggle("No Equip Animation", Viewmodel.noEquip, "Removes the item swapping animation."),
                                new Toggle("No Bow Swing", Viewmodel.noBowSwing, "Removes the swing animation for all bows."),
                                new Toggle("No Swing Reset", Viewmodel.noSwingReset, "Prevents the swing animation from getting cancelled mid-animation."),
                                new Toggle("Apply To Hand", Viewmodel.applyToHand, "Applies the viewmodel changes to the empty hand."),
                                new SliderInt("Swing Speed", 0, 100, 1, Viewmodel.speed, "Apply a custom swing speed. Set to 0 to disable."),
                                new SliderDouble("Offset X", -2, 2, 0.01, Viewmodel.offsetX, "The X axis offset position of your held item."),
                                new SliderDouble("Offset Y", -2, 2, 0.01, Viewmodel.offsetY, "The Y axis offset position of your held item."),
                                new SliderDouble("Offset Z", -2, 2, 0.01, Viewmodel.offsetZ, "The Z axis offset position of your held item."),
                                new SliderDouble("Scale X", 0, 5, 0.01, Viewmodel.scaleX, "The X axis scale of your held item."),
                                new SliderDouble("Scale Y", 0, 5, 0.01, Viewmodel.scaleY, "The Y axis scale of your held item."),
                                new SliderDouble("Scale Z", 0, 5, 0.01, Viewmodel.scaleZ, "The Z axis scale of your held item."),
                                new SliderDouble("Rotation X", -180, 180, 0.5, Viewmodel.rotX, "The X axis rotation of your held item."),
                                new SliderDouble("Rotation Y", -180, 180, 0.5, Viewmodel.rotY, "The Y axis rotation of your held item."),
                                new SliderDouble("Rotation Z", -180, 180, 0.5, Viewmodel.rotZ, "The Z axis rotation of your held item."),
                                new SliderDouble("Swing X", 0, 2, 0.01, Viewmodel.swingX, "The X multiplier for the swing animation offset."),
                                new SliderDouble("Swing Y", 0, 2, 0.01, Viewmodel.swingY, "The Y multiplier for the swing animation offset."),
                                new SliderDouble("Swing Z", 0, 2, 0.01, Viewmodel.swingZ, "The Z multiplier for the swing animation offset.")
                        ))),
                        new Module("Command Keybinds", CommandKeybinds.instance, "Create keybinds that run a custom command when pressed.", CommandKeybinds.buildSettings()),
                        new Module("Chat Rules", ChatRules.instance, "Create custom rules that activate when a matching message is sent in chat.", ChatRules.buildSettings()),
                        new Module("Chat Tweaks", ChatTweaks.instance, "Various features/improvements for the chat hud.", new SettingsScreen(List.of(
                                new Keybind("Copy Key", ChatTweaks.copyKey, "Copies the hovered message to clipboard when pressed."),
                                new Keybind("Copy Line Key", ChatTweaks.copyLineKey, "Copies the hovered line of a message to clipboard when pressed."),
                                new Toggle("Trim On Copy", ChatTweaks.trimOnCopy, "Trims copied chat messages to remove any leading/trailing space characters."),
                                new Toggle("Message On Copy", ChatTweaks.msgOnCopy, "Sends a feedback message in chat after copying any message."),
                                new SliderInt("Feedback Limit", 0, 512, 1, ChatTweaks.copyMsgLength, "The max length of the copied message within the feedback message.\nHelps to prevent the chat from filling up when copying large messages."),
                                new Toggle("Keep History", ChatTweaks.keepHistory, "Prevents the chat history from clearing on disconnect."),
                                new Toggle("Extra Lines", ChatTweaks.extraLines, "Overrides the chat line limit. Allows you to keep more messages in the chat history."),
                                new SliderInt("Lines", 100, 5000, 10, ChatTweaks.lines, "The chat line limit override.")
                        ))),
                        new Module("Item Protection", ItemProtection.instance, "Prevents you from accidentally dropping, selling or salvaging your important items.", new SettingsScreen(List.of(
                                new Keybind("UUID Protect Key", ItemProtection.uuidKey, "The keybind to protect a specific item by UUID."),
                                new Keybind("ID Protect Key", ItemProtection.skyblockIdKey, "The keybind to protect a specific item by Skyblock ID."),
                                new Keybind("Override Key", ItemProtection.overrideKey, "The keybind to override the item protection. Temporarily disables all protection rules while held."),
                                new Toggle("Override Sound", ItemProtection.playOverrideSound, "Whether to play sound when override is activated/deactivated."),
                                new Toggle("Protect By UUID", ItemProtection.protectUUID, "Protects items that you have protected by UUID."),
                                new Toggle("Protect By ID", ItemProtection.protectSkyblockId, "Protects items that you have protected by Skyblock ID."),
                                new Toggle("Protect Max Quality", ItemProtection.protectMaxQuality, "Protects Dungeon items with a rarity upgrade (50/50 quality)."),
                                new Toggle("Protect Starred", ItemProtection.protectStarred, "Protects any item with Dungeon stars on it."),
                                new Toggle("Protect Rarity Upgraded", ItemProtection.protectRarityUpgraded, "Protects any item that is Recombobulated."),
                                new Toggle("Protect By Value", ItemProtection.protectValue, "Protects any item that has a high enough NPC/Auction/Bazaar value."),
                                new DoubleInput("Minimum Value", ItemProtection.protectValueMin, "The minimum item value for an item to be protected by value."),
                                new Toggle("Hide Tooltip", ItemProtection.hideTooltip, "Hides the tooltip line about items being protected. Hold Left Shift to reveal the line."),
                                new Toggle("Draw Overlay", ItemProtection.drawOverlay, "Draws a visual indicator for protected items."),
                                new ColorPicker("Regular Tint", ItemProtection.regularOverlay, "Tint for regular protected items."),
                                new ColorPicker("Manual Tint", ItemProtection.manualOverlay, "Tint for manually protected items.")
                        ))),
                        new Module("Info Tooltips", InfoTooltips.instance, "Adds various information about an item to its tooltip.", new SettingsScreen(List.of(
                                new Toggle("Dungeon Quality", InfoTooltips.dungeonQuality, "Displays the quality and the floor tier on applicable dungeon items."),
                                new Toggle("Created Date", InfoTooltips.createdDate, "Displays the exact date at which an item was created."),
                                new Toggle("Hex Color", InfoTooltips.hexColor, "Displays the color of an item. Only applies to armor pieces that use leather armor as their base."),
                                new Toggle("Museum Donated", InfoTooltips.museum, "Displays the Museum category of the item (if applicable)."),
                                new Toggle("Item ID", InfoTooltips.skyblockId, "Displays the Skyblock ID of an item.\nNote: Items with generic IDs (pets, potions etc.) will display a dynamically generated ID.")
                        ))),
                        new Module("Skill Tracker", SkillTracker.instance, "Tracks the experience you gain for specific skills, similarly to SBE on 1.8.9.\nThis feature will be inaccurate if your tracked skills are not maxed (Catacombs excluded).", SkillTracker.buildSettings()),
                        new Module("Command Shortcuts", CommandShortcuts.instance, "Create shortcuts which send a specific message/command when ran.\nNote: A lobby change is required to fully apply the changes made to the shortcuts.", CommandShortcuts.buildSettings()),
                        new Module("Loadout Keybinds", LoadoutKeybinds.instance, "Allows you to set custom keybinds for loadouts.", new SettingsScreen(List.of(
                                new Description("Usage", "Press the Edit Bind key while hovering over a loadout in the Loadouts menu."),
                                new Keybind("Edit Bind Key", LoadoutKeybinds.editBindKey, "The key to edit your keybinds. Press while hovering over a loadout slot to use."),
                                new Toggle("Play Sound", LoadoutKeybinds.playSound, "Plays a sound effect upon successfully switching loadout."),
                                new SoundInput("Sound", LoadoutKeybinds.sound, "The sound to play on loadout switch.")
                        ))),
                        new Module("Slot Locking", SlotLocking.instance, "Allows you to lock inventory slots to prevent accidental clicks.", new SettingsScreen(List.of(
                                new Keybind("Lock Key", SlotLocking.keybind, "The key to lock/unlock a slot."),
                                new Toggle("Draw Overlay", SlotLocking.overlay, "Draw a colored overlay on top of slots that are locked."),
                                new ColorPicker("Overlay Color", SlotLocking.color, "The color of the locked overlay")
                        ))),
                        new Module("Inventory Buttons", InventoryButtons.instance, "Allows you to add custom command buttons to container screens.", new SettingsScreen(
                                new Keybind("Manage Key", InventoryButtons.manageKey, "The key to manage your inventory buttons.\n\nWhen hovered over an existing inventory button: Locks/unlocks button position and scale.\nWhen hovered over a container slot: Sends button in chat to copy specific item properties.\nWhen not hovered over anything: Creates a new button at the cursor's position."),
                                new SliderInt("Grid Precision", 1, 20, 1, InventoryButtons.gridPrecision, "The precision of the grid snapping calculation while dragging buttons around.")
                        ))
                )),
                new Category("Tweaks", List.of(
                        new Module("No Loading Screen", NoLoadingScreen.instance, "Fully removes the loading terrain screen that appears when switching islands.", new SettingsScreen(List.of(
                                new Toggle("Server Only", NoLoadingScreen.serverOnly, "Only prevent loading screens while connected to a third party server.")
                        ))),
                        new Module("Middle Click Override", MiddleClickOverride.instance, "Replaces left clicks with middle clicks in applicable GUIs, making navigation smoother.", new SettingsScreen(
                                new Toggle("Debug", MiddleClickOverride.debug, "Outputs a chat message once a click is successfully replaced.")
                        )),
                        new Module("No Front Perspective", NoFrontPerspective.instance, "Removes the front facing camera perspective."),
                        new Module("Item Count Fix", ItemCountFix.instance, "Prevents the game from hiding item counts for unstackable items."),
                        new Module("Middle Click Fix", MiddleClickFix.instance, "Allows the middle mouse button to work just as it does on 1.8.9."),
                        new Module("No Pearl Cooldown", NoPearlCooldown.instance, "Removes the visual cooldown from Ender Pearls.", new SettingsScreen(List.of(
                                new Toggle("Skyblock Only", NoPearlCooldown.skyblockCheck, "Prevent the feature from activating outside of Skyblock.")
                        ))),
                        new Module("Riding Camera Fix", RidingCameraFix.instance, "Removes the floaty camera movement effect while riding entities.", new SettingsScreen(List.of(
                                new Toggle("Skyblock Only", RidingCameraFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock.")
                        ))),
                        new Module("Animation Fix", AnimationFix.instance, "Fixes the sneaking/swimming animations being able to play twice.", new SettingsScreen(List.of(
                                new Toggle("Skyblock Only", AnimationFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock.")
                        ))),
                        new Module("Double Use Fix", DoubleUseFix.instance, "Fixes Blaze Daggers and Fishing Rods being able to activate twice at once.", new SettingsScreen(List.of(
                                new Toggle("Skyblock Only", DoubleUseFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock.")
                        ))),
                        new Module("Disconnect Fix", DisconnectFix.instance, "Fixes Tarantula slayer disconnecting you due to corrupted packets."),
                        new Module("No Confirm Screen", NoConfirmScreen.instance, "Removes the \"Confirm Command Execution\" screen that prevents you from running certain commands."),
                        new Module("No Cursor Reset", NoCursorReset.instance, "Retains your cursor position between container screens.", new SettingsScreen(List.of(
                                new SliderInt("Clear Time", 0, 1200, 5, NoCursorReset.clearTicks, "The amount of ticks until your last cursor position is forgotten. Set to 0 to always remember.")
                        ))),
                        new Module("Instant Sneak", InstantSneak.instance, "Removes the smooth sneaking/swimming animation."),
                        new Module("Eye Height Fix", EyeHeightFix.instance, "Visually reverts the sneaking eye height on islands without modern version support.", new SettingsScreen(
                                new Toggle("Skyblock Only", EyeHeightFix.skyblockCheck, "Prevent the feature from activating outside of Skyblock.")
                        )),
                        new Module("No Ghost Place", NoGhostPlace.instance, "Prevents ghost blocks from appearing when placing non-placeable Skyblock block items."),
                        new Module("Legacy Textures", LegacyTextures.instance, "Replaces the models of most items added before Skyblock 0.26 with their old models.", new SettingsScreen(
                                new Toggle("Revert Items", LegacyTextures.revertItems, "Allows you to disable the item model revert functionality of this feature."),
                                new Toggle("Unlock Pack Position", LegacyTextures.unlockPackPos, "Allows you to move the official resource pack up and down on the list."),
                                new Toggle("Force Pack Position", LegacyTextures.forcePackPos, "Forces the official resource pack to be at the bottom by default.\nMay require a game restart to apply."),
                                new Toggle("No Tooltip Styling", LegacyTextures.noTooltipStyle, "Disables custom styling of tooltips. Reverts item tooltips if the official pack is below the vanilla pack."),
                                new Toggle("No Bow Cooldown", LegacyTextures.noBowCooldown, "Prevents the cooldown overlay from rendering on bows/shortbows."),
                                new Toggle("More Legacy", LegacyTextures.moreLegacy, "Reverts several items which used to be axes (before the Foraging Update) into axes."),
                                new Toggle("Master Stars", LegacyTextures.masterStars, "Reverts the visual feedback of Master Stars applied to items.")
                        ))
                )),
                new Category("Misc", List.of(
                        new Module("Tooltip Scale", TooltipScale.instance, "Customize the scale of tooltips.", new SettingsScreen(List.of(
                                new EnumToggle<>("Mode", TooltipScale.mode, "The scaling mode.\n\nDynamic: Automatically scales down tooltips so that they always fit the screen.\nCustom: Scales tooltips using the Custom Scale value."),
                                new SliderDouble("Custom Scale", 0.0, 4.0, 0.01, TooltipScale.scale, "The custom scale multiplier. Ignored if using Dynamic mode.")
                        ))),
                        new Module("Recipe Lookup", RecipeLookup.instance, "Search up recipes for the hovered item with a keybind.", new SettingsScreen(List.of(
                                new EnumToggle<>("Mode", RecipeLookup.mode, "The command that is used to check normal items."),
                                new Keybind("Keybind", RecipeLookup.keybind, "The key that activates the feature.")
                        ))),
                        new Module("Update Checker", UpdateChecker.instance, "Checks if a NoFrills update is available the first time you join any world/server."),
                        new Module("Hotbar Swap", HotbarSwap.instance, "A simple alternative to slot binding with no configuration needed.", new SettingsScreen(List.of(
                                new Separator("Usage"),
                                new Description("Swapping", "Left ctrl + Left click on an item in your inventory to swap it with the hotbar slot directly below it."),
                                new Separator("Settings"),
                                new SliderInt("Last Override", 1, 9, 1, HotbarSwap.override, "Specify a replacement hotbar slot in cases where you swap with the 9th (unused) hotbar slot.")
                        ))),
                        new Module("Auto Requeue", AutoRequeue.instance, "Automatically starts a new Dungeons/Kuudra run once finished.", new SettingsScreen(List.of(
                                new SliderInt("Delay", 0, 400, 5, AutoRequeue.delay, "The delay (in ticks) until the new run is started.")
                        ))),
                        new Module("Party Finder", PartyFinder.instance, "Various features for your monkey finding adventures.", new SettingsScreen(List.of(
                                new Toggle("Buttons", PartyFinder.buttons, "Adds various buttons in chat whenever anyone joins your party, such as kick or copy name.")
                        ))),
                        new Module("Command Tooltip", CommandTooltip.instance, "Reveals the command that the hovered chat message would run when clicked."),
                        new Module("Auto Save", AutoSave.instance, "Automatically saves your settings after closing the settings/HUD editor screen."),
                        new Module("Unfocused Tweaks", UnfocusedTweaks.instance, "Various optimizations for when you are tabbed out of Minecraft.", new SettingsScreen(List.of(
                                new Toggle("Skip World Render", UnfocusedTweaks.noWorldRender, "Skips world rendering while unfocused which greatly reduces usage."),
                                new Toggle("Mute Sounds", UnfocusedTweaks.muteSounds, "Mutes the in-game sound while unfocused."),
                                new Toggle("No Vanilla Limit", UnfocusedTweaks.noVanilla, "Fully disables the vanilla \"Reduce FPS\" option."),
                                new SliderInt("FPS Limit", 0, 200, 1, UnfocusedTweaks.fpsLimit, "The max FPS the game will render at while unfocused. Set to 0 to disable.")
                        ))),
                        new Module("GUI Keybinds", GuiKeybinds.instance, "Adds navigation keybinds to applicable Skyblock GUIs.", new SettingsScreen(List.of(
                                new Keybind("Next Page", GuiKeybinds.next, "The keybind to go to the next page of the GUI."),
                                new Keybind("Previous Page", GuiKeybinds.previous, "The keybind to go to the previous page of the GUI."),
                                new Keybind("Scroll Up", GuiKeybinds.up, "The keybind to scroll up in the GUI."),
                                new Keybind("Scroll Down", GuiKeybinds.down, "The keybind to scroll down in the GUI."),
                                new Keybind("Go Back", GuiKeybinds.back, "The keybind to go back to the previous GUI.")
                        ))),
                        new Module("Force Nametag", ForceNametag.instance, "Makes player nametags always visible, even if they are invisible and/or sneaking.", new SettingsScreen(List.of(
                                new Toggle("Self Nametag", ForceNametag.self, "Forces your own player nametag to render. Only visible in the third person perspective.")
                        ))),
                        new Module("Hotbar Scroll Lock", HotbarScrollLock.instance, "Prevents your hotbar going from the first to the last slot when scrolling, and vice versa."),
                        new Module("Auto Tip", AutoTip.instance, "Automatically runs /tipall every 15 minutes while connected to Hypixel."),
                        new Module("Item Scale", ItemScale.instance, "Changes the scale of items laying on the ground.", new SettingsScreen(List.of(
                                new SliderDouble("Scale", 0.0, 10.0, 0.01, ItemScale.scale, "The scale multiplier.")
                        ))),
                        new Module("No Damage Splash", NoDamageSplash.instance, "Hides damage splash nametags.", new SettingsScreen(
                                new EnumToggle<>("Hide Mode", NoDamageSplash.mode, "Allows you to control when damage splash nametags should be hidden.\n\nAlways: always hides damage splashes.\nSlayerOnly: only hides damage splashes while your Slayer boss is alive.\nDungeonsOnly: only hides damage splashes while in Dungeons.\nBoth: combines the SlayerOnly and DungeonsOnly modes.")
                        )),
                        new Module("Block List", BlockList.instance, "Allows you to keep a client side player block list.", new SettingsScreen(
                                new Description("Usage", "You can manage this feature with /nf blockList."),
                                new Toggle("Auto Kick", BlockList.autoKick, "Automatically kicks blocked players if they join your party through party finder."),
                                new Toggle("Send Kick", BlockList.sendKick, "Sends a chat message when the auto kick fires to inform the party of the kick reason."),
                                new TextInput("Kick Message", BlockList.kickMsg, "The message to send.\n\nReplaces {name} with the blocked player's name.\nReplaces {reason} with the player's block reason."),
                                new Toggle("Join Alert", BlockList.joinAlert, "Notifies you when a blocked player is detected in your lobby.")
                        )),
                        new Module("Streamer Mode", StreamerMode.instance, "While nicked: replaces your nick with your real username, hides the lobby ID, and hides all player names.", new SettingsScreen(
                                new TextInput("Base Name", StreamerMode.baseName, "The name to use to hide the names of other players. Replaces placeholders \"{}\" with random digits."),
                                new TextInput("Current Nick", StreamerMode.currentNick, "This option stores your last known nick (or nothing when unnicked).")
                        )),
                        new Module("Sound Blocker", SoundBlocker.instance, "Prevents various sounds from playing.", new SettingsScreen(
                                new TextEntryList(
                                        "Custom Sounds",
                                        () -> SoundBlocker.data.value().has("custom")
                                                ? SoundBlocker.data.value().get("custom").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()
                                                : List.of(),
                                        "Blocks sounds which match any of the sound identifiers on this list.",
                                        l -> SoundBlocker.data.edit(obj -> obj.add("custom", Utils.toJsonArray(l))),
                                        SettingsScreen.soundSuggestionProvider
                                ),
                                new EnumToggle<>("Explosions", SoundBlocker.explosions, "Cancels explosion sound effects."),
                                new Toggle("Angry Enderman", SoundBlocker.angryEnderman, "Prevents the angry Enderman noises from being played by the client."),
                                new Toggle("Vampire Abilities", SoundBlocker.vampireAbilities, "Cancels the Vampire Slayer Mania and Killer Springs sound effects."),
                                new Toggle("Reindrake Gifts", SoundBlocker.reindrakeGifts, "Cancels the loud Totem of Undying sound effects that play when picking up Reindrake loot."),
                                new Toggle("Composter", SoundBlocker.composter, "Cancels the sounds that the running composter makes."),
                                new Toggle("Farming Tool Overflow", SoundBlocker.farmingToolOverflow, "Cancels the sound effect that plays when the EXP level of your farming tool overflows.")
                        ))
                )),
                new Category("Solvers", List.of(
                        new Module("Experiment Solver", ExperimentSolver.instance, "Solves the Experimentation Table mini-games and prevents wrong clicks.", new SettingsScreen(List.of(
                                new Toggle("Chronomatron", ExperimentSolver.chronomatron, "Reveals the solution in Chronomatron."),
                                new Toggle("Ultrasequencer", ExperimentSolver.ultrasequencer, "Reveals the solution in Ultrasequencer."),
                                new Toggle("Superpairs", ExperimentSolver.superpairs, "Reveals uncovered rewards in Superpairs and highlights matchable/matched pairs."),
                                new ColorPicker("Superpairs Matched", ExperimentSolver.superColorMatched, "The color of the matched pair highlight in Superpairs."),
                                new ColorPicker("Superpairs Matchable", ExperimentSolver.superColorMatch, "The color of the matchable pair highlight in Superpairs."),
                                new ColorPicker("Superpairs Powerup", ExperimentSolver.superColorPowerup, "The color of the powerup highlight in Superpairs.")
                        ))),
                        new Module("Calendar Date", CalendarDate.instance, "Calculates the exact starting dates of events in the calendar."),
                        new Module("Spooky Chest Highlight", SpookyChestHighlight.instance, "Highlights nearby trick or treat chests during the Spooky Festival.", new SettingsScreen(List.of(
                                new Toggle("Tracer", SpookyChestHighlight.tracer, "Draws tracers towards unopened trick or treat chests."),
                                new ColorPicker("Color", SpookyChestHighlight.color, "The color of the spooky chest highlight.")
                        ))),
                        new Module("Diana Solver", DianaSolver.instance, "Guesses Diana burrow positions when using your spade. Also highlights nearby burrows.", new SettingsScreen(List.of(
                                new Separator("Burrows"),
                                new Toggle("Guess Tracer", DianaSolver.guessTracer, "Draws a tracer towards the guessed burrow."),
                                new ColorPicker("Tracer Color", DianaSolver.guessTracerColor, "The color of the guessed burrow tracer."),
                                new ColorPicker("Guess Color", DianaSolver.guessColor, "The color of the guessed burrow beacon."),
                                new ColorPicker("Treasure Color", DianaSolver.treasureColor, "The color of the treasure burrow beacon."),
                                new ColorPicker("Enemy Color", DianaSolver.enemyColor, "The color of the enemy burrow beacon."),
                                new ColorPicker("Start Color", DianaSolver.startColor, "The color of the start burrow beacon."),
                                new Separator("Warps"),
                                new Keybind("Warp Keybind", DianaSolver.warpKey, "The keybind to warp to the location closest to the guessed burrow."),
                                new Toggle("Warp Message", DianaSolver.warpMsg, "Shows a message in chat with the location name if the closest warp is successful."),
                                new Toggle("Hub Warp", DianaSolver.hubToggle, "Consider Hub a valid warp location when using the Warp Keybind."),
                                new Toggle("Stonks Warp", DianaSolver.stonksToggle, "Consider Stonks Auction as a valid warp location when using the Warp Keybind."),
                                new Toggle("Museum Warp", DianaSolver.museumToggle, "Consider Museum as a valid warp location when using the Warp Keybind."),
                                new Toggle("Castle Warp", DianaSolver.castleToggle, "Consider Castle as a valid warp location when using the Warp Keybind."),
                                new Toggle("Wizard Tower Warp", DianaSolver.wizardToggle, "Consider Wizard Tower as a valid warp location when using the Warp Keybind."),
                                new Toggle("Dark Auction Warp", DianaSolver.daToggle, "Consider Dark Auction as a valid warp location when using the Warp Keybind."),
                                new Toggle("Crypt Warp", DianaSolver.cryptToggle, "Consider Crypt as a valid warp location when using the Warp Keybind.")
                        ))),
                        new Module("Hoppity Solver", HoppitySolver.instance, "Guesses Hoppity egg positions when using your Egglocator.", new SettingsScreen(List.of(
                                new Toggle("Tracer", HoppitySolver.tracer, "Draws a tracer towards the guess."),
                                new ColorPicker("Color", HoppitySolver.color, "The color of the guess highlight."),
                                new ColorPicker("Tracer Color", HoppitySolver.tracerColor, "The color of the guess tracer.")
                        ))),
                        new Module("Foraging Beacon", BeaconTuningSolver.instance, "Solves the beacon tuning mini-game on foraging islands."),
                        new Module("Anvil Helper", AnvilHelper.instance, "Highlights the enchanted books which you can safely combine while using the anvil.", new SettingsScreen(List.of(
                                new ColorPicker("Color", AnvilHelper.color, "The color of the highlight.")
                        ))),
                        new Module("Active Pet Highlight", ActivePetHighlight.instance, "Highlights your selected pet while in the pets menu.", new SettingsScreen(
                                new ColorPicker("Color", ActivePetHighlight.color, "The color of the highlight.")
                        ))
                )),
                new Category("Fishing", List.of(
                        new Module("Rare Highlight", RareHighlight.instance, "Highlights rare/profitable sea creatures.", new SettingsScreen(List.of(
                                new EnumToggle<>("Highlight Style", RareHighlight.style, "The style of the highlight."),
                                new ColorPicker("Outline Color", RareHighlight.outlineColor, "The color of the outline style highlight."),
                                new ColorPicker("Fill Color", RareHighlight.fillColor, "The color of the fill style highlight.")
                        ))),
                        new Module("Rare Alert", RareAnnounce.instance, "Alerts you, and/or your party when you catch a rare sea creature.", new SettingsScreen(List.of(
                                new Toggle("Show Title", RareAnnounce.title, "Shows a title on screen with the name of the sea creature."),
                                new Toggle("Play Sound", RareAnnounce.sound, "Plays a sound effect once you catch a rare sea creature."),
                                new Toggle("Replace Message", RareAnnounce.replace, "Replaces the catch message of rare sea creatures with colored versions."),
                                new Toggle("Send Message", RareAnnounce.sendMsg, "Sends a specific message once you catch a rare sea creature."),
                                new TextInput("Message", RareAnnounce.msg, "The message to send. Replaces {spawnmsg} with the catch message, and {name} with the sea creature name.")
                        ))),
                        new Module("Radar Solver", RadarSolver.instance, "Guesses Fishing Hotspot positions when using your Hotspot Radar.", new SettingsScreen(List.of(
                                new Toggle("Tracer", RadarSolver.tracer, "Draws a tracer towards the guess."),
                                new ColorPicker("Color", RadarSolver.color, "The color of the guess highlight."),
                                new ColorPicker("Tracer Color", RadarSolver.tracerColor, "The color of the guess tracer.")
                        )))
                )),
                new Category("Hunting", List.of(
                        new Module("Invisibug Highlight", InvisibugHighlight.instance, "Highlights nearby Invisibugs on the Moonglade Marsh.", new SettingsScreen(List.of(
                                new ColorPicker("Color", InvisibugHighlight.color, "The color of the Invisibug highlight.")
                        ))),
                        new Module("Cinderbat Highlight", CinderbatHighlight.instance, "Highlights the annoying bats on the Crimson Isle.", new SettingsScreen(List.of(
                                new ColorPicker("Color", CinderbatHighlight.color, "The color of the Cinderbat highlight.")
                        ))),
                        new Module("Fusion Keybinds", FusionKeybinds.instance, "Adds handy keybinds to the Fusion Machine.", new SettingsScreen(List.of(
                                new Keybind("Repeat Previous", FusionKeybinds.repeat, "The keybind to repeat the previous fusion."),
                                new Keybind("Confirm Fusion", FusionKeybinds.confirm, "The keybind to confirm a fusion."),
                                new Keybind("Cancel Fusion", FusionKeybinds.cancel, "The keybind to cancel a fusion.")
                        ))),
                        new Module("Lasso Alert", LassoAlert.instance, "Plays a sound effect once you can reel in with your lasso."),
                        new Module("Instant Fog", InstantFog.instance, "Makes the thick underwater fog disappear instantly."),
                        new Module("Shard Tracker", ShardTracker.instance, "Tracks obtained shards for you and displays information with a HUD element.", ShardTracker.buildSettings()),
                        new Module("Huntaxe Lock", HuntaxeLock.instance, "Requires you to double right click with your Huntaxe to be able to open the GUI."),
                        new Module("Honeycomb Timer", HoneycombTimer.instance, "Adds timers, beacons, and chat reminders for the Pots of Honeycomb you use on protected trees.", new SettingsScreen(List.of(
                                new ColorPicker("Color", HoneycombTimer.color, "The color of the honeycomb beacon highlight."),
                                new SliderDouble("Text Scale", 0.0, 1.0, 0.01, HoneycombTimer.scale, "The scale of the honeycomb timer text.")
                        )))
                )),
                new Category("Dungeons", List.of(
                        new Module("Device Solvers", DeviceSolvers.instance, "Solvers for the F7/M7 devices.", new SettingsScreen(List.of(
                                new Separator("Simon Says"),
                                new Toggle("Solve Simon Says", DeviceSolvers.simonSays, "Shows the buttons you must click while doing the 1st device."),
                                new Toggle("Block Wrong Clicks", SimonSaysSolver.blockWrong, "Prevents you from being able to click the wrong button."),
                                new Toggle("Inverted Block", SimonSaysSolver.blockInvert, "Prevents wrong clicks while sneaking instead of while standing up."),
                                new SliderInt("Throttle Ticks", 0, 10, 1, SimonSaysSolver.throttleTicks, "Allows only a single click to go through until the specified amount of server ticks passes.\nHelps prevent the device from resetting if the server is lagging."),
                                new Toggle("Send Progress", SimonSaysSolver.progressSend, "Sends messages when you make progress in the device, similarly to Melody Message."),
                                new TextInput("Message", SimonSaysSolver.progressMsg, "The message to send when progress is made. Replaces {percent} with the percentage."),
                                new TextInput("Reset Message", SimonSaysSolver.resetMsg, "The message to send when the device is reset."),
                                new EnumToggle<>("Render Style", SimonSaysSolver.style, "The style of the button highlights."),
                                new ColorPicker("First Fill Color", SimonSaysSolver.firstColorFill, "The fill color of the first button to click."),
                                new ColorPicker("First Outline Color", SimonSaysSolver.firstColorOutline, "The outline color of the first button to click."),
                                new ColorPicker("Second Fill Color", SimonSaysSolver.secondColorFill, "The fill color of the second button to click."),
                                new ColorPicker("Second Outline Color", SimonSaysSolver.secondColorOutline, "The outline color of the second button to click."),
                                new ColorPicker("Third Fill Color", SimonSaysSolver.thirdColorFill, "The fill color of the third button to click."),
                                new ColorPicker("Third Outline Color", SimonSaysSolver.thirdColorOutline, "The outline color of the third button to click."),
                                new Separator("Arrow Align"),
                                new Toggle("Solve Arrow Align", DeviceSolvers.arrowAlign, "Shows the amount of needed clicks while doing the 3rd device."),
                                new Toggle("Block Wrong Clicks", ArrowAlignSolver.blockWrong, "Prevents you from rotating the arrows that already have the correct rotation."),
                                new Toggle("Inverted Block", ArrowAlignSolver.blockInvert, "Prevents wrong rotations while sneaking instead of while standing up."),
                                new Separator("Sharpshooter"),
                                new Toggle("Solve Sharpshooter", DeviceSolvers.sharpshooter, "Highlights the active and the hit targets while doing the 4th device."),
                                new Toggle("Done Alert", SharpshooterSolver.doneAlert, "Displays a title on screen once the device is completed. Useful for Pre4."),
                                new EnumToggle<>("Render Style", SharpshooterSolver.style, "The style of the target highlights."),
                                new ColorPicker("Target Fill Color", SharpshooterSolver.targetColorFill, "The fill color of the active target block."),
                                new ColorPicker("Target Outline Color", SharpshooterSolver.targetColorOutline, "The outline color of the active target block."),
                                new ColorPicker("Hit Fill Color", SharpshooterSolver.hitColorFill, "The fill color of the hit target blocks."),
                                new ColorPicker("Hit Outline Color", SharpshooterSolver.hitColorOutline, "The outline color of the hit target blocks.")
                        ))),
                        new Module("Starred Mob Highlight", StarredMobHighlight.instance, "High performance starred mob highlights.", new SettingsScreen(List.of(
                                new EnumToggle<>("Highlight Style", StarredMobHighlight.style, "The style of the starred mob highlight."),
                                new ColorPicker("Outline Color", StarredMobHighlight.outlineColor, "The color of the outline style highlight."),
                                new ColorPicker("Fill Color", StarredMobHighlight.fillColor, "The color of the fill style highlight.")
                        ))),
                        new Module("Miniboss Highlight", MinibossHighlight.instance, "Highlights minibosses.", new SettingsScreen(List.of(
                                new EnumToggle<>("Highlight Style", MinibossHighlight.style, "The style of the miniboss highlight."),
                                new ColorPicker("Outline Color", MinibossHighlight.outlineColor, "The color of the outline style highlight."),
                                new ColorPicker("Fill Color", MinibossHighlight.fillColor, "The color of the fill style highlight.")
                        ))),
                        new Module("Key Alert", KeyAlert.instance, "Alerts you when a Wither/Blood key is dropped/obtained.", new SettingsScreen(List.of(
                                new TitleInput("Dropped Title", KeyAlert.droppedTitle, "The title to display once a key is dropped. Replaces {key} with the colored name of the key."),
                                new SoundInput("Dropped Sound", KeyAlert.droppedSound, "The sound to play once a key is dropped."),
                                new TitleInput("Obtained Title", KeyAlert.obtainedTitle, "The title to display once a key is obtained. Replaces {key} with the colored name of the key."),
                                new SoundInput("Obtained Sound", KeyAlert.obtainedSound, "The sound to play once a key is obtained."),
                                new Toggle("Pickup Radius", KeyAlert.radius, "Renders a pickup radius circle around dungeon door keys."),
                                new ColorPicker("Radius Color", KeyAlert.radiusColor, "The color of the pickup radius.")
                        ))),
                        new Module("Spirit Bow Highlight", SpiritBowHighlight.instance, "Highlights the Spirit Bow in the F4/M4 boss fight.", new SettingsScreen(List.of(
                                new ColorPicker("Color", SpiritBowHighlight.color, "The color of the Spirit Bow highlight.")
                        ))),
                        new Module("Leap Overlay", LeapOverlay.instance, "Renders a custom overlay in place of the Spirit Leap menu.", new SettingsScreen(List.of(
                                new SliderDouble("Text Scale", 1.0, 4.0, 1.0, LeapOverlay.scale, "The scale of the text on the overlay."),
                                new Keybind("Leap First Key", LeapOverlay.firstKey, "The keybind to leap to the first player on the overlay (left -> right order)."),
                                new Keybind("Leap Second Key", LeapOverlay.secondKey, "The keybind to leap to the second player on the overlay (left -> right order)."),
                                new Keybind("Leap Third Key", LeapOverlay.thirdKey, "The keybind to leap to the third player on the overlay (left -> right order)."),
                                new Keybind("Leap Fourth Key", LeapOverlay.fourthKey, "The keybind to leap to the fourth player on the overlay (left -> right order).")
                        ))),
                        new Module("Terminal Solvers", TerminalSolvers.instance, "Solves terminals and prevents wrong clicks in F7/M7. Also hides item tooltips in every terminal.", new SettingsScreen(List.of(
                                new Toggle("Solve Panes", TerminalSolvers.panes, "Solves the \"Correct all panes\" terminal."),
                                new ColorPicker("Panes Color", TerminalSolvers.panesColor, "The button color used in the Panes terminal."),
                                new Toggle("Solve In Order", TerminalSolvers.inOrder, "Solves the \"Click in order\" Among Us task."),
                                new Toggle("In Order Numbers", TerminalSolvers.inOrderDrawNumbers, "Draws the 1-14 numbers on top of the In Order solution."),
                                new ColorPicker("In Order First Color", TerminalSolvers.inOrderColorFirst, "The first button color used in the In Order terminal."),
                                new ColorPicker("In Order Second Color", TerminalSolvers.inOrderColorSecond, "The second button color used in the In Order terminal."),
                                new ColorPicker("In Order Third Color", TerminalSolvers.inOrderColorThird, "The third button color used in the In Order terminal."),
                                new Toggle("Solve Starts With", TerminalSolvers.startsWith, "Solves the \"What starts with\" terminal."),
                                new ColorPicker("Starts With Color", TerminalSolvers.startsWithColor, "The button color used in the Starts With terminal."),
                                new Toggle("Solve Select", TerminalSolvers.select, "Solves the \"Select all\" terminal."),
                                new ColorPicker("Select Color", TerminalSolvers.selectColor, "The button color used in the Select terminal."),
                                new Toggle("Solve Colors", TerminalSolvers.colors, "Solves the \"Change all to same color\" terminal."),
                                new ColorPicker("Colors First Color", TerminalSolvers.colorsColorFirst, "The left click button color used in the Colors terminal."),
                                new ColorPicker("Colors Second Color", TerminalSolvers.colorsColorSecond, "The right click button color used in the Colors terminal."),
                                new SliderInt("First Click Ticks", 0, 20, 1, TerminalSolvers.firstClickTicks, "Blocks the first click in a terminal for the specified amount of server ticks.\nHelps prevent false flagging Watchdog if the server is lagging."),
                                new SliderInt("Throttle Ticks", 0, 10, 1, TerminalSolvers.throttleTicks, "Allows only a single click to go through until the specified amount of server ticks passes.\nHelps prevent your clicks from being rate limited if the server is lagging."),
                                new Toggle("Sound On Click", TerminalSolvers.soundOnClick, "Plays a sound effect upon clicking a terminal slot."),
                                new SoundInput("Click Sound", TerminalSolvers.clickSound, "The sound to play on click."),
                                new ColorPicker("Background Color", TerminalSolvers.backgroundColor, "The color used to depict already clicked/solved terminal slots.")
                        ))),
                        new Module("Terracotta Timers", TerracottaTimer.instance, "Renders respawn timers for the dead terracottas in F6/M6.", new SettingsScreen(List.of(
                                new SliderDouble("Text Scale", 0.0, 1.0, 0.01, TerracottaTimer.scale, "The scale of the timer text.")
                        ))),
                        new Module("Wither Dragons", WitherDragons.instance, "Features for the last phase of M7.", new SettingsScreen(List.of(
                                new Toggle("Spawn Alert", WitherDragons.alert, "Alerts you when a dragon is about to spawn."),
                                new Toggle("Kill Areas", WitherDragons.boxes, "Renders the kill areas of every alive dragon."),
                                new Toggle("Hitboxes", WitherDragons.hitboxes, "Renders the hitbox parts of every alive dragon."),
                                new Toggle("Tracers", WitherDragons.tracers, "Draws tracer lines to spawning dragons."),
                                new EnumToggle<>("Spawn Waypoints", WitherDragons.waypoints, "Renders waypoints for the dragon spawn positions.\n\nSimple: Highlights the exact spawn position of a spawning dragon.\nAdvanced: Highlights each individual hitbox of a spawning dragon."),
                                new Toggle("Spawn Timer", WitherDragons.timer, "Renders timers for when a dragon should finish spawning."),
                                new Toggle("Dragon Health", WitherDragons.health, "Renders the exact health of the dragons."),
                                new Toggle("Track Ice Spray", WitherDragons.trackIceSpray, "Tracks how many ticks it takes for a dragon to get ice sprayed."),
                                new Toggle("Track Arrow Hits", WitherDragons.trackArrowHits, "Tracks how many arrows each player hits on a dragon.\nWarning: Experimental option, likely will produce inaccurate results.")
                        ))),
                        new Module("Secret Bat Highlight", SecretBatHighlight.instance, "Highlights secret bats.", new SettingsScreen(List.of(
                                new EnumToggle<>("Highlight Style", SecretBatHighlight.style, "The style of the highlight."),
                                new ColorPicker("Outline Color", SecretBatHighlight.outlineColor, "The color of the outline style highlight."),
                                new ColorPicker("Fill Color", SecretBatHighlight.fillColor, "The color of the fill style highlight.")
                        ))),
                        new Module("Livid Solver", LividSolver.instance, "Finds and highlights the correct Livid in F5/M5.", new SettingsScreen(List.of(
                                new Toggle("Title", LividSolver.title, "Show a title on screen once the Livid color is identified."),
                                new Toggle("Highlight", LividSolver.highlight, "Renders an outline for the correct Livid."),
                                new Toggle("Tracer", LividSolver.tracer, "Renders a tracer towards the correct Livid."),
                                new EnumToggle<>("Highlight Style", LividSolver.style, "The style of the correct Livid highlight."),
                                new ColorPicker("Outline Color", LividSolver.outlineColor, "The color of the outline style highlight."),
                                new ColorPicker("Fill Color", LividSolver.fillColor, "The color of the fill style highlight."),
                                new ColorPicker("Tracer Color", LividSolver.tracerColor, "The color of the tracer.")
                        ))),
                        new Module("Shard Score Message", BonusScoreMessage.instance, "Sends a message when you gain bonus score from an attribute.", new SettingsScreen(List.of(
                                new Toggle("Send Prince", BonusScoreMessage.sendPrince, "Whether to send the Prince message."),
                                new TextInput("Prince Message", BonusScoreMessage.msgPrince, "The message to send when a Prince is killed."),
                                new Toggle("Send Bat", BonusScoreMessage.sendBat, "Whether to send the Bat message."),
                                new TextInput("Bat Message", BonusScoreMessage.msgBat, "The message to send when a Bat is killed.")
                        ))),
                        new Module("Mimic Message", MimicMessage.instance, "Sends a message once you kill the Mimic. Should work even if it's instantly killed.", new SettingsScreen(List.of(
                                new TextInput("Message", MimicMessage.msg, "The message to send.")
                        ))),
                        new Module("Secret Chime", SecretChime.instance, "Plays sounds upon collecting specific secrets.", new SettingsScreen(List.of(
                                new Toggle("Items", SecretChime.itemsToggle, "Play a chime upon picking up a secret item."),
                                new SoundInput("Items Sound", SecretChime.itemsSound, "The sound to play upon picking up a secret item."),
                                new Toggle("Chests", SecretChime.chestToggle, "Play a chime upon opening a secret chest."),
                                new SoundInput("Chests Sound", SecretChime.chestSound, "The sound to play upon opening a secret chest."),
                                new Toggle("Essence", SecretChime.essenceToggle, "Play a chime upon collecting a Wither Essence secret."),
                                new SoundInput("Essence Sound", SecretChime.essenceSound, "The sound to play upon collecting a Wither Essence secret."),
                                new Toggle("Bats", SecretChime.batToggle, "Play a chime upon killing a secret bat."),
                                new SoundInput("Bats Sound", SecretChime.batSound, "The sound to play upon killing a secret bat."),
                                new Toggle("Levers", SecretChime.leverToggle, "Play a chime upon interacting with a lever."),
                                new SoundInput("Levers Sound", SecretChime.leverSound, "The sound to play upon interacting with a lever.")
                        ))),
                        new Module("Melody Message", MelodyMessage.instance, "Send start and progress messages when you get the Melody terminal in F7/M7.", new SettingsScreen(List.of(
                                new TextInput("Message", MelodyMessage.msg, "The message to send when the terminal is opened."),
                                new Toggle("Send Progress", MelodyMessage.progress, "Send messages when you make progress in the terminal."),
                                new TextInput("% Message", MelodyMessage.progressMsg, "The message to send when you make progress.\nReplaces {percent} with your progress percentage (25%/50%/75%).")
                        ))),
                        new Module("Quick Close", QuickClose.instance, "Quickly close Dungeon secret and/or loot chests by pressing any of the movement keys (WASD)."),
                        new Module("Chest Value", DungeonChestValue.instance, "Calculates the value of your Dungeons loot. Requires connectivity to the NoFrills API.", new SettingsScreen(List.of(
                                new ColorPicker("Background", DungeonChestValue.background, "The color of the background of the value text.")
                        ))),
                        new Module("Relic Highlight", RelicHighlight.instance, "Highlights the correct placement position of your M7 king relic."),
                        new Module("Class Nametags", ClassNametags.instance, "Renders large nametags for your teammates, indicating their selected class and position.", new SettingsScreen(List.of(
                                new SliderDouble("Text Scale", 0.0, 1.0, 0.01, ClassNametags.scale, "The scale of the text.")
                        ))),
                        new Module("Score Calculator", ScoreCalculator.instance, "Calculates the score in your dungeon runs.", new SettingsScreen(List.of(
                                new EnumToggle<>("Paul State", ScoreCalculator.paulState, "Allows you to force the state of Paul's EZPZ perk.\nSet to Auto to automatically check for EZPZ through the NoFrills API."),
                                new Toggle("Send 270 Message", ScoreCalculator.sendMsg270, "If enabled, send a message upon reaching 270 score."),
                                new TextInput("270 Message", ScoreCalculator.msg270, "The message to send when 270 score is reached."),
                                new Toggle("Show 270 Title", ScoreCalculator.showTitle270, "If enabled, show a title on screen upon reaching 270 score."),
                                new TitleInput("270 Title", ScoreCalculator.title270, "The title to show when 270 score is reached."),
                                new SoundInput("270 Sound", ScoreCalculator.sound270, "The sound to play when the 270 title is shown."),
                                new Toggle("Send 300 Message", ScoreCalculator.sendMsg300, "If enabled, send a message upon reaching 300 score."),
                                new TextInput("300 Message", ScoreCalculator.msg300, "The message to send when 300 score is reached."),
                                new Toggle("Show 300 Title", ScoreCalculator.showTitle300, "If enabled, show a title on screen upon reaching 300 score."),
                                new TitleInput("300 Title", ScoreCalculator.title300, "The title to show when 300 score is reached."),
                                new SoundInput("300 Sound", ScoreCalculator.sound300, "The sound to play when the 300 title is shown.")
                        ))),
                        new Module("Platform Highlight", PlatformHighlight.instance, "Highlights the 3x3 area which you mine in Necron phase to stop the platform from breaking.", new SettingsScreen(
                                new Toggle("Healer Only", PlatformHighlight.healerOnly, "Whether to only draw the highlight while playing as Healer."),
                                new EnumToggle<>("Highlight Style", PlatformHighlight.style, "The style of the highlight"),
                                new ColorPicker("Outline Color", PlatformHighlight.outlineColor, "The color of the outline highlight."),
                                new ColorPicker("Fill Color", PlatformHighlight.fillColor, "The color of the filled highlight.")
                        )),
                        new Module("Croesus Solver", CroesusSolver.instance, "Highlights Dungeons loot in the Croesus/Vesuvius menu.\nRequires connectivity to the NoFrills API.", new SettingsScreen(
                                new ColorPicker("Profit Color", CroesusSolver.profitColor, "The highlight color for the most profitable chest."),
                                new ColorPicker("Secondary Profit Color", CroesusSolver.profitSecondaryColor, "The highlight color for the 2nd most profitable chest."),
                                new ColorPicker("Key Profit Color", CroesusSolver.profitKeyColor, "The highlight color for the 2nd most profitable chest if using a Dungeon Chest Key is worth it."),
                                new ColorPicker("High Profit Color", CroesusSolver.profitHighColor, "The highlight color for the most profitable chest if it exceeds the profit threshold.\nThis color is also applied regardless of profit if a chest contains a dye."),
                                new DoubleInput("Profit Threshold", CroesusSolver.profitHighThreshold, "The minimum profit for a chest to be considered as high value."),
                                new ColorPicker("Unopened Color", CroesusSolver.unopenedColor, "The highlight color for unopened loot."),
                                new ColorPicker("Rerolled Color", CroesusSolver.rerolledColor, "The highlight color for unopened loot that is already rerolled."),
                                new ColorPicker("Opened Color", CroesusSolver.openedColor, "The highlight color for opened loot."),
                                new ColorPicker("Key Opened Color", CroesusSolver.openedKeyColor, "The highlight color for fully opened loot using a Dungeon Chest Key."),
                                new Toggle("Value Tooltip", CroesusSolver.valueTooltip, "Displays the value of individual chests in the tooltip."),
                                new Toggle("Floor Label", CroesusSolver.floorLabel, "Adds labels which indicate the floor the loot was obtained on.")
                        )),
                        new Module("Dupe Class Alert", DupeClassAlert.instance, "Shows text on screen if your party has multiple of the same class before the dungeon starts."),
                        new Module("Leap Message", LeapMessage.instance, "Sends a message when you leap to another player.", new SettingsScreen(
                                new TextInput("Message", LeapMessage.message, "The message to send. Replaces {name} with the player's name.")
                        )),
                        new Module("Class Colors", ClassColors.instance, "Controls the colors used for the dungeon classes across various features.", new SettingsScreen(
                                new ColorPicker("Healer Color", ClassColors.heal, "The color used for Healer."),
                                new ColorPicker("Mage Color", ClassColors.mage, "The color used for Mage."),
                                new ColorPicker("Bers Color", ClassColors.bers, "The color used for Berserker."),
                                new ColorPicker("Arch Color", ClassColors.arch, "The color used for Archer."),
                                new ColorPicker("Tank Color", ClassColors.tank, "The color used for Tank.")
                        ))
                )),
                new Category("Kuudra", List.of(
                        new Module("Fresh Timer", FreshTimer.instance, "Shows a timer on screen for the Fresh Tools essence shop ability.", new SettingsScreen(List.of(
                                new Toggle("Send Message", FreshTimer.send, "Send a message once Fresh Tools activates."),
                                new TextInput("Message", FreshTimer.message, "The message to send.")
                        ))),
                        new Module("Kuudra Hitbox", KuudraHitbox.instance, "Renders a hitbox for Kuudra.", new SettingsScreen(List.of(
                                new ColorPicker("Color", KuudraHitbox.color, "The color of the hitbox.")
                        ))),
                        new Module("Build Pile Highlight", BuildPileHighlight.instance, "Renders beacons for the supply build piles in Kuudra.", new SettingsScreen(List.of(
                                new ColorPicker("Color", BuildPileHighlight.color, "The color of the beacons.")
                        ))),
                        new Module("Supply Highlight", SupplyHighlight.instance, "Renders beacons for the supply crates in Kuudra.", new SettingsScreen(List.of(
                                new ColorPicker("Color", SupplyHighlight.color, "The color of the beacons.")
                        ))),
                        new Module("Drop Off Highlight", DropOffHighlight.instance, "Renders beacons for the supply drop off points in Kuudra.", new SettingsScreen(List.of(
                                new ColorPicker("Color", DropOffHighlight.color, "The color of the beacons.")
                        ))),
                        new Module("Pre Message", PreMessage.instance, "Announces if no supply spawns at your pre spot (or your next pickup spot)."),
                        new Module("Shop Cleaner", ShopCleaner.instance, "Removes useless things from the perk shop."),
                        new Module("Chest Value", KuudraChestValue.instance, "Calculates the value of your Kuudra loot. Requires connectivity to the NoFrills API.", new SettingsScreen(List.of(
                                new SliderInt("Pet Bonus", 0, 20, 1, KuudraChestValue.petBonus, "The extra Crimson Essence percentage granted by your Kuudra pet.\nUsed to calculate the value of the essence with the extra perk included."),
                                new EnumToggle<>("Faction", KuudraChestValue.faction, "Your selected Crimson Isle faction. Used to calculate the cost of the Kuudra key."),
                                new EnumToggle<>("Reputation Range", KuudraChestValue.reputation, "Your faction reputation range. Used to calculate the cost of the Kuudra key."),
                                new EnumToggle<>("Discount Item", KuudraChestValue.discountItem, "The tier of your Dark Auction accessory. Used to calculate the cost of the Kuudra key."),
                                new Toggle("Use Salvage Value", KuudraChestValue.salvageValue, "Prices armor and equipment pieces based on the amount of essence gained from salvaging them.\nCan give a more accurate chest value compared to the default Lowest BIN value."),
                                new ColorPicker("Background", KuudraChestValue.background, "The color of the background of the value text.")
                        ))),
                        new Module("Crate Priority", CratePriority.instance, "Shows which crates to pull and/or grab after you collect your Pre.\nThis feature requires the full party to be using some kind of Pre message feature."),
                        new Module("Vesuvius Solver", VesuviusSolver.instance, "Highlights Kuudra loot in the Croesus/Vesuvius menu.\nThis feature depends on the Kuudra Chest Value options to correctly calculate profits.\nRequires connectivity to the NoFrills API.", new SettingsScreen(
                                new ColorPicker("Profit Color", VesuviusSolver.profitColor, "The highlight color for the most profitable chest."),
                                new ColorPicker("High Profit Color", VesuviusSolver.profitHighColor, "The highlight color for the most profitable chest if it exceeds the profit threshold.\nThis color is also applied regardless of profit if a chest contains a dye."),
                                new DoubleInput("Profit Threshold", VesuviusSolver.profitHighThreshold, "The minimum profit for a chest to be considered as high value."),
                                new ColorPicker("Unopened Color", VesuviusSolver.unopenedColor, "The highlight color for unopened loot."),
                                new ColorPicker("Rerolled Color", VesuviusSolver.rerolledColor, "The highlight color for unopened loot that is already rerolled."),
                                new ColorPicker("Opened Color", VesuviusSolver.openedColor, "The highlight color for opened loot."),
                                new Toggle("Value Tooltip", VesuviusSolver.valueTooltip, "Displays the value of individual chests in the tooltip."),
                                new Toggle("Tier Label", VesuviusSolver.tierLabel, "Adds labels which indicate the tier the loot was obtained in.")
                        ))
                )),
                new Category("Slayer", List.of(
                        new Module("Boss Highlight", BossHighlight.instance, "Highlights your slayer boss.", new SettingsScreen(List.of(
                                new ColorPicker("Fill Color", BossHighlight.fillColor, "The color of the filled box highlight (if applicable)."),
                                new ColorPicker("Outline Color", BossHighlight.outlineColor, "The color of the outline box highlight (if applicable)."),
                                new EnumToggle<>("Highlight Style", BossHighlight.highlightStyle, "The style of the highlight."),
                                new Separator("Inferno Demonlord"),
                                new ColorPicker("Ashen Fill", BossHighlight.ashenFill, "The color of the filled box if your boss is using the Ashen attunement."),
                                new ColorPicker("Ashen Outline", BossHighlight.ashenOutline, "The color of the outline box if your boss is using the Ashen attunement."),
                                new ColorPicker("Spirit Fill", BossHighlight.spiritFill, "The color of the filled box if your boss is using the Spirit attunement."),
                                new ColorPicker("Spirit Outline", BossHighlight.spiritOutline, "The color of the outline if your boss is using the Spirit attunement."),
                                new ColorPicker("Auric Fill", BossHighlight.auricFill, "The color of the filled box if your boss is using the Auric attunement."),
                                new ColorPicker("Auric Outline", BossHighlight.auricOutline, "The color of the outline box if your boss is using the Auric attunement."),
                                new ColorPicker("Crystal Fill", BossHighlight.crystalFill, "The color of the filled box if your boss is using the Crystal attunement."),
                                new ColorPicker("Crystal Outline", BossHighlight.crystalOutline, "The color of the outline box if your boss is using the Crystal attunement.")
                        ))),
                        new Module("Pillar Alert", PillarAlert.instance, "Displays the hits and the timer of your Blaze slayer fire pillars.\nThis feature tries to prevent false flags by tracking the \"path\" that the pillars take."),
                        new Module("No Attunement Spam", NoAttunementSpam.instance, "Filters the chat messages about using the wrong attunement on the Blaze boss."),
                        new Module("Kill Timer", KillTimer.instance, "Tracks how long your slayer boss took to kill."),
                        new Module("Chalice Highlight", ChaliceHighlight.instance, "Highlights the Blood Ichor chalices spawned by the T5 Vampire.", new SettingsScreen(List.of(
                                new ColorPicker("Color", ChaliceHighlight.color, "The color of the chalice highlight.")
                        ))),
                        new Module("Egg Hits Display", EggHitsDisplay.instance, "Renders the needed hits for the Tarantula Broodfather egg sack phase.", new SettingsScreen(List.of(
                                new ColorPicker("Color", EggHitsDisplay.color, "The color of the text."),
                                new SliderDouble("Scale", 0.0, 1.0, 0.01, EggHitsDisplay.scale, "The scale of the text.")
                        ))),
                        new Module("Beacon Tracer", BeaconTracer.instance, "Draws tracers towards the Yang Glyphs thrown by the Voidgloom Seraph.", new SettingsScreen(List.of(
                                new ColorPicker("Color", BeaconTracer.color, "The color of the tracer.")
                        ))),
                        new Module("Spawn Alert", SpawnAlert.instance, "Alerts you when your slayer boss is spawned.", new SettingsScreen(
                                new TitleInput("Title", SpawnAlert.title, "The title to display once your boss is spawned."),
                                new SoundInput("Sound", SpawnAlert.sound, "The sound to play once your boss is spawned.")
                        )),
                        new Module("Miniboss Highlight", SlayerMinibossHighlight.instance, "Highlights slayer minibosses.", new SettingsScreen(List.of(
                                new Toggle("Hide Nametag", SlayerMinibossHighlight.hide, "Hides the apparent \"SLAYER MINIBOSS\" nametag that appears above the miniboss."),
                                new EnumToggle<>("Highlight Style", SlayerMinibossHighlight.style, "The style of the highlight."),
                                new ColorPicker("Fill Color", SlayerMinibossHighlight.fillColor, "The color of the filled box."),
                                new ColorPicker("Outline Color", SlayerMinibossHighlight.outlineColor, "The color of the outline box.")
                        ))),
                        new Module("Miniboss Alert", SlayerMinibossAlert.instance, "Alerts you when a slayer miniboss is spawned.", new SettingsScreen(
                                new TitleInput("Title", SlayerMinibossAlert.title, "The title to display once a miniboss is spawned. Replaces {name} with the name of the miniboss."),
                                new SoundInput("Sound", SlayerMinibossAlert.sound, "The sound to play once a miniboss is spawned.")
                        )),
                        new Module("Spawn Timer", SpawnTimer.instance, "Tracks how long your slayer bosses take to spawn.")
                )),
                new Category("Mining", List.of(
                        new Module("Ability Alert", AbilityAlert.instance, "Alerts you when your Pickaxe Ability is available.\n\nIf present, uses the Pickaxe Ability widget for best accuracy.\nOtherwise, uses the cooldown displayed on your drill/pickaxe.\nMight be inaccurate in some cases.", new SettingsScreen(List.of(
                                new SliderInt("Override Ticks", 0, 6000, 1, AbilityAlert.override, "Overrides your pickaxe ability cooldown to a custom value in ticks.\nOnly applies if the Pickaxe Ability widget isn't present.\nSet to 0 to disable.")
                        ))),
                        new Module("Corpse Highlight", CorpseHighlight.instance, "Highlights corpses in the Glacite Mineshafts.", new SettingsScreen(List.of(
                                new Toggle("Hide Opened", CorpseHighlight.hideOpened, "Removes the highlight from corpses you've already opened."),
                                new EnumToggle<>("Highlight Style", CorpseHighlight.style, "The style of the corpse highlight."),
                                new ColorPicker("Lapis Outline", CorpseHighlight.lapisOutline, "The color of the Lapis corpse outline style highlight."),
                                new ColorPicker("Lapis Fill", CorpseHighlight.lapisFill, "The color of the Lapis corpse fill style highlight."),
                                new ColorPicker("Tungsten Outline", CorpseHighlight.mineralOutline, "The color of the Tungsten corpse outline style highlight."),
                                new ColorPicker("Tungsten Fill", CorpseHighlight.mineralFill, "The color of the Tungsten corpse fill style highlight."),
                                new ColorPicker("Umber Outline", CorpseHighlight.yogOutline, "The color of the Umber corpse outline style highlight."),
                                new ColorPicker("Umber Fill", CorpseHighlight.yogFill, "The color of the Umber corpse fill style highlight."),
                                new ColorPicker("Vanguard Outline", CorpseHighlight.vanguardOutline, "The color of the Vanguard corpse outline style highlight."),
                                new ColorPicker("Vanguard Fill", CorpseHighlight.vanguardFill, "The color of the Vanguard corpse fill style highlight.")
                        ))),
                        new Module("Scatha Mining", ScathaMining.instance, "Scatha mining features.", new SettingsScreen(List.of(
                                new Toggle("Spawn Alert", ScathaMining.alert, "Alerts you when a Worm/Scatha spawns nearby."),
                                new Toggle("Cooldown", ScathaMining.cooldown, "Tracks the Worm spawn cooldown for you.")
                        ))),
                        new Module("End Node Highlight", EndNodeHighlight.instance, "Highlights End Nodes.", new SettingsScreen(List.of(
                                new EnumToggle<>("Highlight Style", EndNodeHighlight.style, "The style of the highlight."),
                                new ColorPicker("Outline Color", EndNodeHighlight.outlineColor, "The color of the outline style highlight."),
                                new ColorPicker("Fill Color", EndNodeHighlight.fillColor, "The color of the fill style highlight.")
                        ))),
                        new Module("Gemstone Desync Fix", GemstoneDesyncFix.instance, "Fixes adjacent gemstone blocks not correctly updating when mining."),
                        new Module("Break Reset Fix", BreakResetFix.instance, "Fixes item updates resetting your block breaking progress, also known as HSM."),
                        new Module("Shaft Announce", ShaftAnnounce.instance, "Sends a message with the mineshaft ID and the list of corpses upon entering a Glacite Mineshaft.", new SettingsScreen(List.of(
                                new TextInput("Message", ShaftAnnounce.msg, "The message to send.\n\nReplaces {id} with the ID of the mineshaft, for example: \"JASP_1\".\nReplaces {corpses} with the list of corpses in the mineshaft, for example: \"2x Lapis, 1x Umber\".")
                        ))),
                        new Module("Commission Highlight", CommissionHighlight.instance, "Highlights completed commissions in the commissions menu.", new SettingsScreen(
                                new ColorPicker("Color", CommissionHighlight.color, "The color of the highlight.")
                        ))
                )),
                new Category("Farming", List.of(
                        new Module("Mouse Lock", MouseLock.instance, "Locks your player view with a keybind. Toggleable while on the Garden.", new SettingsScreen(
                                new Keybind("Keybind", MouseLock.keybind, "The key to toggle the mouse lock."),
                                new Toggle("Rebind Keys", MouseLock.rebind, "Rebinds your break and your jump keys while the feature is active."),
                                new Keybind("Active Break Key", MouseLock.breakKeyActive, "The key to set breaking to while active."),
                                new Keybind("Active Jump Key", MouseLock.jumpKeyActive, "The key to set jumping to while active."),
                                new Keybind("Inactive Break Key", MouseLock.breakKeyInactive, "The key to set breaking to when deactivated."),
                                new Keybind("Inactive Jump Key", MouseLock.jumpKeyInactive, "The key to set jumping to when deactivated.")
                        )),
                        new Module("Glowing Mushrooms", GlowingMushroom.instance, "Highlights Glowing Mushrooms.", new SettingsScreen(List.of(
                                new ColorPicker("Color", GlowingMushroom.color, "The color of the highlight.")
                        ))),
                        new Module("Plot Borders", PlotBorders.instance, "Renders borders for plots.", new SettingsScreen(List.of(
                                new Toggle("Infested Plots", PlotBorders.infested, "Adds borders to plots with pests in them."),
                                new ColorPicker("Infested Color", PlotBorders.infestedColor, "The color of the infested plot border."),
                                new Toggle("Current Plot", PlotBorders.current, "Adds a border to the plot you are in."),
                                new ColorPicker("Current Color", PlotBorders.currentColor, "The color of the current plot border."),
                                new Toggle("All Plots", PlotBorders.all, "Adds borders to every plot if no other border should apply."),
                                new ColorPicker("All Color", PlotBorders.allColor, "The color of the border for every plot.")
                        ))),
                        new Module("Vacuum Solver", VacuumSolver.instance, "Guesses Pest positions when using the Pest Tracker ability on your vacuum.", new SettingsScreen(List.of(
                                new Toggle("Tracer", VacuumSolver.tracer, "Draws a tracer towards the guess."),
                                new ColorPicker("Color", VacuumSolver.color, "The color of the guess highlight."),
                                new ColorPicker("Tracer Color", VacuumSolver.tracerColor, "The color of the guess tracer.")
                        ))),
                        new Module("Watering Helper", WateringHelper.instance, "Improves the Greenhouse watering cans.", new SettingsScreen(List.of(
                                new Toggle("Better Visibility", WateringHelper.betterVisibility, "Makes the crop water levels more visible by replacing them with seethrough text."),
                                new Toggle("Hide Particles", WateringHelper.hideParticles, "Removes the particles that appear while using a watering can.")
                        ))),
                        new Module("Phantomleaf Solver", PhantomleafSolver.instance, "Helps you with harvesting the Phantomleaf Greenhouse mutation.")
                ))
        );
        root.surface(Surface.VANILLA_TRANSLUCENT).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        FlowLayout parent = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        parent.surface(Surface.flat(RenderColor.DARK_GRAY.argb())).padding(Insets.of(5));
        this.categoriesParent = UIContainers.verticalScroll(
                Sizing.content(),
                Sizing.fixed(300),
                UIContainers.verticalFlow(Sizing.content(), Sizing.content())
        );
        this.categoriesParent.child().alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        this.modulesParent = UIContainers.verticalScroll(
                Sizing.content(),
                Sizing.fixed(300),
                UIContainers.verticalFlow(Sizing.content(), Sizing.content())
        );
        this.modulesParent.child().alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        for (Category category : this.categories) {
            category.onClick = c -> {
                this.updateCategories(c);
                lastCategory.set(c);
            };
        }
        this.updateCategories(Optional.ofNullable(lastCategory.get()).orElse(this.categories.getFirst()));
        parent.child(this.categoriesParent);
        parent.child(this.modulesParent);
        root.child(parent);
        /*
         this.mainScroll = UIContainers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent);
        this.mainScroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        root.child(this.mainScroll);
        ButtonComponent hudEditorButton = UIComponents.button(Component.literal("Open HUD Editor"), button -> mc.gui.setScreen(new HudEditorScreen()));
        hudEditorButton.margins(Insets.of(0, 3, 0, 3));
        hudEditorButton.positioning(Positioning.relative(100, 100));
        hudEditorButton.renderer((context, button, delta) -> {
            context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
            Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
        });
        root.child(hudEditorButton);
        this.searchBox = new FlatTextbox(Sizing.fixed(200));
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
                                for (UIComponent child : setting.children()) {
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
         */
    }

    @Override
    public void onClose() {
        if (AutoSave.instance.isActive()) {
            AutoSave.save();
        }
        super.onClose();
    }

    private void updateCategories(Category selected) {
        for (Category category : this.categories) {
            if (category.title.equals(selected.title)) {
                category.label.text(category.label.text().copy().withColor(RenderColor.NF_BLUE.hex()));
                this.modulesParent.child().clearChildren();
                this.modulesParent.child().children(category.build(""));
            } else {
                category.label.text(category.label.text().copy().withColor(RenderColor.WHITE.hex()));
            }
        }
        this.categoriesParent.child().clearChildren();
        this.categoriesParent.child().children(this.categories);
    }
}
