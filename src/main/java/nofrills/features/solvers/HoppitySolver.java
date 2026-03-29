package nofrills.features.solvers;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.List;

public class HoppitySolver {
    public static final Feature instance = new Feature("hoppitySolver");

    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xaa55ff55), "color", instance);
    public static final SettingColor tracerColor = new SettingColor(RenderColor.fromArgb(0xff55ff55), "tracerColor", instance);

    private static final CurveSolver solver = new CurveSolver();
    private static final HashSet<String> textureList = Sets.newHashSet(
            "a49333d85b8a315d0336eb2df37d8a714ca24c51b8c6074f1b5b927deb516c24",
            "7ae6d2d31d8167bcaf95293b68a4acd872d66e751db5a34f2cbc6766a0356d0a",
            "e5e36165819fd2850f98552edcd763ff986313119283c126ace0c4cc495e76a8"
    );
    private static SlopEgg currentEgg = null;
    private static int ticks = 0;

    private static boolean isHoldingEgglocator() {
        return Utils.getSkyblockId(Utils.getHeldItem()).equals("EGGLOCATOR");
    }

    private static boolean isEgglocatorParticle(SpawnParticleEvent event) {
        return event.matchParameters(ParticleTypes.ENCHANT, 10, -2.0f, 0.0f, 0.0f, 0.0f);
    }

    private static boolean isEgg(Entity entity) {
        if (entity instanceof ArmorStand stand) {
            ItemStack helmet = Utils.getEntityArmor(stand).getFirst();
            if (helmet.getItem().equals(Items.PLAYER_HEAD)) {
                GameProfile textures = Utils.getTextures(helmet);
                for (String texture : textureList) {
                    if (Utils.isTextureEqual(textures, texture)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSlopSeason() {
        if (instance.isActive()) {
            for (String line : SkyblockData.getLines()) {
                if (line.startsWith("Spring") || line.startsWith("Early Spring") || line.startsWith("Late Spring")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void onLocatingStart() {
        currentEgg = null;
        solver.resetFitter();
        ticks = 40;
    }

    private static void eggInteract(Entity entity) {
        if (currentEgg != null && isEgg(entity) && currentEgg.pos.equals(entity.getEyePosition())) {
            solver.resetFitter();
            currentEgg = null;
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (isSlopSeason() && isEgglocatorParticle(event) && ticks > 0 && solver.getLastDist(event.pos) <= 5.0) {
            solver.addPos(event.pos);
            Vec3 solved = solver.getSolvedPos();
            if (solved != null) {
                if (currentEgg != null && !currentEgg.guess && currentEgg.getBox().getCenter().distanceTo(solved) <= 5.0) {
                    return;
                }
                currentEgg = new SlopEgg(solved, true);
            }
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (isSlopSeason() && isHoldingEgglocator()) onLocatingStart();
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (isSlopSeason() && isHoldingEgglocator()) onLocatingStart();
    }

    @EventHandler
    private static void onInteractEntity(InteractEntityEvent event) {
        if (isSlopSeason()) eggInteract(event.entity);
    }

    @EventHandler
    private static void onAttackEntity(AttackEntityEvent event) {
        if (isSlopSeason()) eggInteract(event.entity);
    }

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (isSlopSeason()) {
            if (ticks > 0) {
                ticks--;
                if (ticks == 0) {
                    solver.resetFitter();
                }
            }
            if (currentEgg != null && currentEgg.guess) {
                List<Entity> nearest = Utils.getOtherEntities(null, currentEgg.getBox().inflate(2.0), HoppitySolver::isEgg);
                if (!nearest.isEmpty()) {
                    currentEgg = new SlopEgg(nearest.getFirst().getEyePosition(), false);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (isSlopSeason() && currentEgg != null) {
            AABB box = currentEgg.getBox();
            Vec3 textPos = box.getCenter().add(0.0, 1.0, 0.0);
            Component label = currentEgg.guess ? Component.literal("Guess") : Component.literal("Egg");
            float scale = Utils.getTextScale(textPos, 0.05f);
            event.drawFilled(box, true, color.value());
            event.drawText(textPos, label, scale, true, color.valueWithAlpha(1.0f));
            if (tracer.value()) {
                event.drawTracer(box.getCenter(), tracerColor.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        currentEgg = null;
        solver.resetFitter();
        ticks = 0;
    }

    public static class SlopEgg {
        public Vec3 pos;
        public boolean guess;
        public AABB box = null;

        public SlopEgg(Vec3 pos, boolean guess) {
            this.pos = pos;
            this.guess = guess;
        }

        public AABB getBox() {
            if (this.box == null) {
                BlockPos blockPos = BlockPos.containing(this.pos);
                this.box = AABB.encapsulatingFullBlocks(blockPos, blockPos);
            }
            return this.box;
        }
    }
}
