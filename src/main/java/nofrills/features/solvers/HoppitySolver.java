package nofrills.features.solvers;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static nofrills.Main.mc;

public class HoppitySolver {
    public static final Feature instance = new Feature("hoppitySolver");

    public static final SettingBool guessTracer = new SettingBool(true, "guessTracer", instance);
    public static final SettingColor guessColor = new SettingColor(RenderColor.fromArgb(0xaaffffff), "guessColor", instance);
    public static final SettingColor guessTracerColor = new SettingColor(RenderColor.fromArgb(0xffffffff), "guessTracerColor", instance);

    private static final CurveSolver solver = new CurveSolver();
    private static final EntityCache eggCache = new EntityCache();
    private static final HashMap<Integer, Vec3d> farGuesses = new HashMap<>();
    private static final HashSet<String> textureList = Sets.newHashSet(
            "a49333d85b8a315d0336eb2df37d8a714ca24c51b8c6074f1b5b927deb516c24",
            "7ae6d2d31d8167bcaf95293b68a4acd872d66e751db5a34f2cbc6766a0356d0a",
            "e5e36165819fd2850f98552edcd763ff986313119283c126ace0c4cc495e76a8"
    );
    private static int ticks = 0;
    private static int farScanTimer = 40;
    private static int scanInstance = 0;

    private static boolean isHoldingEgglocator() {
        return Utils.getSkyblockId(Utils.getHeldItem()).equals("EGGLOCATOR");
    }

    private static void onLocatingStart() {
        solver.resetFitter();
        ticks = 40;
    }

    private static boolean isEgglocatorParticle(ParticleS2CPacket packet) {
        return packet.getParameters().getType().equals(ParticleTypes.HAPPY_VILLAGER) && packet.getSpeed() == 0.0f && packet.getCount() == 1
                && packet.getOffsetX() == 0.0f && packet.getOffsetY() == 0.0f && packet.getOffsetZ() == 0.0f;
    }

    private static Optional<Entity> refineGuess(Vec3d pos) {
        for (Entity ent : Utils.getOtherEntities(null, Box.of(pos, 5.0, 5.0, 5.0), null)) {
            if (ent instanceof ArmorStandEntity stand) {
                GameProfile textures = Utils.getTextures(Utils.getEntityArmor(stand).getFirst());
                for (String texture : textureList) {
                    if (Utils.isTextureEqual(textures, texture)) {
                        return Optional.of(ent);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static void eggInteract() {
        solver.resetFitter();
        solver.resetSolvedPos();
        scanInstance++;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (isEgglocatorParticle(event.packet) && ticks > 0 && solver.getLastDist(event.pos) <= 5.0) {
            solver.addPos(event.pos);
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (isHoldingEgglocator()) {
            onLocatingStart();
            ticks = 40;
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (isHoldingEgglocator()) {
            onLocatingStart();
            ticks = 40;
        }
    }

    @EventHandler
    private static void onInteractEntity(InteractEntityEvent event) {
        if (eggCache.has(event.entity)) {
            eggInteract();
        }
        eggCache.remove(event.entity);
    }

    @EventHandler
    private static void onAttackEntity(AttackEntityEvent event) {
        if (eggCache.has(event.entity)) {
            eggInteract();
        }
        eggCache.remove(event.entity);
    }

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (instance.isActive()) {
            if (ticks > 0) {
                ticks--;
                if (ticks == 0) {
                    solver.resetFitter();
                }
            }
            if (solver.getSolvedPos() != null) {
                farGuesses.put(scanInstance, solver.getSolvedPos());
            }
            if (farScanTimer > 0) {
                farScanTimer--;
                if (farScanTimer == 0) {
                    HashSet<Integer> toRemove = new HashSet<>();
                    for (HashMap.Entry<Integer, Vec3d> entry : farGuesses.entrySet()) {
                        Optional<Entity> refined = refineGuess(entry.getValue());
                        if (refined.isPresent()) {
                            eggCache.add(refined.get());
                            toRemove.add(entry.getKey());
                        }
                        if (mc.player != null && mc.player.squaredDistanceTo(entry.getValue()) < 16 * 16) {
                            toRemove.add(entry.getKey());
                        }
                    }
                    solver.resetFitter();
                    toRemove.forEach(farGuesses::remove);
                    farScanTimer = 40;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        try {
            for (Vec3d guess : farGuesses.values()) {
                BlockPos pos = BlockPos.ofFloored(guess);
                Box box = Box.enclosing(pos, pos);
                event.drawFilled(box, true, guessColor.value());
                if (guessTracer.value()) {
                    event.drawTracer(guess, guessTracerColor.value());
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
        for (Entity egg : eggCache.get()) {
            BlockPos pos = BlockPos.ofFloored(egg.getEyePos());
            Box box = Box.enclosing(pos, pos);
            event.drawFilled(box, true, guessColor.value());
            if (guessTracer.value()) {
                event.drawTracer(egg.getEyePos(), guessTracerColor.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        solver.resetFitter();
        eggCache.clear();
        farGuesses.clear();
        scanInstance = 0;
        ticks = 0;
    }
}
