package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.SoundPitch;
import nofrills.misc.Utils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static nofrills.Main.mc;
import static nofrills.misc.Utils.getLoreLines;

public class BeaconTuningSolver {
    public static final Feature instance = new Feature("beaconTuningSolver");
    private static final List<Identifier> EXCLUDED_COLORS = List.of(Identifier.of("minecraft", "gray_stained_glass_pane"), Identifier.of("minecraft", "light_gray_stained_glass_pane"), Identifier.of("minecraft", "black_stained_glass_pane"));

    private static final List<Item> colorsOrder = createColorsOrder();

    private static List<Item> createColorsOrder() {
        return StreamSupport.stream(Registries.ITEM.spliterator(), false).filter(item -> {
            Identifier id = Registries.ITEM.getId(item);
            return id.getPath().endsWith("_stained_glass_pane");
        }).filter(item -> !EXCLUDED_COLORS.contains(Registries.ITEM.getId(item))).sorted(Comparator.comparingInt(Registries.ITEM::getRawId)).collect(Collectors.toList());
    }

    private static int tickCounter = 0;
    private static int lastMatchTick = 0;
    public static int matchSpeed = 0;
    public static int changeSpeed = 0;
    private static Item matchColor = null;
    private static Item changeColor = null;
    public static int colorSlot1Id = -1;
    public static int speedSlot1Id = -1;
    public static int pitchSlot1Id = -1;
    private static boolean isPaused = false;
    private static PitchType changePitch = null;
    public static int colorTarget1 = 0;
    public static HashSet<PitchType> heardPitch = new HashSet<>();

    public enum TuningType {
        Normal, Upgrade, None
    }

    public enum PitchType {
        Low(0.0952381f), Normal(0.7936508f), High(1.4920635f);

        public final SoundPitch value;

        PitchType(float v) {
            this.value = SoundPitch.from(v);
        }

        public static PitchType match(float v) {
            for (PitchType pitchType : values()) {
                if (SoundPitch.from(v).equals(pitchType.value)) {
                    return pitchType;
                }
            }
            return null; // realistically never happens
        }
    }

    public static TuningType getTuningType() {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.startsWith("Tune Frequency")) {
                return TuningType.Normal;
            }
            if (title.startsWith("Upgrade Signal Strength")) {
                return TuningType.Upgrade;
            }
        }
        return TuningType.None;
    }

    public static int getTileSpeed(int tickDelta) {
        return 6 - Math.floorDiv(tickDelta, 10);
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive()) {
            tickCounter++;
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive()) {
            tickCounter = 0;
            lastMatchTick = 0;
            matchSpeed = 0;
            changeSpeed = 0;
            matchColor = null;
            changeColor = null;
            colorSlot1Id = -1;
            speedSlot1Id = -1;
            pitchSlot1Id = -1;
            isPaused = false;
            changePitch = null;
            colorTarget1 = 0;
            heardPitch.clear();
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        TuningType tuningType = getTuningType();
        if (event.isSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS)) {
            Utils.infoFormat("change pitch: {}, heard: {}", changePitch, heardPitch);
            switch (tuningType) {
                case TuningType.Normal -> {
                    PitchType soundPitch = PitchType.match(event.packet.getPitch());
                    if (changePitch != null) {
                        heardPitch.add(soundPitch);
                        heardPitch.remove(changePitch);
                    }
                }
                case TuningType.Upgrade -> {} // TODO
                case TuningType.None -> {}
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (!instance.isActive() || event.inventory.getStack(event.slotId).isEmpty()) {
            return;
        }
        TuningType tuningType = getTuningType();

        switch (tuningType) {
            case TuningType.Normal -> {
                Slot slot = event.handler.getSlot(event.slotId);
                if (event.stack.getName().getString().startsWith("Pause")) {
                    getLoreLines(event.stack).stream().filter(s -> s.contains("Currently")).findFirst().ifPresent(pauseLine -> isPaused = !pauseLine.contains("UNPAUSED"));
                }
                if (event.stack.getName().getString().startsWith("Color")) {
                    colorSlot1Id = event.slotId;
                    int matchRoll = colorsOrder.indexOf(matchColor);
                    int changeRoll = colorsOrder.indexOf(changeColor);
                    int target1 = (matchRoll-changeRoll+colorsOrder.size()) % colorsOrder.size();
                    int target2 = (changeRoll-matchRoll+colorsOrder.size()) % colorsOrder.size();
                    colorTarget1 = target1 < target2 ? -target1 : target2;

                }
                if (event.stack.getName().getString().startsWith("Pitch")) {
                    pitchSlot1Id = event.slotId;
                    getLoreLines(event.stack).stream().filter(s -> s.contains("Current pitch:")).findFirst().ifPresent(x -> {
                        if (x.endsWith("Low")) changePitch = PitchType.Low;
                        if (x.endsWith("Normal")) changePitch = PitchType.Normal;
                        if (x.endsWith("High")) changePitch = PitchType.High;
                    });
                }
                if (event.stack.getName().getString().startsWith("Speed")) {
                    speedSlot1Id = event.slotId;
                    getLoreLines(event.stack).stream().filter(s -> s.contains("Current speed")).findFirst().ifPresent(x -> {
                        changeSpeed = x.charAt(x.length() - 1) - '0';
                        if (matchSpeed > 0 && matchSpeed == changeSpeed) {
                            SlotOptions.setBackground(slot, RenderColor.green);
                        } else if (matchSpeed > 0) {
                            SlotOptions.setBackground(slot, RenderColor.red);
                        }
                    });
                }
                // 1/2/3/4/5 speed matches to 55/45/35/25/15 ticks delays accordingly
                if (event.stack.getName().getString().startsWith("Match the Beat")) {
                    matchSpeed = getTileSpeed(tickCounter - lastMatchTick);
                    if (colorsOrder.contains(event.stack.getItem())) {
                        lastMatchTick = tickCounter;
                        matchColor = event.stack.getItem();
                    }
                    if (colorSlot1Id != -1) {
                        if (changeColor != null && changeColor.equals(matchColor)) {
                            SlotOptions.disableSlot(event.handler.getSlot(colorSlot1Id), true);
                        } else if (changeColor != null && !changeColor.equals(matchColor)) {
                            SlotOptions.disableSlot(event.handler.getSlot(colorSlot1Id), false);
                        }
                    }
                    if (pitchSlot1Id != -1) {
                        SlotOptions.setBackground(event.handler.getSlot(pitchSlot1Id), heardPitch.contains(changePitch) || heardPitch.isEmpty() ? RenderColor.green : RenderColor.red);
                    }
                }
                if (event.stack.getName().getString().startsWith("Change the Beat")) {
                    if (colorsOrder.contains(event.stack.getItem())) {
                        changeColor = event.stack.getItem();
                    }
                    if (colorSlot1Id != -1) {
                        if (changeColor != null && changeColor.equals(matchColor)) {
                            SlotOptions.disableSlot(event.handler.getSlot(colorSlot1Id), true);
                        } else if (changeColor != null && !changeColor.equals(matchColor)) {
                            SlotOptions.disableSlot(event.handler.getSlot(colorSlot1Id), false);
                        }
                    }
                    if (pitchSlot1Id != -1) {
                        SlotOptions.setBackground(event.handler.getSlot(pitchSlot1Id), heardPitch.contains(changePitch) || heardPitch.isEmpty() ? RenderColor.green : RenderColor.red);
                    }
                }
            }
            case TuningType.Upgrade -> {} // TODO
            case TuningType.None -> {} // TODO
        }
    }
}
