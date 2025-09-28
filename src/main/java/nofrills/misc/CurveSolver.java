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

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class CurveSolver {
    private static final List<Vec3d> particleList = new ArrayList<>();
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

    private static double[][] arraySet(double[][] target, double[] value, int index) {
        int length = Math.max(index + 1, target.length);
        double[][] array = Arrays.copyOf(target, length);
        array[index] = value;
        return array;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (event.type.equals(currentParticle)) {
            if (event.pos.distanceTo(particleList.isEmpty() ? mc.player.getPos() : particleList.getLast()) > 4) {
                return;
            }
            particleList.add(event.pos);
            currentTicks = 40;
            try {
                lastPos = BezierCurve.solve();
            } catch (RuntimeException exception) {
                LOGGER.error("Caught exception while solving curve", exception);
            }
        }
    }

    @EventHandler
    private static void onClientTick(WorldTickEvent event) {
        if (currentParticle == null) {
            if (mc.options.attackKey.isPressed() || mc.options.useKey.isPressed()) {
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
    }

    public static class BezierCurve {
        public Matrix coefficients;

        public BezierCurve(double[][] coefficients) {
            double[][] bezierArray = {
                    {1, 0, 0, 0},
                    {-3, 3, 0, 0},
                    {3, -6, 3, 0},
                    {-1, 3, -3, 1}
            };
            this.coefficients = new Matrix(coefficients);
            Matrix bezierMatrix = new Matrix(bezierArray).inverse();
            Matrix coefficientsMatrix = new Matrix(Arrays.copyOf(coefficients, coefficients.length)).transpose();
            for (int i = 0; i < 3; i++) { // may or may not be broken :kek:
                double[] row = coefficientsMatrix.getArray()[i];
                Matrix coefficient = new Matrix(new double[][]{{row[0], row[1], row[2], row[3]}}).transpose();
                Matrix matrix = bezierMatrix.times(coefficient).transpose();
                this.coefficients = new Matrix(arraySet(this.coefficients.getArray(), matrix.getArray()[i], i));
            }
        }

        public static Vec3d solve() {
            if (particleList.size() < 3) {
                return null;
            }
            PolynomialFitter3D fitter3D = new PolynomialFitter3D();
            for (int i = 0; i < particleList.size(); i++) {
                fitter3D.addPoint(i, particleList.get(i));
            }
            BezierCurve curve = new BezierCurve(fitter3D.fit());
            double[] vecFirst = curve.coefficients.getArray()[0];
            double[] vecSecond = curve.coefficients.getArray()[1];
            double t = curve.calculateT(new double[]{vecSecond[0] - vecFirst[0], vecSecond[1] - vecFirst[1], vecSecond[2] - vecFirst[2]});
            double[] vecResult = curve.at(1 / t);
            return new Vec3d(vecResult[0], vecResult[1], vecResult[2]).subtract(0, 0.5, 0);
        }

        public double[] at(double t) {
            double[][] array = this.coefficients.copy().getArray();
            int length = array.length;
            for (int i = 0; i < length - 1; i++) {
                double[][] coefficients1 = Arrays.copyOfRange(array, 0, array.length - 2);
                double[][] coefficients2 = Arrays.copyOfRange(array, 1, array.length - 1);
                double[][] data = new double[][]{};
                for (int j = 1; j < length - 1; j++) {
                    double[] vec1 = coefficients1[j];
                    double[] vec2 = coefficients2[j];
                    Vec3d result = new Vec3d(vec1[0], vec1[1], vec1[2]).multiply(1 - t).add(new Vec3d(vec2[0], vec2[1], vec2[2]).multiply(t));
                    data = arrayPush(data, new double[]{result.getX(), result.getY(), result.getZ()});
                }
                array = data;
            }
            return array[0];
        }

        public double calculateT(double[] vec) {
            double x = vec[0], y = vec[1], z = vec[2];
            return 7 / (Math.sqrt(9 * java.lang.Math.pow(y, 2) + 7 * (java.lang.Math.pow(x, 2) + java.lang.Math.pow(z, 2) + java.lang.Math.pow(y, 2))) - 3 * y);
        }
    }
}