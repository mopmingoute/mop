/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LectorValOptSimsee is part of MOP.
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

package optimizacion;

import utilitarios.Constantes;
import utilitarios.LeerDatosArchivo;
import java.util.*;

import futuro.InformacionValorPunto;


/**
 * La clase tiene
 * @author ut601781
 */
public class LectorValOptSimsee {
	
	/*
	 * El VAGUA.D del EDF siempre tiene anios enteros
	 */
	

    // Parametros basicos ingresados como atributos
    private int anini;   // anio inicial
    private int anfin;   // anio final 
    private int cantSem; // cantidad de semanas o paso de tiempo en un anio    
    private int cantPasoStock;  // cantidad de pasos de stock (incrementos) uno menos que los puntos
    private int cantVarEst;     // cantidad de discetización de las variables de estado
    private double[] valoresStock;  // valores de discretizacion del volumen util de Bonete, incluso el cero;
    
    /**
     * valRecursoLeidos: Lista por paso de tiempo con la matriz de datos por estado y paso de estock
     * el array es [indice estado hidrológico][indice paso de stock]; son incrementos medios.
     * 
     * valRecurso: valores resultantes de la interpolación Lagrange a partir de los anteriores. 
     * el array es [indice estado hidrológico][indice paso de stock] y tiene un paso de stock adicional;
     * son derivadas en los puntos de discretización.
     */
    private ArrayList<double[][] > valBellman;
    private ArrayList<double[][] > valRecursoLeidos;  
    private ArrayList<double[][]>  valRecurso;   
    
    
    public LectorValOptSimsee() {
    }
    
    public LectorValOptSimsee(int anini, int anfin, int cantSem, int cantPasoStock, int cantVarEst) {
        this.anini = anini;
        this.anfin = anfin;
        this.cantSem = cantSem;
        this.cantPasoStock = cantPasoStock;
        this.cantVarEst = cantVarEst;
        this.valRecursoLeidos = new ArrayList<double[][]>();
        this.valRecurso =  new ArrayList<double[][]>();
        // Ver como especifico las unidades: USD/m^3
    }
    
    
    /**
     * Lee el archivo VAGUA.D que se encuentra en el directorio dirDatYSal y 
     * crea en el atributo valRecurso, los valores del agua como derivadas parciales.
     * Se produce para cada semana y clase de aportes un valor mós que los leódos en VAGUA.D
     * Por ejemplo si hay 10 pasos de stock, VAGUA.D tiene 9 incrementos medios, pero valRecurso
     * tiene 10 derivadas en los puntos de discretización
     * 
     * @param dirDatYSal es el directorio donde se busca el archivo VAGUA.D
     */
    public void creaValorDelAgua(String dirDatYSal, double[] volumenesStock){
    	leerDatGen(dirDatYSal);
    	leerVagua(dirDatYSal);
    	int pp=0;
    }
    
    
    /**
     * Lee los datos generales del archivo VAGUA.D
     * @param dirDatYSal
     */
    public void leerDatGen(String dirDatYSal){
        // leer del archivo vagua el aóo inicial y el final, cantidad de semanas, pasos de stock y clases.
        String dirDatCarga = dirDatYSal + "/CF_Base.xlt";
        ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirDatCarga);   
        int i=0;
        String sep="********************************************************************************";
        ArrayList<Integer> anios = new ArrayList<Integer>();
        ArrayList<Integer> cantvarEstadoAnual = new ArrayList<Integer>();
        ArrayList<Integer> cantpasoDeStockAnual = new ArrayList<Integer>();
        ArrayList<Integer> cantpasoDeTiempoAnual = new ArrayList<Integer>();
        while (i<texto.size()){
            if(texto.get(i).get(0).equalsIgnoreCase(sep)){
                i=i+8;
                anios.add(Integer.parseInt(texto.get(i).get(0)));
                i=i+7;
                cantvarEstadoAnual.add(Integer.parseInt(texto.get(i).get(0)));
                i=i+2;
                cantpasoDeStockAnual.add(Integer.parseInt(texto.get(i).get(0)));
                i=i+2;
                cantpasoDeTiempoAnual.add(Integer.parseInt(texto.get(i).get(0)));   
                i++;
            }
            i++;
        }
        // Cargo atributos segón las lecturas realizadas
        anfin = 2039;
        anini = 2020;
        // Inicialmente se suponen fijas las clases, pasos de stock y paso de tiempo
        cantSem = 1044;
        cantPasoStock = 10;
        cantVarEst = 5;
        valBellman = new ArrayList<double[][]>();
        valRecursoLeidos = new ArrayList<double[][]>(); 
        valRecurso = new ArrayList<double[][]>();
    }
    
    
    /**
     * Lee los valores del agua de CF y los carga en el orden en que vienen
     * en valRecursosLeidos
     * 
     * @param dirDatYSal
     */
    public void leerVagua(String dirDatYSal){ 
        //Idem a la lectura del carga.
        // Leer los datos y cargarlos en valRecursos
        String dirDatCarga = dirDatYSal+ "/CF_Base.xlt";
        ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirDatCarga);       
   
        int  i=25;
        
        double [] pasos = new double[cantPasoStock];
        for (int j=0; j < cantPasoStock; j++) {
        	pasos[j]=50+8200/cantPasoStock*j;
        }
        while (i<texto.size() && texto.get(i).size()>10){
        	double [][] aux = new double [cantVarEst][cantPasoStock];
        	double [][] auxvagua = new double [cantVarEst][cantPasoStock];
        	for (int varEstado=0; varEstado<5; varEstado++) {
        		for (int pasoStock = 0; pasoStock<10; pasoStock++) {
        			aux[varEstado][pasoStock] = Double.parseDouble(texto.get(i).get(2+varEstado*pasoStock+pasoStock)); 
            	}                             
            } 
        	
        	double[] X = new double[3];
			double[] Y = new double[3];
        	for (int varEstado=0; varEstado<5; varEstado++) {
        		for (int pasoStock = 0; pasoStock<10; pasoStock++) {        			
        			int indPunto=0;
        			if (pasoStock==0) {
        				X[0] = pasos[0];
        				Y[0] = aux[varEstado][0];
        				X[1] = pasos[1];
        				Y[1] = aux[varEstado][1];
        				X[2] = pasos[2];
        				Y[2] = aux[varEstado][2];
        			} else if (pasoStock==9) {
        				indPunto=2;
        				X[2] = pasos[9];
        				Y[2] = aux[varEstado][9];
        				X[1] = pasos[8];
        				Y[1] = aux[varEstado][8];
        				X[0] = pasos[7];
        				Y[0] = aux[varEstado][7];
        			} else {
        				indPunto=1;
        				X[2] = pasos[pasoStock+1];
        				Y[2] = aux[varEstado][pasoStock+1];
        				X[1] = pasos[pasoStock];
        				Y[1] = aux[varEstado][pasoStock];
        				X[0] = pasos[pasoStock-1];
        				Y[0] = aux[varEstado][pasoStock-1];        				
        			}
        			double deriv = calculaDerivada3PCuadConvex(X, Y, indPunto);
        			auxvagua[varEstado][pasoStock] = -deriv;
            	}                             
            } 
        	valRecursoLeidos.add(0,auxvagua);
        	i++;  
        }
        valRecurso = valRecursoLeidos; 
       
    }
    
    public double calculaDerivada3PCuadConvex(double[] valoresX, double[] valoresY, int indPunto){		
		double g1 = (valoresY[1]-valoresY[0])/(valoresX[1]-valoresX[0]);
		double g2 = (valoresY[2]-valoresY[1])/(valoresX[2]-valoresX[1]);
		double der;
		if(g1>g2){
			der = (valoresY[2] - valoresY[0])/((valoresX[2] - valoresX[0])) ;
			
		}else{
			der = calculaDerivada3PCuad(valoresX, valoresY, indPunto);
		}
		return der;
	}
	
	
	/*
	 * Calcula derivada parcial en un punto por el mótodo de Lagrange (aproximación cuadrótica por 
	 * tres puntos de la función).
	 * 
	 * @param valoresX son los valores del argumento respecto al cual se deriva, en tres puntos
	 * @param valoresY son los valores de la función a derivar, para los tres argumentos de valoresX
	 * @param indPunto vale 0 si se quiere estimar la derivada en el menor argumento, valoresX[0]
	 *        indPunto vale 1 si se quiere estimar la derivada en el punto central, valoresX[1]
	 *        indPunto vale 2 si se quiere estimar la derivada en el mayor argumento, valoresX[2]
	 */
	public double calculaDerivada3PCuad(double[] valoresX, double[] valoresY, int indPunto){	
		double l1 = valoresX[1]-valoresX[0]; 
		double l2 = valoresX[2]-valoresX[1]; 
		double g1 = (valoresY[1]-valoresY[0])/l1;
		double g2 = (valoresY[2]-valoresY[1])/l2;		
		if(indPunto==0) {
			// Derivada en el primer punto, menor argumento		
			return ((2*l1+l2)*g1-l1*g2)/(l1+l2);
		}else if(indPunto==1){
			// Derivada en el punto central		
			return (l2*g1+l1*g2)/(l1+l2);
		}else {
			// Derivada en el tercer punto, mayor argumento
			return ((2*l2+l1)*g2-l2*g1)/(l1+l2);
		}
	}
		
 

    public void setValRecurso(ArrayList<double[][]> valRecurso) {
        this.valRecurso = valRecurso;
    }
    
    
    
    
      
    public int getAnini() {
		return anini;
	}

	public void setAnini(int anini) {
		this.anini = anini;
	}

	public int getAnfin() {
		return anfin;
	}

	public void setAnfin(int anfin) {
		this.anfin = anfin;
	}

	public int getCantSem() {
		return cantSem;
	}

	public void setCantSem(int cantSem) {
		this.cantSem = cantSem;
	}

	public int getCantPasoStock() {
		return cantPasoStock;
	}

	public void setCantPasoStock(int cantPasoStock) {
		this.cantPasoStock = cantPasoStock;
	}

	public int getCantVarEst() {
		return cantVarEst;
	}

	public void setCantVarEst(int cantVarEst) {
		this.cantVarEst = cantVarEst;
	}

	public ArrayList<double[][]> getValRecurso() {
		return valRecurso;
	}

	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        LectorValOptSimsee LecVagua = new LectorValOptSimsee();
        String dirDatYSal = "G:/PLA/Pla_datos/Archivos/ModeloOp/ValBellmanSimsee";
        
        // nuevo archivo emiliano 27/9/16 "S:/Pla/EDF/2016/ModeloOpComparar";
        double[] volumenesBon = new double[10];
        for(int i=0; i<10; i++){
        	volumenesBon[i] = i*(8200.0/9.0);
        }
        LecVagua.creaValorDelAgua(dirDatYSal, volumenesBon);
        if (Constantes.NIVEL_CONSOLA > 1) {
	        System.out.println("Aóos: "+LecVagua.anfin +" - " +LecVagua.anini);
	        System.out.println("Estados: " + LecVagua.cantVarEst +" Pasos De Stock " +LecVagua.cantPasoStock);
	        System.out.println("DERIVADAS EN LOS PUNTOS, PASO 10/11, CLASE 2/3");        
	        for(int ip=0; ip<10; ip++){
	            System.out.println(LecVagua.valRecurso.get(10)[2][ip]);
	        }
        }
    }

	public double[] getValoresStock() {
		return valoresStock;
	}

	public void setValoresStock(double[] valoresStock) {
		this.valoresStock = valoresStock;
	}
    
}

