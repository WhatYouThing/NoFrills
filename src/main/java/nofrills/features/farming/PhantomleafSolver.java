package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static nofrills.Main.mc;

@EventListener
public class PhantomleafSolver {
    public static final Feature instance = new Feature("phantomleafSolver");

    private static final BlockBox barnArea =  new BlockBox(new BlockPos(48, 0, 48), new BlockPos(-48, 255, -48));
    private static final List<PhantomleafEvent> events = new ArrayList<>();
    private static final ConcurrentHashMap<BlockPos, AtomicInteger> candidateScores = new ConcurrentHashMap<>();
    private static final List<BlockPos> bestCandidates = new ArrayList<>();
    private static BlockPos currentCarpenter = BlockPos.ZERO;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Utils.isInGarden() && event.namePlain.equals("Carpenter") && !barnArea.contains(event.entity.blockPosition())) {
            currentCarpenter = event.entity.getOnPos();
            Utils.infoFormat("found carpenter: {}", currentCarpenter);
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && event.isSound(SoundEvents.NOTE_BLOCK_BASEDRUM) && Utils.isInGarden()) {
            if (event.pitch() > 0.6 && event.pitch() < 0.62) {
                Vec3 pos = mc.player.getPosition(0);
                Vec3 playerPos = new Vec3(pos.x, 74, pos.z);

                if (!events.isEmpty()) {
                    if (events.getLast().playerPos.distanceTo(playerPos) < 1.) {
                        Utils.infoRaw(Component.literal("§aMove around to get more position data."));
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
                Vec3 pos = new Vec3(currentCarpenter.getX() + x - 5.5, 74, currentCarpenter.getZ() + z + 2.5);
                if (event.playerPos.distanceTo(pos) < 0.01) {
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
    private static void onJoin(ServerJoinEvent event) {
        events.clear();
        candidateScores.clear();
        bestCandidates.clear();
        currentCarpenter = BlockPos.ZERO;
    }

    private record PhantomleafEvent(
            float volume,
            Vec3 playerPos
    ) {}
}
