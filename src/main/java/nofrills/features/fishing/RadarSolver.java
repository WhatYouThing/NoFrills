package nofrills.features.fishing;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class RadarSolver {
    public static final Feature instance = new Feature("radarSolver");

    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xaaff55ff), "color", instance);
    public static final SettingColor tracerColor = new SettingColor(RenderColor.fromArgb(0xffff55ff), "tracerColor", instance);

    private static final CurveSolver solver = new CurveSolver();

    private static boolean isHoldingRadar() {
        return Utils.getSkyblockId(Utils.getHeldItem()).equals("HOTSPOT_RADAR");
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && event.isCurveParticle() && solver.active() && solver.getLastDist(event.pos) <= 5.0) {
            solver.addPos(event.pos);
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && isHoldingRadar()) {
            solver.start();
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && isHoldingRadar()) {
            solver.start();
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive()) {
            solver.getSolvedPos().ifPresent(pos -> {
                Vec3d textPos = pos.subtract(0.0, 0.25, 0.0);
                event.drawBeam(pos, 256, true, color.value());
                event.drawDistanceScaledText(textPos, Text.literal("Hotspot"), 0.05f, true, color.valueWithAlpha(1.0f));
                if (tracer.value()) {
                    event.drawTracer(pos, tracerColor.value());
                }
            });
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive()) {
            solver.tick();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        solver.clear();
    }
}
