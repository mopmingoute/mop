/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Matrices is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package matrices;


import org.ejml.simple.SimpleMatrix;


/**
 *
 * @author Maite
 */
public class Matrices {

    /**
     * @param args the command line arguments
     */ 
    
    
    

    
    
    public static void main(String[] args) {
        // TODO code application logic here
        double[][] mat = new double[3][3];
        double[] f1 = {1,2,3};
        double[] f2 = {2,2,2};
        double[] f3 = {1,2,1};
        mat[0]=f1;
        mat[1]=f2;
        mat[2]=f3;
        SimpleMatrix sm = new SimpleMatrix(mat);
        SimpleMatrix invsm = sm.invert();
        SimpleMatrix prod = sm.mult(invsm);
        System.out.print(prod.toString());
        double[][] matg = new double[3][3];
        double[] g1 = {1,2,3};
        double[] g2 = {2,2,2};
        double[] g3 = {3,2,1};
        matg[0]=g1;
        matg[1]=g2;
        matg[2]=g3;
        sm = new SimpleMatrix(mat);
        double[][] diag = new double[3][3];
        double[][] q = new double[3][3];        
        double[][] qt = new double[3][3];                
        Oper.eigenDecomp(matg, diag, q, qt);
        
        
        
    }    
}

