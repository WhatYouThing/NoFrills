package nofrills.misc;

import jama.Matrix;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurveSolver {
    private final List<Vec3d> particleList = new ArrayList<>();
    private final PolynomialFitter3D fitter3D = new PolynomialFitter3D();
    private Vec3d lastPos = null;

    private double[][] arrayPush(double[][] target, double[] value) {
        int length = target.length;
        double[][] array = Arrays.copyOf(target, length + 1);
        array[length] = value;
        return array;
    }

    public void addPos(Vec3d pos) {
        if (this.particleList.size() % 2 == 0) {
            this.fitter3D.addPoint(this.particleList.size() / 2.0, pos);
        }
        this.particleList.add(pos);
        if (this.particleList.size() > 6) {
            this.lastPos = solve();
        }
    }

    public Vec3d getSolvedPos() {
        return this.lastPos;
    }

    public void resetSolvedPos() {
        this.lastPos = null;
    }

    public void resetFitter() {
        this.particleList.clear();
        this.fitter3D.clear();
    }

    private double calculateT(double[] vec) {
        double x = vec[0], y = vec[1], z = vec[2];
        return 7 / (Math.sqrt(9 * Math.pow(y, 2) + 7 * (Math.pow(x, 2) + Math.pow(z, 2) + Math.pow(y, 2))) - 3 * y);
    }

    private Vec3d solve() {
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
}