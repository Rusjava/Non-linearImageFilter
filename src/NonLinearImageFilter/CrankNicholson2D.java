/*
 * Copyright (C) 2015 Ruslan Feshchenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package NonLinearImageFilter;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for 2D finite-difference algorithms for 2D diffusion equation with
 * variable diffusion coefficient
 *
 * @author Ruslan Feshchenko
 * @version 2.0
 */
public class CrankNicholson2D {

    private final double[] bConditionCoef;
    private final double diffCoefFactor;
    private final double nonLinearFactor;
    private final double anisotropyFactor;
    protected final double eps;
    private final ExecutorService exc;

    /**
     * Constructor
     *
     * @param bConditionCoef coefficients in boundary condition
     * @param diffCoef diffusion coefficient
     * @param nonLinearCoef non-Linear coefficient;
     * @param precision precision of numerical solution
     * @param anisotropy
     * @param threadNumber
     */
    public CrankNicholson2D(double[] bConditionCoef, double diffCoef, double nonLinearCoef,
            double precision, double anisotropy, int threadNumber) {
        this.bConditionCoef = Arrays.copyOfRange(bConditionCoef, 0, 3);
        this.diffCoefFactor = diffCoef;
        this.nonLinearFactor = 1 / Math.pow(nonLinearCoef, 2);
        this.anisotropyFactor = anisotropy;
        this.eps = precision;
        this.exc = Executors.newFixedThreadPool(threadNumber);
    }

    /**
     * Calculating diffusion coefficient as a exponential function of the field
     * gradient
     *
     * @param data
     * @return
     */
    protected double[][] getDiffCoefficient(double[][] data) {
        int xsize = data[0].length;
        int ysize = data.length;
        double[] column = new double[ysize];
        Arrays.fill(column, diffCoefFactor);
        double[][] diffCoef = new double[ysize][xsize];
        for (int i = 2; i < ysize - 2; i++) {
            for (int k = 2; k < xsize - 2; k++) {
                double tm = diffCoefFactor
                        * Math.exp(-(Math.pow(data[i][k + 1] - data[i][k - 1], 2) / (1 - anisotropyFactor)
                                + Math.pow(data[i + 1][k] - data[i - 1][k], 2) * (1 - anisotropyFactor)) * nonLinearFactor);
                if ((new Double(tm).isNaN())) {
                    diffCoef[i][k] = 0;
                } else {
                    diffCoef[i][k] = tm;
                }
            }
        }
        /*
         * Treating boundaries differently
         */
        diffCoef[0] = getDiffCoefficient1D(data, 0, true, 1 - anisotropyFactor);
        diffCoef[1] = getDiffCoefficient1D(data, 1, true, 1 - anisotropyFactor);
        diffCoef[ysize - 2] = getDiffCoefficient1D(data, ysize - 2, true, 1 - anisotropyFactor);
        diffCoef[ysize - 1] = getDiffCoefficient1D(data, ysize - 1, true, 1 - anisotropyFactor);
        putColumn(0, diffCoef, getDiffCoefficient1D(data, 0, false, 1 / (1 - anisotropyFactor)));
        putColumn(1, diffCoef, getDiffCoefficient1D(data, 1, false, 1 / (1 - anisotropyFactor)));
        putColumn(xsize - 2, diffCoef, getDiffCoefficient1D(data, xsize - 2, false, 1 / (1 - anisotropyFactor)));
        putColumn(xsize - 1, diffCoef, getDiffCoefficient1D(data, xsize - 1, false, 1 / (1 - anisotropyFactor)));

        return diffCoef;
    }

    /**
     * calculating one column/row of diffusion coefficient
     *
     * @param data
     * @param index
     * @param ifrow
     * @param factor
     * @return
     */
    protected double[] getDiffCoefficient1D(double[][] data, int index, boolean ifrow, double factor) {
        double[] result;
        int size;
        if (ifrow) {
            size = data[0].length;
            result = new double[data[0].length];
            for (int i = 2; i < size - 2; i++) {
                double tm = diffCoefFactor
                        * Math.exp(-Math.pow(data[index][i + 1] - data[index][i - 1], 2) * nonLinearFactor * factor);
                if ((new Double(tm).isNaN())) {
                    result[i] = 0;
                } else {
                    result[i] = tm;
                }
            }
        } else {
            size = data.length;
            result = new double[size];
            for (int i = 2; i < size - 2; i++) {
                double tm = diffCoefFactor
                        * Math.exp(-Math.pow(data[i + 1][index] - data[i - 1][index], 2) * nonLinearFactor * factor);
                if ((new Double(tm).isNaN())) {
                    result[i] = 0;
                } else {
                    result[i] = tm;
                }
            }
        }
        result[0] = diffCoefFactor;
        result[1] = diffCoefFactor;
        result[size - 2] = diffCoefFactor;
        result[size - 1] = diffCoefFactor;
        return result;
    }

    /**
     * Calculating normalized squared difference
     *
     * @param data1
     * @param data2
     * @return
     */
    protected double calcDifference(double[][] data1, double[][] data2) {
        int xsize = data1[0].length;
        int ysize = data1.length;
        double sumDiff = 0, sum = 0;
        for (int i = 0; i < ysize; i++) {
            for (int k = 0; k < xsize; k++) {
                sumDiff += Math.pow(data1[i][k] - data2[i][k], 2);
                sum += Math.pow(data1[i][k], 2) + Math.pow(data2[i][k], 2);
            }
        }
        return 2 * sumDiff / sum;
    }

    /**
     * Extracting a column from 2D array
     *
     * @param index
     * @param data
     * @return
     */
    protected double[] getColumn(int index, double[][] data) {
        int size = data.length;
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i][index];
        }
        return result;
    }

    /**
     * Putting a column to 2D array
     *
     * @param index
     * @param data
     * @param dataY
     */
    protected void putColumn(int index, double[][] data, double[] dataY) {
        int size = dataY.length;
        for (int i = 0; i < size; i++) {
            data[i][index] = dataY[i];
        }
    }

    /**
     * 2D linear iteration by one step
     *
     * @param data data from the previous step
     * @param oldDiffCoef diffusion coefficient array from the previous step
     * @param newDiffCoef diffusion coefficient array from the current step
     * @param bConditions 4*size array containing boundary condition values at
     * four edges
     * @return
     * @throws java.lang.InterruptedException
     */
    protected double[][] iterateLinear2D(double[][] data, double[][] oldDiffCoef,
            double[][] newDiffCoef, double[][] bConditions) throws InterruptedException {
        int xsize = data[0].length;
        int ysize = data.length;
        double[][] result = new double[ysize][xsize];
        double[][] coefNew = new double[4][];
        double[][] coefOld = new double[4][];
        double[] column = new double[ysize];
        Arrays.fill(column, diffCoefFactor);
        CountDownLatch lt;
        AxThread th;
        /*
         * Saving diffusion coefficients on the row boundaries and filling them in with constants instead
         */
        coefNew[0] = getColumn(0, newDiffCoef);
        coefNew[1] = getColumn(1, newDiffCoef);
        coefNew[2] = getColumn(xsize - 2, newDiffCoef);
        coefNew[3] = getColumn(xsize - 1, newDiffCoef);
        putColumn(0, newDiffCoef, column);
        putColumn(1, newDiffCoef, column);
        putColumn(xsize - 2, newDiffCoef, column);
        putColumn(xsize - 1, newDiffCoef, column);
        coefOld[0] = getColumn(0, oldDiffCoef);
        coefOld[1] = getColumn(1, oldDiffCoef);
        coefOld[2] = getColumn(xsize - 2, oldDiffCoef);
        coefOld[3] = getColumn(xsize - 1, oldDiffCoef);
        putColumn(0, oldDiffCoef, column);
        putColumn(1, oldDiffCoef, column);
        putColumn(xsize - 2, oldDiffCoef, column);
        putColumn(xsize - 1, oldDiffCoef, column);

        /*
         * Iteration over rows. New thread latch
         */
        lt = new CountDownLatch(ysize);
        Future<double[]>[] res = new Future[ysize];
        for (int i = 0; i < ysize; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            double[] bCond = new double[2];
            bCond[0] = bConditions[0][i];
            bCond[1] = bConditions[2][i];
            th = new AxThread(data[i], bCond, oldDiffCoef[i], newDiffCoef[i], lt);
            res[i] = exc.submit(th);
        }
        lt.await();
        for (int i = 0; i < ysize; i++) {
            try {
                result[i] = res[i].get();
            } catch (ExecutionException | InterruptedException ex) {
                System.out.println("Error in a thread!");
                result[i] = data[i];
            }
        }

        /*
         * Reinstating old diffusion coefficient values at the boundaries of rows
         */
        putColumn(0, newDiffCoef, coefNew[0]);
        putColumn(1, newDiffCoef, coefNew[1]);
        putColumn(xsize - 2, newDiffCoef, coefNew[2]);
        putColumn(xsize - 1, newDiffCoef, coefNew[3]);
        putColumn(0, oldDiffCoef, coefOld[0]);
        putColumn(1, oldDiffCoef, coefOld[1]);
        putColumn(xsize - 2, oldDiffCoef, coefOld[2]);
        putColumn(xsize - 1, oldDiffCoef, coefOld[3]);

        /*
         * Filling in column's boundaries with constants
         */
        Arrays.fill(newDiffCoef[0], diffCoefFactor);
        Arrays.fill(newDiffCoef[1], diffCoefFactor);
        Arrays.fill(newDiffCoef[ysize - 2], diffCoefFactor);
        Arrays.fill(newDiffCoef[ysize - 1], diffCoefFactor);
        Arrays.fill(oldDiffCoef[0], diffCoefFactor);
        Arrays.fill(oldDiffCoef[1], diffCoefFactor);
        Arrays.fill(oldDiffCoef[ysize - 2], diffCoefFactor);
        Arrays.fill(oldDiffCoef[ysize - 1], diffCoefFactor);
        /*
         * Iteration over columns. New Thred latch
         */
        lt = new CountDownLatch(xsize);
        res = new Future[xsize];
        for (int i = 0; i < xsize; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            double[] bCond = new double[2];
            bCond[0] = bConditions[1][i];
            bCond[1] = bConditions[3][i];
            th = new AxThread(getColumn(i, result), bCond, getColumn(i, oldDiffCoef), getColumn(i, newDiffCoef), lt);
            res[i] = exc.submit(th);
        }
        lt.await();
        for (int i = 0; i < xsize; i++) {
            double[] dataY;
            try {
                dataY = res[i].get();
                putColumn(i, result, dataY);
            } catch (ExecutionException | InterruptedException ex) {
                System.out.println("Error in a thread!");
            }

        }
        return result;
    }

    /**
     * 1D linear iteration by one step. Trigonal matrix reversion.
     *
     * @param data initial data
     * @param bSum two-member array containing boundary condition values at two
     * boundaries
     * @param coefOld the values of diffusion coefficient from the previous step
     * @param coef the iterated values of diffusion coefficient
     * @return
     * @throws java.lang.InterruptedException
     */
    protected double[] iterateLinear1D(double[] data, double[] bSum, double[] coefOld,
            double[] coef) throws InterruptedException {
        int size = data.length;
        double[] result = new double[size];
        double[] p = new double[size - 1];
        double[] q = new double[size - 1];
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        /*
         * The step zero - calculationg p0 and q0 based on the lower boundary condition 
         */
        double a = (coef[1] + coef[2]) / 2;
        double b = (coef[0] + coef[1]) / 2;
        double c = a + b + 1;
        double d = (coefOld[1] + coefOld[2]) / 2 * data[2] + (coefOld[0] + coefOld[1]) / 2 * data[0]
                - (coefOld[1] + (coefOld[0] + coefOld[2]) / 2 - 1) * data[1];
        double factor = 1 / (bConditionCoef[0] * a - bConditionCoef[2] * b);
        p[0] = (bConditionCoef[2] * c + bConditionCoef[1] * a) * factor;
        q[0] = (bSum[0] * a + bConditionCoef[2] * d) * factor;

        /*
         * Iteratively calculating all p and q coefficients
         */
        for (int m = 1; m < size - 1; m++) {
            a = (coef[m] + coef[m + 1]) / 2;
            b = (coef[m - 1] + coef[m]) / 2;
            c = a + b + 1;
            d = (coefOld[m] + coefOld[m + 1]) / 2 * data[m + 1] + (coefOld[m - 1] + coefOld[m]) / 2 * data[m - 1]
                    - (coefOld[m] + (coefOld[m - 1] + coefOld[m + 1]) / 2 - 1) * data[m];
            factor = 1 / (c + b * p[m - 1]);
            p[m] = -a * factor;
            q[m] = (d + b * q[m - 1]) * factor;
        }
        result[size - 1] = (bSum[1] * (coef[size - 3] + coef[size - 2]) / 2 + bConditionCoef[0] * d - q[size - 2]
                * (bConditionCoef[1] * (coef[size - 3] + coef[size - 2]) / 2
                + bConditionCoef[0] * (coef[size - 2] + (coef[size - 3] + coef[size - 1]) / 2 + 1)))
                / (bConditionCoef[2] * (coef[size - 3] + coef[size - 2]) / 2 - bConditionCoef[0] * (coef[size - 2] + coef[size - 1]) / 2 - p[size - 2]
                * (bConditionCoef[1] * (coef[size - 3] + coef[size - 2]) / 2 + bConditionCoef[0] * (coef[size - 2] + (coef[size - 3] + coef[size - 1]) / 2 + 1)));

        /*
         * Iteratively calculating the result
         */
        for (int m = size - 2; m > -1; m--) {
            result[m] = q[m] - p[m] * result[m + 1];
        }
        return result;
    }

    /**
     * Applies non-linear filter with constant diffusion coefficient and zero
     * boundary sums
     *
     * @param data
     * @return
     * @throws java.lang.InterruptedException
     */
    public double[][] solveNonLinear(double[][] data) throws InterruptedException {
        int xsize = data[0].length;
        int ysize = data.length;
        double[][] bCond = new double[4][];
        bCond[0] = new double[ysize];
        bCond[2] = new double[ysize];
        bCond[1] = new double[xsize];
        bCond[3] = new double[xsize];
        double[][] coef = getDiffCoefficient(data);
        double[][] result = data;
        double[][] prevResult;
        do {
            prevResult = result;
            result = iterateLinear2D(data, coef, getDiffCoefficient(prevResult), bCond);
        } while (calcDifference(result, prevResult) > eps);
        exc.shutdown();
        return result;
    }

    /**
     * Applies linear filter with constant diffusion coefficient and zero
     * boundary sums
     *
     * @param data
     * @return
     * @throws java.lang.InterruptedException
     */
    public double[][] solveLinear(double[][] data) throws InterruptedException {
        int xsize = data[0].length;
        int ysize = data.length;
        double[][] bCond = new double[4][];
        bCond[0] = new double[ysize];
        bCond[2] = new double[ysize];
        bCond[1] = new double[xsize];
        bCond[3] = new double[xsize];
        double[][] coef = new double[ysize][];
        for (int k = 0; k < ysize; k++) {
            coef[k] = new double[xsize];
            Arrays.fill(coef[k], diffCoefFactor);
        }
        double[][] res = iterateLinear2D(data, coef, coef, bCond);
        exc.shutdown();
        return res;
    }

    /**
     * An inner class for Callable object wrappers for iterateLinear1D
     */
    private class AxThread implements Callable<double[]> {

        private final double[] arg1, arg2, arg3, arg4;
        private final CountDownLatch lt;

        /**
         * Constructor
         */
        AxThread(double[] data, double[] bSum, double[] coefOld, double[] coef, CountDownLatch lt) {
            this.arg1 = data;
            this.arg2 = bSum;
            this.arg3 = coefOld;
            this.arg4 = coef;
            this.lt = lt;
        }

        @Override
        public double[] call() throws Exception {
            double[] res = iterateLinear1D(arg1, arg2, arg3, arg4);
            lt.countDown();
            return res;
        }

    }
}
