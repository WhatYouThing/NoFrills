package nofrills.features.farming;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.events.*;
import nofrills.misc.DebugStuff;
import nofrills.misc.MutableReference;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static nofrills.Main.mc;

@EventListener
public class PhantomleafSolver {
    public static final Feature instance = new Feature("phantomleafSolver");

    private static final MutableReference<AABB> planterArea = new MutableReference<>(null);
    private static final List<PhantomleafEvent> events = new ArrayList<>();
    private static final ConcurrentHashMap<BlockPos, AtomicInteger> candidateScores = new ConcurrentHashMap<>();
    private static final List<BlockPos> bestCandidates = new ArrayList<>();
    private static final JsonArray debugOutput = new JsonArray();
    private static BlockPos fallbackSolution;
    private static int ticks = 0;

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

            JsonObject obj = new JsonObject();
            JsonArray soundPosArray = new JsonArray();
            soundPosArray.add(event.pos.x);
            soundPosArray.add(event.pos.y);
            soundPosArray.add(event.pos.z);
            obj.add("soundPos", soundPosArray);
            obj.addProperty("soundPitch", event.pitch());
            obj.addProperty("soundVolume", event.volume());
            JsonArray soundOffsetArray = new JsonArray();
            soundOffsetArray.add(planterArea.get().maxX - event.pos.x);
            soundOffsetArray.add(planterArea.get().maxY - event.pos.y);
            soundOffsetArray.add(planterArea.get().maxZ - event.pos.z);
            obj.add("soundOffset", soundOffsetArray);
            JsonArray playerPosArray = new JsonArray();
            playerPosArray.add(pos.x);
            playerPosArray.add(pos.y);
            playerPosArray.add(pos.z);
            obj.add("playerPos", playerPosArray);
            JsonArray planterTopCornerArray = new JsonArray();
            planterTopCornerArray.add(planterArea.get().maxX);
            planterTopCornerArray.add(planterArea.get().maxY);
            planterTopCornerArray.add(planterArea.get().maxZ);
            obj.add("planterTopCorner", planterTopCornerArray);
            obj.addProperty("tick", DebugStuff.getTickCounter());
            debugOutput.add(obj);

            if (event.volume() >= 0.99) {
                fallbackSolution = new BlockPos(mc.player.getBlockX(), 74, mc.player.getBlockZ());
                Utils.infoFormat("got fallback solution: {}", fallbackSolution);
            }

            if (event.pitch() > 0.6 && event.pitch() < 0.62) {
                Vec3 playerPos = new Vec3(pos.x, 74, pos.z);
                if (!events.isEmpty()) {
                    if (events.getLast().playerPos.distanceTo(playerPos) < 1.) {
                        Utils.infoRaw(Component.literal("§aMove around to get more position data."));
                        return;
                    }
                }
                PhantomleafEvent phantomleafEvent = new PhantomleafEvent(event.volume(), playerPos);
                Utils.infoFormat("got new sound event: {}", phantomleafEvent);
                events.add(phantomleafEvent);
                processCandidates(phantomleafEvent);
            }
        }
    }

    private static void processCandidates(PhantomleafEvent event) {
        double calculated = (1.0 - event.volume) * 30.0;
        for (double z = 0; z < 10; z++) {
            for (double x = 0; x < 10; x++) {
                Vec3 pos = new Vec3(planterArea.get().maxX, 74, planterArea.get().maxZ);
                if (Utils.isNearlyEqual(event.playerPos.distanceTo(pos), calculated, 0.001)) {
                    int score = candidateScores.computeIfAbsent(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), v -> new AtomicInteger(0)).incrementAndGet();
                    Utils.infoFormat("found candidate at {}, score = {}", pos, score);
                }
            }
        }
        bestCandidates.clear();
        int bestScore = -1;

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
        if (instance.isActive() && Utils.isInGarden()) {
            if (fallbackSolution != null) {
                event.drawBeam(fallbackSolution.getCenter(), 5, true, RenderColor.fromHex(0x7f7fff));
                event.drawFilled(AABB.encapsulatingFullBlocks(fallbackSolution, fallbackSolution), true, RenderColor.fromHex(0x7f7fff));
            }
            if (bestCandidates.size() > 1) {
                for (BlockPos pos : bestCandidates) {
                    event.drawBeam(pos.getCenter(), 5, true, RenderColor.fromHex(0xffff7f));
                    event.drawFilled(AABB.encapsulatingFullBlocks(pos, pos), true, RenderColor.fromHex(0xffff7f));
                }
            } else if (bestCandidates.size() == 1) {
                BlockPos pos = bestCandidates.getFirst();
                event.drawBeam(pos.getCenter(), 5, true, RenderColor.GREEN);
                event.drawFilled(AABB.encapsulatingFullBlocks(pos, pos), true, RenderColor.GREEN);
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && ticks > 0) {
            ticks--;
            if (ticks == 0) {
                Thread.startVirtualThread(()->{
                    Path path = FabricLoader.getInstance().getConfigDir().resolve("NoFrills").resolve("PhantomleafDebug.json");
                    try {
                        Utils.atomicWrite(path, new GsonBuilder().setPrettyPrinting().create().toJson(debugOutput));
                        debugOutput.asList().clear();
                        mc.schedule(()->Utils.info("Phantomleaf solver debug data saved to config folder"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
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
            if (event.messagePlain.equals("[CROP] Phantomleaf: You found me!")) {
                cleanUp();
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        cleanUp();
    }

    private static void cleanUp() {
        events.clear();
        fallbackSolution = null;
        candidateScores.clear();
        bestCandidates.clear();
        planterArea.set(null);
    }

    private record PhantomleafEvent(
            float volume,
            Vec3 playerPos
    ) {}
}
