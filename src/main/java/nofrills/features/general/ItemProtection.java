package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.*;

public class ItemProtection {
    public static final Feature instance = new Feature("itemProtection");

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

    private static boolean isSellGUI = false;
    private static boolean isSalvageGUI = false;
    private static boolean overrideActive = false;

    public static boolean isProtectingValue() {
        return instance.isActive() && protectValue.value();
    }

    public static ProtectType getProtectType(ItemStack stack) {
        if (overrideActive || stack.isEmpty()) return ProtectType.None;
        NbtCompound customData = Utils.getCustomData(stack);
        if (customData == null) return ProtectType.None;
        String id = Utils.getMarketId(stack);
        if (protectUUID.value() && data.value().has("uuids")) {
            String uuid = customData.getString("uuid", "");
            if (data.value().getAsJsonArray("uuids").contains(new JsonPrimitive(uuid))) {
                return ProtectType.UUID;
            }
        }
        if (protectSkyblockId.value() && data.value().has("ids")) {
            if (data.value().getAsJsonArray("ids").contains(new JsonPrimitive(id))) {
                return ProtectType.SkyblockID;
            }
        }
        if (protectMaxQuality.value() && customData.getInt("baseStatBoostPercentage", 0) == 50) {
            return ProtectType.MaxQuality;
        }
        if (protectStarred.value() && customData.getInt("upgrade_level", 0) > 0 && !customData.contains("boss_tier")) {
            return ProtectType.Starred;
        }
        if (protectRarityUpgraded.value() && customData.getInt("rarity_upgrades", 0) > 0) {
            return ProtectType.RarityUpgraded;
        }
        if (protectValue.value()) {
            double min = protectValueMin.value();
            List<Double> prices = new ArrayList<>();
            if (bazaarPricing.containsKey(id)) prices.add(bazaarPricing.get(id).get("buy"));
            if (auctionPricing.containsKey(id)) prices.add(Double.valueOf(auctionPricing.get(id)));
            if (npcPricing.containsKey(id) && npcPricing.get(id).containsKey("coin"))
                prices.add(npcPricing.get(id).get("coin"));
            for (double price : prices) {
                if (price >= min) {
                    return ProtectType.Value;
                }
            }
        }
        return ProtectType.None;
    }

    private static boolean isSellStack(ItemStack stack) {
        return (stack.getItem().equals(Items.HOPPER) && Utils.toPlain(stack.getName()).equals("Sell Item"))
                || Utils.getLoreLines(stack).contains("Click to buyback!");
    }

    private static boolean isSalvageButton(ItemStack stack) {
        String name = Utils.toPlain(stack.getName());
        return name.equals("Salvage Items") || name.equals("Confirm Salvage");
    }

    private static void addUUID(ItemStack stack) {
        NbtCompound customData = Utils.getCustomData(stack);
        if (customData == null) {
            Utils.infoRaw(Text.literal("§cItem ").append(stack.getName()).append(" §chas no custom data, unable to protect."));
            return;
        }
        String uuid = customData.getString("uuid", "");
        if (uuid.isEmpty()) {
            Utils.infoRaw(Text.literal("§cItem ").append(stack.getName()).append(" §chas no UUID, unable to protect."));
            return;
        }
        data.edit(object -> {
            if (!object.has("uuids")) {
                object.add("uuids", new JsonArray());
            }
            JsonArray array = object.getAsJsonArray("uuids");
            JsonPrimitive primitive = new JsonPrimitive(uuid);
            if (array.remove(primitive)) {
                Utils.infoRaw(Text.literal("§eItem ").append(stack.getName()).append(" §eis no longer protected by UUID."));
            } else {
                Utils.infoRaw(Text.literal("§aItem ").append(stack.getName()).append(" §ais now protected by UUID."));
                array.add(primitive);
            }
        });
    }

    private static void addSkyblockID(ItemStack stack) {
        String id = Utils.getMarketId(stack);
        if (id.isEmpty()) {
            Utils.infoRaw(Text.literal("§cItem ").append(stack.getName()).append(" §chas no Skyblock ID, unable to protect."));
            return;
        }
        data.edit(object -> {
            if (!object.has("ids")) {
                object.add("ids", new JsonArray());
            }
            JsonArray array = object.getAsJsonArray("ids");
            JsonPrimitive primitive = new JsonPrimitive(id);
            if (array.remove(primitive)) {
                Utils.infoRaw(Text.literal("§eItem ").append(stack.getName()).append(" §eis no longer protected by Skyblock ID."));
            } else {
                Utils.infoRaw(Text.literal("§aItem ").append(stack.getName()).append(" §ais now protected by Skyblock ID."));
                array.add(primitive);
            }
        });
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && (mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof GenericContainerScreen)) {
            if (overrideKey.isKey(event.key)) {
                overrideActive = event.action != GLFW.GLFW_RELEASE;
                event.cancel();
                return;
            }
            if (uuidKey.isKey(event.key) || skyblockIdKey.isKey(event.key)) {
                Slot focused = Utils.getFocusedSlot();
                if (focused == null) return;
                ItemStack stack = focused.getStack();
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
            ProtectType type = getProtectType(event.stack);
            if (!type.equals(ProtectType.None)) {
                MutableText line = Text.literal(Utils.format("§aItem Protected §7({})", type.name()));
                event.addLine(Utils.getShortTag().append(line.withColor(0xffffff)));
            }
        }
    }

    @EventHandler
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive()) {
            ItemStack stack = event.slot != null ? event.slot.getStack() : event.handler.getCursorStack();
            if (event.handler instanceof GenericContainerScreenHandler handler) {
                if (isSellGUI && event.slotId >= 0 && event.slotId < handler.getRows() * 9) {
                    return;
                }
                if (isSalvageGUI && isSalvageButton(stack)) {
                    for (Slot slot : Utils.getContainerSlots(handler)) {
                        ItemStack slotStack = slot.getStack();
                        if (!getProtectType(slotStack).equals(ProtectType.None)) {
                            Utils.infoRaw(Text.literal("§aPrevented salvage, ").append(slotStack.getName()).append(" §ais a protected item."));
                            event.cancel();
                            return;
                        }
                    }
                }
            }
            if (event.slotId == -999 || isSellGUI || event.actionType.equals(SlotActionType.THROW)) {
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