package nofrills.features;

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
import nofrills.misc.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static nofrills.Main.mc;

public class AttributeDebug {
    private static final Path filePath = FabricLoader.getInstance().getConfigDir().resolve("NoFrills/fusion_data.json");
    private static final JsonObject data = loadData();
    public static boolean isEnabled = false;

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

    private static String getShardID(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (line.contains("SHARD (ID ")) {
                return line.substring(line.indexOf("(ID ")).replace("(ID", "").replace(")", "").trim();
            }
        }
        return "";
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
                for (Slot slot : container.getScreenHandler().slots) {

                }
            }
        }
    }

    @EventHandler
    private static void onTooltip(DrawItemTooltip event) {
        if (isEnabled && mc.currentScreen instanceof GenericContainerScreen) {
            Slot slot = Utils.getFocusedSlot();
            event.addLine(Text.of(Utils.format("Slot ID: {}", slot != null ? slot.id : "null")));
            event.addLine(Text.of(Utils.format("Shard ID: {}", slot != null ? getShardID(slot.getStack()) : "null")));
        }
    }
}
