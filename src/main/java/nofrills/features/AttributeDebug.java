package nofrills.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import nofrills.events.DrawItemTooltip;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class AttributeDebug {
    private static final Path filePath = FabricLoader.getInstance().getConfigDir().resolve("NoFrills/fusion_data.json");
    public static final JsonObject data = loadData();
    public static boolean isEnabled = false;
    public static List<Slot> highlightedSlots = new ArrayList<>();

    private static boolean isFirstInput(Slot slot) {
        return slot != null && slot.id == 10;
    }

    private static boolean isSecondInput(Slot slot) {
        return slot != null && slot.id == 12;
    }

    private static boolean isOutput(Slot slot) {
        return slot != null && (slot.id == 14 || slot.id == 15 || slot.id == 16);
    }

    private static boolean isSelection(Slot slot) {
        return slot != null && slot.id >= 28 && slot.id <= 43;
    }

    private static boolean isMassSyphon(Slot slot) {
        return slot != null && slot.id == 52;
    }

    private static String getShardID(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (line.contains("SHARD (ID ")) {
                return line.substring(line.indexOf("(ID ")).replace("(ID", "").replace(")", "").trim();
            }
        }
        return "";
    }

    private static int getShardFuseAmount(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (line.startsWith("Required to fuse:")) {
                try {
                    return Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return -1;
    }

    private static JsonObject loadData() {
        if (Files.exists(filePath)) {
            try {
                return JsonParser.parseString(Files.readString(filePath)).getAsJsonObject();
            } catch (IOException ignored) {
            }
        }
        return new JsonObject();
    }

    public static void saveData() {
        try {
            Files.writeString(filePath, data.toString());
        } catch (IOException ignored) {
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (isEnabled && mc.currentScreen instanceof GenericContainerScreen container) {
            if (container.getTitle().getString().equals("Shard Fusion")) {
                String firstID = "";
                String secondID = "";
                List<Slot> outputSlots = new ArrayList<>();
                List<Slot> selectionSlots = new ArrayList<>();
                for (Slot slot : container.getScreenHandler().slots) {
                    ItemStack stack = slot.getStack();
                    String id = getShardID(stack);
                    if (isFirstInput(slot)) {
                        firstID = id;
                    }
                    if (isSecondInput(slot)) {
                        secondID = id;
                    }
                    if (isOutput(slot)) {
                        outputSlots.add(slot);
                    }
                    if (isSelection(slot)) {
                        selectionSlots.add(slot);
                    }
                    if (isMassSyphon(slot) && !SlotOptions.isSlotDisabled(slot)) {
                        SlotOptions.disableSlot(slot, true);
                    }
                }
                if (!data.has("recipes")) {
                    data.add("recipes", new JsonObject());
                }
                JsonObject recipes = data.getAsJsonObject("recipes");
                if (!firstID.isEmpty() && !secondID.isEmpty()) {
                    String fusionKey = Utils.format("{}+{}", firstID, secondID);
                    if (!recipes.has(fusionKey)) {
                        recipes.add(fusionKey, new JsonArray());
                    }
                    for (Slot slot : outputSlots) {
                        ItemStack stack = slot.getStack();
                        String id = getShardID(stack);
                        int required = getShardFuseAmount(stack);
                        if (!id.isEmpty() && required != -1) {
                            if (recipes.getAsJsonArray(fusionKey).asList().stream().noneMatch(element -> element.getAsJsonObject().get("id").getAsString().equals(id))) {
                                JsonObject object = new JsonObject();
                                object.addProperty("id", id);
                                object.addProperty("count", stack.getCount());
                                recipes.getAsJsonArray(fusionKey).add(object);
                            }
                        }
                    }
                }
                List<Slot> unknownRecipes = new ArrayList<>();
                if (!firstID.isEmpty()) {
                    for (Slot slot : selectionSlots) {
                        String id = getShardID(slot.getStack());
                        if (!id.isEmpty()) {
                            String fusionKey = Utils.format("{}+{}", firstID, id);
                            if (!recipes.has(fusionKey)) {
                                unknownRecipes.add(slot);
                            }
                        }
                    }
                }
                highlightedSlots = unknownRecipes;
            }
        }
    }

    @EventHandler
    private static void onTooltip(DrawItemTooltip event) {
        if (isEnabled && mc.currentScreen instanceof GenericContainerScreen) {
            Slot slot = Utils.getFocusedSlot();
            if (slot != null) {
                event.addLine(Text.of(Utils.format("Slot ID: {}", slot.id)));
                event.addLine(Text.of(Utils.format("Shard ID: {}", getShardID(slot.getStack()))));
                event.addLine(Text.of(Utils.format("Fuse Amount: {}", getShardFuseAmount(slot.getStack()))));
            }
        }
    }
}
