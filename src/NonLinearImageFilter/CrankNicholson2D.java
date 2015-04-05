/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NonLinearImageFilter;

import java.util.Arrays;

/**
 * Class for 2D finite-difference algorithms for 2D diffusion equation with
 * variable diffusion coefficient
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class CrankNicholson2D {

    private double[] bConditionCoef;
    private double diffCoefFactor;
    private double nonLinearCoef;

    /**
     * Constructor
     *
     * @param bConditionCoef coefficients in boundary condition
     * @param diffCoef diffusion coefficient
     * @param nonLinearCoef nonLinearCoefficient;
     */
    public CrankNicholson2D(double[] bConditionCoef, double diffCoef, double nonLinearCoef) {
        this.bConditionCoef = Arrays.copyOfRange(bConditionCoef, 0, 3);
        this.diffCoefFactor = diffCoef;
        this.nonLinearCoef = nonLinearCoef;
    }

    private double[][] diffCoefficient(double[][] data) {
        int xsize = data[0].length;
        int ysize = data.length;
        double[][] diffCoef = new double[ysize][xsize];
        for (int i = 1; i < ysize - 1; i++) {
            for (int k = 1; k < xsize - 1; k++) {
                diffCoef[i][k] = diffCoefFactor
                        * Math.exp(-(Math.pow(data[i][k + 1] - data[i][k - 1], 2)
                                + Math.pow(data[i + 1][k] - data[i - 1][k], 2))
                                / nonLinearCoef);
            }
        }
        return diffCoef;
    }
    /*
     * Exctrating a column from 2D array
     */

    private double[] getColumn(int index, double[][] data) {
        int size = data.length;
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i][index];
        }
        return result;
    }

    /*
     * Putting a column to 2D array
     */
    private void putColumn(int index, double[][] data, double[] dataY) {
        int size = dataY.length;
        for (int i = 0; i < size; i++) {
            data[i][index] = dataY[i];
        }
    }

    /**
     * 2D linear iteration by one step
     *
     * @param data initial data
     * @param bConditions 4*size array containing boundary condition values at
     * four edges
     * @return
     */
    public double[][] iterateLinear2D(double[][] data, double[][] bConditions) {
        int xsize = data[0].length;
        int ysize = data.length;
        double[][] result = new double[ysize][xsize];
        double[][] diffCoef = diffCoefficient(data);
        /*
         * Iteration over rows
         */
        for (int i = 0; i < ysize; i++) {
            double[] bCond = new double[2];
            bCond[0] = bConditions[0][i];
            bCond[0] = bConditions[2][i];
            result[i] = iterateLinear1D(data[i], bCond, diffCoef[i], diffCoef[i]);
        }
        /*
         * Iteration over columns
         */
        for (int i = 0; i < xsize; i++) {
            double[] bCond = new double[2];
            bCond[0] = bConditions[1][i];
            bCond[0] = bConditions[3][i];
            double[] dataY = iterateLinear1D(getColumn(i, result), bCond, getColumn(i, diffCoef), getColumn(i, diffCoef));
            putColumn(i, result, dataY);
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
     */
    public double[] iterateLinear1D(double[] data, double[] bSum, double[] coefOld, double[] coef) {
        int size = data.length;
        double[] result = new double[size];
        double[] p = new double[size - 1];
        double[] q = new double[size - 1];
        /*
         * The step zero - calculationg p0 and q0 based on the lower boundary condition 
         */
        double d = coefOld[1] * data[2] + coefOld[0] * data[0] - (coefOld[1] + coefOld[0] - 1) * data[1];
        double factor = 1 / (bConditionCoef[0] * coef[1] - bConditionCoef[2] * coef[0]);
        p[0] = (bConditionCoef[2] * (coef[1] + coef[0] + 1) + bConditionCoef[1] * coef[1]) * factor;
        q[0] = (bSum[0] * coef[1] + bConditionCoef[2] * d) * factor;
        /*
         * Iteratively calculating all p and q coefficients
         */
        for (int m = 1; m < size - 1; m++) {
            d = coefOld[m] * data[m + 1] + coefOld[m - 1] * data[m - 1] - (coefOld[m] + coefOld[m - 1] - 1) * data[m];
            factor = 1 / ((coef[m] + coef[m - 1] + 1) + coef[m - 1] * p[m - 1]);
            p[m] = -coef[m] * factor;
            q[m] = (d + coef[m - 1] * q[m - 1]) * factor;
        }
        result[size - 1] = (bSum[1] * coef[size - 3] + bConditionCoef[0] * d - q[size - 2]
                * (bConditionCoef[1] * coef[size - 3] + bConditionCoef[0] * (coef[size - 2] + coef[size - 3] + 1)))
                / (bConditionCoef[2] * coef[size - 3] - bConditionCoef[0] * coef[size - 2] - p[size - 2]
                * (bConditionCoef[1] * coef[size - 3] + bConditionCoef[0] * (coef[size - 2] + coef[size - 3] + 1)));
        /*
         * Iteratively calculating the result
         */
        for (int m = size - 2; m > -1; m--) {
            result[m] = q[m] - p[m] * result[m + 1];
        }
        return result;
    }
}
