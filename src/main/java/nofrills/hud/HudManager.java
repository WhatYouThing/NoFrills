package nofrills.hud;

import io.wispforest.owo.ui.hud.Hud;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import nofrills.events.*;
import nofrills.features.fishing.CapTracker;
import nofrills.hud.elements.*;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class HudManager {
    public static final List<HudElement> elements = new ArrayList<>();

    public static final FPS fps = register(new FPS("FPS: §f0"));
    public static final TPS tps = register(new TPS("TPS: §f20.00"));
    public static final Ping ping = register(new Ping("Ping: §f0ms"));
    public static final Day day = register(new Day("Day: §f0"));
    public static final Armor armor = register(new Armor());
    public static final Inventory inventory = register(new Inventory());
    public static final Quiver quiver = register(new Quiver("Quiver: §fN/A"));
    public static final LagMeter lagMeter = register(new LagMeter("Last server tick was 0.00s ago"));
    public static final PickaxeAbilityTimer pickAbilityTimer = register(new PickaxeAbilityTimer());
    public static final QueueCooldownTimer queueCooldownTimer = register(new QueueCooldownTimer());
    public static final SlayerHealth slayerHealth = register(new SlayerHealth());
    public static final SlayerTimer slayerTimer = register(new SlayerTimer());
    public static final BossHealth bossHealth = register(new BossHealth());
    public static final DungeonMap dungeonMap = register(new DungeonMap());
    public static final DungeonScore dungeonScore = register(new DungeonScore());
    public static final SpiritMaskTimer spiritMaskTimer = register(new SpiritMaskTimer());
    public static final PhoenixPetTimer phoenixPetTimer = register(new PhoenixPetTimer());
    public static final BonzoMaskTimer bonzoMaskTimer = register(new BonzoMaskTimer());
    public static final SpiritBearTimer spiritBearTimer = register(new SpiritBearTimer());
    public static final TerracottaGyroTimer terraGyroTimer = register(new TerracottaGyroTimer());
    public static final PadTimer padTimer = register(new PadTimer());
    public static final TerminalStartTimer terminalStartTimer = register(new TerminalStartTimer());
    public static final GoldorTickTimer goldorTickTimer = register(new GoldorTickTimer());
    public static final Power power = register(new Power("Power: §f0"));
    public static final FreshToolsTimer freshToolsTimer = register(new FreshToolsTimer());
    public static final SeaCreatures seaCreatures = register(new SeaCreatures("Sea Creatures: §70"));
    public static final FishingBobber bobber = register(new FishingBobber("Bobber: §7Inactive"));
    public static final FishingBag fishingBag = register(new FishingBag("Bait: §fN/A"));
    public static final ShardTrackerDisplay shardTracker = register(new ShardTrackerDisplay());
    public static final SkillTrackerDisplay skillTracker = register(new SkillTrackerDisplay());

    private static CustomTitle currentTitle = new CustomTitle(Text.empty(), 0);

    public static boolean isEditingHud() {
        return mc.currentScreen instanceof HudEditorScreen;
    }

    public static List<HudElement> getElements() {
        return elements;
    }

    public static <T extends HudElement> T register(T element) {
        elements.add(element);
        return element;
    }

    public static void registerElements() {
        for (HudElement element : elements) {
            Identifier identifier = element.getIdentifier();
            if (!Hud.hasComponent(identifier)) {
                Hud.add(identifier, () -> element);
            }
        }
    }

    public static void setCustomTitle(MutableText text, int ticks) {
        currentTitle = new CustomTitle(text, ticks);
    }

    public static void setCustomTitle(String text, int ticks) {
        setCustomTitle(Text.literal(text), ticks);
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
        ping.reset();
        tps.reset();
        fps.reset();
        lagMeter.setTickTime(0);
        bossHealth.reset();
        dungeonMap.reset();
        for (HudElement element : elements) {
            if (element instanceof TickTimerElement tickTimer && tickTimer.isAutoPause()) {
                tickTimer.pause();
            }
            if (element instanceof TimerElement timer && timer.isAutoPause()) {
                timer.pause();
            }
        }
        currentTitle.reset();
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (event.packet instanceof PingResultS2CPacket(long startTime)) {
            if (ping.isActive()) {
                ping.setPing(Util.getMeasuringTimeMs() - startTime);
                ping.ticks = 20;
            }
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (power.isActive()) {
            power.setPower(DungeonUtil.getPower());
        }
        if (day.isActive() && mc.world != null) {
            day.setDay(mc.world.getLevelProperties().getTimeOfDay() / 24000L);
        }
        if (ping.isActive()) { // pings every second when element is enabled, waits until ping result is received
            if (ping.ticks > 0) {
                ping.ticks -= 1;
                if (ping.ticks == 0) {
                    Utils.sendPingPacket();
                }
            }
        }
        if (tps.isActive()) {
            if (tps.clientTicks > 0) {
                tps.clientTicks -= 1;
                if (tps.clientTicks == 0) {
                    tps.setTps(tps.serverTicks);
                    tps.clientTicks = 20;
                    tps.serverTicks = 0;
                }
            }
        }
        if (seaCreatures.isActive()) {
            seaCreatures.setCount(CapTracker.seaCreatures.size());
        }
        if (fps.isActive()) {
            if (fps.ticks > 0) {
                fps.ticks -= 1;
                if (fps.ticks == 0) {
                    fps.setFps(mc.getCurrentFps());
                    fps.ticks = 20;
                }
            }
        }
        if (armor.isActive()) {
            armor.updateArmor();
        }
        if (inventory.isActive()) {
            inventory.updateInventory();
        }
        if (slayerHealth.isActive()) {
            slayerHealth.update();
        }
        if (slayerTimer.isActive()) {
            slayerTimer.update();
        }
        if (bossHealth.isActive()) {
            bossHealth.update();
        }
        if (dungeonScore.isActive()) {
            dungeonScore.tick();
        }
        if (skillTracker.isActive()) {
            skillTracker.tick();
        }
        if (currentTitle.isActive()) {
            currentTitle.tick();
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (lagMeter.isActive()) {
            lagMeter.setTickTime(Util.getMeasuringTimeMs());
        }
        if (tps.isActive()) {
            tps.serverTicks += 1;
        }
        if (bobber.isActive()) {
            bobber.onServerTick();
        }
        if (Utils.isOnDungeonFloor("6")) {
            if (terraGyroTimer.isActive()) {
                terraGyroTimer.tick();
            }
        }
        if (Utils.isOnDungeonFloor("7")) {
            if (padTimer.isActive()) {
                padTimer.tick();
            }
            if (terminalStartTimer.isActive()) {
                terminalStartTimer.tick();
            }
            if (goldorTickTimer.isActive()) {
                goldorTickTimer.tick();
            }
        }
        if (Utils.isInKuudra()) {
            if (freshToolsTimer.isActive()) {
                freshToolsTimer.tick();
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (bobber.isActive()) {
            bobber.onNamed(event);
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (queueCooldownTimer.isActive()) {
            for (Pattern pattern : queueCooldownTimer.patterns) {
                if (pattern.matcher(event.msg()).matches()) {
                    queueCooldownTimer.start(30000);
                    break;
                }
            }
        }
        if (spiritMaskTimer.isActive() && event.msg().equals("Second Wind Activated! Your Spirit Mask saved your life!")) {
            spiritMaskTimer.start(30000);
        }
        if (phoenixPetTimer.isActive() && event.msg().equals("Your Phoenix Pet saved you from certain death!")) {
            phoenixPetTimer.start(60000);
        }
        if (bonzoMaskTimer.isActive() && event.msg().replace("⚚ ", "").equals("Your Bonzo's Mask saved your life!")) {
            ItemStack helmet = Utils.getEntityHelmet(mc.player);
            Optional<String> line = Utils.getLoreLines(helmet).stream().filter(l -> l.startsWith("Cooldown: ")).findFirst();
            if (line.isPresent()) {
                String cooldown = line.get();
                String duration = cooldown.substring(cooldown.indexOf(":") + 2).replace("s", "");
                bonzoMaskTimer.start((long) Math.ceil(Utils.parseDouble(duration).orElse(180.0) * 1000));
            } else {
                bonzoMaskTimer.start(180000);
            }
        }
        if (Utils.isOnDungeonFloor("7")) {
            switch (event.messagePlain) {
                case "[BOSS] Storm: Pathetic Maxor, just like expected." -> {
                    if (padTimer.isActive()) {
                        padTimer.start();
                    }
                }
                case "[BOSS] Storm: I should have known that I stood no chance." -> {
                    padTimer.pause();
                    if (terminalStartTimer.isActive()) {
                        terminalStartTimer.start();
                    }
                }
                case "[BOSS] Goldor: Who dares trespass into my domain?" -> {
                    if (goldorTickTimer.isActive()) {
                        goldorTickTimer.start();
                    }
                }
                case "The Core entrance is opening!" -> goldorTickTimer.pause();
                default -> {
                }
            }
        }
    }

    @EventHandler
    private static void onBlockUpdate(BlockUpdateEvent event) {
        if (spiritBearTimer.isActive() && event.newState.getBlock().equals(Blocks.SEA_LANTERN) && Utils.isInDungeonBoss("4")) {
            if (event.pos.getX() == 7 && event.pos.getY() == 77 && event.pos.getZ() == 34) {
                spiritBearTimer.start();
            }
        }
    }

    @EventHandler
    private static void onInventory(InventoryUpdateEvent event) {
        if (event.slotId == 44) { // 9th hotbar slot
            if (quiver.isActive()) {
                quiver.update(event.stack);
            }
            if (fishingBag.isActive()) {
                fishingBag.update(event.stack);
            }
        }
    }

    public static class CustomTitle {
        public MutableText text;
        public int ticks;

        public CustomTitle(MutableText text, int ticks) {
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

        public void draw(DrawContext context) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(context.getScaledWindowWidth() * 0.5f, context.getScaledWindowHeight() * 0.5f);
            context.getMatrices().pushMatrix();
            context.getMatrices().scale(4.0F, 4.0F);
            int width = mc.textRenderer.getWidth(this.text);
            context.drawTextWithBackground(mc.textRenderer, this.text, -width / 2, -context.getScaledWindowHeight() / 20, width, -1);
            context.getMatrices().popMatrix();
            context.getMatrices().popMatrix();
        }
    }
}
