package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingString;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.ScreenOptions;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class LeapOverlay {
    public static final Feature instance = new Feature("leapOverlay");

    public static final SettingBool send = new SettingBool(false, "send", instance.key());
    public static final SettingString message = new SettingString("/pc Leaped to {name}!", "message", instance.key());
    public static final SettingColor healer = new SettingColor(RenderColor.fromHex(0xecb50c), "healerColor", instance.key());
    public static final SettingColor mage = new SettingColor(RenderColor.fromHex(0x1793c4), "mageColor", instance.key());
    public static final SettingColor bers = new SettingColor(RenderColor.fromHex(0xe7413c), "bersColor", instance.key());
    public static final SettingColor arch = new SettingColor(RenderColor.fromHex(0x4a14b7), "archColor", instance.key());
    public static final SettingColor tank = new SettingColor(RenderColor.fromHex(0x768f46), "tankColor", instance.key());

    public static final String leapMenuName = "Spirit Leap";
    public static final RenderColor nameColor = RenderColor.fromHex(0xffffff);
    private static final RenderColor deadColor = RenderColor.fromHex(0xaaaaaa);

    private static RenderColor getColor(String className) {
        return switch (className) {
            case "Healer" -> healer.value();
            case "Mage" -> mage.value();
            case "Berserk" -> bers.value();
            case "Archer" -> arch.value();
            case "Tank" -> tank.value();
            case "DEAD" -> deadColor;
            default -> nameColor;
        };
    }

    @EventHandler
    private static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (instance.isActive() && event.isFinal && event.title.equals(leapMenuName) && Utils.isInDungeons()) {
            List<LeapTarget> validTargets = new ArrayList<>();
            List<LeapTarget> deadTargets = new ArrayList<>();
            for (Slot slot : event.handler.slots) {
                ItemStack stack = event.inventory.getStack(slot.id);
                if (!stack.isEmpty() && stack.getItem().equals(Items.PLAYER_HEAD)) {
                    LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
                    if (lore != null) {
                        String line = Formatting.strip(lore.lines().getFirst().getString());
                        String name = Formatting.strip(stack.getName().getString());
                        if (name.equals("Unknown Player") || line.equals("This player is offline!")) {
                            continue;
                        }
                        if (line.equals("This player is currently dead!")) {
                            deadTargets.add(new LeapTarget(-1, name, "", false, true));
                        } else if (line.equals("Click to teleport!")) {
                            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                                Text displayName = entry.getDisplayName();
                                if (displayName != null) {
                                    String entryName = Formatting.strip(displayName.getString());
                                    if (entryName.contains(name)) {
                                        for (String dungeonClass : SkyblockData.dungeonClasses) {
                                            if (entryName.contains("(" + dungeonClass) && entryName.endsWith(")")) {
                                                int classStart = entryName.indexOf("("); // this may or may not be broken with a nick hider. too bad!
                                                int classEnd = Math.min(entryName.indexOf(" ", classStart), entryName.indexOf(")", classStart));
                                                validTargets.add(new LeapTarget(slot.id, name, entryName.substring(classStart + 1, classEnd), false, false));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            validTargets.sort(Comparator.comparing(target -> target.dungeonClass + target.name));
            deadTargets.sort(Comparator.comparing(target -> target.name));
            List<LeapTarget> targets = new ArrayList<>();
            targets.addAll(validTargets);
            targets.addAll(deadTargets);
            if (targets.size() < 4) {
                int missing = 4 - targets.size();
                for (int i = 1; i <= missing; i++) {
                    targets.add(new LeapTarget(-1, "", "", true, false));
                }
            }
            for (LeapTarget target : targets) {
                String name = target.empty ? "Empty" : target.name;
                String dungeonClass = target.dead ? "DEAD" : target.dungeonClass;
                ((ScreenOptions) event.screen).nofrills_mod$addLeapButton(target.slotId, name, dungeonClass, getColor(dungeonClass));
            }
        }
    }

    private static class LeapTarget {
        public int slotId;
        public String name;
        public String dungeonClass;
        public boolean empty;
        public boolean dead;

        public LeapTarget(int slotId, String name, String dungeonClass, boolean empty, boolean dead) {
            this.slotId = slotId;
            this.name = name;
            this.dungeonClass = dungeonClass;
            this.empty = empty;
            this.dead = dead;
        }
    }
}
