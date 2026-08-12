package nofrills.features.hunting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingJson;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.events.SlotUpdateEvent;
import nofrills.features.general.PriceTooltips;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.EnumButton;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.misc.Rendering;
import nofrills.misc.ShardData;
import nofrills.misc.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static nofrills.Main.mc;

@EventListener
public final class ShardTracker {
    public static final Feature instance = new Feature("shardTracker");

    public static final SettingBool boxApply = new SettingBool(false, "load", instance.key());
    public static final SettingBool doneMsg = new SettingBool(false, "doneMsg", instance.key());
    public static final SettingBool filterFuse = new SettingBool(false, "filterFuse", instance.key());
    public static final SettingBool filterDirect = new SettingBool(false, "filterDirect", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    private static final Pattern sentToBoxPattern = Pattern.compile("You sent (?<quantity>a|an|[0-9]*) (?<name>.*) (?:Shard|Shards) to your Hunting Box.");
    private static final Pattern boughtPattern = Pattern.compile("You bought (?<name>.*)!");
    private static final List<Pattern> shardCaughtPatterns = List.of(
            Pattern.compile("You caught (?<quantity>a|an|x[0-9]*) (?<name>.*) (?:Shard|Shards)!"),
            Pattern.compile("CHARM! You charmed .* and received (?<quantity>[0-9]*) (?<name>.*) (?:Shard|Shards)!"),
            Pattern.compile("LOOT SHARE You received (?<quantity>a|an|[0-9]*) (?<name>.*) (?:Shard|Shards) for assisting .*!"),
            Pattern.compile("CAPTURE! You caught (?:a|an) .* and gained (?<quantity>a|an|[0-9]*x) (?<name>.*) (?:Shard|Shards)!"),
            Pattern.compile("CAPTURE! You caught (?:a|an) SPARKLING .* and received .* and (?<quantity>a|an|[0-9]*x) (?<name>.*) Shard!"),
            Pattern.compile("LOOT SHARE! You received (?<quantity>a|an|[0-9]*x) (?<name>.*) Shard from .* catching .*!"),
            Pattern.compile("LOOT SHARE! You received .* and (?<quantity>a|an|[0-9]*x) (?<name>.*) Shard from .* catching (?:a|an) SPARKLING .*!"),
            Pattern.compile("FLOOR DROP! You found (?<name>.*) Shard on the ground!"),
            Pattern.compile("You received (?<quantity>a|an|[0-9]*|x[0-9]*|[0-9]*x) (?<name>.*) (?:Shard|Shards)!"),
            Pattern.compile("FUSION! You obtained (?:a |an |)(?<name>.*) Shard(?<quantity> x[0-9]*|)!(?:| NEW!)"),
            Pattern.compile(Utils.Symbols.treasureCatch + " .* CATCH! You caught (?:a |an |)(?<name>.*) Shard(?<quantity> x[0-9]*|)!"),
            Pattern.compile(" *(?<name>.*) Shard \\(.*%\\)"),
            sentToBoxPattern,
            boughtPattern
    );

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        list.add(new Settings.Description("Usage", "Run the \"/nf shardTracker\" command to see more information."));
        list.add(new Settings.Toggle("Apply From Box", boxApply, "Automatically applies obtained amounts to shards when you open your Hunting Box."));
        list.add(new Settings.Toggle("Done Message", doneMsg, "Shows a message in chat once you reach the needed amount for any shard."));
        list.add(new Settings.Toggle("Filter Fuse", filterFuse, "Hides every Fuse/Cycle shard while outside of the Fusion Machine."));
        list.add(new Settings.Toggle("Filter Direct", filterDirect, "Hides every Direct/Bazaar shard while inside of the Fusion Machine."));
        Settings.BigButton clearButton = new Settings.BigButton("Clear Shard List", btn -> {
            data.edit(object -> object.add("shards", new JsonArray()));
            mc.setScreen(buildSettings());
        });
        clearButton.button.verticalSizing(Sizing.fixed(18));
        clearButton.button.tooltip(Component.literal("Clears the list of your tracked shards."));
        list.add(clearButton);
        Settings.BigButton importButton = new Settings.BigButton("Import Shard Tree", btn -> {
            importTreeData();
            mc.setScreen(buildSettings());
        });
        importButton.button.verticalSizing(Sizing.fixed(18));
        importButton.button.tooltip(Component.literal("Pastes the list of shards that you need to get."));
        list.add(importButton);
        Settings.BigButton button = new Settings.BigButton("Add New Shard", btn -> {
            data.edit(object -> {
                if (!object.has("shards")) {
                    object.add("shards", new JsonArray());
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("name", "");
                obj.addProperty("needed", 0L);
                obj.addProperty("obtained", 0L);
                obj.addProperty("source", "Direct");
                object.get("shards").getAsJsonArray().add(obj);
            });
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
        settings.setTitle(Component.literal("Shard Tracker"));
        return settings;
    }

    public static void importTreeData() {
        String clipboard = mc.keyboardHandler.getClipboard();
        JsonArray treeData = parseTreeData(clipboard);
        if (treeData == null) {
            Utils.info("§cFailed to import the fusion tree from the SkyShards calculator, no valid data found in your clipboard.");
            return;
        }
        try {
            data.edit(object -> {
                if (!object.has("shards")) {
                    object.add("shards", new JsonArray());
                }
                JsonArray shards = object.get("shards").getAsJsonArray();
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
                    JsonObject obj = new JsonObject();
                    obj.addProperty("name", name);
                    obj.addProperty("needed", needed);
                    obj.addProperty("obtained", 0L);
                    obj.addProperty("source", source);
                    shards.add(obj);
                }
            });
        } catch (Exception ignored) {
            Utils.info("§cSuccessfully read the fusion tree data, but an unknown error occurred while importing. Try updating the mod to the newest version.");
            return;
        }
        Utils.info("§aShard list imported successfully.");
    }

    public static boolean shouldFilter(TrackerSource source) {
        return switch (source) {
            case Direct, Bazaar -> filterDirect.value() && isInFusion();
            case Fuse, Cycle -> filterFuse.value() && !isInFusion();
        };
    }

    public static String getSourceColor(String source) {
        return switch (Utils.toLower(source)) {
            case "direct", "bazaar" -> "§a";
            case "fuse" -> "§d";
            case "cycle" -> "§6";
            default -> "§7";
        };
    }

    public static TrackerSource getTrackedSource(String source) {
        for (TrackerSource value : TrackerSource.values()) {
            if (value.name().equals(source)) {
                return value;
            }
        }
        return TrackerSource.Direct;
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
        if (mc.screen instanceof ContainerScreen container) {
            String title = container.getTitle().getString();
            return title.equals("Fusion Box") || title.equals("Confirm Fusion");
        }
        return false;
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && !event.messagePlain.trim().isEmpty() && data.value().has("shards")) {
            for (Pattern pattern : shardCaughtPatterns) {
                Matcher matcher = pattern.matcher(event.messagePlain);
                if (!matcher.matches()) continue;
                String name = matcher.group("name");
                if (name == null) continue;
                if (pattern == sentToBoxPattern && ShardData.getShardSkill(name).equals("Fishing")) return;
                if (pattern == boughtPattern && ShardData.getShardSkill(name).isEmpty()) return;
                int quantity = matcher.namedGroups().containsKey("quantity")
                        ? Utils.parseInt(matcher.group("quantity").replace("x", "")).orElse(1)
                        : 1;
                JsonObject tracked = getTrackedShard(Utils.toLower(name));
                if (tracked != null) {
                    long needed = tracked.get("needed").getAsLong();
                    long obtained = tracked.get("obtained").getAsLong();
                    long newQuantity = obtained + quantity;
                    if (doneMsg.value() && needed != 0 && obtained < needed && newQuantity >= needed) {
                        String shardName = tracked.get("name").getAsString();
                        Utils.infoFormat("{}§l{} §r§aShard done! {}/{}x obtained.",
                                ShardData.getColorPrefix(shardName),
                                Utils.uppercaseFirst(shardName, false),
                                Utils.formatSeparator(newQuantity),
                                Utils.formatSeparator(needed)
                        );
                    }
                    data.edit(_ -> tracked.addProperty("obtained", obtained + quantity));
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && boxApply.value() && event.isPaginatedMenu("Hunting Box") && !event.isInventory && data.value().has("shards")) {
            JsonArray shards = data.value().get("shards").getAsJsonArray();
            if (!shards.isEmpty()) {
                for (String line : Utils.getLoreLines(event.stack)) {
                    if (line.startsWith("Owned: ")) {
                        String name = Utils.toLower(Utils.toPlain(event.stack.getHoverName()));
                        JsonObject tracked = getTrackedShard(name);
                        if (tracked != null) {
                            data.edit(_ -> tracked.addProperty("obtained", PriceTooltips.getStackQuantity(event.stack)));
                        }
                        break;
                    }
                }
            }
        }
    }

    public enum TrackerSource {
        Direct,
        Fuse,
        Cycle,
        Bazaar
    }

    public static final class Setting extends FlowLayout {
        public int index;
        public FlatTextbox inputName;
        public FlatTextbox inputObtained;
        public FlatTextbox inputNeeded;
        public EnumButton<TrackerSource> inputSource;
        public ButtonComponent delete;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.index = index;
            this.inputName = new FlatTextbox(Sizing.fixed(80));
            this.inputName.margins(Insets.of(0, 0, 0, 5));
            this.inputName.tooltip(Component.literal("The name of the shard you want to track."));
            this.inputName.text(getData().get("name").getAsString());
            this.inputName.borderColor = ShardData.getColorHex(getData().get("name").getAsString());
            this.inputName.onChanged().subscribe(value -> {
                data.edit(object -> getData(object).addProperty("name", Utils.toLower(value)));
                this.inputName.borderColor = ShardData.getColorHex(Utils.toLower(value));
            });
            this.inputObtained = new FlatTextbox(Sizing.fixed(50));
            this.inputObtained.margins(Insets.of(0, 0, 0, 5));
            this.inputObtained.tooltip(Component.literal("The amount of this shard that you currently have."));
            this.inputObtained.text(String.valueOf(getData().get("obtained").getAsLong()));
            this.inputObtained.onChanged().subscribe(text -> Utils.parseLong(text).ifPresent(value -> data.edit(object -> getData(object).addProperty("obtained", value))));
            this.inputNeeded = new FlatTextbox(Sizing.fixed(50));
            this.inputNeeded.margins(Insets.of(0, 0, 0, 5));
            this.inputNeeded.tooltip(Component.literal("The amount of this shard that you want to obtain. Set to 0 for no target amount."));
            this.inputNeeded.text(String.valueOf(getData().get("needed").getAsLong()));
            this.inputNeeded.onChanged().subscribe(text -> Utils.parseLong(text).ifPresent(value -> data.edit(object -> getData(object).addProperty("needed", value))));
            this.inputSource = new EnumButton<>(getData().get("source").getAsString(), TrackerSource.Direct, TrackerSource.class);
            this.inputSource.setMessage(this.getSourceInputLabel(getData().get("source").getAsString()));
            this.inputSource.onChanged().subscribe(value -> {
                data.edit(object -> getData(object).addProperty("source", value));
                this.inputSource.setMessage(this.getSourceInputLabel(value));
            });
            this.inputSource.margins(Insets.of(1, 0, 0, 0));
            this.inputSource.sizing(Sizing.fixed(48), Sizing.fixed(18));
            this.inputSource.tooltip(Component.literal("The source that this shard is obtained from. Click to rotate."));
            this.delete = UIComponents.button(Component.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("shards").getAsJsonArray().remove(this.index));
                mc.setScreen(buildSettings());
            });
            this.delete.positioning(Positioning.relative(100, 0)).verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            this.delete.renderer((context, btn, delta) -> {
                context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
                Rendering.drawBorder(context, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xffffffff);
            });
            this.child(this.inputName);
            this.child(this.inputObtained);
            this.child(this.inputNeeded);
            this.child(this.inputSource);
            this.child(this.delete);
        }

        public JsonObject getData(JsonObject object) {
            return object.get("shards").getAsJsonArray().get(this.index).getAsJsonObject();
        }

        public JsonObject getData() {
            return this.getData(data.value());
        }

        public MutableComponent getSourceInputLabel(String source) {
            return Component.literal(Utils.format("{}{}", getSourceColor(source), source));
        }
    }
}
