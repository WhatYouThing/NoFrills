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
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingJson;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.SlotUpdateEvent;
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
    public static final SettingBool doneMsg = new SettingBool(false, "doneMsg", instance.key());
    public static final SettingBool filterFuse = new SettingBool(false, "filterFuse", instance.key());
    public static final SettingBool filterDirect = new SettingBool(false, "filterDirect", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    public static final MutableText displayNone = Text.literal("§bShard Tracker\n§7None tracked.");
    public static MutableText display = displayNone;

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        list.add(new Settings.Description("Usage", "Run the \"/nf shardTracker\" command to see more information."));
        list.add(new Settings.Toggle("Apply From Box", boxApply, "Automatically applies obtained amounts to shards when you open your Hunting Box."));
        list.add(new Settings.Toggle("Done Message", doneMsg, "Shows a message in chat once you reach the needed amount for any shard."));
        list.add(new Settings.Toggle("Filter Fuse", filterFuse, "Hides every Fuse/Cycle shard while outside of the Fusion Machine."));
        list.add(new Settings.Toggle("Filter Direct", filterDirect, "Hides every Direct/Bazaar shard while inside of the Fusion Machine."));
        Settings.BigButton clearButton = new Settings.BigButton("Clear Shard List", btn -> {
            data.value().add("shards", new JsonArray());
            mc.setScreen(buildSettings());
        });
        clearButton.button.verticalSizing(Sizing.fixed(18));
        clearButton.button.tooltip(Text.literal("Clears the list of your tracked shards."));
        list.add(clearButton);
        Settings.BigButton importButton = new Settings.BigButton("Import Shard Tree", btn -> {
            importTreeData();
            mc.setScreen(buildSettings());
        });
        importButton.button.verticalSizing(Sizing.fixed(18));
        importButton.button.tooltip(Text.literal("Pastes the list of shards that you need to get."));
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

    public static void importTreeData() {
        if (!data.value().has("shards")) {
            data.value().add("shards", new JsonArray());
        }
        JsonArray shards = data.value().get("shards").getAsJsonArray();
        String clipboard = mc.keyboard.getClipboard();
        JsonArray treeData = parseTreeData(clipboard);
        if (treeData == null) {
            Utils.info("§cFailed to import the fusion tree from the SkyShards calculator, no valid data found in your clipboard.");
            return;
        }
        try {
            for (JsonElement element : treeData) {
                JsonObject shardData = element.getAsJsonObject();
                String name = Utils.toLower(shardData.get("name").getAsString());
                long needed = shardData.get("needed").getAsLong();
                String source = shardData.get("source").getAsString();
                JsonObject tracked = getTrackedShard(name);
                if (tracked != null && tracked.get("source").getAsString().equals(source)) {
                    tracked.addProperty("needed", tracked.get("needed").getAsLong() + needed);
                    continue; // add the needed amount to the shard if its already being tracked under the same source
                }
                JsonObject object = new JsonObject();
                object.addProperty("name", name);
                object.addProperty("needed", needed);
                object.addProperty("obtained", 0L);
                object.addProperty("source", source);
                shards.add(object);
            }
        } catch (Exception ignored) {
            Utils.info("§cSuccessfully read the fusion tree data, but an unknown error occurred while importing. Try updating the mod to the newest version.");
            return;
        }
        Utils.info("§aShard list imported successfully.");
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

    private static JsonObject getTrackedShard(String shardName) {
        List<JsonObject> trackedList = new ArrayList<>();
        for (JsonElement shard : data.value().get("shards").getAsJsonArray()) {
            JsonObject shardData = shard.getAsJsonObject();
            if (shardData.get("name").getAsString().equals(shardName)) {
                trackedList.add(shardData);
            }
        }
        if (!trackedList.isEmpty()) { // scuffed order handling for if the same shard has multiple tracker entries
            return trackedList.size() == 1 ? trackedList.getFirst() : trackedList.reversed().stream().filter(shard -> {
                long needed = shard.get("needed").getAsLong();
                long obtained = shard.get("obtained").getAsLong();
                return needed == 0 || obtained < needed;
            }).findFirst().orElse(trackedList.getFirst());
        }
        return null;
    }

    private static boolean isInFusion() {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            return title.equals("Fusion Box") || title.equals("Confirm Fusion");
        }
        return false;
    }

    private static boolean shouldFilter(TrackerSource source) {
        return switch (source) {
            case Direct, Bazaar -> filterDirect.value() && isInFusion();
            case Fuse, Cycle -> filterFuse.value() && !isInFusion();
        };
    }

    public static void refreshDisplay() {
        if (data.value().has("shards")) {
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            List<String> lines = new ArrayList<>();
            for (JsonElement shard : shards) {
                JsonObject shardData = shard.getAsJsonObject();
                String name = shardData.get("name").getAsString();
                long needed = shardData.get("needed").getAsLong();
                long obtained = shardData.get("obtained").getAsLong();
                String source = shardData.get("source").getAsString();
                if (name.isEmpty() || shouldFilter(getTrackedSource(source))) {
                    continue;
                }
                String shardName = Utils.format("{}§l{}", ShardData.getColorPrefix(Utils.toLower(name)), Utils.uppercaseFirst(name, false));
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

    private static Shard getShardFromMsg(String msg) { // parses various messages about obtaining shards, do not touch
        if (msg.startsWith("You caught ") && (msg.endsWith("Shards!") || msg.endsWith("Shard!"))) {
            msg = msg.replace("You caught ", "").replace(" Shards!", "").replace(" Shard!", "").trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1);
            return Shard.of(name, quantity.replace("x", ""), ShardSource.Caught);
        }
        if (msg.startsWith("LOOT SHARE You received ") && (msg.contains(" Shard for assisting ") || msg.contains(" Shards for assisting "))) {
            msg = msg.replace("LOOT SHARE You received ", "").trim();
            msg = msg.substring(0, msg.indexOf(" Shard")).trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1);
            return Shard.of(name, quantity, ShardSource.Lootshare);
        }
        if (msg.startsWith("CHARM ") || msg.startsWith("NAGA ") || msg.startsWith("SALT ")) {
            int index = msg.indexOf("You charmed a ");
            if (index != -1) {
                msg = msg.substring(index + "You charmed a ".length());
                int quantityStart = msg.indexOf(" and captured") + " and captured".length() + 1;
                String quantity = msg.substring(quantityStart, msg.indexOf(" ", quantityStart));
                String name = msg.substring(0, msg.indexOf(" and captured"));
                return Shard.of(name, quantity, ShardSource.Charmed);
            }
        }
        if (msg.startsWith("FUSION! You obtained ") && msg.contains(" Shard")) {
            msg = msg.replace("FUSION! You obtained ", "").replace(" NEW!", "").trim();
            if (msg.startsWith("an ") || msg.startsWith("a ")) {
                msg = msg.substring(msg.indexOf(" ") + 1);
            }
            String quantity = msg.substring(msg.indexOf(" Shard") + " Shard".length() + 1).trim();
            String name = msg.substring(0, msg.indexOf(" Shard"));
            return Shard.of(name, quantity.replace("x", "").replace("!", ""), ShardSource.Fused);
        }
        if (msg.startsWith("You sent ") && msg.endsWith(" to your Hunting Box.")) {
            msg = msg.replace("You sent ", "").replace(" to your Hunting Box.", "").trim();
            String quantity = msg.substring(0, msg.indexOf(" "));
            String name = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" Shard"));
            return Shard.of(name, quantity, ShardSource.Absorbed);
        }
        if (msg.startsWith("⛃ ") && msg.contains(" CATCH! You caught ") && msg.endsWith(" Shard!")) {
            msg = msg.substring(msg.indexOf(" CATCH! You caught ") + 19).replace(" Shard!", "").trim();
            if (msg.startsWith("an ") || msg.startsWith("a ")) {
                msg = msg.substring(msg.indexOf(" ") + 1);
            }
            return new Shard(msg, 1, ShardSource.TreasureCatch);
        }
        if (msg.contains(" Shard (") && msg.endsWith(")")) {
            msg = msg.substring(0, msg.indexOf(" Shard (")).trim();
            return new Shard(msg, 1, ShardSource.TreeGift);
        }
        return null;
    }

    public static String getSourceColor(String source) {
        return switch (Utils.toLower(source)) {
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

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && !event.messagePlain.isEmpty() && Utils.isInSkyblock()) {
            Shard shard = getShardFromMsg(event.messagePlain.trim());
            if (shard != null && data.value().has("shards")) {
                if (shard.source.equals(ShardSource.Absorbed) && ShardData.fishingShards.contains(shard.name)) {
                    return;
                }
                JsonObject tracked = getTrackedShard(shard.name);
                if (tracked != null) {
                    long needed = tracked.get("needed").getAsLong();
                    long obtained = tracked.get("obtained").getAsLong();
                    long quantity = obtained + shard.quantity;
                    if (doneMsg.value() && needed != 0 && obtained < needed && quantity >= needed) {
                        String name = tracked.get("name").getAsString();
                        Utils.infoFormat("{}§l{} §r§aShard done! {}/{}x obtained.",
                                ShardData.getColorPrefix(name),
                                Utils.uppercaseFirst(name, false),
                                Utils.formatSeparator(quantity),
                                Utils.formatSeparator(needed)
                        );
                    }
                    tracked.addProperty("obtained", obtained + shard.quantity);
                    refreshDisplay();
                }
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && boxApply.value() && event.title.equals("Hunting Box")) {
            if (event.isInventory || !data.value().has("shards")) {
                return;
            }
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            if (!shards.isEmpty()) {
                for (String line : Utils.getLoreLines(event.stack)) {
                    if (line.startsWith("Owned: ")) {
                        String name = Utils.toLower(Utils.toPlainString(event.stack.getName()));
                        JsonObject tracked = getTrackedShard(name);
                        if (tracked != null) {
                            tracked.addProperty("obtained", PriceTooltips.getStackQuantity(event.stack, event.title));
                            refreshDisplay();
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive()) {
            refreshDisplay();
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

        public static Shard of(String name, String quantity, ShardSource source) {
            String shardName = Utils.toLower(name);
            try {
                int amount = Integer.parseInt(quantity);
                return new Shard(shardName, amount, source);
            } catch (NumberFormatException ignored) {
            }
            return new Shard(shardName, 1, source);
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
            this.inputName.borderColor = ShardData.getColorHex(getData().get("name").getAsString());
            this.inputName.onChanged().subscribe(value -> {
                getData().addProperty("name", Utils.toLower(value));
                this.inputName.borderColor = ShardData.getColorHex(Utils.toLower(value));
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
