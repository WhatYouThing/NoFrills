package nofrills.misc;

import jama.Matrix;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import nofrills.events.ServerTickEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import org.joml.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nofrills.Main.mc;

public class CurveSolver {
    private static final List<Vec3d> particleList = new ArrayList<>();
    private static final PolynomialFitter3D fitter3D = new PolynomialFitter3D();
    private static ParticleType<?> currentParticle = null;
    private static int currentTicks = 0;
    private static Vec3d lastPos = null;

    public static ParticleType<?> getCurveParticle() {
        return switch (Utils.getSkyblockId(Utils.getHeldItem())) {
            case "ANCESTRAL_SPADE" -> ParticleTypes.DRIPPING_LAVA;
            case "HOTSPOT_RADAR" -> ParticleTypes.FLAME;
            case "EGGLOCATOR" -> ParticleTypes.HAPPY_VILLAGER;
            case "SKYMART_VACUUM", "SKYMART_TURBO_VACUUM", "SKYMART_HYPER_VACUUM", "INFINI_VACUUM",
                 "INFINI_VACUUM_HOOVERIUS" -> ParticleTypes.ANGRY_VILLAGER;
            default -> null;
        };
    }

    private static double[][] arrayPush(double[][] target, double[] value) {
        int length = target.length;
        double[][] array = Arrays.copyOf(target, length + 1);
        array[length] = value;
        return array;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (event.type.equals(currentParticle)) {
            if (event.pos.distanceTo(particleList.isEmpty() ? mc.player.getPos() : particleList.getLast()) > 4) {
                return;
            }
            if (particleList.size() % 2 == 0) {
                fitter3D.addPoint((double) (particleList.size() / 2), event.pos);
            }
            particleList.add(event.pos);
            currentTicks = 40;
            if (particleList.size() > 6) {
                lastPos = solve();
            }
        }
    }

    @EventHandler
    private static void onClientTick(WorldTickEvent event) {
        if (currentParticle == null) {
            if (mc.options.attackKey.isPressed() || mc.options.useKey.isPressed()) {
                particleList.clear();
                fitter3D.clear();
                currentParticle = getCurveParticle();
                currentTicks = 40;
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (currentTicks > 0) {
            currentTicks--;
            if (currentTicks == 0) {
                currentParticle = null;
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (lastPos != null) {
            event.drawBeam(lastPos, 128, true, RenderColor.fromHex(0xffffff, 0.67f));
        }
    }

    public static class PolynomialFitter {
        public int degree;
        public Matrix yMatrix = new Matrix(0, 1);
        public Matrix xMatrix = new Matrix(0, degree + 1);

        public PolynomialFitter(int degree) {
            this.degree = degree;
        }

        public void addPoint(double x, double y) {
            this.yMatrix = new Matrix(arrayPush(this.yMatrix.getArray(), new double[]{y}));
            double[] xMatrixData = new double[4];
            for (int i = 0; i < this.degree + 1; i++) {
                xMatrixData[i] = java.lang.Math.pow(x, i);
            }
            this.xMatrix = new Matrix(arrayPush(this.xMatrix.getArray(), xMatrixData));
        }

        public double[] fit() {
            Matrix xMatrixTransposed = this.xMatrix.transpose();
            Matrix result = xMatrixTransposed
                    .times(this.xMatrix)
                    .inverse()
                    .times(xMatrixTransposed)
                    .times(this.yMatrix)
                    .transpose();
            return result.getArray()[0];
        }

        public void clear() {
            this.yMatrix = new Matrix(0, 1);
            this.xMatrix = new Matrix(0, degree + 1);
        }
    }

    public static class PolynomialFitter3D {
        public PolynomialFitter[] fitters = {
                new PolynomialFitter(3),
                new PolynomialFitter(3),
                new PolynomialFitter(3)
        };

        public PolynomialFitter3D() {

        }

        public void addPoint(double t, Vec3d point) {
            this.fitters[0].addPoint(t, point.getX());
            this.fitters[1].addPoint(t, point.getY());
            this.fitters[2].addPoint(t, point.getZ());
        }

        public double[][] fit() {
            double[][] coefficients = new double[3][];
            for (int i = 0; i < 3; i++) {
                coefficients[i] = this.fitters[i].fit();
            }
            return new Matrix(coefficients).transpose().getArray();
        }

        public void clear() {
            for (PolynomialFitter fitter : fitters) {
                fitter.clear();
            }
        }
    }


    private static double calculateT(double[] vec) {
        double x = vec[0], y = vec[1], z = vec[2];
        return 7 / (Math.sqrt(9 * java.lang.Math.pow(y, 2) + 7 * (java.lang.Math.pow(x, 2) + java.lang.Math.pow(z, 2) + java.lang.Math.pow(y, 2))) - 3 * y);
    }

    private static Vec3d solve() {
        double[][] res = fitter3D.fit();
        double[] deriv_0 = new double[3];
        for (int i = 0; i < 3; i++) {
            deriv_0[i] = res[1][i] / 3;
        }
        double end_t = calculateT(deriv_0);
        double[] acc = new double[3];
        double term = 1;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                acc[j] += res[i][j] * term;
            }
            term *= end_t;
        }
        acc[1] -= 0.5;
        return new Vec3d(acc[0], acc[1], acc[2]);
    }
}