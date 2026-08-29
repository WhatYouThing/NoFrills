package nofrills.hud;

import io.wispforest.owo.ui.hud.Hud;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import nofrills.events.*;
import nofrills.hud.elements.*;

import java.util.List;
import java.util.function.Consumer;

import static nofrills.Main.mc;

@EventListener
public class HudManager {
    public static final DungeonMap dungeonMap = new DungeonMap();
    public static final PickaxeAbilityTimer pickAbilityTimer = new PickaxeAbilityTimer();
    public static final List<HudElement> elements = List.of(
            new FPS(),
            new TPS(),
            new Clock(),
            new Ping(),
            new Day(),
            new Armor(),
            new InventoryOverlay(),
            new Quiver(),
            new LagMeter(),
            new QueueCooldownTimer(),
            new SlayerHealth(),
            new SlayerTimer(),
            new BossHealth(),
            new DungeonScore(),
            new SpiritMaskTimer(),
            new PhoenixPetTimer(),
            new BonzoMaskTimer(),
            new SpiritBearTimer(),
            new TerracottaGyroTimer(),
            new PadTimer(),
            new TerminalStartTimer(),
            new GoldorTickTimer(),
            new Power(),
            new FreshToolsTimer(),
            new SeaCreatures(),
            new FishingBobber(),
            new FishingBag(),
            new BeaconPower(),
            new ShardTrackerDisplay(),
            new SkillTrackerDisplay(),
            new KickCooldownTimer(),
            dungeonMap,
            pickAbilityTimer
    );

    private static CustomTitle currentTitle = new CustomTitle(Component.empty(), 0);

    private static void forEachListening(Consumer<ListeningHudElement> consumer) {
        elements.stream()
                .filter(e -> e instanceof ListeningHudElement && e.isActive())
                .map(e -> (ListeningHudElement) e)
                .forEach(consumer);
    }

    public static boolean isEditingHud() {
        return mc.screen instanceof HudEditorScreen;
    }

    public static List<HudElement> getElements() {
        return elements;
    }

    public static void registerElements() {
        for (HudElement element : elements) {
            Identifier identifier = element.getIdentifier();
            if (!Hud.hasComponent(identifier)) {
                Hud.add(identifier, () -> element);
            }
        }
    }

    public static void setCustomTitle(MutableComponent text, int ticks) {
        currentTitle = new CustomTitle(text, ticks);
    }

    public static void setCustomTitle(String text, int ticks) {
        setCustomTitle(Component.literal(text), ticks);
    }

    @EventHandler
    private static void onRenderHud(HudRenderEvent event) {
        if (!isEditingHud()) {
            for (HudElement element : HudManager.elements) {
                if (element.isAdded()) element.updatePosition();
            }
        }
        if (currentTitle.isActive()) {
            currentTitle.draw(event.context);
        }
    }

    @EventHandler
    private static void onJoinServer(ServerJoinEvent event) {
        for (HudElement element : elements) {
            if (element instanceof ListeningHudElement tickableElement) {
                tickableElement.onServerJoin();
            }
            if (element instanceof TimerElement timer && timer.isAutoPause()) {
                timer.pause();
            }
        }
        currentTitle.reset();
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        forEachListening(element -> element.onReceivePacket(event));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onWorldTick(WorldTickEvent event) {
        forEachListening(ListeningHudElement::onClientTick);
        if (currentTitle.isActive()) {
            currentTitle.tick();
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        forEachListening(ListeningHudElement::onServerTick);
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        forEachListening(element -> element.onEntityNamed(event));
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        forEachListening(element -> element.onChatMessage(event));
    }

    @EventHandler
    private static void onBlockUpdate(BlockUpdateEvent event) {
        forEachListening(element -> element.onBlockUpdate(event));
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        forEachListening(element -> element.onSlotUpdate(event));
    }

    @EventHandler
    private static void onInventory(InventoryUpdateEvent event) {
        forEachListening(element -> element.onInventoryUpdate(event));
    }

    public static class CustomTitle {
        public MutableComponent text;
        public int ticks;

        public CustomTitle(MutableComponent text, int ticks) {
            this.text = text;
            this.ticks = ticks;
        }

        public boolean isActive() {
            return this.ticks > 0;
        }

        public void tick() {
            this.ticks--;
        }

        public void reset() {
            this.ticks = 0;
        }

        public void draw(GuiGraphicsExtractor context) {
            context.pose().pushMatrix();
            context.pose().translate(context.guiWidth() * 0.5f, context.guiHeight() * 0.5f);
            context.pose().pushMatrix();
            context.pose().scale(4.0F, 4.0F);
            int width = mc.font.width(this.text);
            context.textWithBackdrop(mc.font, this.text, -width / 2, -context.guiHeight() / 20, width, -1);
            context.pose().popMatrix();
            context.pose().popMatrix();
        }
    }
}
