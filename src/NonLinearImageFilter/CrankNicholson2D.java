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
    private double[][] diffCoef;

    /**
     * Constructor
     *
     * @param bCondition coefficients in boundary condition
     * @param diffCoef diffusion coefficient
     * @param nonLinearCoef nonLinearCoefficient;
     */
    public CrankNicholson2D(double[] bCondition, double diffCoef, double nonLinearCoef) {
        bConditionCoef = new double[3];
        bConditionCoef = Arrays.copyOfRange(bCondition, 0, 3);
        this.diffCoefFactor = diffCoef;
        this.nonLinearCoef = nonLinearCoef;
    }

    private void diffCoefficient(double[][] data) {
        int xsize = data[0].length;
        int ysize = data.length;
        diffCoef = new double[ysize][xsize];
        for (int i = 1; i < ysize - 1; i++) {
            for (int k = 1; k < xsize - 1; k++) {
                diffCoef[i][k] = diffCoefFactor
                        * Math.exp(-(Math.pow(data[i][k + 1] - data[i][k - 1], 2)
                                + Math.pow(data[i + 1][k] - data[i - 1][k], 2))
                                / nonLinearCoef);
            }
        }
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
    private void putColumn(int index, double [][] data, double [] dataY) {
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
        /*
         * Iteration over rows
         */
        for (int i = 0; i < ysize; i++) {
            double[] bCond = new double[2];
            bCond[0] = bConditions[0][i];
            bCond[0] = bConditions[2][i];
            result[i] = iterateLinear1D(data[i], bCond);
        }
        /*
         * Iteration over columns
         */
        for (int i = 0; i < xsize; i++) {
            double[] bCond = new double[2];
            bCond[0] = bConditions[1][i];
            bCond[0] = bConditions[3][i];
            double[] dataY = iterateLinear1D(getColumn(i, result), bCond);
            putColumn(i, result, dataY);
        }
        return result;
    }

    /**
     * 2D linear iteration by one step. Trigonal matrix reversion.
     *
     * @param data initial data
     * @param bCondition two-member array containing boundary condition values
     * at two boundaries
     * @return
     */
    public double[] iterateLinear1D(double[] data, double[] bCondition) {
        int size = data.length;
        double[] result = new double[size];
        return result;
    }
}
