package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class HoppitySolver {
    public static final Feature instance = new Feature("hoppitySolver");

    public static final SettingBool guessTracer = new SettingBool(true, "guessTracer", instance);
    public static final SettingColor guessColor = new SettingColor(RenderColor.fromArgb(0xaaffffff), "guessColor", instance);
    public static final SettingColor guessTracerColor = new SettingColor(RenderColor.fromArgb(0xffffffff), "guessTracerColor", instance);

    private static final CurveSolver solver = new CurveSolver();
    private static Vec3d guess = null;
    private static int ticks = 0;

    private static boolean isHoldingEgglocator() {
        return Utils.getSkyblockId(Utils.getHeldItem()).equals("EGGLOCATOR");
    }

    private static void startTicking() {
        ticks = 10;
    }

    private static void onLocatingStart() {
        solver.resetFitter();
        solver.resetSolvedPos();
        startTicking();
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (event.type.equals(ParticleTypes.HAPPY_VILLAGER)) {
            Utils.infoFormat("{} {} {} {} {} {} {} {}", event.pos.getX(), event.pos.getY(), event.pos.getZ(), event.packet.getSpeed(), event.packet.getCount(), event.packet.getOffsetX(), event.packet.getOffsetY(), event.packet.getOffsetZ());
            solver.addPos(event.pos);
            guess = solver.getSolvedPos();
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (isHoldingEgglocator()) {
            onLocatingStart();
            startTicking();
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (isHoldingEgglocator()) {
            onLocatingStart();
            startTicking();
        }
    }

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (ticks > 0) {
            ticks--;
            if (ticks == 0) {
                solver.resetFitter();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (guess != null) {
            BlockPos pos = BlockPos.ofFloored(guess);
            event.drawFilled(Box.enclosing(pos, pos), true, guessColor.value());
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        solver.resetFitter();
        solver.resetSolvedPos();
        guess = null;
        ticks = 0;
    }
}
