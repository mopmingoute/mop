/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AsistenteLectorTextos is part of MOP.
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

package utilitarios;

import java.util.ArrayList;

import datatypesProcEstocasticos.DatosGeneralesPE;

/**
 * Contiene  métodos para verificar etiquetas y parsear textos que vienen como 
 * ArrayList<String> procedentes de LeerDatosArchivo.
 * Los métodos no afectan el índice que reciben indicando la fila donde parsear.
 * @author ut469262
 *
 */
public class AsistenteLectorTextos {
	
	private ArrayList<ArrayList<String>> texto;  // texto leído
	private String dirArchivo;  // nombre del archivo de donde se extrajo el texto 
	

	public AsistenteLectorTextos(ArrayList<ArrayList<String>> texto, String dirArchivo) {
		super();
		this.texto = texto;
		this.dirArchivo = dirArchivo;
	}


	
	/**
	 * Si el String en columna 0 de fila i no es igual a etiqueta detiene la ejecución
	 * @param i
	 * @param etiqueta
	 */
	public void verificaEtiqueta(int i, String etiqueta){
		if(!texto.get(i).get(0).equalsIgnoreCase(etiqueta)){
    		System.out.println("Error en etiqueta de " + etiqueta + " en archivo " + dirArchivo);
    		System.exit(0);  
		}    	
	}	
	

	/**
	 * Devuelve el String que está en la columna 1 y si no hay columna 1 devuelve null
	 * Verifica si en la columna 0 de la línea i está la etiqueta e interrumpe
	 * la ejecución en caso de que no sea así.
	 * @param i
	 * @param etiqueta
	 */
	public String cargaPalabra(int i, String etiqueta){
		verificaEtiqueta(i, etiqueta);
		if(texto.get(i).size()>1) return texto.get(i).get(1);
		return null;
	}


	/**
	 * Devuelve el entero que está en la columna 1
	 * Verifica si en la columna 0 de la l�nea i est� la etiqueta e interrumpe
	 * la ejecuci�n en caso de que no sea as�.
	 * @param i
	 * @param etiqueta
	 */
	public int cargaEntero(int i, String etiqueta){
		verificaEtiqueta(i, etiqueta);
		return Integer.parseInt(texto.get(i).get(1));
	}
	
	
	/**
	 * Devuelve el real que est� en la columna 1
	 * Verifica si en la columna 0 de la l�nea i est� la etiqueta e interrumpe
	 * la ejecuci�n en caso de que no sea as�.
	 * @param i
	 * @param etiqueta
	 */
	public double cargaReal(int i, String etiqueta){
		verificaEtiqueta(i, etiqueta);
		return Double.parseDouble(texto.get(i).get(1));
	}	
	

	/**
	 * Carga un booleano seg�n el String le�do en la columna 1 de la fila i.
	 * Si la columna 0 no coincida con la etiqueta detiene la ejecuci�n.
	 * Si el String le�do es "SI" o "TRUE" con may�scula o min�scula carga el valor booleano true
	 * id�m NO y FALSE
	 * De lo contrario detiene la ejecuci�n 
	 * @param i
	 * @param etiqueta
	 * @return
	 */
	public boolean cargaBooleano(int i, String etiqueta){
		verificaEtiqueta(i, etiqueta);
		if(texto.get(i).get(1).equalsIgnoreCase("SI")||texto.get(i).get(1).equalsIgnoreCase("true")){
			return true;
		}else if(texto.get(i).get(1).equalsIgnoreCase("NO")||texto.get(i).get(1).equalsIgnoreCase("false")){
			return false;
		}else{
			System.out.println("Error en booleano de " + etiqueta + " en archivo " + dirArchivo);
    		System.exit(0);
    		return false;
		}    			
	}
	
	
	/**
	 * Verifica que la palabra leída en la columna 1 de la fila i sea una de la lista, 
	 * sin importar mayúsculas o minúsculas y devuelve esa palabra. 
	 * De lo contrario detiene la ejecución.
	 * Si la columna 0 no coincida con la etiqueta detiene la ejecución.
	 * @param i
	 * @param palabra
	 * @param lista
	 * @return
	 */
	public String cargaPalabraDeLista(int i, String etiqueta, ArrayList<String> lista){
		verificaEtiqueta(i, etiqueta);		
		for(String plista: lista){
			if(texto.get(i).get(1).equalsIgnoreCase(plista)) return plista;
		}
		String lmensaje = null;
		for(String plista: lista){
			lmensaje = lmensaje + plista + " ";
		}
		System.out.println("Error, no encontró palabra de lista " + lmensaje + " en archivo " + dirArchivo);
		System.exit(0);
		return "LINEA INACCESIBLE";			
	}
	
	
	/**
	 * Carga una lista tomada de la fila i, de palabras a partir de la columna 1
	 * Si la columna 0 no coincide con la etiqueta detiene la ejecuci�n.
	 * @param i
	 * @param etiqueta
	 * @return
	 */
	public ArrayList<String> cargaLista(int i, String etiqueta){
		verificaEtiqueta(i, etiqueta);
		ArrayList<String> aux = new ArrayList<String>();
		for(int j=1; j<texto.get(i).size(); j++){
			aux.add(texto.get(i).get(j));
		}
		return aux;
	}
	
	
	/**
	 * Carga una matriz de reales de nfil filas y ncol columnas
	 * que se encuentra a partir de la primera columna del texto en la fila i.
	 * @param i
	 * @param nfil
	 * @param ncol
	 * @return
	 */
	public double[][] cargaMatriz(int i, int nfil, int ncol){
		double[][] mat = new double[nfil][ncol];
		for(int ifil = 0; ifil<nfil; ifil++){
			for(int icol = 0; icol<ncol; icol++){
				mat[ifil][icol]=Double.parseDouble(texto.get(i).get(icol));
			}
			i++;
		}
		return mat;
	}
	
	
	/**
	 * Carga un vector de reales de n posiciones
	 * que se encuentra a partir de la primera columna del texto en la fila i.
	 * @param i
	 * @param n
	 * @return
	 */
	public double[] cargaVector(int i, int n){
		double[] vec = new double[n];
		for(int in = 0; in<n; in++){
			vec[in]=Double.parseDouble(texto.get(i).get(in));
		}
		return vec;
	}	
	
	
	
	public void pruebaDelI(int i){
		i++;
	}

	public void setTexto(ArrayList<ArrayList<String>> texto) {
		this.texto = texto;
	}
	
    public void setDirArchivo(String dirArchivo) {
		this.dirArchivo = dirArchivo;
	}



	public static void main(String[] args) { 
    	
		DatosGeneralesPE dat = new DatosGeneralesPE();	
		String dirArchivo = "D:/Proyectos/ModeloOp/resources/varmaAportes" + "/datosGenerales.txt";		
        ArrayList<ArrayList<String>> texto;       
        texto = LeerDatosArchivo.getDatos(dirArchivo);	
        	
    	AsistenteLectorTextos lector = new AsistenteLectorTextos(texto, dirArchivo);    	
    	int i=0;   		    	

    	String identEst = lector.cargaPalabra(i, "NOMBRE_ESTIMACION");
    	i++;
    	boolean b1 = lector.cargaBooleano(i, "USO_SIMULACION");
    	i++;
    	boolean b2 = lector.cargaBooleano(i, "uso_OPTIMIZACION");
    	i++;
    	boolean b3 = lector.cargaBooleano(i, "USA_TRANSFORMACIONES");
    	i++;    	
    	ArrayList<String> nomVar = lector.cargaLista(i, "NOMBRES_VARIABLES");
    	i++;
    	ArrayList<String> nomVarEst = lector.cargaLista(i, "NOMBRES_VARS_ESTADO");
    	i++;
    	boolean b4 = lector.cargaBooleano(i, "DISCRETO_EXHAUSTIVO");
    	i++;
    	lector.verificaEtiqueta(i, "PRIORIDAD_SORTEOS");
    	i++;
    	String nomPaso = lector.cargaPalabraDeLista(i, "NOMBRE_PASO", utilitarios.Constantes.NOMBRESPASOS);
    	i++;
    	boolean b5 = lector.cargaBooleano(i, "TIENE_VARS_EXOGENAS");
    	i++;
    	ArrayList<String> nex = lector.cargaLista(i,"NOMBRES_VAR_EXOGENAS");
    	i++;
    	ArrayList<String> npex = lector.cargaLista(i,"NOMBRES_PROCESOS_EXOGENAS"); 
    	
    	i = 0;
    	lector.pruebaDelI(i);
    	
    	System.out.println("termin� prueba " + i);
    }
	
	
	
}
