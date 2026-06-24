package nofrills.features.farming;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

import static nofrills.Main.mc;

@EventListener
public class VacuumSolver {
    public static final Feature instance = new Feature("vacuumSolver");

    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xaaff5555), "color", instance);
    public static final SettingColor tracerColor = new SettingColor(RenderColor.fromArgb(0xffff5555), "tracerColor", instance);

    private static final CurveSolver solver = new CurveSolver();
    private static final HashSet<String> hooverStore = Sets.newHashSet(
            "SKYMART_VACUUM",
            "SKYMART_TURBO_VACUUM",
            "SKYMART_HYPER_VACUUM",
            "INFINI_VACUUM",
            "INFINI_VACUUM_HOOVERIUS"
    );

    private static boolean isHoldingVacuum() {
        return hooverStore.contains(Utils.getSkyblockId(Utils.getHeldItem()));
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && solver.active() && event.isCurveParticle() && solver.getLastDist(event.pos) <= 5.0) {
            solver.addPos(event.pos);
        }
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && Utils.matchesKey(mc.options.keyAttack, event) && event.action == GLFW.GLFW_PRESS && Utils.isInGarden() && isHoldingVacuum()) {
            solver.start();
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive()) {
            solver.getSolvedPos().ifPresent(pos -> {
                Vec3 textPos = pos.subtract(0.0, 0.25, 0.0);
                event.drawBeam(pos, 256, true, color.value());
                event.drawDistanceScaledText(textPos, Component.literal("Pest"), 0.05f, true, color.valueWithAlpha(1.0f));
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
