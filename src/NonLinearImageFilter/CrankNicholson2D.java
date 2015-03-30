/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NonLinearImageFilter;

import java.util.Arrays;

/**
 * Class for 2D finite-difference algorithms
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class CrankNicholson2D {

    private double[] boundaryCondition;
    private double diffCoef;
    private double nonLinearCoef;

    /**
     * Constructor
     *
     * @param bCondition coefficients in boundary condition
     * @param diffCoef diffusion coefficient
     * @param nonLinearCoef nonLinearCoefficient;
     */
    public CrankNicholson2D(double[] bCondition, double diffCoef, double nonLinearCoef) {
        boundaryCondition = new double[3];
        boundaryCondition = Arrays.copyOfRange(bCondition, 0, 3);
        this.diffCoef = diffCoef;
        this.nonLinearCoef = nonLinearCoef;
    }

    private double[][] diffCoefficient(double[][] data) {
        int xsize = data[0].length;
        int ysize = data.length;
        double[][] coef = new double[ysize][xsize];
        for (int i = 1; i < ysize - 1; i++) {
            for (int k = 1; k < xsize - 1; k++) {
                coef[i][k] = diffCoef
                        * Math.exp(-(Math.pow(data[i][k + 1] - data[i][k - 1], 2)
                                + Math.pow(data[i + 1][k] - data[i - 1][k], 2))
                                / nonLinearCoef);
            }
        }
        return coef;
    }
    
    /**
     * Linear iteration by one step
     * @param data initial image data
     * @param boundaryConditions arrays containing boundary condition values at four edges
     * @return
     */
    public double [][] iterateLinear (double [] [] data, double [][] boundaryConditions) {
        int xsize = data[0].length;
        int ysize = data.length;
        double [][] result=new double [ysize][xsize];
        return result;
    }
}
