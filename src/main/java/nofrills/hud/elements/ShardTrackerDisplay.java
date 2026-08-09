package nofrills.hud.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.features.hunting.ShardTracker;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.TickableHudElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.MutableReference;
import nofrills.misc.ShardData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

public final class ShardTrackerDisplay extends SimpleTextElement implements TickableHudElement {
    private static final String displayNone = "Shard Tracker\n§7None tracked.";
    public final SettingBool hideIfNone = new SettingBool(false, "hideIfNone", instance);
    private final MutableReference<String> display = new MutableReference<>(displayNone);

    public ShardTrackerDisplay() {
        super(Component.literal(displayNone), new Feature("shardTrackerElement"), "Shard Tracker Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If None", hideIfNone, "Hides the element if you are not tracking any shards (or the Shard Tracker is disabled).")
        ));
        this.setDesc("Displays information about tracked attribute shards. Used by the Shard Tracker feature.");
        this.setCategory(Category.Misc);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (hideIfNone.value() && (!ShardTracker.instance.isActive() || this.display.get().equals(displayNone))) {
                return;
            }
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void onClientTick() {
        if (ShardTracker.data.value().has("shards")) {
            JsonArray shards = ShardTracker.data.value().get("shards").getAsJsonArray();
            List<String> lines = new ArrayList<>();
            for (JsonElement shard : shards) {
                JsonObject shardData = shard.getAsJsonObject();
                String name = shardData.get("name").getAsString();
                long needed = shardData.get("needed").getAsLong();
                long obtained = shardData.get("obtained").getAsLong();
                String source = shardData.get("source").getAsString();
                if (name.isEmpty() || ShardTracker.shouldFilter(ShardTracker.getTrackedSource(source))) {
                    continue;
                }
                String shardName = Utils.format("{}§l{}", ShardData.getColorPrefix(Utils.toLower(name)), Utils.uppercaseFirst(name, false));
                String shardSource = Utils.format("{}[{}]", ShardTracker.getSourceColor(source), source);
                String quantityColor = needed > 0 & obtained >= needed ? "§a" : "§f";
                String shardQuantity = needed <= 0 ? Utils.format("{}x", Utils.formatSeparator(obtained)) : Utils.format("{}/{}x", Utils.formatSeparator(obtained), Utils.formatSeparator(needed));
                lines.add(Utils.format("{}{}§r {}§r {}", quantityColor, shardQuantity, shardName, shardSource));
            }
            if (!lines.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Shard Tracker");
                for (String line : lines) {
                    builder.append("\n").append(line);
                }
                this.display.set(builder.toString());
            } else {
                this.display.set(displayNone);
            }
        } else {
            this.display.set(displayNone);
        }
        this.setText(this.display.get());
    }
}
