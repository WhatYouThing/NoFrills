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
    public static final SettingBool protectUUID = new SettingBool(false, "protectUUID", instance);
    public static final SettingBool protectSkyblockId = new SettingBool(false, "protectSkyblockId", instance);
    public static final SettingBool protectStarred = new SettingBool(false, "protectStarred", instance);
    public static final SettingBool protectRarityUpgraded = new SettingBool(false, "protectRarityUpgraded", instance);
    public static final SettingBool protectValue = new SettingBool(false, "protectValue", instance);
    public static final SettingDouble protectValueMin = new SettingDouble(1000000.0, "protectValueMin", instance);

    private static boolean isSellGUI = false;

    public static boolean isProtectingValue() {
        return instance.isActive() && protectValue.value();
    }

    public static ProtectType getProtectType(ItemStack stack) {
        NbtCompound customData = Utils.getCustomData(stack);
        if (customData == null) {
            return ProtectType.None;
        }
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
        if (protectStarred.value() && customData.getInt("upgrade_level", 0) > 0) {
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
            Slot focused = Utils.getFocusedSlot();
            if (focused == null) return;
            ItemStack stack = focused.getStack();
            if (event.key == uuidKey.key()) {
                if (event.action == GLFW.GLFW_PRESS && !stack.isEmpty()) {
                    addUUID(stack);
                }
                event.cancel();
            } else if (event.key == skyblockIdKey.key()) {
                if (event.action == GLFW.GLFW_PRESS && !stack.isEmpty()) {
                    addSkyblockID(stack);
                }
                event.cancel();
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
        if (instance.isActive() && (event.slotId == -999 || isSellGUI || event.actionType.equals(SlotActionType.THROW))) {
            ItemStack stack = event.slot != null ? event.slot.getStack() : event.handler.getCursorStack();
            if (!stack.isEmpty() && !getProtectType(stack).equals(ProtectType.None)) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && !event.isInventory && !event.stack.isEmpty()) {
            if (event.stack.getItem().equals(Items.HOPPER) && Utils.toPlain(event.stack.getName()).equals("Sell Item")) {
                isSellGUI = true;
                return;
            }
            if (Utils.getLoreLines(event.stack).contains("Click to buyback!")) {
                isSellGUI = true;
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        isSellGUI = false;
    }

    public enum ProtectType {
        UUID,
        SkyblockID,
        Starred,
        RarityUpgraded,
        Value,
        None
    }
}