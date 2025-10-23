package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.SlotUpdateEvent;
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

public class BeaconTuningSolver {
    public static final Feature instance = new Feature("beaconTuningSolver");

    private static final List<Identifier> EXCLUDED_COLORS = List.of(
            Identifier.of("minecraft", "gray_stained_glass_pane"),
            Identifier.of("minecraft", "light_gray_stained_glass_pane"),
            Identifier.of("minecraft", "black_stained_glass_pane")
    );
    private static final List<Item> colorsOrder = createColorsOrder();
    public static int matchSpeed = 0;
    public static int changeSpeed = 0;
    public static int colorSlot1Id = -1;
    public static int speedSlot1Id = -1;
    public static int pitchSlot1Id = -1;
    public static int colorTarget1 = 0;
    public static HashSet<PitchType> heardPitch = new HashSet<>();
    private static int tickCounter = 0;
    private static int lastMatchTick = 0;
    private static Item matchColor = null;
    private static Item changeColor = null;
    private static boolean isPaused1 = false; // might be useful
    private static PitchType changePitch = null;

    private static List<Item> createColorsOrder() {
        return StreamSupport.stream(Registries.ITEM.spliterator(), false).filter(item -> {
            Identifier id = Registries.ITEM.getId(item);
            return id.getPath().endsWith("_stained_glass_pane");
        }).filter(item -> !EXCLUDED_COLORS.contains(Registries.ITEM.getId(item))).sorted(Comparator.comparingInt(Registries.ITEM::getRawId)).collect(Collectors.toList());
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
        // 1/2/3/4/5 speed matches to 55/45/35/25/15 ticks delays accordingly
        // this can return wrong values (outside 1-5 range), but that's not a huge issue
        return 6 - Math.floorDiv(tickDelta, 10);
    }

    private static void updateSpeedSlot(GenericContainerScreenHandler handler) {
        if (speedSlot1Id != -1) {
            ItemStack stack = Items.WHITE_WOOL.getDefaultStack();
            if (matchSpeed > 0 && matchSpeed == changeSpeed) {
                stack = Items.GREEN_WOOL.getDefaultStack();
            } else if (matchSpeed > 0) {
                stack = Items.RED_WOOL.getDefaultStack();
            }
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Target speed: ").styled(style -> style.withItalic(false).withColor(Formatting.WHITE))
                            .append(Text.literal(Utils.format("{}", matchSpeed)).styled(style -> style.withItalic(false).withColor(matchSpeed == changeSpeed ? Formatting.GREEN : Formatting.RED))),
                    Text.literal("Current speed: ").styled(style -> style.withItalic(false).withColor(Formatting.WHITE))
                            .append(Text.literal(Utils.format("{}", changeSpeed)).styled(style -> style.withItalic(false).withColor(matchSpeed == changeSpeed ? Formatting.GREEN : Formatting.RED)))

            )));
            SlotOptions.spoofSlot(handler.getSlot(speedSlot1Id), SlotOptions.stackWithName(
                    SlotOptions.stackWithQuantity(stack, changeSpeed),
                    Text.literal("Speed").styled(style -> style.withItalic(false).withColor(Formatting.YELLOW))
            ));
        }
    }

    private static void updatePitchSlot(GenericContainerScreenHandler handler) {
        if (pitchSlot1Id != -1) {
            ItemStack stack;
            stack = Items.CYAN_WOOL.getDefaultStack();
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Wait for the target tile to move once to see the target pitch.").styled(style -> style.withItalic(false).withColor(Formatting.WHITE)),
                    Text.literal("If possible target is empty - it's likely already solved.").styled(style -> style.withItalic(false).withColor(Formatting.WHITE)),
                    Text.literal("Possible target: ").styled(style -> style.withColor(Formatting.AQUA))
                            .append(Text.literal(Utils.format("{}", heardPitch))
                                    .styled(style -> style.withItalic(false).withColor(Formatting.WHITE))),
                    Text.literal("Current pitch: ").styled(style -> style.withItalic(false).withColor(Formatting.WHITE))
                            .append(Text.literal(Utils.format("{}", changePitch)).styled(style -> style.withItalic(false).withColor(
                                    switch (changePitch) {
                                        case PitchType.Low -> Formatting.GREEN;
                                        case PitchType.Normal -> Formatting.YELLOW;
                                        case PitchType.High -> Formatting.RED;
                                    }
                            )))
            )));
            SlotOptions.spoofSlot(handler.getSlot(pitchSlot1Id), SlotOptions.stackWithName(stack,
                    Text.literal("Pitch").styled(style -> style.withItalic(false).withColor(Formatting.YELLOW))
            ));
        }
    }

    private static void updateColorSlot(GenericContainerScreenHandler handler) {
        int matchRoll = colorsOrder.indexOf(matchColor);
        int changeRoll = colorsOrder.indexOf(changeColor);
        int target1 = (matchRoll - changeRoll + colorsOrder.size()) % colorsOrder.size();
        int target2 = (changeRoll - matchRoll + colorsOrder.size()) % colorsOrder.size();
        colorTarget1 = target1 < target2 ? -target1 : target2;
        ItemStack stack;
        if (colorTarget1 < 0) {
            stack = Items.GREEN_WOOL.getDefaultStack();
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Left clicks ").styled(style -> style.withColor(RenderColor.green.hex))
                            .append(Text.literal(Utils.format("required: {}", Math.abs(colorTarget1)))
                                    .styled(style -> style.withItalic(false).withColor(Formatting.WHITE)))
            )));
        } else if (colorTarget1 > 0) {
            stack = Items.ORANGE_WOOL.getDefaultStack();
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Right clicks ").styled(style -> style.withColor(RenderColor.red.hex))
                            .append(Text.literal(Utils.format("required: {}", Math.abs(colorTarget1)))
                                    .styled(style -> style.withItalic(false).withColor(Formatting.WHITE)))
            )));
        } else {
            // just in case, unreachable
            stack = Items.WHITE_WOOL.getDefaultStack();
        }
        SlotOptions.spoofSlot(handler.getSlot(colorSlot1Id), SlotOptions.stackWithName(
                SlotOptions.stackWithQuantity(stack, Math.abs(colorTarget1)),
                Text.literal("Color").styled(style -> style.withItalic(false).withColor(Formatting.YELLOW))
        ));
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInArea("Galatea") && !getTuningType().equals(TuningType.None)) {
            tickCounter++;
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive() && Utils.isInArea("Galatea")) {
            tickCounter = 0;
            lastMatchTick = 0;
            matchSpeed = 0;
            changeSpeed = 0;
            matchColor = null;
            changeColor = null;
            colorSlot1Id = -1;
            speedSlot1Id = -1;
            pitchSlot1Id = -1;
            isPaused1 = false;
            changePitch = null;
            colorTarget1 = 0;
            heardPitch.clear();
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && Utils.isInArea("Galatea")) {
            TuningType tuningType = getTuningType();
            if (tuningType.equals(TuningType.None)) return;
            if (event.isSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS)) {
                switch (tuningType) {
                    case TuningType.Normal -> {
                        PitchType soundPitch = PitchType.match(event.packet.getPitch());
                        if (changePitch != null && changePitch != soundPitch) {
                            heardPitch.add(soundPitch);
                        }
                        heardPitch.remove(changePitch);
                    }
                    case TuningType.Upgrade -> {
                    } // TODO
                }
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && Utils.isInArea("Galatea") && !event.isInventory) {
            TuningType tuningType = getTuningType();
            if (tuningType.equals(TuningType.None)) return;
            String name = Utils.toPlain(event.stack.getName());
            switch (tuningType) {
                case TuningType.Normal -> {
                    if (name.startsWith("Pause")) {
                        Utils.getLoreLines(event.stack).stream().filter(s -> s.contains("Currently")).findFirst().ifPresent(pauseLine -> isPaused1 = !pauseLine.contains("UNPAUSED"));
                    }
                    if (name.startsWith("Color")) {
                        colorSlot1Id = event.slotId;
                        updateColorSlot(event.handler);
                    }
                    if (name.startsWith("Pitch")) {
                        pitchSlot1Id = event.slotId;
                        Utils.getLoreLines(event.stack).stream().filter(s -> s.contains("Current pitch:")).findFirst().ifPresent(x -> {
                            if (x.endsWith("Low")) changePitch = PitchType.Low;
                            if (x.endsWith("Normal")) changePitch = PitchType.Normal;
                            if (x.endsWith("High")) changePitch = PitchType.High;
                        });
                        heardPitch.remove(changePitch);
                    }
                    if (name.startsWith("Speed")) {
                        speedSlot1Id = event.slotId;
                        Utils.getLoreLines(event.stack).stream().filter(s -> s.contains("Current speed")).findFirst().ifPresent(x -> {
                            changeSpeed = x.charAt(x.length() - 1) - '0';
                        });
                        updateSpeedSlot(event.handler);
                    }
                    if (name.startsWith("Match the Beat")) {
                        int temp = getTileSpeed(tickCounter - lastMatchTick);
                        if (temp < 6) {
                            matchSpeed = temp;
                        }
                        if (colorsOrder.contains(event.stack.getItem())) {
                            lastMatchTick = tickCounter;
                            matchColor = event.stack.getItem();
                            updateSpeedSlot(event.handler);
                        }
                        if (colorSlot1Id != -1) {
                            if (changeColor != null && changeColor.equals(matchColor)) {
                                SlotOptions.disableSlot(event.handler.getSlot(colorSlot1Id), true);
                            } else if (changeColor != null && !changeColor.equals(matchColor)) {
                                SlotOptions.disableSlot(event.handler.getSlot(colorSlot1Id), false);
                            }
                        }
                    }
                    if (name.startsWith("Change the Beat")) {
                        if (colorsOrder.contains(event.stack.getItem())) {
                            changeColor = event.stack.getItem();
                        }
                    }
                    updatePitchSlot(event.handler);
                }
                case TuningType.Upgrade -> {
                } // TODO
            }
        }
    }

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
}
