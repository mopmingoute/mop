/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MatPTrans is part of MOP.
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

package procesosEstocasticos;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;

/**
 * Clase para representar matrices cuadradas de probabilidades de transici�n
 * est1 es la estaci�n en el paso inicial
 * La matriz no depende de la estaci�n en el paso final.
 * 
 */
public class MatPTrans implements Comparable{
	/**
	 * Estaci�n en el paso inicial para las matrices de un paso 
	 * y paso inicial en las matrices de m�ltiples pasos
	 */
	private int est1;   
    
	/**
	 * Paso final en las matrices de m�ltiples pasos. No se usa en las matrices de un paso.
	 */
    private int est2;

    /**
     * Matriz de probabilidades de transici�n. 
     * Primer �ndice estado compuesto inicial
     * Segundo �ndice estado compuesto final
     * Estado compuesto: resulta de las ordenaci�n
     * lexicogr�fica de los estados de cada variable de estado.
     * Ejemplo para dos variables de estado:
     * (1,1),(1,2), ....;(2,1),(2,2),(2,3),... 
     */        
    private double[][] probs;
    
    /**
     * 
     */
    private ArrayList<int[]> defEstadosComp;

    public MatPTrans(int est1, double[][] probs) {
        this.est1 = est1;
        this.probs = probs;
    }
    
    
    public MatPTrans(int pasoIni, int pasoFin, double[][] probs) {
        this.est1 = pasoIni;
    	this.est2 = pasoFin;
        this.probs = probs;
    }    
    
    /**
     * Construye una matriz de transici�n a partir de un texto que se ha le�do, 
     * sabiendo la dimensi�n dim 
     * dim es la cantidad de estados compuestos, que es la cantidad de combinaciones de estados posibles
     * 
     * @param texto 
     * @param filaini �ndice de la fila en texto dice "PROBABILIDADES DE TRANSICI�N" de la matriz a leer
     * @param colini �ndice de la primera columna de la matriz en texto, empezando en cero
     * @param dim
     */
    public MatPTrans(ArrayList<ArrayList<String>> texto, int filaIni, int colini, int dim){    	
    	int i=filaIni;
    	probs = new double[dim][dim]; 
    	i++;
    	est1= Integer.parseInt(texto.get(i).get(1));	
    	i++;  // saltea los nombres de estados finales
    	for(int ifil=0; ifil<dim; ifil++){
    		for(int icol=0; icol<dim; icol++){
    			probs[ifil][icol] = Double.parseDouble(texto.get(i+ifil).get(colini +icol));
    		}
    	}    	
    }
    
    
    /**
     * Construye una matriz de transici�n a partir de un texto que se ha le�do, 
     * sabiendo la dimensi�n dim 
     * dim es la cantidad de estados compuestos, que es la cantidad de combinaciones de estados posibles
     * 
     * @param texto 
     * @param filaini índice de primera fila de la matriz a leer
     * @param colini índice de la primera columna de la matriz en texto, empezando en cero
     * @param cantFil cantidad de filas de la matriz
     * @param cantCol cantidad de columnas de la matriz
     */
    public MatPTrans(ArrayList<ArrayList<String>> texto, int filaIni, int colini, int cantFil, int cantCol){    	
    	int i=filaIni;
    	probs = new double[cantFil][cantCol]; 
    	est1= Integer.parseInt(texto.get(i).get(1));	
    	for(int ifil=0; ifil<cantFil; ifil++){
    		for(int icol=0; icol<cantCol; icol++){
    			probs[ifil][icol] = Double.parseDouble(texto.get(i+ifil).get(colini +icol));
    		}
    	}    	
    }    
    
    
    
    
    
    

    public ArrayList<int[]> getDefEstadosComp() {
        return defEstadosComp;
    }

    public void setDefEstadosComp(ArrayList<int[]> defEstadosComp) {
        defEstadosComp = defEstadosComp;
    }
    
    
    public double devuelveProb(int estIni, int estFin) {
    	return probs[estIni][estFin];
    }
    
    

    public int getEst1() {
        return est1;
    }

    public void setEst1(int est1) {
        this.est1 = est1;
    }


    public double[][] getProbs() {
        return probs;
    }

    public void setProbs(double[][] probs) {
        this.probs = probs;
    }
    
    @Override
    public int compareTo(Object t) {
        MatPTrans t1 = (MatPTrans)t;
        if(est1<t1.getEst1()) return -1;
        if(est1==t1.getEst1()) return 0;
        return 1;
    }
    
    
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        // Imprime l�nea de t�tulos
        sb.append("PROBABILIDADES-DE-TRANSICION" + "\n");    
        sb.append("Estaci�n-paso-ini= ");
        sb.append(est1);
        
        // Imprime una l�nea con los estados finales
        sb.append(" ");
        for (int j=0; j<probs.length; j++){
            sb.append("E.comp.fin=");
            sb.append(j);
            sb.append(" ");            
        }
        sb.append("\n");
        // Imprime una l�nea por cada estado inicial
        for (int i=0; i<probs.length; i++){
            sb.append("E.comp.ini= ");
            sb.append(i);                    
            for (int j=0; j<probs.length; j++){
                sb.append(" ");    
                sb.append(probs[i][j]);           
            }
            if(i<probs.length-1) sb.append("\n");
        }        
        String st = sb.toString();        
        return st;
    }
    
    
}

