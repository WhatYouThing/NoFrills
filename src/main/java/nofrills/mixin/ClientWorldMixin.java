package nofrills.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;
import static nofrills.misc.Utils.SkyblockData;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "processPendingUpdate", at = @At("HEAD"), cancellable = true)
    private void onBlockUpdate(BlockPos pos, BlockState state, Vec3d playerPos, CallbackInfo ci) {
        if (Config.stonkFix) {
            BlockState blockState = this.getBlockState(pos);
            if (blockState != state) {
                this.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
            }
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(CallbackInfo ci) {
        if (mc.player == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        SkyblockData data = new SkyblockData();
        Scoreboard scoreboard = mc.player.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
        for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
            if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if (!line.trim().isEmpty()) {
                        String cleanLine = Formatting.strip(line.trim());
                        if (cleanLine.startsWith(Utils.Symbols.zone) || cleanLine.startsWith(Utils.Symbols.zoneRift)) {
                            data.currentLocation = cleanLine;
                        }
                        lines.add(cleanLine);
                    }
                }

            }
            if (objective != null) {
                String objectiveLine = Formatting.strip(objective.getDisplayName().getString());
                if (objectiveLine.contains("SKYBLOCK")) {
                    data.isInSkyblock = true;
                }
                lines.add(objectiveLine);
            }
            Utils.scoreboardLines = lines;
            Utils.skyblockData = data;
        }
        eventBus.post(new WorldTickEvent());
    }
}