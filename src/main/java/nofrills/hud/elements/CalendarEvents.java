package nofrills.hud.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Config;
import nofrills.config.DataFile;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.SlotClickEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.time.Instant;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public final class CalendarEvents extends SimpleTextElement implements ListeningHudElement {
    public final DataFile data = Config.getDataFile("CalendarEventsData.json");
    public final SettingBool hideIfNone = new SettingBool(true, "hideIfNone", this.instance);

    public CalendarEvents() {
        super(Component.literal("Calendar Events\n§7None pinned."), new Feature("calendarEventsElement"), "Calendar Events");
        this.options = this.getBaseSettings(List.of(
                new Settings.BigButton("Unpin All", _ -> this.data.get().add("events", new JsonArray())),
                new Settings.Toggle("Hide If None", this.hideIfNone, "Hides the element if no events are pinned.")
        ));
        this.setDesc("Displays the starting times of specific calendar events.\nClick on a event while in the SkyBlock calendar to pin/unpin it.");
        this.setCategory(Category.Misc);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.hideIfNone.value() && this.text == this.defaultText) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void onClientTick() {
        if (this.data.get().has("events")) {
            long now = Instant.now().toEpochMilli();
            List<JsonElement> list = this.data.get().get("events").getAsJsonArray().asList();
            list.removeIf(element -> element.getAsJsonObject().get("timestamp").getAsLong() < now);
            list.sort(Comparator.comparingLong(element -> element.getAsJsonObject().get("timestamp").getAsLong()));
            if (!list.isEmpty()) {
                MutableComponent text = Component.literal("Calendar Events");
                for (JsonElement element : list) {
                    JsonObject event = element.getAsJsonObject();
                    MutableComponent line = Component.literal(event.get("name").getAsString()).withColor(event.get("color").getAsInt())
                            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(Utils.millisecondsToTime(event.get("timestamp").getAsLong() - now)).withStyle(ChatFormatting.YELLOW));
                    text.append("\n").append(line);
                }
                this.setText(text);
                return;
            }
        }
        this.setDefaultText();
    }

    @Override
    public void onSlotClick(SlotClickEvent event) {
        if (event.title.equals("Calendar and Events") && event.slot != null) {
            ItemStack stack = event.slot.getItem();
            for (String line : Utils.getLoreLines(stack)) {
                if (line.startsWith("Starts in: ") && !line.contains("-")) { // calendar can desync and display negative time
                    Calendar calendar = Utils.parseTime(line.substring(line.indexOf(":") + 2));
                    this.pinOrUnpin(calendar, stack);
                    event.cancel();
                    break;
                }
            }
        }
    }

    private void pinOrUnpin(Calendar date, ItemStack stack) {
        if (!this.data.get().has("events")) {
            this.data.get().add("events", new JsonArray());
        }
        JsonArray array = this.data.get().get("events").getAsJsonArray();
        String name = Utils.toPlain(stack.getHoverName());
        Style style = stack.getHoverName().getStyle();
        long time = date.getTimeInMillis();
        boolean removed = array.asList().removeIf(element -> {
            JsonObject event = element.getAsJsonObject();
            return event.get("name").getAsString().equals(name) && Utils.difference(time, event.get("timestamp").getAsLong()) < 5;
        });
        if (removed) {
            Utils.infoRaw(Component.empty()
                    .append(stack.getHoverName())
                    .append(Component.literal(" unpinned from Calendar Events HUD.").withStyle(ChatFormatting.YELLOW)));
        } else {
            JsonObject object = new JsonObject();
            object.addProperty("name", name);
            object.addProperty("color", style.getColor() != null ? style.getColor().getValue() : 0);
            object.addProperty("timestamp", time);
            array.add(object);
            Utils.infoRaw(Component.empty()
                    .append(stack.getHoverName())
                    .append(Component.literal(" pinned to Calendar Events HUD.").withStyle(ChatFormatting.GREEN)));
        }
    }
}
