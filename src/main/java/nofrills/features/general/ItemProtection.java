package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.*;

public class ItemProtection {
    public static final Feature instance = new Feature("itemProtection").requiresPricingAPI();

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind uuidKey = new SettingKeybind(-1, "uuidKey", instance);
    public static final SettingKeybind skyblockIdKey = new SettingKeybind(-1, "skyblockIdKey", instance);
    public static final SettingKeybind overrideKey = new SettingKeybind(-1, "overrideKey", instance);
    public static final SettingBool protectUUID = new SettingBool(false, "protectUUID", instance);
    public static final SettingBool protectSkyblockId = new SettingBool(false, "protectSkyblockId", instance);
    public static final SettingBool protectMaxQuality = new SettingBool(false, "protectMaxQuality", instance);
    public static final SettingBool protectStarred = new SettingBool(false, "protectStarred", instance);
    public static final SettingBool protectRarityUpgraded = new SettingBool(false, "protectRarityUpgraded", instance);
    public static final SettingBool protectValue = new SettingBool(false, "protectValue", instance);
    public static final SettingDouble protectValueMin = new SettingDouble(5000000.0, "protectValueMin", instance);
    public static final SettingBool hideTooltip = new SettingBool(false, "hideTooltip", instance);

    private static boolean isSellGUI = false;
    private static boolean isSalvageGUI = false;
    private static boolean overrideActive = false;
    private static boolean revealingTooltip = false;

    public static ProtectType getProtectType(ItemStack stack) {
        if (overrideActive || stack.isEmpty()) return ProtectType.None;
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
    }

    private static boolean isSellStack(ItemStack stack) {
        return (stack.getItem().equals(Items.HOPPER) && Utils.toPlain(stack.getHoverName()).equals("Sell Item"))
                || Utils.getLoreLines(stack).contains("Click to buyback!");
    }

    private static boolean isSalvageButton(ItemStack stack) {
        String name = Utils.toPlain(stack.getHoverName());
        return name.equals("Salvage Items") || name.equals("Confirm Salvage");
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
            } else {
                Utils.infoRaw(Component.literal("§aItem ").append(stack.getHoverName()).append(" §ais now protected by UUID."));
                array.add(primitive);
            }
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
            } else {
                Utils.infoRaw(Component.literal("§aItem ").append(stack.getHoverName()).append(" §ais now protected by Skyblock ID."));
                array.add(primitive);
            }
        });
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && (mc.screen instanceof InventoryScreen || mc.screen instanceof ContainerScreen)) {
            if (hideTooltip.value() && event.key == GLFW.GLFW_KEY_LEFT_SHIFT) {
                revealingTooltip = event.action != GLFW.GLFW_RELEASE;
            }
            if (overrideKey.isKey(event.key)) {
                overrideActive = event.action != GLFW.GLFW_RELEASE;
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

    @EventHandler
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive() && !event.stack.isEmpty() && event.customData != null) {
            if (hideTooltip.value() && !revealingTooltip) {
                return;
            }
            ProtectType type = getProtectType(event.stack);
            if (!type.equals(ProtectType.None)) {
                MutableComponent line = Component.literal(Utils.format("§aItem Protected §7({})", type.name()));
                event.addLine(Utils.getShortTag().append(line.withColor(0xffffff)));
            }
        }
    }

    @EventHandler
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
            isSellGUI = false;
            isSalvageGUI = event.screen.getTitle().getString().equals("Salvage Items");
        }
    }

    @EventHandler
    private static void onScreenClose(ScreenCloseEvent event) {
        if (instance.isActive()) {
            overrideActive = false;
            revealingTooltip = false;
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