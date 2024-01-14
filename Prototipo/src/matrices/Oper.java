/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Oper is part of MOP.
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
 * OPERACIONES MATRICIALES
 * En todo esto se supone que las matrices son arrays de dos dimensiones
 * La primera dimensión son las filas y la segunda las columnas.
 * Las matrices fila son de dimensiones [1][n]
 * Las matrices columna son de dimensiones [n][1]
 */
public class Oper {

    public static double[][] prod(double[][] a, double[][] b) {
        // el primer índice son filas y el segundo columnas
        int nfilA = a.length;
        int ncolA = a[0].length;
        int nfilB = b.length;
        int ncolB = b[0].length;
        if (ncolA != nfilB){
        	System.out.println("en el producto matricial las dimensiones no cuadran");
        	System.exit(0);
        }
        double[][] prod = new double [nfilA][ncolB];
        for (int i=1; i<= nfilA; i++){
            for(int j=1; j<=ncolB; j++){
                double suma = 0.0;
                for(int k = 1; k<= ncolA; k++){
                    suma += a[i-1][k-1]*b[k-1][j-1];
                }
                prod[i-1][j-1] = suma;
            }
        }
        return prod;
    }
    
    public static double[][] suma(double[][] a, double[][] b) {
        // el primer get son filas y el segundo columnas
        int nfilA = a.length;
        int ncolA = a[0].length;
        int nfilB = b.length;
        int ncolB = b[0].length;
        if (ncolA != ncolB || nfilA != nfilB ){
        	System.out.println("En la suma matricial las dimensiones no cuadran");
        	System.exit(0);
        }                
        double[][] suma = new double [nfilA][ncolA];
        for (int i=1; i<= nfilA; i++){
            for(int j=1; j<=ncolA; j++){
                suma[i-1][j-1] = a[i-1][j-1] + b[i-1][j-1];
            }
        }
        return suma;
    }  
    
    public static void ceros(double[][]  mat){
    	for (int ifil = 0; ifil < mat.length; ifil++){
    		for (int icol = 0; icol< mat[0].length; icol++){
    			mat[ifil][icol] = 0.0;
    		}
    	}
    }
    

    /**
     * Devuelve la suma de cada fila (suma variando el segundo índice)
     */
    public static double[] sumaPorFilas(double[][] mat){
    	double[] sumafil = new double[mat.length];
       	for (int ifil = 0; ifil < mat.length; ifil++){
    		for (int icol = 0; icol< mat[0].length; icol++){
    			sumafil[ifil] += mat[ifil][icol];
    		}
    	}
       	return sumafil;
    }
    
    /**
     * Devuelve matriz normalizando las filas para que cada una sume 1
     * (la fila está dada por el primer índice)
     */
    public static double[][] normalizaFilas(double[][] mat){
		double[][] result = new double[mat.length][mat[0].length];
		double[] sumafil = sumaPorFilas(mat);
    	for(int ifil=0; ifil<mat.length; ifil++){
			for(int icol=0; icol<mat[0].length; icol++){
				result[ifil][icol]=mat[ifil][icol]/sumafil[ifil];
			}
		}
    	return result;
    }
    
    /**
     * Devuelve una nueva matriz m escalando las filas de la matriz original por
     * los valores respectivos de un vector v con dimensión la cantidad de filas de m
     */
    public static double[][] escalaFilas(double[] v, double[][] mat){
		double[][] result = new double[mat.length][mat[0].length];
    	for(int ifil=0; ifil<mat.length; ifil++){
			for(int icol=0; icol<mat[0].length; icol++){
				result[ifil][icol]=mat[ifil][icol]*v[ifil];
			}
		}
    	return result;
    }
    
    /**
     * Yuxtapone matrices de reales una al lado de otra horizontalmente A|B|C.....
     * Deben tener todas la misma cantidad de filas
     * @param matrices
     * @return
     * @throws XcargaDatos 
     */
    public static double[][] yuxtaMH(ArrayList<double[][]> matrices) {
        // el primer get son filas y el segundo columnas
        int im, ifil, icol, ncolm;
        int cantMat = matrices.size();
        int nfil = matrices.get(0).length;
        int sumaCol = 0;    // cantidad de columnas del resultado
        for(im=0; im<cantMat; im++){
            double[][] m = matrices.get(im);
            sumaCol += m[0].length;
            if(m.length!= nfil){
            	System.out.println("Error en dimensiones de matrices");
            	System.exit(0);
            }
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
    public static double[][] yuxtaMV(ArrayList<double[][]> matrices) {
        // el primer get son filas y el segundo columnas
        int im, ifil, icol, nfilm;
        int cantMat = matrices.size();
        int ncol = matrices.get(0)[0].length;
        int sumaFil = 0;    // cantidad de columnas del resultado
        for(im=0; im<cantMat; im++){
            double[][] m = matrices.get(im);
            sumaFil += m.length;
            if(m[0].length!= ncol){
            	System.out.println("Error en dimensiones de matrices");
            	System.exit(0);         
            }
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
    
    /**
     * Yuxtapone uno a continuaci�n del otro v1 y v2 y devuelve un
     * nuevo vector resultante.
     * @param v1
     * @param v2
     * @return
     */
    public static double[] yuxtaVecs(double[] v1, double[] v2){
    	int largoRes = v1.length + v2.length;
    	double[] res = new double[largoRes];
    	for(int i=0; i<v1.length; i++){
    		res[i] = v1[i];
    	}
    	for(int i=0; i<v2.length; i++){
    		res[v1.length + i] = v2[i];
    	}
    	return res;
    }
    
    
    /**
     * Yuxtapone dos listas de Strings una a continuación de la otra
     * y devuelve una nueva lista
     */
    public static String[] yuxtaListas(String[] l1, String[] l2){
    	int largoRes = l1.length + l2.length;
    	String[] res = new String[largoRes];
    	for(int i=0; i<l1.length; i++){
    		res[i] = l1[i];
    	}
    	for(int i=0; i<l2.length; i++){
    		res[l1.length + i] = l2[i];
    	}
    	return res;    		
    }
    
    
    /**
     * Invierte la matriz m y devuelve la inversa
     * @param m
     * @return
     */
    public static double[][] inv(double[][] m) {
        int nfil = m.length;
        int ncol = m[0].length;
        if(nfil!=ncol){
        	System.out.println("La matriz a invertir no es cuadrada");
        	System.exit(0);
        }
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
    
    public static double[][] opuesta(double[][] m) {
        int nfil = m.length;
        int ncol = m[0].length;
        double[][] op = new double[nfil][nfil];
        for(int ifil=0; ifil<nfil; ifil++){
            for(int icol=0; icol<ncol; icol++){
                op[ifil][icol] = - m[ifil][icol];
            }
        }
        return op;
    }	
    
    	
    
    /**
     * Devuelve en diag los valores propios y en qmat y qmatinv (inversa de qmat)
     * el resultado de la eigen descomposici�n matSim = qmat diag qmatinv
     * qmat tiene como columnas los vectores propios
     * (Si matSim es sim�trica qmatinv es sim�trica de qmat)
     */
    public static void eigenDecomp(double[][] matSim, double[][] diag, double[][] qmat, double[][] qmatinv){
        DenseMatrix64F mS = new DenseMatrix64F(matSim);
        DenseMatrix64F v1;
        int dim = matSim.length;     
        EigenDecomposition<DenseMatrix64F> decomp =  DecompositionFactory.eig(dim, true, true);
        boolean resultado = decomp.decompose(mS);
        for(int icol = 0; icol<dim; icol ++){
            Complex64F ev1 = decomp.getEigenvalue(icol);
            diag[icol][icol] = (double)ev1.getReal();
            v1 = decomp.getEigenVector(icol);            
            for(int ifil = 0; ifil<dim; ifil ++){                    
                qmat[ifil][icol] = (double)v1.get(ifil);
            }            
        }        
        SimpleMatrix qsm = new SimpleMatrix(qmat);
        SimpleMatrix qsminv = qsm.invert();  
        for(int icol = 0; icol<dim; icol ++){
            for(int ifil = 0; ifil<dim; ifil ++){   
                qmatinv[ifil][icol] = (double)qsminv.get(ifil, icol);
            }                                  
        }        
        SimpleMatrix diagsm = new SimpleMatrix(diag); 
        SimpleMatrix verif = diagsm.mult(qsminv);
        verif = qsm.mult(verif);      
    }
    
    
    /**
     * Devuelve la matriz transpuesta
     */
    public static double[][] transpuesta(double[][] m1){   	
    	int nfil = m1.length;
    	int ncol = m1[0].length;
    	double[][] mtr = new double[ncol][nfil];
    	for(int i=0; i<ncol; i++){
        	for(int j=0; j<nfil; j++){
        		mtr[i][j] = m1[j][i];
        	}
    	}
    	return mtr;
    }
    	
   
    
    /**
     * Devuelve una matriz que es el producto kronecker de A y B.
     * es decir que tiene bloques que son Aij B.
     * @param A
     * @param B
     * @return 
     */
    public static double[][] kronecker(double[][] A, double[][] B){
        int nfilA = A.length;
        int ncolA = A[0].length;
        int nfilB = B.length;
        int ncolB = B[0].length;
        int nfilK = nfilA*nfilB;
        int ncolK = ncolA*ncolB;     
        int ifK = 0;
        int icK = 0;
        double[][] K = new double[nfilK][ncolK];
        for(int ifA=0; ifA<nfilA; ifA++){
            for(int icA=0; icA<ncolA; icA++){
                for(int ifB=0; ifB<nfilB; ifB++){
                    for(int icB=0; icB<ncolB; icB++){
                        ifK = ifA*nfilA + ifB;
                        icK = icA*ncolA + icB;
                        K[ifK][icK]=A[ifA][icA]*B[ifB][icB];
                    }
                }
            }
        }      
        return K;
        
    }
    
    /**
     * Devuelve un vector columna  único formado por la yuxtaposici�n
     * vertical de todas las columnas de la matriz mat, en orden
     * @param mat
     * @return 
     */
    public static double[][] vecCol(double[][] mat){
        int cantFil = mat.length;
        int cantCol = mat[0].length;
        double[][] result = new double[cantFil*cantCol][1];        
        for(int icol=0; icol<cantCol; icol++){
            for(int ifil=0;ifil<cantFil; ifil++){        
                result[icol*cantFil+ifil][1] = mat[ifil][icol];
            }
        }
        return result;        
    }
    
    /**
     * Multiplica el vector vec por un escalar esc
     * @param mat
     * @return
     */
    public static void vecPorUnEscalar(double[] vec, double esc){
    	for(int i=0; i<vec.length; i++){
    		vec[i] = vec[i]*esc;
    	}    	    	
    }
    
    
    /**
     * Devuelve un nuevo vector que es el producto de vec por un escalar esc
     * @param mat
     * @return
     */
    public static double[] prodVecEscalar(double[] vec, double esc){
    	double[] res = new double[vec.length];
    	for(int i=0; i<vec.length; i++){
    		res[i] = vec[i]*esc;
    	}
    	return res;
    }
        
    
    /**
     * Multiplica la matriz mat por un escalar esc
     * @param mat
     * @return
     */
    public static void matPorUnEscalar(double[][] mat, double esc){
    	for(int i=0; i<mat.length; i++){
    		for(int j=0; j<mat[0].length; j++)
    			mat[i][j] = mat[i][j]*esc;
    	}    	    	
    }    
    
    
    public static int cantFil(double[][] mat){
        return mat.length;
    }

    public static int cantCol(double[][] mat){
        return mat[0].length;
    }
    
    
    /**
     * Devuelve la columna j-ésima de la matriz mat, con j empezando en cero.
     * Es decir devuelve mat[.][j]
     * @param mat
     * @return
     */
    public static double[] devuelveCol(double[][] mat, int j){
    	double[] colj = new double[mat.length];
    	for(int i=0; i<mat.length; i++){
    		colj[i]=mat[i][j];
    	}
    	return colj;
    }

    public static double[][] copia(double[][] mat){
        int nfil = cantFil(mat);
        int ncol = cantCol(mat);
        double[][] copia = new double[nfil][ncol];
        for(int i=1; i<= nfil; i++){
            for(int j=1; j<= ncol; i++){
                copia[i-1][j-1] = mat[i-1][j-1];
            }
        }
        return copia;

    }

    public static boolean[][] prod(boolean[][] a, boolean[][] b) throws XcargaDatos{
        // el primer get son filas y el segundo columnas
        int nfilA = a.length;
        int ncolA = a[0].length;
       // int nfilB = b.length;
        int ncolB = b[0].length;
        if (ncolA != ncolB){
        	System.out.println("En el producto matricial las dimensiones no cuadran");
        	System.exit(0);   
        }
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

    public static int cantFil(boolean[][] mat){
        return mat.length;
    }

    public static int cantCol(boolean[][] mat){
        return mat[0].length;
    }

    public static boolean[][] copia(boolean[][] mat){
        int nfil = cantFil(mat);
        int ncol = cantCol(mat);
        boolean[][] copia = new boolean[nfil][ncol];
        for(int i=1; i<= nfil; i++){
            for(int j=1; j<= ncol; i++){
                copia[i-1][j-1] = mat[i-1][j-1];
            }
        }
        return copia;

    }
    
    public static String imprime(double[][] mat){
        StringBuilder sb = new StringBuilder();
        int cantFil = mat.length;
        int cantCol = mat[0].length;
        for(int ifil = 0; ifil< cantFil; ifil++){
            for(int icol = 0; icol< cantCol; icol++){
                sb.append(mat[ifil][icol]);
                sb.append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();               
    }
    
    
    /**
     * Multiplica matriz A por vector x,  Ax
     * y devuelve el vector resultante
     * 
     * @param args
     */
    public static double[] matPorVector(double[][] mat, double[] vec){
    	int ncol = cantCol(mat);
    	int nfil = cantFil(mat);
    	double[] result = new double[nfil];
    	if(ncol != vec.length){
    		System.out.println("Error: dimensiones no conformables en método matPorVector");
    		System.exit(1);
    	}
    	for(int ifil = 0; ifil<nfil; ifil++){
        	double sum = 0;
    		for(int ic=0; ic<ncol; ic ++){
    			sum = sum + mat[ifil][ic]*vec[ic];
    		}
    		result[ifil] = sum;
    	}
    	return result;    	    	
    }
    
   /**
    * Suma un vector v2 a otro preexistente v1
    * @param args
    */
    public static void sumaUnVector(double[] v1, double[] v2){
    	if(v1.length != v2.length){
    		System.out.println("Error: dimensiones no conformables en m�todo sumaVectores");
    		System.exit(1);
    	}
    	for(int i=0; i<v1.length; i++){
    		v1[i] = v1[i] + v2[i]; 
    	}	
    }
    
    
    /**
     * Suma componentes de un vector vector
     */
    public static double sumaComponentesVector(double[] v1) {
    	double suma = 0.0;
    	for(int i=0; i<v1.length; i++){
    		suma += v1[i]; 
    	}	
    	return suma;   	
    }
    
    /**
     * Norma euclídea al cuadrado de un vector
     */
    public static double normaEuCuad(double[] vector) {
    	double n2 = 0.0;
    	for(int i=0; i<vector.length; i++) {
    		n2+= Math.pow(vector[i],2);
    	}
    	return n2;
    }
    
    /**
     * Suma dos vectores creando un tercero con la suma
     * @param 
     */
     public static double[] sumaVectores(double[] v1, double[] v2){
     	if(v1.length != v2.length){
     		System.out.println("Error: dimensiones no conformables en m�todo sumaVectores");
     		System.exit(1);
     	}
     	double[] res = new double[v1.length];
     	for(int i=0; i<v1.length; i++){
     		res[i] = v1[i] + v2[i]; 
     	}	
     	return res;
     }    
    
    /**
     * Producto escalar de dos vectors
     * @param args
     */
     public static double prodEscalarVectores(double[] v1, double[] v2){
      	if(v1.length != v2.length){
      		System.out.println("Error: dimensiones no conformables en m�todo sumaVectores");
      		System.exit(1);
      	}
      	double res=0.0;
      	for(int i=0; i<v1.length; i++){
      		res += v1[i]*v2[i]; 
      	}	
      	return res;
      }    
     
    public static double[][] creaMatAPartirDeVecColumna(double[] vec){
    	int largo = vec.length;
    	double[][] mat = new double[largo][1];
    	for(int i=0; i<largo; i++) {
    		mat[i][0] = vec[i];
    	}
    	return mat;
    }
    public static void main(String[] args){
        double[][] A = {{1,0},{2,3}};
        double[][] B = {{1,1},{1,1}};
        double[][] K = kronecker(A, B);
        System.out.print(imprime(K));                  
    }
    
    

    

}


