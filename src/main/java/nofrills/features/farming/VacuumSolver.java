package nofrills.features.farming;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.InputEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

import static nofrills.Main.mc;

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
    private static Vec3d currentPos = null;
    private static int ticks = 0;

    private static boolean isHoldingVacuum() {
        return hooverStore.contains(Utils.getSkyblockId(Utils.getHeldItem()));
    }

    private static boolean isVacuumParticle(SpawnParticleEvent event) {
        return event.matchParameters(ParticleTypes.ENCHANT, 10, -2.0f, 0.0f, 0.0f, 0.0f);
    }

    private static void onVacuumStart() {
        currentPos = null;
        solver.resetFitter();
        ticks = 40;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && isVacuumParticle(event) && ticks > 0 && solver.getLastDist(event.pos) <= 5.0) {
            solver.addPos(event.pos);
            if (solver.getSolvedPos() != null) {
                currentPos = solver.getSolvedPos();
            }
        }
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && Utils.matchesKey(mc.options.attackKey, event.key) && event.action == GLFW.GLFW_PRESS) {
            if (Utils.isInGarden() && isHoldingVacuum()) onVacuumStart();
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && currentPos != null) {
            if (mc.player.getPos().distanceTo(currentPos) <= 16.0) {
                currentPos = null;
                return;
            }
            Vec3d textPos = currentPos.subtract(0.0, 0.25, 0.0);
            float scale = Utils.getTextScale(textPos, 0.05f);
            event.drawBeam(currentPos, 256, true, color.value());
            event.drawText(textPos, Text.literal("Pest"), scale, true, color.valueWithAlpha(1.0f));
            if (tracer.value()) {
                event.drawTracer(currentPos, tracerColor.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        currentPos = null;
        solver.resetFitter();
        ticks = 0;
    }
}
