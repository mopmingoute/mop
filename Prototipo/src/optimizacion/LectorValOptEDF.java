/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LectorValOptEDF is part of MOP.
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


/**
 * La clase tiene
 * @author ut601781
 */
public class LectorValOptEDF {
	
	/*
	 * El VAGUA.D del EDF siempre tiene aóos enteros
	 */
	

    // Parómetros bósicos ingresados como atributos
    private int anini;   // anio inicial
    private int anfin;   // anio final 
    private int cantSem; // cantidad de semanas o paso de tiempo en un anio    
    private int cantPasoStock;  // cantidad de pasos de stock (incrementos) uno menos que los puntos
    private int cantVarEst;     // cantidad de discetización de las variables de estado
    private double[] valoresStock;  // valores de discretización del volumen ótil de Bonete, incluso el cero;
    
    /**
     * valRecursoLeidos: Lista por paso de tiempo con la matriz de datos por estado y paso de estock
     * el array es [indice estado hidrológico][indice paso de stock]; son incrementos medios.
     * 
     * valRecurso: valores resultantes de la interpolación Lagrange a partir de los anteriores. 
     * el array es [indice estado hidrológico][indice paso de stock] y tiene un paso de stock adicional;
     * son derivadas en los puntos de discretización.
     */
    private ArrayList<double[][] > valRecursoLeidos;  
    private ArrayList<double[][]>  valRecurso;   
    
    
    public LectorValOptEDF() {
    }
    
    public LectorValOptEDF(int anini, int anfin, int cantSem, int cantPasoStock, int cantVarEst) {
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
    	leerVagua (dirDatYSal);
    	invertirValRecYCalcDeriv(volumenesStock);
    }
    
    
    /**
     * Lee los datos generales del archivo VAGUA.D
     * @param dirDatYSal
     */
    public void leerDatGen(String dirDatYSal){
        // leer del archivo vagua el aóo inicial y el final, cantidad de semanas, pasos de stock y clases.
        String dirDatCarga = dirDatYSal + "/VAGUA.D";
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
        anfin = anios.get(0);
        anini = anios.get(anios.size()-1);
        // Inicialmente se suponen fijas las clases, pasos de stock y paso de tiempo
        cantSem = cantpasoDeTiempoAnual.get(0);
        cantPasoStock = cantpasoDeStockAnual.get(0);
        cantVarEst = cantvarEstadoAnual.get(0);
        valRecursoLeidos = new ArrayList<double[][]>(); 
        valRecurso = new ArrayList<double[][]>();
    }
    
    
    /**
     * Lee los valores del agua de VAGUA.D y los carga en el orden en que vienen
     * en valRecursosLeidos
     * 
     * @param dirDatYSal
     */
    public void leerVagua (String dirDatYSal){ 
        //Idem a la lectura del carga.
        // Leer los datos y cargarlos en valRecursos
        String dirDatCarga = dirDatYSal + "/VAGUA.D";
        ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirDatCarga);       
        String sep="********************************************************************************";
        int  i=0;
        int pasoStock=0;
        int varEst=0;
        double [][] aux = new double [cantVarEst][cantPasoStock]; //Matriz para leer valores del texto y guardarlos en valRecursos
        while (i<texto.size()){
            if(texto.get(i).get(0).equalsIgnoreCase(sep)){
                i=i+21;
            }else if(texto.get(i).get(0).equalsIgnoreCase("PAS")){
                if(i!=21) {
                    valRecursoLeidos.add(aux);  // En el primer "PAS DE TEMP" no se carga
                    aux = new double [cantVarEst][cantPasoStock];
                }
                varEst=0;
                i++; // siguiente lónea
            }else if (texto.get(i).get(0).equalsIgnoreCase("CLASSES")){
                i++;
                /**
                 * EL VAGUA.D TIENE VALORES EN USD/M3 SE PASAN A USD/HM3
                 */
                // Cargo los 7 valores del agua del primer renglón
                for(int ih =0; ih<7; ih++){
                    //valRecurso[pasoTemp-1][varEst][pasoStock] = Double.parseDouble(texto.get(i).get(ih));
                    aux[varEst][pasoStock] =  Double.parseDouble(texto.get(i).get(ih))*Constantes.M3XHM3;
                    pasoStock++;
                }
                i++;
                // Cargo los 2 valores del agua del segundo renglón
                 for(int ih =0; ih<2; ih++){
                    //valRecurso[pasoTemp-1][varEst][pasoStock] = Double.parseDouble(texto.get(i).get(ih));
                    aux[varEst][pasoStock] =  Double.parseDouble(texto.get(i).get(ih))*Constantes.M3XHM3;
                    pasoStock++;
                } 
                pasoStock=0;
                varEst++;
                i++;
            } 
            if(i==texto.size()){
                valRecursoLeidos.add(aux);  // se carga el ultimo paso
            }
        }
    }
    
    
    /**
     * Toma el atributo valRecursosLeidos que son los valores del VAGUA.D para 9 incrementos medios y con el orden
     * del VAGUA.D y genera el atributo valRecursos que tiene estimadas las derivadas en 10 puntos, con las semanas
     * en orden cronológico.
     * (El VAGUA.D estó ordenado al revós en los aóos (2030, 2029,....,2016) y al derecho en las semanas)
     * 
     * Reescala los valores del agua que son cocientes incrementales medios en segmentos y los transforma en 
     * derivadas en los puntos de discretización. Para eso: 
     * - recalcula los valores de Bellman (a menos de una constante) 
     *   en los puntos de discretización a partir de los cocientes incrementales
     * - por Lagrange calcula las derivadas en los puntos de discretación.
     * 
     * @param volumenesStock es el vector de volómenes ótiles de los pasos de discretización del stock
     * empleados en EDF, incluso el 0, expresados en hm cóbicos.
     */
    public void invertirValRecYCalcDeriv(double[] volumenesStock){
    	ArrayList<double [][]> valIncrMed = valRecursoLeidos;
      //  int cantPasosTiempo= valIncrMed.size();
        int cantClasesAportes = valIncrMed.get(0).length;
        int cantPasosStock = valIncrMed.get(0)[0].length;
        double l1, l2, g1, g2;
        double [][] valBellman = new double[cantClasesAportes][cantPasosStock+1];
      
        // para el paso de stock 0, se conviene en que el valor ee Bellman tiene nivel 0 sólo a efecto de este cólculo
        for(int ica=0; ica<cantClasesAportes; ica++){
        	valBellman[ica][0]=0.0;
        }        
        for(int ian=anini; ian<=anfin; ian++){
        	for(int isem = 1; isem<=52; isem++){
        		int indice = (anfin - ian)*52 + isem -1;
        		double[][] incrMed1Paso = valIncrMed.get(indice);
       	        double [][] valRecurso1P = new double[cantClasesAportes][cantPasosStock+1];  
        		for(int ica=0; ica<cantClasesAportes; ica++){
        			for(int ips=1; ips<cantPasosStock+1; ips++){
        				valBellman[ica][ips] = valBellman[ica][ips-1] -  incrMed1Paso[ica][ips-1]*
        						(volumenesStock[ips]-volumenesStock[ips-1]);
        			}        		
            		// Calcula las derivadas por Lagrange
 
            		for(int ips=0; ips<cantPasosStock+1; ips++){
            		    if(ips==0){
            		    	l1 = volumenesStock[1]-volumenesStock[0];
            		    	l2 = volumenesStock[2]-volumenesStock[1];
            		    	g1 = (valBellman[ica][1]-valBellman[ica][0])/l1;
            		    	g2 = (valBellman[ica][2]-valBellman[ica][1])/l2;
            		    	valRecurso1P[ica][0] = -((2*l1+l2)*g1-l1*g2)/(l1+l2);
            		    }else if(ips==cantPasosStock){
            		    	l1 = volumenesStock[cantPasosStock-1]-volumenesStock[cantPasosStock-2];
            		    	l2 = volumenesStock[cantPasosStock]-volumenesStock[cantPasosStock-1];
            		    	g1 = (valBellman[ica][cantPasosStock-1]-valBellman[ica][cantPasosStock-2])/l1;
            		    	g2 = (valBellman[ica][cantPasosStock]-valBellman[ica][cantPasosStock-1])/l2;
            		    	valRecurso1P[ica][cantPasosStock] = -((2*l2+l1)*g2-l2*g1)/(l1+l2);            		    	
            		    }else{
            		    	l1 = volumenesStock[ips]-volumenesStock[ips-1];
            		    	l2 = volumenesStock[ips+1]-volumenesStock[ips];
            		    	g1 = (valBellman[ica][ips]-valBellman[ica][ips-1])/l1;
            		    	g2 = (valBellman[ica][ips+1]-valBellman[ica][ips])/l2;            		    	
            		    	valRecurso1P[ica][ips] = -(g1/l1 + g2/l2)/(1/l1 + 1/l2);            		    	            		    	
            		    }
            		}        				        			
        		}
        		valRecurso.add(valRecurso1P);        		        	
        	}        
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
        LectorValOptEDF LecVagua = new LectorValOptEDF();
        String dirDatYSal = "G:/PLA/Pla_datos/Archivos/ModeloOp/ValaguaEDF";
        
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

