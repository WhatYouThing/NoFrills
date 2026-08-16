package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingInt;
import nofrills.events.*;
import nofrills.misc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static nofrills.Main.mc;

@EventListener
public class PhantomleafSolver {
    public static final Feature instance = new Feature("phantomleafSolver");

    public static final SettingColor colorCorrect = new SettingColor(RenderColor.fromArgb(0xaa55ff55), "colorCorrect", instance);
    public static final SettingInt correctThreshold = new SettingInt(2, "correctThreshold", instance);
    public static final SettingBool showSoundMatch = new SettingBool(false, "showSoundMatch", instance);
    public static final SettingColor colorSoundMatch = new SettingColor(RenderColor.fromArgb(0xaaafafff), "colorSoundMatch", instance);
    public static final SettingBool showUncertain =  new SettingBool(false, "showUncertain", instance);
    public static final SettingColor colorUncertain = new SettingColor(RenderColor.fromArgb(0xaaffff55), "colorUncertain", instance);

    private static final MutableReference<AABB> planterArea = new MutableReference<>(null);
    private static final ConcurrentHashMap<BlockPos, AtomicInteger> candidateScores = new ConcurrentHashMap<>();
    private static final ConcurrentHashSet<BlockPos> bestCandidates = new ConcurrentHashSet<>();
    private static BlockPos fallbackSolution;
    private static float highestVolume = 0.0f;
    private static int ticks = 0;
    private static boolean solverActive = false;

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && event.isSound(SoundEvents.NOTE_BLOCK_BASEDRUM) && ticks > 0 && Utils.isInGarden()) {
            if (planterArea.get() == null) {
                PlotBorders.getCurrentPlot().ifPresent(plot->{
                    BlockPos center = plot.getValue().center;
                    planterArea.set(AABB.encapsulatingFullBlocks(center.offset(4, 8, 4), center.offset(-5, 8, -5)));
                });
            }

            Vec3 pos = mc.player.position();

            if (event.volume() >= 0.99 && event.volume() > highestVolume) {
                fallbackSolution = new BlockPos(mc.player.getBlockX(), 74, mc.player.getBlockZ());
                highestVolume =  event.volume();
            }

            if (event.pitch() > 0.6 && event.pitch() < 0.62) {
                Vec3 playerPos = new Vec3(pos.x, 74, pos.z);
                PhantomleafEvent phantomleafEvent = new PhantomleafEvent(event.volume(), playerPos);
                processCandidates(phantomleafEvent);
            }
        }
    }

    private static void processCandidates(PhantomleafEvent event) {
        double bestDiff = Double.MAX_VALUE;
        BlockPos bestCandidate = null;
        double calculated = (1.0 - event.volume) * 30.0;
        for (double z = 0; z < 10; z++) {
            for (double x = 0; x < 10; x++) {
                Vec3 pos = new Vec3(planterArea.get().maxX - x - 0.5, 74, planterArea.get().maxZ - z - 0.5);
                double diff = Math.abs(event.playerPos.distanceTo(pos) - calculated);
                if (diff < bestDiff) {
                    bestCandidate = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
                    bestDiff = diff;
                }
            }
        }
        candidateScores.computeIfAbsent(bestCandidate, v -> new AtomicInteger(0)).incrementAndGet();
        bestCandidates.clear();
        int bestScore = correctThreshold.value();

        for (Map.Entry<BlockPos, AtomicInteger> entry : candidateScores.entrySet()) {
            int score = entry.getValue().get();

            if (score > bestScore) {
                bestScore = score;
                bestCandidates.clear();
                bestCandidates.add(entry.getKey());
            } else if (score == bestScore) {
                bestCandidates.add(entry.getKey());
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInGarden() && solverActive) {
            if (showSoundMatch.value() && fallbackSolution != null) {
                event.drawBeam(fallbackSolution.getCenter(), 5, true, colorSoundMatch.value());
                event.drawFilled(AABB.encapsulatingFullBlocks(fallbackSolution, fallbackSolution), true, colorSoundMatch.value());
            } else if (!bestCandidates.isEmpty()) {
                RenderColor color;
                if (bestCandidates.size() == 1) {
                    color = colorCorrect.value();
                } else {
                    if (!showUncertain.value()) {
                        return;
                    }
                    color = colorUncertain.value();
                }
                for (BlockPos pos : bestCandidates) {
                    event.drawBeam(pos.getCenter(), 5, true, color);
                    event.drawFilled(AABB.encapsulatingFullBlocks(pos, pos), true, color);
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && ticks > 0) {
            ticks--;
        }
    }

    @EventHandler
    private static void onPacket(ReceivePacketEvent event) {
        if (instance.isActive() && event.packet instanceof ClientboundSetSubtitleTextPacket(
                Component text
        ) && Utils.isInGarden()) {
            String subtitle = Utils.toPlain(text).trim();
            if (subtitle.startsWith("(") && subtitle.contains(Utils.Symbols.heart) && subtitle.endsWith(")")) {
                ticks = 40;
            }
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInGarden()) {
            if (event.messagePlain.equals("[CROP] Phantomleaf: Poof! Try and find me!")) {
                solverActive = true;
                Utils.showTitle("§5§lPHANTOMLEAF", "Move around the planter to collect the sounds for solution.", 0, 30, 10);
            } else if (event.messagePlain.equals("[CROP] Phantomleaf: You found me!")) {
                cleanUp();
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        cleanUp();
    }

    private static void cleanUp() {
        fallbackSolution = null;
        highestVolume = 0.0f;
        candidateScores.clear();
        bestCandidates.clear();
        planterArea.set(null);
        solverActive = false;
        ticks = 0;
    }

    private record PhantomleafEvent(
            float volume,
            Vec3 playerPos
    ) {}
}
