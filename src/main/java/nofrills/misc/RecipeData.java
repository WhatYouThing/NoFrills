package nofrills.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Config;
import nofrills.config.DataFile;
import nofrills.events.EventListener;
import nofrills.events.SlotUpdateEvent;

import java.util.Optional;

@EventListener
public class RecipeData {
    private static final DataFile data = Config.getDataFile("RecipeData.json");

    public static Optional<JsonArray> getFromCache(String id) {
        if (data.get().has(id)) {
            return Optional.of(data.get().get(id).getAsJsonArray());
        }
        return Optional.empty();
    }

    private static boolean isCraftingMenu(ChestMenu menu) {
        if (menu.getRowCount() == 6) {
            ItemStack stack = menu.slots.get(23).getItem();
            return stack.is(Items.CRAFTING_TABLE) && Utils.toPlain(stack.getHoverName()).equals("Crafting Table");
        }
        return false;
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (event.isFinal && isCraftingMenu(event.handler)) {
            ItemStack result = event.handler.slots.get(25).getItem();
            String resultID = Utils.getMarketId(result);
            if (result.isEmpty() || resultID.isEmpty()) {
                return;
            }
            if (!data.get().has(resultID)) {
                data.get().add(resultID, new JsonArray());
            }
            JsonArray array = data.get().get(resultID).getAsJsonArray();
            JsonObject recipe = new JsonObject();
            recipe.addProperty("quantity", result.getCount());
            recipe.add("items", new JsonObject());
            JsonObject items = recipe.get("items").getAsJsonObject();
            for (int i = 0; i <= 2; i++) {
                for (int j = 0; j <= 2; j++) {
                    ItemStack stack = event.handler.slots.get(10 + i * 9 + j).getItem();
                    String id = Utils.getMarketId(stack);
                    if (!stack.isEmpty() && !id.isEmpty()) {
                        items.addProperty(id, (items.has(id) ? items.get(id).getAsInt() : 0) + stack.getCount());
                    }
                }
            }
            array.remove(recipe);
            array.add(recipe);
        }
    }
}
