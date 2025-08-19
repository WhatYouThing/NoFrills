package nofrills.features.hunting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingJson;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.features.general.PriceTooltips;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class ShardTracker {
    public static final Feature instance = new Feature("shardTracker");

    public static final SettingBool boxApply = new SettingBool(false, "load", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    public static final MutableText displayNone = Text.literal("§bShard Tracker\n§7None tracked.");
    public static MutableText display = displayNone;

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.Toggle boxToggle = new Settings.Toggle("Apply From Box", boxApply, "Automatically applies obtained amounts to shards when you open your Hunting Box.");
        list.add(boxToggle);
        Settings.BigButton importButton = new Settings.BigButton("Import Shard List", btn -> {
            if (!data.value().has("shards")) {
                data.value().add("shards", new JsonArray());
            }
            try {
                JsonArray array = JsonParser.parseString(mc.keyboard.getClipboard()).getAsJsonArray();
                JsonArray shards = data.value().get("shards").getAsJsonArray();
                List<JsonObject> objects = new ArrayList<>();
                for (JsonElement element : array) {
                    JsonObject shardData = element.getAsJsonObject();
                    JsonObject object = new JsonObject();
                    object.addProperty("name", shardData.get("name").getAsString().toLowerCase());
                    object.addProperty("needed", shardData.get("needed").getAsLong());
                    object.addProperty("obtained", 0L);
                    object.addProperty("source", shardData.get("source").getAsString());
                    objects.add(object);
                }
                for (JsonObject object : objects) {
                    shards.add(object);
                }
            } catch (Exception ignored) {
                Utils.info("§cFailed to import shard list from the calculator, no valid data found in your clipboard. Try updating the mod to the newest version if the import always fails.");
                return;
            }
            Utils.info("§aShard list imported successfully.");
            mc.setScreen(buildSettings());
        });
        importButton.button.verticalSizing(Sizing.fixed(18));
        importButton.button.tooltip(Text.literal("Pastes the list of shards (copied from the SkyShards calculator) that you need to get."));
        list.add(importButton);
        Settings.BigButton button = new Settings.BigButton("Add New Shard", btn -> {
            if (!data.value().has("shards")) {
                data.value().add("shards", new JsonArray());
            }
            JsonObject object = new JsonObject();
            object.addProperty("name", "");
            object.addProperty("needed", 0L);
            object.addProperty("obtained", 0L);
            object.addProperty("source", "Direct");
            data.value().get("shards").getAsJsonArray().add(object);
            mc.setScreen(buildSettings());
        });
        button.button.verticalSizing(Sizing.fixed(18));
        list.add(button);
        if (data.value().has("shards")) {
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            for (int i = 0; i < shards.size(); i++) {
                list.add(new Setting(i));
            }
        }
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Text.literal("Shard Tracker"));
        refreshDisplay();
        return settings;
    }

    public static String getSourceColor(String source) {
        return switch (source.toLowerCase()) {
            case "direct", "bazaar" -> "§a";
            case "fuse" -> "§d";
            case "cycle" -> "§6";
            default -> "§7";
        };
    }

    public static void refreshDisplay() {
        if (data.value().has("shards")) {
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            List<String> lines = new ArrayList<>();
            for (JsonElement shard : shards) {
                JsonObject shardData = shard.getAsJsonObject();
                String name = shardData.get("name").getAsString();
                if (name.isEmpty()) {
                    continue;
                }
                long needed = shardData.get("needed").getAsLong();
                long obtained = shardData.get("obtained").getAsLong();
                String source = shardData.get("source").getAsString();
                lines.add(Utils.format("§f§l{}§r §7{}§f: {}",
                        Utils.uppercaseFirst(name, false),
                        Utils.format("{}{}", getSourceColor(source), source),
                        needed == 0 ? Utils.formatSeparator(obtained) : Utils.format("{}{}/{}",
                                obtained >= needed ? "§a" : "", Utils.formatSeparator(obtained), Utils.formatSeparator(needed)
                        )
                ));
            }
            if (!lines.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("§bShard Tracker");
                for (String line : lines) {
                    builder.append("\n").append(line);
                }
                display = Text.literal(builder.toString());
                return;
            }
        }
        display = displayNone;
    }

    private static Shard buildShardData(String name, String quantity, ShardSource source) {
        try {
            int amount = Integer.parseInt(quantity);
            return new Shard(name.toLowerCase(), amount, source);
        } catch (NumberFormatException ignored) {
        }
        return new Shard(name.toLowerCase(), 1, source);
    }

    private static Shard getShardFromMsg(String msg) { // parses various messages about obtaining shards, do not touch
        if (msg.startsWith("You caught ") && (msg.endsWith("Shards!") || msg.endsWith("Shard!"))) {
            msg = msg.replace("You caught ", "").replace(" Shards!", "").replace(" Shard!", "").trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1);
            return buildShardData(name, quantity.replace("x", ""), ShardSource.Caught);
        }
        if (msg.startsWith("LOOT SHARE You received ") && (msg.contains(" Shard for assisting ") || msg.contains(" Shards for assisting "))) {
            msg = msg.replace("LOOT SHARE You received ", "").substring(0, msg.indexOf(" Shard")).trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1);
            return buildShardData(name, quantity, ShardSource.Caught);
        }
        if (msg.startsWith("CHARM ") || msg.startsWith("NAGA ") || msg.startsWith("SALT ")) {
            int index = msg.indexOf("You charmed a ");
            if (index != -1) {
                msg = msg.substring(index + "You charmed a ".length());
                int quantityStart = msg.indexOf(" and captured") + " and captured".length() + 1;
                String quantity = msg.substring(quantityStart, msg.indexOf(" ", quantityStart));
                String name = msg.substring(0, msg.indexOf(" and captured"));
                return buildShardData(name, quantity, ShardSource.Charmed);
            }
        }
        if (msg.startsWith("FUSION! You obtained ") && msg.contains(" Shard")) {
            msg = msg.replace("FUSION! You obtained ", "").replace(" NEW!", "").trim();
            if (msg.startsWith("an ") || msg.startsWith("a ")) {
                msg = msg.substring(msg.indexOf(" ") + 1);
            }
            String quantity = msg.substring(msg.indexOf(" Shard") + " Shard".length() + 1).trim();
            String name = msg.substring(0, msg.indexOf(" Shard"));
            return buildShardData(name, quantity.replace("x", "").replace("!", ""), ShardSource.Fused);
        }
        if (msg.startsWith("You sent ") && msg.endsWith(" to your Hunting Box.")) {
            msg = msg.replace("You sent ", "").replace(" to your Hunting Box.", "").trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" Shard"));
            return buildShardData(name, quantity, ShardSource.Absorbed);
        }
        if (msg.startsWith("⛃ GOOD CATCH! You caught ") && msg.endsWith(" Shard!")) {
            msg = msg.replace("⛃ GOOD CATCH! You caught ", "").replace(" Shard!", "").trim();
            if (msg.startsWith("an ") || msg.startsWith("a ")) {
                msg = msg.substring(msg.indexOf(" ") + 1);
            }
            return buildShardData(msg, "1", ShardSource.Fished);
        }
        if (msg.contains(" Shard (") && msg.endsWith(")")) {
            msg = msg.substring(0, msg.indexOf(" Shard (")).trim();
            return buildShardData(msg, "1", ShardSource.Gift);
        }
        return null;
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && !event.messagePlain.isEmpty() && Utils.isInSkyblock()) {
            Shard newShard = getShardFromMsg(event.messagePlain.trim());
            if (newShard != null && data.value().has("shards")) {
                for (JsonElement shard : data.value().get("shards").getAsJsonArray()) {
                    JsonObject shardData = shard.getAsJsonObject();
                    if (shardData.get("name").getAsString().equals(newShard.name)) {
                        shardData.addProperty("obtained", shardData.get("obtained").getAsLong() + newShard.quantity);
                        refreshDisplay();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (instance.isActive() && boxApply.value() && event.title.equals("Hunting Box")) {
            if (event.inventory.getStack(event.slotId).isEmpty() || !data.value().has("shards")) {
                return;
            }
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            if (!shards.isEmpty()) {
                for (String line : Utils.getLoreLines(event.stack)) {
                    if (line.startsWith("Owned: ")) {
                        String name = Formatting.strip(event.stack.getName().getString()).toLowerCase();
                        for (JsonElement shard : shards) {
                            JsonObject shardData = shard.getAsJsonObject();
                            if (name.equals(shardData.get("name").getAsString())) {
                                shardData.addProperty("obtained", PriceTooltips.getStackQuantity(event.stack, event.title));
                                refreshDisplay();
                                return;
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            refreshDisplay();
        }
    }

    public enum ShardSource {
        Caught,
        Fished,
        Charmed,
        Fused,
        Absorbed,
        Gift
    }

    public static class Shard {
        public String name;
        public int quantity;
        public ShardSource source;

        public Shard(String name, int quantity, ShardSource source) {
            this.name = name;
            this.quantity = quantity;
            this.source = source;
        }
    }

    public static class Setting extends FlowLayout {
        public int index;
        public FlatTextbox inputName;
        public FlatTextbox inputObtained;
        public FlatTextbox inputNeeded;
        public FlatTextbox inputSource;
        public ButtonComponent delete;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.index = index;
            this.inputName = new FlatTextbox(Sizing.fixed(75));
            this.inputName.margins(Insets.of(0, 0, 0, 4));
            this.inputName.tooltip(Text.literal("The name of the shard you want to track."));
            this.inputName.text(getData().get("name").getAsString());
            this.inputName.onChanged().subscribe(value -> {
                getData().addProperty("name", value.toLowerCase());
                refreshDisplay();
            });
            this.inputObtained = new FlatTextbox(Sizing.fixed(50));
            this.inputObtained.margins(Insets.of(0, 0, 0, 4));
            this.inputObtained.tooltip(Text.literal("The amount of this shard that you currently have."));
            this.inputObtained.text(String.valueOf(getData().get("obtained").getAsLong()));
            this.inputObtained.onChanged().subscribe(value -> {
                try {
                    getData().addProperty("obtained", Long.valueOf(value));
                    refreshDisplay();
                } catch (NumberFormatException ignored) {
                }
            });
            this.inputNeeded = new FlatTextbox(Sizing.fixed(50));
            this.inputNeeded.margins(Insets.of(0, 0, 0, 4));
            this.inputNeeded.tooltip(Text.literal("The amount of this shard that you want to obtain. Set to 0 for no target amount."));
            this.inputNeeded.text(String.valueOf(getData().get("needed").getAsLong()));
            this.inputNeeded.onChanged().subscribe(value -> {
                try {
                    getData().addProperty("needed", Long.valueOf(value));
                    refreshDisplay();
                } catch (NumberFormatException ignored) {
                }
            });
            this.inputSource = new FlatTextbox(Sizing.fixed(50));
            this.inputSource.margins(Insets.of(0, 0, 0, 4));
            this.inputSource.tooltip(Text.literal("The source that this shard is obtained from."));
            this.inputSource.text(getData().get("source").getAsString());
            this.inputSource.onChanged().subscribe(value -> {
                getData().addProperty("source", value);
                refreshDisplay();
            });
            this.delete = Components.button(Text.literal("Delete").withColor(0xffffff), button -> {
                data.value().get("shards").getAsJsonArray().remove(this.index);
                mc.setScreen(buildSettings());
            });
            this.delete.positioning(Positioning.relative(100, 0)).verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            this.delete.renderer((context, btn, delta) -> {
                context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
                context.drawBorder(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xffffffff);
            });
            this.child(this.inputName);
            this.child(this.inputObtained);
            this.child(this.inputNeeded);
            this.child(this.inputSource);
            this.child(this.delete);
        }

        public JsonObject getData() {
            return data.value().get("shards").getAsJsonArray().get(this.index).getAsJsonObject();
        }
    }
}
