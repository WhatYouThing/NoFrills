package nofrills.misc;

import jama.Matrix;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static nofrills.Main.mc;

public class CurveSolver {
    private final List<Vec3> particleList = new ArrayList<>();
    private final PolynomialFitter3D fitter3D = new PolynomialFitter3D();
    private final int activeDuration;
    private final int clearDuration;
    private final double arriveDistance;
    private Vec3 solvedPos = null;
    private Vec3 startPos = null;
    private int activeTicks = 0;
    private int clearTicks = 0;

    public CurveSolver(int activeTicks, int clearTicks, double arriveDistance) {
        this.activeDuration = activeTicks;
        this.clearDuration = clearTicks;
        this.arriveDistance = arriveDistance;
    }

    public CurveSolver() {
        this(40, 2400, 16.0);
    }

    private double[][] arrayPush(double[][] target, double[] value) {
        int length = target.length;
        double[][] array = Arrays.copyOf(target, length + 1);
        array[length] = value;
        return array;
    }

    public void start() {
        this.resetFitter();
        this.activeTicks = this.activeDuration;
        this.clearTicks = this.clearDuration;
        this.startPos = mc.player.getEyePosition();
    }

    public void tick() {
        if (this.activeTicks > 0) {
            this.activeTicks -= 1;
            if (this.activeTicks == 0) {
                this.resetFitter();
            }
        }
        if (this.clearTicks > 0) {
            this.clearTicks -= 1;
            if (this.clearTicks == 0) {
                this.clear();
            }
        }
        if (this.solvedPos != null && this.solvedPos.distanceTo(mc.player.position()) < this.arriveDistance) {
            this.solvedPos = null;
        }
    }

    public boolean active() {
        return this.activeTicks > 0;
    }

    public void addPos(Vec3 pos) {
        this.fitter3D.addPoint(this.particleList.size(), pos);
        this.particleList.add(pos);
        this.activeTicks = this.activeDuration;
        this.clearTicks = this.clearDuration;
        if (this.particleList.size() > 3) {
            this.solvedPos = solve();
        }
    }

    public double getLastDist(Vec3 pos) {
        return !this.particleList.isEmpty() ? this.particleList.getLast().distanceTo(pos) : this.startPos.distanceTo(pos);
    }

    public Optional<Vec3> getSolvedPos() {
        return this.solvedPos != null ? Optional.of(this.solvedPos) : Optional.empty();
    }

    public void resetFitter() {
        this.particleList.clear();
        this.fitter3D.clear();
    }

    public void clear() {
        this.resetFitter();
        this.solvedPos = null;
        this.activeTicks = 0;
        this.clearTicks = 0;
    }

    private double calculateT(double[] vec) {
        double x = vec[0], y = vec[1], z = vec[2];
        return 7 / (Math.sqrt(9 * Math.pow(y, 2) + 7 * (Math.pow(x, 2) + Math.pow(z, 2) + Math.pow(y, 2))) - 3 * y);
    }

    private Vec3 solve() {
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
        return new Vec3(acc[0], acc[1], acc[2]);
    }

    public class PolynomialFitter {
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
                xMatrixData[i] = Math.pow(x, i);
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

    public class PolynomialFitter3D {
        public PolynomialFitter[] fitters = {
                new PolynomialFitter(3),
                new PolynomialFitter(3),
                new PolynomialFitter(3)
        };

        public PolynomialFitter3D() {
        }

        public void addPoint(double t, Vec3 point) {
            this.fitters[0].addPoint(t, point.x());
            this.fitters[1].addPoint(t, point.y());
            this.fitters[2].addPoint(t, point.z());
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
}