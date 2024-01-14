/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * OperMatrices2 is part of MOP.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package matrices;

import java.util.ArrayList;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;
import org.ejml.simple.SimpleMatrix;



/**
 *
 * @author ut469262
 */
public class OperMatrices2 {

    public static double[][] productoMatRealAB(double[][] a, double[][] b) throws XcargaDatos{
        // el primer get son filas y el segundo columnas
        int nfilA = a.length;
        int ncolA = a[0].length;
//        int nfilB = b.length;
        int ncolB = b[0].length;
        if (ncolA != ncolB) throw new XcargaDatos("en el producto matricial las" +
                "dimensiones no cuadran");
        double[][] prod = new double [nfilA][ncolB];
        for (int i=1; i<= nfilA; i++){

            for(int j=1; j<ncolB; j++){
                double suma = 0.0;
                for(int k = 1; k<= ncolA; k++){
                    suma += a[i-1][k-1]*b[k-1][j-1];
                }
                prod[i-1][j-1] = suma;
            }
        }
        return prod;
    }
    
    public static double[][] sumaMatRealAB(double[][] a, double[][] b) throws XcargaDatos{
        // el primer get son filas y el segundo columnas
        int nfilA = a.length;
        int ncolA = a[0].length;
        int nfilB = b.length;
        int ncolB = b[0].length;
        if (ncolA != ncolB || nfilA != nfilB ) throw new XcargaDatos("en la suma matricial las" +
                "dimensiones no cuadran");
        double[][] suma = new double [nfilA][ncolA];
        for (int i=1; i<= nfilA; i++){
            for(int j=1; j<ncolA; j++){
                suma[i-1][j-1] = a[i-1][j-1] + b[i-1][j-1];
            }
        }
        return suma;
    }  
    

    
    /**
     * Yuxtapone matrices de reales una al lado de otra horizontalmente A|B|C.....
     * Deben tener todas la misma cantidad de filas
     * @param matrices
     * @return
     * @throws XcargaDatos 
     */
    public static double[][] yuxtaMatRealHoriz(ArrayList<double[][]> matrices) throws XcargaDatos{
        // el primer get son filas y el segundo columnas
        int im, ifil, icol, ncolm;
        int cantMat = matrices.size();
        int nfil = matrices.get(0).length;
        int sumaCol = 0;    // cantidad de columnas del resultado
        for(im=0; im<cantMat; im++){
            double[][] m = matrices.get(im);
            sumaCol += m[0].length;
            if(m.length!= nfil) throw new XcargaDatos("Error en dimensiones de matrices");            
        }
        double[][] result = new double[nfil][sumaCol];
        for(ifil = 0; ifil<nfil; ifil++){
            icol = 0;            
            for (im=1; im<= cantMat; im++){
                double[][] m = matrices.get(im);
                ncolm = m[0].length;
                for(int j=1; j<ncolm; j++){
                    result[ifil][icol] = m[ifil][icol];
                    icol++;
                }
            }
        }
        return result;
    }      
    
    /**
     * Yuxtapone matrices de reales una al lado de otra verticalmente
     * A
     * B
     * C
     * .
     * .
     * 
     * Deben tener todas la misma cantidad de columnas
     * @param matrices
     * @return
     * @throws XcargaDatos 
     */    
    public static double[][] yuxtaMatRealVert(ArrayList<double[][]> matrices) throws XcargaDatos{
        // el primer get son filas y el segundo columnas
        int im, ifil, icol, nfilm;
        int cantMat = matrices.size();
        int ncol = matrices.get(0)[0].length;
        int sumaFil = 0;    // cantidad de columnas del resultado
        for(im=0; im<cantMat; im++){
            double[][] m = matrices.get(im);
            sumaFil += m.length;
            if(m[0].length!= ncol) throw new XcargaDatos("Error en dimensiones de matrices");            
        }
        double[][] result = new double[ncol][sumaFil];
        for(icol = 0; icol<ncol; icol++){
            ifil = 0;            
            for (im=1; im<= cantMat; im++){
                double[][] m = matrices.get(im);
                nfilm = m.length;
                for(int j=1; j<nfilm; j++){
                    result[ifil][icol] = m[ifil][icol];
                    icol++;
                }
            }
        }
        return result;
    }    
    
    
    public static double[][] inversa(double[][] m) throws XcargaDatos{
        int nfil = m.length;
        int ncol = m[0].length;
        if(nfil!=ncol) throw new XcargaDatos("La matriz a invertir no es cuadrada");
        SimpleMatrix mSM = new SimpleMatrix(m);
        SimpleMatrix invSM = mSM.invert();
        double[][] inv = new double[nfil][nfil];
        for(int ifil=0; ifil<nfil; ifil++){
            for(int icol=0; icol<nfil; icol++){
                inv[ifil][icol] = invSM.get(ifil, icol);
            }
        }
        return inv;
    }
    
    
//    /**
//     * ATENCION !!!!!!!!!!!!!!!!!!!!
//     * NO USAR ESTE METODO SINO EL DEL MISMO NOMBRE EN matrices.OPER 
//     * Si la matriz matSim es simótrica devuelve en diag los valores propios y en qmat y qmatt
//     * el resultado de la eigen descomposición matSim = qmatt diag qmat
//     */
//    public static void eigenDecomp(double[][] matSim, double[][] diag, double[][] qmat, double[][] qmatt){
//        DenseMatrix64F mS = new DenseMatrix64F(matSim);
//        int dim = matSim.length;
//        diag = new double[dim][dim];
//        qmat = new double[dim][dim];
//        qmatt = new double[dim][dim];        
//        EigenDecomposition<DenseMatrix64F> decomp =  DecompositionFactory.eig(dim, true, true);
//        decomp.decompose(mS);
//        for(int ifil = 0; ifil<dim; ifil ++){
//            Complex64F ev1 = decomp.getEigenvalue(ifil);
//            diag[ifil][ifil] = ev1.getReal();
//            DenseMatrix64F v1 = decomp.getEigenVector(ifil);            
//            for(int icol = 0; icol<dim; icol ++){                    
//                qmat[ifil][icol] = v1.get(icol);
//                qmatt[icol][ifil] = v1.get(icol);
//            }            
//        }
//        SimpleMatrix qsm = new SimpleMatrix(qmat);
//        SimpleMatrix qsmt = new SimpleMatrix(qmatt);  
//        SimpleMatrix diagsm = new SimpleMatrix(diag); 
//        SimpleMatrix verif = diagsm.mult(qsm);
//        verif = qsmt.mult(verif);
//        System.out.print(verif.toString());
//        
//        
//    }
        
    
    public static int cantFilasMatReal(double[][] mat){
        return mat.length;
    }

    public static int cantColMatReal(double[][] mat){
        return mat[0].length;
    }

    public static double[][] copiaMatReal(double[][] mat){
        int nfil = cantFilasMatReal(mat);
        int ncol = cantColMatReal(mat);
        double[][] copia = new double[nfil][ncol];
        for(int i=1; i<= nfil; i++){
            for(int j=1; j<= ncol; i++){
                copia[i-1][j-1] = mat[i-1][j-1];
            }
        }
        return copia;

    }

    public static boolean[][] productoMatBoolAB(boolean[][] a, boolean[][] b) throws XcargaDatos{
        // el primer get son filas y el segundo columnas
        int nfilA = a.length;
        int ncolA = a[0].length;
    //    int nfilB = b.length;
        int ncolB = b[0].length;
        if (ncolA != ncolB) throw new XcargaDatos("en el producto matricial las" +
                "dimensiones no cuadran");
        boolean[][] prod = new boolean [nfilA][ncolB];
        for (int i=1; i<= nfilA; i++){

            for(int j=1; j<ncolB; j++){
                boolean suma = false;
                for(int k = 1; k<= ncolA; k++){
                    if(a[i-1][k-1]== true & b[k-1][j-1]==true){
                        suma = true;
                        break;
                    }
                }
                prod[i-1][j-1] = suma;
            }
        }
        return prod;
    }

    public static int cantFilasMatBool(boolean[][] mat){
        return mat.length;
    }

    public static int cantColMatBool(boolean[][] mat){
        return mat[0].length;
    }

    public static boolean[][] copiaMatBool(boolean[][] mat){
        int nfil = cantFilasMatBool(mat);
        int ncol = cantColMatBool(mat);
        boolean[][] copia = new boolean[nfil][ncol];
        for(int i=1; i<= nfil; i++){
            for(int j=1; j<= ncol; i++){
                copia[i-1][j-1] = mat[i-1][j-1];
            }
        }
        return copia;

    }

}

