package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.*;

@EventListener
public class ItemProtection {
    public static final Feature instance = new Feature("itemProtection", Feature.Flags.UsePricingAPI);

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind uuidKey = new SettingKeybind(-1, "uuidKey", instance);
    public static final SettingKeybind skyblockIdKey = new SettingKeybind(-1, "skyblockIdKey", instance);
    public static final SettingKeybind overrideKey = new SettingKeybind(-1, "overrideKey", instance);
    public static final SettingBool playOverrideSound = new SettingBool(false, "playOverrideSound", instance);
    public static final SettingBool protectUUID = new SettingBool(false, "protectUUID", instance);
    public static final SettingBool protectSkyblockId = new SettingBool(false, "protectSkyblockId", instance);
    public static final SettingBool protectMaxQuality = new SettingBool(false, "protectMaxQuality", instance);
    public static final SettingBool protectStarred = new SettingBool(false, "protectStarred", instance);
    public static final SettingBool protectRarityUpgraded = new SettingBool(false, "protectRarityUpgraded", instance);
    public static final SettingBool protectValue = new SettingBool(false, "protectValue", instance);
    public static final SettingDouble protectValueMin = new SettingDouble(5000000.0, "protectValueMin", instance);
    public static final SettingBool hideTooltip = new SettingBool(false, "hideTooltip", instance);
    public static final SettingBool drawOverlay = new SettingBool(false, "drawOverlay", instance);
    public static final SettingColor regularOverlay = new SettingColor(RenderColor.GREEN, "regularOverlay", instance);
    public static final SettingColor manualOverlay = new SettingColor(RenderColor.fromHex(0xffff7f), "manualOverlay", instance);

    private static final Identifier overlaySprite = Identifier.fromNamespaceAndPath("nofrills", "item_protection");
    private static final WeakHashMap<ItemStack, ProtectType> stackCache = new WeakHashMap<>();
    private static boolean isSellGUI = false;
    private static boolean isSalvageGUI = false;

    public static ProtectType getProtectType(ItemStack item) {
        if (mc.screen != null && overrideKey.isDown()) {
            return ProtectType.None;
        }
        return stackCache.computeIfAbsent(item, (stack) -> {
            if (stack.isEmpty()) return ProtectType.None;
            CompoundTag customData = Utils.getCustomData(stack);
            if (customData == null) return ProtectType.None;
            String id = Utils.getMarketId(stack);
            if (protectUUID.value() && data.value().has("uuids")) {
                String uuid = customData.getStringOr("uuid", "");
                if (data.value().getAsJsonArray("uuids").contains(new JsonPrimitive(uuid))) {
                    return ProtectType.UUID;
                }
            }
            if (protectSkyblockId.value() && data.value().has("ids")) {
                if (data.value().getAsJsonArray("ids").contains(new JsonPrimitive(id))) {
                    return ProtectType.SkyblockID;
                }
            }
            if (protectMaxQuality.value() && customData.getIntOr("baseStatBoostPercentage", 0) == 50) {
                return ProtectType.MaxQuality;
            }
            if (protectStarred.value() && customData.getIntOr("upgrade_level", 0) > 0 && !customData.contains("boss_tier")) {
                return ProtectType.Starred;
            }
            if (protectRarityUpgraded.value() && customData.getIntOr("rarity_upgrades", 0) > 0) {
                return ProtectType.RarityUpgraded;
            }
            if (protectValue.value()) {
                double min = protectValueMin.value();
                List<Double> prices = new ArrayList<>();
                if (bazaarPricing.containsKey(id)) prices.add(bazaarPricing.get(id).buy());
                if (auctionPricing.containsKey(id)) prices.add(Double.valueOf(auctionPricing.get(id)));
                if (npcPricing.containsKey(id)) prices.add(npcPricing.get(id).coin());
                for (double price : prices) {
                    if (price >= min) {
                        return ProtectType.Value;
                    }
                }
            }
            return ProtectType.None;
        });
    }

    public static void drawOverlayIcon(GuiGraphicsExtractor context, int slotX, int slotY, ProtectType type) {
        if (!drawOverlay.value() || type.equals(ProtectType.None)) {
            return;
        }
        RenderColor color = switch (type) {
            case UUID, SkyblockID -> manualOverlay.value();
            default -> regularOverlay.value();
        };
        context.blitSprite(RenderPipelines.GUI_TEXTURED, overlaySprite, slotX, slotY, 16, 16, color.getArgb());
    }

    private static boolean isSellStack(ItemStack stack) {
        return (stack.getItem().equals(Items.HOPPER) && Utils.toPlain(stack.getHoverName()).equals("Sell Item"))
                || Utils.getLoreLines(stack).contains("Click to buyback!");
    }

    private static boolean isSalvageButton(ItemStack stack) {
        String name = Utils.toPlain(stack.getHoverName());
        return name.equals("Salvage Items") || name.equals("Confirm Salvage") || name.equals("Draconic Altar");
    }

    private static void addUUID(ItemStack stack) {
        CompoundTag customData = Utils.getCustomData(stack);
        if (customData == null) {
            Utils.infoRaw(Component.literal("§cItem ").append(stack.getHoverName()).append(" §chas no custom data, unable to protect."));
            return;
        }
        String uuid = customData.getStringOr("uuid", "");
        if (uuid.isEmpty()) {
            Utils.infoRaw(Component.literal("§cItem ").append(stack.getHoverName()).append(" §chas no UUID, unable to protect."));
            return;
        }
        data.edit(object -> {
            if (!object.has("uuids")) {
                object.add("uuids", new JsonArray());
            }
            JsonArray array = object.getAsJsonArray("uuids");
            JsonPrimitive primitive = new JsonPrimitive(uuid);
            if (array.remove(primitive)) {
                Utils.infoRaw(Component.literal("§eItem ").append(stack.getHoverName()).append(" §eis no longer protected by UUID."));
                Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
            } else {
                Utils.infoRaw(Component.literal("§aItem ").append(stack.getHoverName()).append(" §ais now protected by UUID."));
                Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                array.add(primitive);
            }
            stackCache.remove(stack);
        });
    }

    private static void addSkyblockID(ItemStack stack) {
        String id = Utils.getMarketId(stack);
        if (id.isEmpty()) {
            Utils.infoRaw(Component.literal("§cItem ").append(stack.getHoverName()).append(" §chas no Skyblock ID, unable to protect."));
            return;
        }
        data.edit(object -> {
            if (!object.has("ids")) {
                object.add("ids", new JsonArray());
            }
            JsonArray array = object.getAsJsonArray("ids");
            JsonPrimitive primitive = new JsonPrimitive(id);
            if (array.remove(primitive)) {
                Utils.infoRaw(Component.literal("§eItem ").append(stack.getHoverName()).append(" §eis no longer protected by Skyblock ID."));
                Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
            } else {
                Utils.infoRaw(Component.literal("§aItem ").append(stack.getHoverName()).append(" §ais now protected by Skyblock ID."));
                Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                array.add(primitive);
            }
            stackCache.remove(stack);
        });
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?>) {
            if (overrideKey.isKey(event.key)) {
                if (event.action == GLFW.GLFW_PRESS) {
                    Utils.infoRaw(Component.literal("Item Protection override is now active.").withStyle(ChatFormatting.RED));
                    if (playOverrideSound.value()) Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
                } else if (event.action == GLFW.GLFW_RELEASE) {
                    Utils.infoRaw(Component.literal("Item Protection override deactivated.").withStyle(ChatFormatting.GREEN));
                    if (playOverrideSound.value()) Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
                event.cancel();
                return;
            }
            if (uuidKey.isKey(event.key) || skyblockIdKey.isKey(event.key)) {
                Slot focused = Utils.getFocusedSlot();
                if (focused == null) return;
                ItemStack stack = focused.getItem();
                if (!stack.isEmpty()) {
                    if (event.action == GLFW.GLFW_PRESS) {
                        if (uuidKey.isKey(event.key)) addUUID(stack);
                        if (skyblockIdKey.isKey(event.key)) addSkyblockID(stack);
                    }
                    event.cancel();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive() && !event.stack.isEmpty() && event.customData != null) {
            if (hideTooltip.value() && !InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_LSHIFT)) {
                return;
            }
            ProtectType type = getProtectType(event.stack);
            if (!type.equals(ProtectType.None)) {
                MutableComponent line = Component.literal(Utils.format("§aItem Protected §7({})", type.name()));
                event.addLine(Utils.getShortTag().append(line.withColor(0xffffff)));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive()) {
            ItemStack stack = event.slot != null ? event.slot.getItem() : event.handler.getCarried();
            if (event.handler instanceof ChestMenu handler) {
                if (isSellGUI && event.slotId >= 0 && event.slotId < handler.getRowCount() * 9) {
                    return;
                }
                if (isSalvageGUI && isSalvageButton(stack)) {
                    for (Slot slot : Utils.getContainerSlots(handler)) {
                        ItemStack slotStack = slot.getItem();
                        if (!getProtectType(slotStack).equals(ProtectType.None)) {
                            Utils.infoRaw(Component.literal("§aPrevented salvage, ").append(slotStack.getHoverName()).append(" §ais a protected item."));
                            event.cancel();
                            return;
                        }
                    }
                }
            }
            if (Utils.getFocusedSlot() == null || isSellGUI || event.actionType.equals(ContainerInput.THROW)) {
                if (!getProtectType(stack).equals(ProtectType.None)) {
                    event.cancel();
                }
            }
        }
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && !event.isInventory && !event.stack.isEmpty() && isSellStack(event.stack)) {
            isSellGUI = true;
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive()) {
            String title = event.screen.getTitle().getString();
            isSellGUI = false;
            isSalvageGUI = title.equals("Salvage Items") || title.equals("Draconic Sacrifice");
        }
    }

    public enum ProtectType {
        UUID,
        SkyblockID,
        MaxQuality,
        Starred,
        RarityUpgraded,
        Value,
        None
    }
}