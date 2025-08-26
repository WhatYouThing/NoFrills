package nofrills.features.hunting;

import com.google.common.collect.Lists;
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
import nofrills.misc.ShardData;
import nofrills.misc.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static nofrills.Main.mc;

public class ShardTracker {
    public static final Feature instance = new Feature("shardTracker");

    public static final SettingBool boxApply = new SettingBool(false, "load", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    public static final MutableText displayNone = Text.literal("§bShard Tracker\n§7None tracked.");
    public static MutableText display = displayNone;

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        list.add(new Settings.Separator("Usage"));
        list.add(new Settings.Description("Importing", "Click Copy Tree on the calculator, choose the NoFrills format, and click the Import Shard List button below."));
        list.add(new Settings.Description("Tracking", "Enable this feature, and then enable the Shard Tracker element in the NoFrills HUD editor."));
        list.add(new Settings.Separator("Settings"));
        Settings.Toggle boxToggle = new Settings.Toggle("Apply From Box", boxApply, "Automatically applies obtained amounts to shards when you open your Hunting Box.");
        list.add(boxToggle);
        list.add(new Settings.Separator("Shards"));
        Settings.BigButton importButton = new Settings.BigButton("Import Shard List", btn -> {
            if (!data.value().has("shards")) {
                data.value().add("shards", new JsonArray());
            }
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            List<JsonObject> objects = new ArrayList<>();
            String clipboard = mc.keyboard.getClipboard();
            JsonArray treeData = parseTreeData(clipboard);
            if (treeData == null) {
                Utils.info("§cFailed to import the fusion tree from the SkyShards calculator, no valid data found in your clipboard.");
                return;
            }
            try {
                for (JsonElement element : treeData) {
                    JsonObject shardData = element.getAsJsonObject();
                    JsonObject object = new JsonObject();
                    object.addProperty("name", shardData.get("name").getAsString().toLowerCase());
                    object.addProperty("needed", shardData.get("needed").getAsLong());
                    object.addProperty("obtained", 0L);
                    object.addProperty("source", shardData.get("source").getAsString());
                    objects.add(object);
                }
            } catch (Exception ignored) {
                Utils.info("§cSuccessfully read the fusion tree dara, but an unknown error occurred while importing. Try updating the mod to the newest version.");
                return;
            }
            for (JsonObject object : objects) {
                shards.add(object);
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

    private static JsonArray parseTreeData(String payload) {
        try {
            String data = payload.substring(payload.indexOf(":") + 1);
            GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
            return JsonParser.parseReader(new InputStreamReader(gzipStream, StandardCharsets.UTF_8)).getAsJsonArray();
        } catch (Exception ignored) {
        }
        return null;
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
                String shardName = Utils.format("{}§l{}", getShardColor(name.toLowerCase()), Utils.uppercaseFirst(name, false));
                String shardSource = Utils.format("{}[{}]", getSourceColor(source), source);
                String quantityColor = needed > 0 & obtained >= needed ? "§a" : "§f";
                String shardQuantity = needed <= 0 ? Utils.format("{}x", Utils.formatSeparator(obtained)) : Utils.format("{}/{}x", Utils.formatSeparator(obtained), Utils.formatSeparator(needed));
                lines.add(Utils.format("{}{}§r {}§r {}", quantityColor, shardQuantity, shardName, shardSource));
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
            msg = msg.replace("LOOT SHARE You received ", "").trim();
            msg = msg.substring(0, msg.indexOf(" Shard")).trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1);
            return buildShardData(name, quantity, ShardSource.Lootshare);
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
        if (msg.startsWith("⛃ ") && msg.contains(" CATCH! You caught ") && msg.endsWith(" Shard!")) {
            msg = msg.substring(msg.indexOf(" CATCH! You caught ") + 19).replace(" Shard!", "").trim();
            if (msg.startsWith("an ") || msg.startsWith("a ")) {
                msg = msg.substring(msg.indexOf(" ") + 1);
            }
            return buildShardData(msg, "1", ShardSource.TreasureCatch);
        }
        if (msg.contains(" Shard (") && msg.endsWith(")")) {
            msg = msg.substring(0, msg.indexOf(" Shard (")).trim();
            return buildShardData(msg, "1", ShardSource.TreeGift);
        }
        return null;
    }

    public static String getSourceColor(String source) {
        return switch (source.toLowerCase()) {
            case "direct", "bazaar" -> "§a";
            case "fuse" -> "§d";
            case "cycle" -> "§6";
            default -> "§7";
        };
    }

    private static TrackerSource getTrackedSource(String source) {
        for (TrackerSource value : TrackerSource.values()) {
            if (value.name().equals(source)) {
                return value;
            }
        }
        return TrackerSource.Direct;
    }

    public static String getShardColor(String shard) {
        if (ShardData.legendaryShards.contains(shard)) return "§6";
        if (ShardData.epicShards.contains(shard)) return "§5";
        if (ShardData.rareShards.contains(shard)) return "§9";
        if (ShardData.uncommonShards.contains(shard)) return "§a";
        if (ShardData.commonShards.contains(shard)) return "§f";
        return "§7";
    }

    public static int getShardColorHex(String shard) {
        if (ShardData.legendaryShards.contains(shard)) return 0xffffaa00;
        if (ShardData.epicShards.contains(shard)) return 0xffaa00aa;
        if (ShardData.rareShards.contains(shard)) return 0xff5555ff;
        if (ShardData.uncommonShards.contains(shard)) return 0xff55ff55;
        if (ShardData.commonShards.contains(shard)) return 0xffffffff;
        return 0xffaaaaaa;
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && !event.messagePlain.isEmpty() && Utils.isInSkyblock()) {
            Shard newShard = getShardFromMsg(event.messagePlain.trim());
            if (newShard != null && data.value().has("shards")) {
                if (newShard.source.equals(ShardSource.Absorbed) && ShardData.fishingShards.contains(newShard.name)) {
                    return;
                }
                for (JsonElement shard : Lists.reverse(data.value().get("shards").getAsJsonArray().asList())) {
                    JsonObject shardData = shard.getAsJsonObject();
                    if (shardData.get("name").getAsString().equals(newShard.name)) {
                        long needed = shardData.get("needed").getAsLong();
                        long obtained = shardData.get("obtained").getAsLong();
                        if (needed == 0 || obtained < needed) {
                            shardData.addProperty("obtained", obtained + newShard.quantity);
                            refreshDisplay();
                            return;
                        }
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
                        break;
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
        getShardFromMsg("LOOT SHARE You received 2 Mossybit Shards for assisting etic1118!");
    }

    public enum ShardSource {
        Caught,
        Lootshare,
        TreasureCatch,
        Charmed,
        Fused,
        Absorbed,
        TreeGift
    }

    public enum TrackerSource {
        Direct,
        Fuse,
        Cycle,
        Bazaar
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
        public ButtonComponent inputSource;
        public ButtonComponent delete;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.index = index;
            this.inputName = new FlatTextbox(Sizing.fixed(80));
            this.inputName.margins(Insets.of(0, 0, 0, 5));
            this.inputName.tooltip(Text.literal("The name of the shard you want to track."));
            this.inputName.text(getData().get("name").getAsString());
            this.inputName.borderColor = getShardColorHex(getData().get("name").getAsString());
            this.inputName.onChanged().subscribe(value -> {
                getData().addProperty("name", value.toLowerCase());
                this.inputName.borderColor = getShardColorHex(value.toLowerCase());
                refreshDisplay();
            });
            this.inputObtained = new FlatTextbox(Sizing.fixed(50));
            this.inputObtained.margins(Insets.of(0, 0, 0, 5));
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
            this.inputNeeded.margins(Insets.of(0, 0, 0, 5));
            this.inputNeeded.tooltip(Text.literal("The amount of this shard that you want to obtain. Set to 0 for no target amount."));
            this.inputNeeded.text(String.valueOf(getData().get("needed").getAsLong()));
            this.inputNeeded.onChanged().subscribe(value -> {
                try {
                    getData().addProperty("needed", Long.valueOf(value));
                    refreshDisplay();
                } catch (NumberFormatException ignored) {
                }
            });
            this.inputSource = Components.button(this.getSourceInputLabel(getTrackedSource(getData().get("source").getAsString())), button -> {
                TrackerSource[] values = TrackerSource.values();
                TrackerSource source = getTrackedSource(getData().get("source").getAsString());
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(source)) {
                        TrackerSource newSource = i == values.length - 1 ? values[0] : values[i + 1];
                        getData().addProperty("source", newSource.name());
                        this.inputSource.setMessage(this.getSourceInputLabel(newSource));
                        refreshDisplay();
                        return;
                    }
                }
                getData().addProperty("source", TrackerSource.Direct.name());
                this.inputSource.setMessage(this.getSourceInputLabel(TrackerSource.Direct));
                refreshDisplay();
            });
            this.inputSource.renderer((context, button, delta) -> {
                context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
                context.drawBorder(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
            });
            this.inputSource.margins(Insets.of(1, 0, 0, 0));
            this.inputSource.sizing(Sizing.fixed(48), Sizing.fixed(18));
            this.inputSource.tooltip(Text.literal("The source that this shard is obtained from. Click to rotate."));
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

        public MutableText getSourceInputLabel(TrackerSource source) {
            return Text.literal(Utils.format("{}{}", getSourceColor(source.name()), source.name()));
        }
    }
}
