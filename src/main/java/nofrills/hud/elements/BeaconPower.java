package nofrills.hud.elements;

import com.google.gson.JsonObject;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nofrills.config.Config;
import nofrills.config.DataFile;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.SlotUpdateEvent;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.TickableHudElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

public final class BeaconPower extends SimpleTextElement implements TickableHudElement {
    public final DataFile data = Config.getDataFile("BeaconPowerData.json");
    public final SettingBool hideIfInactive = new SettingBool(false, "hideIfInactive", this.instance);
    private boolean active = false;

    public BeaconPower() {
        super(Component.literal("Beacon: §7Inactive"), new Feature("beaconPowerElement"), "Beacon Power");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If Inactive", this.hideIfInactive, "Hides the HUD element if your Beacon power is inactive.")
        ));
        this.setDesc("Displays the duration and the stat upgrade granted by your Beacon.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (this.hideIfInactive.value() && !this.active) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void onClientTick() {
        JsonObject object = this.data.get();
        if (object.has("statDuration")) {
            Instant instant = Instant.now();
            long statDuration = object.get("statDuration").getAsLong();
            if (instant.toEpochMilli() <= statDuration) {
                if (!object.has("statColor") || !object.has("statText")) return;
                MutableComponent stat = Component.literal(object.get("statText").getAsString()).withColor(object.get("statColor").getAsInt());
                Duration duration = Duration.between(instant, Instant.ofEpochMilli(statDuration));
                StringBuilder time = new StringBuilder();
                int[] parts = new int[]{(int) duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()};
                String[] units = new String[]{"d", "h", "m", "s"};
                for (int i = 0; i < units.length; i++) {
                    if (parts[i] != 0) {
                        time.append(parts[i]).append(units[i]).append(" ");
                    }
                }
                this.setText(Component.literal("Beacon: ").append(stat).append(" §7" + time.toString().trim()));
                this.active = true;
                return;
            }
        }
        if (this.active) {
            this.setText("Beacon: §7Inactive");
            this.active = false;
        }
    }

    public void update(SlotUpdateEvent event) {
        if (!event.title.equals("Beacon")) return;
        String name = Utils.toPlain(event.stack.getHoverName());
        if (!name.equals("Beacon Power") && !name.equals("Profile Stat Upgrades")) return;
        for (Component line : Utils.getLoreText(event.stack)) {
            String string = Utils.toPlain(line);
            if (string.equals("No active profile stat bonus set!")) {
                this.data.get().addProperty("statColor", ChatFormatting.RED.getColor());
                this.data.get().addProperty("statText", "No stat!");
                break;
            }
            if (string.startsWith("Current Stat: ") || string.startsWith("Power Remaining: ")) {
                String value = string.substring(string.indexOf(":") + 2).trim();
                if (string.startsWith("Current Stat: ")) {
                    Utils.getStyle(line, str -> str.trim().equals(value)).ifPresent(style -> {
                        if (style.getColor() == null) return;
                        this.data.get().addProperty("statColor", style.getColor().getValue());
                        this.data.get().addProperty("statText", value);
                    });
                } else {
                    Calendar calendar = Utils.parseTime(value);
                    this.data.get().addProperty("statDuration", calendar.getTimeInMillis());
                }
                break;
            }
        }
    }
}
