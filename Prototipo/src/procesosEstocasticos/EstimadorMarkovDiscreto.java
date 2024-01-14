/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorMarkovDiscreto is part of MOP.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import utilitarios.*;

import static persistencia.EscritorTextosGeneralesPE.escribeDatosGeneralesPE;
import static persistencia.EscritorTextosGeneralesPE.escribeTextoAgregadorLineal;


/**
 * Estima procesos discretos de Markov
 * 
 */

public class EstimadorMarkovDiscreto {
	private String identificadorEstimacion;         
    private int cantSeries;    
    private String[] nombreSeries;
    private int cantEstac;  // cantidad de estaciones 
    private int cantMaxPasos; // cantidad m�xima de pasos que puede tener un a�o
    private String nombreDurPaso; // SEMANA, DIA, HORA, ETC.
    
    /**
     * Un entero mayor que cero consecutivo entre 1 y cantEstac que indica la estaci�n
     * a la que pertenece cada paso del a�o
     * estDelPaso[0] es la estaci�n del paso 1
     * estDelPaso[1] es la estaci�n del paso 2 y as� sucesivamente
     */
    int[] estDelPaso;
    
    
    /**
     * Un entero para cada tiempo que indica la estaci�n 
     * a la que pertenecen los datos de ese tiempo.
     * Los valores deben ir entre 1 y cantEstac.
     * A cada estaci�n corresponder� una matriz de transici�n y un
     * conjunto de observaciones de las series para cada clase de las VE.
     * Obs�rvese que la matriz de transici�n depende de la estaci�n a la que pertenece el paso inicial,
     * la estaci�n del paso final de la transici�n es irrelevante.
     */
    private int[] estac;    
    
    
    /**
     * Para cada tiempo indica el paso (entero entre 1 y la cantidad m�xima de pasos en el a�o)
     */
    private int[] pasos;
    
    
    private int cantVarEst;  // cantidad de variables de estado 
    
    /**
     * Coeficientes de los datos para crear la variable de estado.
     * Primer �ndice: variable de estado
     * Segundo �ndice: serie de datos
     * Tercer �ndice: rezago en la serie empezando de 0, -1, -2,....
     * Define la combinaci�n lineal de datos rezagados que determina cada estado.
     */
    private double[][][] defVarEst;  
    
    
    /**
	 * Los datos para estimar. En primer �ndice el tiempo,
	 * en el segundo �ndice el ordinal de la serie.
	 */
    private double[][] datos;  
    
    /**
     * Etiqueta entera de cada cr�nica, en el orden en que aparecen las cr�nicas:
     * Ejemplo: 1909, 1910, .....2013
     */
    private int[] etiquetaCron;
    
    /**
     * EtiquetasCronDatos
     * La etiqueta asociada a cada dato t
     */
    private int[] etiquetaCronDatos;
    
    /**
     * Cantidad de cr�nicas le�das
     */
    private int cantCron;   
    
	private int cantDatos;
	
	private String[] nombreVarEst;    
    
    /**
     * Valores de las variables de estado continuas que se calculan.
     * Primer �ndice tiempo, segundo �ndice variable de estado.
     */
    private double[][] valVarEst;  
    
    /**
     * Probabilidades de cada clase para Variable de estado definida.
     * Primer indice variable de estado, segundo �ndice clase.
     * Las clases se numeran del 0 en adelante.
     * Puede haber diferente cantidad de clases en cada serie.
     */
    private double[][] probCla;      
        
    /**
     * Cantidad de clases de cada variable de estado
     */
    private int[] cantCla;     
    
    /**
     * L�mite superior de cada clase para cada variable de estado y estaci�n    
     * Primer �ndice variable de estado, segundo �ndice estaci�n, tercer �ndice clase-1.
     * Las observaciones de la clase k de la variable i en la estaci�n e
     * son menores o iguales a limSupVE[e][i][k]
     * Es igual al promedio de la mayor observaci�n de la clase inferior 
     * y la menor observaci�n de la clase superior 
     */
    private double[][][] limSupVE;
    
    /**
     * Para cada tiempo y cada variable de estado indica cu�l es la clase
     * Primer �ndice tiempo, segundo �ndice variable de estado
     * Las clases se numeran del 0 en adelante.
     */
    private int[][] clases; 
    
    /**
     * Para cada serie, estaci�n y estado compuesto, 
     * guarda la media de la serie en la estaci�n y estado compuesto
     * en un array
     * Primer �ndice estaci�n, segundo �ndice serie, tercer indice estado compuesto.
     */
    private double[][][] medias;
    
    /**
     * Para cada estaci�n y serie, guarda la media, es decir no condicionada 
     * a ning�n estado.
     * Primer �ndice estaci�n, segundo �ndice serie
     */
    private double[][] mediaAbs;
    
    /**
     * Para cada estaci�n y estado compuesto almacena las observaciones de los datos de las series originales
     * El Object ser� un ArrayList<Double[]>
     */
    private Object[][] datosEstacEstComp;
        
    
    /**
     * Cantidad de rezagos (incluso el valor corriente) en la definici�n de las VE
     * primer �ndice VE, segundo �ndice serie
     */
    private int[][] cantRezagos;        

    
    /**
     * Hash map de matrices de probabilidades de transici�n
     * La clave es la estaci�n en el paso inicial de la transici�n
     * No se discrimina seg�n el paso final de la transici�n
     * El orden de filas y columnas en las matrices de transici�n es
     * el de defEstadosComp.
     */
    private Hashtable<Integer,MatPTrans> matrices;
    
    /**
     * Cantidad de estados compuestos
     * Los estados compuestos se numeran a partir del 0
     */
    private int cantEstadosComp; 
    
    /**
     * Cantidad de observaciones por estaci�n y estado compuesto
     * Primer �ndice estaci�n, segundo �ndice estado compuesto.
     */
    private int[][] cantObsEstacEstadoComp;
    
    /**
     * Definici�n de los estados compuestos
     * Cada elemento de la lista es un int[] que indica la clase en cada variable de estado.
     * Ejemplo: Hay tres variables de estado. El estado compuesto en el que
     * var1 est� en clase 2, var2 est� en clase 3 y var3 est� en clase 1 se representa
     * por un array {2,3,1}
     */
    private ArrayList<int[]> defEstadosComp;
    
    /**
     * El �ndice de estado compuesto para cada tiempo
     */
    int[] estadoComp;
    

    // ATENCI�N COMIENZA UNA CLASE INTERNA
    public class ClaveParInt implements Comparable{
    	private int int1;
        private int int2;

        public int getInt1() {
            return int1;
        }

        public void setInt1(int int1) {
            this.int1 = int1;
        }

        public int getInt2() {
            return int2;
        }

        public void setInt2(int int2) {
            this.int2 = int2;
        }

        public ClaveParInt(int int1, int int2) {
            this.int1 = int1;
            this.int2 = int2;
        }
        
        

        @Override
        public int compareTo(Object t) {
            ClaveParInt tt = (ClaveParInt)t;
            if(int1<tt.getInt1() || (int1==tt.getInt1()&& int2<tt.getInt2()) ) return -1;
            if(int1==tt.getInt1() && int2==tt.getInt2()) return 0;
            return 1;
        }    

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClaveParInt other = (ClaveParInt) obj;
            if (this.int1 != other.int1) {
                return false;
            }
            if (this.int2 != other.int2) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.int1;
            hash = 71 * hash + this.int2;
            return hash;
        }	    
    }
    // TERMINA LA CLASE INTERNA	    

    public int[] getCantCla() {
        return cantCla;
    }

    public void setCantCla(int[] cantCla) {
        this.cantCla = cantCla;
    }

    public int getCantDatos() {
        return cantDatos;
    }

    public void setCantDatos(int cantDatos) {
        this.cantDatos = cantDatos;
    }

    public int getCantEstac() {
        return cantEstac;
    }

    public void setCantEstac(int cantEstac) {
        this.cantEstac = cantEstac;
    }

    public int getCantEstadosComp() {
        return cantEstadosComp;
    }

    public void setCantEstadosComp(int cantEstadosComp) {
        this.cantEstadosComp = cantEstadosComp;
    }

    public int[][] getCantObsEstacEstadoComp() {
        return cantObsEstacEstadoComp;
    }

    public void setCantObsEstacEstadoComp(int[][] cantObsEstacEstadoComp) {
        this.cantObsEstacEstadoComp = cantObsEstacEstadoComp;
    }

    public int getCantSeries() {
        return cantSeries;
    }

    public void setCantSeries(int cantSeries) {
        this.cantSeries = cantSeries;
    }

    public int getCantVarEst() {
        return cantVarEst;
    }

    public void setCantVarEst(int cantVarEst) {
        this.cantVarEst = cantVarEst;
    }

    public int[][] getClases() {
        return clases;
    }

    public void setClases(int[][] clases) {
        this.clases = clases;
    }

    public double[][] getDatos() {
        return datos;
    }

    public void setDatos(double[][] datos) {
        this.datos = datos;
    }

    public Object[][] getDatosEstacEstComp() {
        return datosEstacEstComp;
    }

    public void setDatosEstacEstComp(Object[][] datosEstacEstComp) {
        this.datosEstacEstComp = datosEstacEstComp;
    }

    public ArrayList<int[]> getDefEstadosComp() {
        return defEstadosComp;
    }

    public void setDefEstadosComp(ArrayList<int[]> defEstadosComp) {
        this.defEstadosComp = defEstadosComp;
    }

    public double[][][] getDefVarEst() {
        return defVarEst;
    }

    public void setDefVarEst(double[][][] defVarEst) {
        this.defVarEst = defVarEst;
    }

    public int[] getEstac() {
        return estac;
    }

    public void setEstac(int[] estac) {
        this.estac = estac;
    }

    public int[] getEstadoComp() {
        return estadoComp;
    }

    public void setEstadoComp(int[] estadoComp) {
        this.estadoComp = estadoComp;
    }

    public double[][][] getLimSupVE() {
        return limSupVE;
    }

    public void setLimSupVE(double[][][] limSupVE) {
        this.limSupVE = limSupVE;
    }

    public Hashtable<Integer, MatPTrans> getMatrices() {
        return matrices;
    }

    public void setMatrices(Hashtable<Integer, MatPTrans> matrices) {
        this.matrices = matrices;
    }

    public double[][] getMediaAbs() {
        return mediaAbs;
    }

    public void setMediaAbs(double[][] mediaAbs) {
        this.mediaAbs = mediaAbs;
    }

    public double[][][] getMedias() {
        return medias;
    }

    public void setMedias(double[][][] medias) {
        this.medias = medias;
    }

    public String[] getNombreSeries() {
        return nombreSeries;
    }

    public void setNombreSeries(String[] nombreSeries) {
        this.nombreSeries = nombreSeries;
    }

    public String[] getNombreVarEst() {
        return nombreVarEst;
    }

    public void setNombreVarEst(String[] nombreVarEst) {
        this.nombreVarEst = nombreVarEst;
    }



    public double[][] getProbCla() {
        return probCla;
    }

    public void setProbCla(double[][] probCla) {
        this.probCla = probCla;
    }

    public double[][] getValVarEst() {
        return valVarEst;
    }

    public void setValVarEst(double[][] valVarEst) {
        this.valVarEst = valVarEst;
    }
    

    
    
	//////////////////////////////////////////////////////////
	//
	//	METODOS DE LECTURA DE DATOS Y PAR�METROS
	//
	//////////////////////////////////////////////////////////
    
    /**
     * Lee todos los datos
     */
    public void cargaTodo(String dirArchivo){
    	cargaEstac(dirArchivo +"/defEstac.txt");
    	cargaDatos(dirArchivo +"/datos.txt");
    	cargaParamVE(dirArchivo +"/defVE.txt" );    		
    }
    
    /**
     * Carga datos de las series a partir del archivo dirArchivo
     * Se debe invocar primero que cargaParamVE y cargaParamSeries.
     * @param dirArchivo 
     */
    public void cargaDatos(String dirArchivo){
        ArrayList<ArrayList<String>> texDatos;
        texDatos = LeerDatosArchivo.getDatos(dirArchivo);
        int i=0;
        // Lee el identificador de la estimaci�n
        identificadorEstimacion = texDatos.get(i).get(1);
        i++;
        // Lee el nombre de la duraci�n del paso
        setNombreDurPaso(texDatos.get(i).get(1));
        i++;
        // Lee cantidad m�xima de pasos anuales de los datos
        cantMaxPasos = Integer.parseInt(texDatos.get(i).get(1));
        i++;
        cantCron = Integer.parseInt(texDatos.get(i).get(1));
        i++;
        cantSeries = texDatos.get(i).size()-2;
        nombreSeries = new String[cantSeries];
        etiquetaCron = new int[cantCron];
        for(int j=2; j<texDatos.get(i).size();j++){
            nombreSeries[j-2] = texDatos.get(i).get(j);            
        }
        i++;
        int itope=i;
        cantDatos = texDatos.size()-itope;
        estac = new int[cantDatos];
        pasos = new int[cantDatos];
        datos = new double[cantDatos][cantSeries];
        etiquetaCronDatos = new int[cantDatos];

        int ifil;
        int paso;
        int rotuloAnt = Integer.MAX_VALUE;
        int icron = -1;
        for(i=0; i<cantDatos; i++){
        	ifil = i+itope;
        	int rotuloLeido = Integer.parseInt(texDatos.get(ifil).get(0));
        	if (rotuloLeido!= rotuloAnt){
        		icron++;
        		etiquetaCron[icron] = rotuloLeido;
        		rotuloAnt = rotuloLeido;
        	}
        	paso = Integer.parseInt(texDatos.get(ifil).get(1));
            estac[i]=estDelPaso[paso-1];
            etiquetaCronDatos[i] = Integer.parseInt(texDatos.get(ifil).get(0));            
            for(int j=2; j<texDatos.get(ifil).size();j++){
                datos[i][j-2] = Double.parseDouble(texDatos.get(ifil).get(j)); 
            }                        
        } 
        System.out.println("TERMINA LA LECTURA DE DATOS");
    }
    
    
    /**
     * Lee la definici�n de las estaciones asociadas a cada uno de los pasos del a�o
     * Debe leerse antes que los datos
     */
    public void cargaEstac(String dirArchivo){
        ArrayList<ArrayList<String>> texto;
        texto = LeerDatosArchivo.getDatos(dirArchivo);    	
    	int i=0;   
    	cantMaxPasos = Integer.parseInt(texto.get(i).get(1));
    	i++;
    	cantEstac = Integer.parseInt(texto.get(i).get(1));    	
    	i++;
    	estDelPaso = new int[cantEstac];    	
    	int ifil = i;
    	for(i = 0; i<cantMaxPasos; i++){
    		int paso = Integer.parseInt(texto.get(ifil + i).get(1));
    		if(paso!=i+1){
    			System.out.println("Error en lectura de estaciones en el paso " + paso);
    			System.exit(1);
    		}    		
    		estDelPaso[i] = Integer.parseInt(texto.get(ifil+i).get(1));    	
    	}    	
    }
    
    
    /**
     * Lee los par�metros de las variables de estado del archivo dirArchivo
     * @param dirArchivo 
     */
    public void cargaParamVE(String dirArchivo){
        ArrayList<ArrayList<String>> texto;
        texto = LeerDatosArchivo.getDatos(dirArchivo);
        int i=0;
        cantVarEst = Integer.parseInt(texto.get(i).get(1));
        nombreVarEst = new String[cantVarEst];
        defVarEst = new double[cantVarEst][cantSeries][];
        cantRezagos = new int[cantVarEst][cantSeries];
        probCla = new double[cantVarEst][];  
        double[] aux;
        i++;
        for(int ive=0; ive< cantVarEst; ive++){
            nombreVarEst[ive]=texto.get(i).get(1);
            i++;
            
            for(int iserie = 0; iserie< cantSeries; iserie++){
            	// verifica que cada serie aparezca en su orden
            	if(!texto.get(i).get(1).equalsIgnoreCase(nombreSeries[iserie])){
            		System.out.println("error en lectura de serie" + nombreSeries[iserie]);
            		System.exit(1);
            	}
            	i++;
            	double pondSerie = Double.parseDouble(texto.get(i).get(1));
            	i++;
            	int cr = Integer.parseInt(texto.get(i).get(1));
            	cantRezagos[ive][iserie] = cr;
            	i++;
	            aux = new double[cr];
	            defVarEst[ive][iserie]=aux;
	            for(int j=1; j<texto.get(i).size(); j++){
	                defVarEst[ive][iserie][j-1] = Double.parseDouble(texto.get(i).get(j))*pondSerie;
	            }
	            i++;	           
            }
            aux = new double[texto.get(i).size()-1];
            probCla[ive]= aux;            
            for(int j=1; j<texto.get(i).size(); j++){
                probCla[ive][j-1] = Double.parseDouble(texto.get(i).get(j));
            }    
            i++;            
        }
        System.out.println("TERMINO LA LECTURA DE PARAMETROS DE VE");
        
    }


	//////////////////////////////////////////////////////////
	//
	//	PROGRAMA PRINCIPAL
	//
	//////////////////////////////////////////////////////////    
     
    /**
     * 
     * M�todo principal que hace la estimaci�n invocando a otros m�todos
     * 
     */
    public void estimaMarkov() {         
        if(cantVarEst!=nombreVarEst.length || cantVarEst!=probCla.length){
        	System.out.print("Los nombres o probabilidades de var. de estado no tienen la dimensi�n correcta ");
        	System.exit(1);
    	}
        // cantCla: cantidad de clases de cada variable de estado continua.
        cantCla = new int[cantVarEst];        
        for(int ive=0; ive<cantVarEst; ive++){
            cantCla[ive] = probCla[ive].length;            
        }

        // valVarEst: valores de las variables de estado, primer �ndice tiempo, segundo variable de estado.
        valVarEst = new double[cantDatos][cantVarEst];
               
        // Calcula la media por cada estaci�n de cada una de las series, absoluta no condicionada a estados
        calculaMediaAbsSeries();   
        
        // Calcula los valores de las variables de estado para cada tiempo
        calculaVE();

        // Calcula los l�mites de las clases de las VE y la clase a la que pertenece cada VE en cada tiempo.
        calculaLimClaDeVEs();
        
        // Calcula las matrices de transici�n y las medidas para cada estaci�n serie y estado compuesto.
        // Estado compuesto es cada una de las combinaciones posibles de las VE.
        calculaMatTransYMed();
                                     
        
        // Crea la estructura datosEstacEstComp para cargar las observaciones asociadas a cada estaci�n y estado compuesto.
        // Para cada estaci�n y estado compuesto hay un ArrayList de double[] con las observaciones
        creaDatosEstacEstComp();
          
        System.out.println("FIN DE LA ESTIMACI�N");                   
    }
    
    
	//////////////////////////////////////////////////////////
	//
	//	METODOS DE CALCULO INVOCADOS POR EL PROGRAMA PRINCIPAL
	//
	//////////////////////////////////////////////////////////    
    
    
    /**
     * Calcula la media de cada serie en cada estaci�n, no condicionada a ning�n estado.         
     */
    public void calculaMediaAbsSeries(){
        mediaAbs = new double[cantEstac][cantSeries];
        int[] obsEnCadaEstac = new int[cantEstac];
        for(int iest=0;iest<cantEstac;iest++){
            obsEnCadaEstac[iest] = 0;
            for(int is = 0; is<cantSeries; is++){
                mediaAbs[iest][is] = 0.0;    
            }
        }
        for(int t=0; t<cantDatos; t++ ){          
            obsEnCadaEstac[estac[t]-1] ++;
            for(int is=0; is<cantSeries; is++){
            mediaAbs[estac[t]-1][is] += datos[t][is];
            }            
        }    
        for(int iest=0;iest<cantEstac;iest++){
            for(int is = 0; is<cantSeries; is++){
                mediaAbs[iest][is] = mediaAbs[iest][is]/obsEnCadaEstac[iest];    
            }
        }        
	
    }
    
    
    /**
     * Calcula los valores de las variables de estado para cada tiempo.
     * Si no hay datos rezagados se repite el valor corriente
     */
    public void calculaVE(){
	    double vv;        
	    for(int t=0; t<cantDatos; t++ ){
	        for(int ive=0; ive<cantVarEst; ive ++){                
	        	vv = 0.0;
	        	for(int is=0; is<cantSeries; is++){
	                for(int ir=0; ir< cantRezagos[ive][is]; ir++){
	                  if(t-ir >= 0){
	                      vv += datos[t-ir][is]*defVarEst[ive][is][ir];
	                  }else{
	                	  vv += datos[t][is]*defVarEst[ive][is][ir];
	                  }
	                }                  	                	                   
	            }
	            valVarEst[t][ive] = vv;
	        }                        
	    }
    }
    

    /**       
     * Determina los l�mites superiores y medias de clases de las variables de estado en cada estaci�n
     * y los carga en limSupVE[iest][ive][clase]
     *        
     * Genera la estructura obs[iest][ive] para acumular observaciones double de valores de VE por estaci�n y VE
     * 
     * Carga en clases[t][ive]  la clase (discreta) para cada tiempo y VE.
     * 
     * Atenci�n: las estaciones se numeran empezando en 1 en estac[].
     *           
     */         
    public void calculaLimClaDeVEs(){

    	int ive, iest, t, indice, iclas, N, iec; 	    
	    Object[][] obs = new Object[cantEstac][cantVarEst];  
	    for(ive=0; ive<cantVarEst; ive++){
	        for(iest=0; iest<cantEstac; iest++){
	            obs[iest][ive] = new ArrayList<Double>();
	        }                                             
	    }
	            
	    /**         
	     * Acumula observaciones por estaci�n y VE 
	     * hay un l�mite superior m�s que la cantidad de probabilidades de transici�n
	     * en el �ltimo l�mite se carga un high-value de double.
	     * limSupVE: Primier �ndice estaci�n, segundo �ndice variable de estado, tercer �ndice clase-1.
	     * probCla: Primer indice variable de estado, segundo �ndice clase-1.        
	     */
	    limSupVE = new double[cantEstac][cantVarEst][];
	    for(ive=0; ive<cantVarEst; ive ++){ 
	        for(iest=0; iest<cantEstac; iest++){
	            double[] aux = new double[cantCla[ive]];  
	            limSupVE[iest][ive] = aux;
	        }
	    }
	            	            
	    for(t=0; t<cantDatos; t++ ){
	        // genera obs: las listas con las observaciones de las VE para cada VE en cada estaci�n
	        // primer indice de obs estaci�n, segundo �ndice VE
	        for(ive=0; ive<cantVarEst; ive++){
	            ((ArrayList<Double>)obs[estac[t]-1][ive]).add(valVarEst[t][ive]);                
	        }
	    }    
	          
	    // ordena las observaciones de las VE para cada estaci�n y VE  y halla l�mites superiores
	
	    for(iest=0; iest<cantEstac; iest++){
	        for(ive=0; ive<cantVarEst; ive++){        	
	            ArrayList<Double> obs1 = (ArrayList<Double>)obs[iest][ive];
	            Collections.sort(obs1);
	            double percentAcum=0.0;
	            for(iclas=0; iclas<cantCla[ive]; iclas++){
	                percentAcum += probCla[ive][iclas];
	                if(iclas<cantCla[ive]-1){
	                    N = obs1.size();
	                    indice = (int)Math.round(percentAcum*N+Constantes.EPSILONCOEF);
	                    if(indice>N) indice = N;
	                    if(indice==0) indice = 1;
	                    indice--;
	                    limSupVE[iest][ive][iclas]=obs1.get(indice);
	                }else{
	                // se carga el high-value como l�mite superior de la �ltima clase
	                    limSupVE[iest][ive][cantCla[ive]-1]= Double.POSITIVE_INFINITY;
	                }
	            }
	            if(Math.abs(percentAcum-1.0)>0.00001){
	               System.out.print("Error en definici�n de probabilidades");
	               System.exit(1);
	            }
	        }            
	    }
	    
	    // Carga las clases para cada tiempo y variable de estado
	    clases = new int[cantDatos][cantVarEst];
	    for(ive=0; ive<cantVarEst; ive++){
	        for(t=0; t<cantDatos; t++ ){
	            iest = estac[t]-1;
	            boolean hallo = false;
	            for(iclas=0; iclas<cantCla[ive]; iclas++){
	                if (valVarEst[t][ive]<= limSupVE[iest][ive][iclas]){
	                    clases[t][ive] = iclas;
	                    hallo = true;
	                    break;                        
	                }                    
	            } 
	            if(!hallo) {
	            	System.out.print("No se encontr� clase de un valor: ive = " + ive + "t = " + t);
	            	System.exit(1);
	            }
	        }                        
	    }
    }
    
    
    /**
     * Calcula las matrices de transici�n en el HashMap matrices
     * 
     * Calcula las medias por estaci�n, serie y estado compuesto (clase) en medias[iest][is][iec]
     */
    public void calculaMatTransYMed(){
        // Calcula las matrices de transici�n
        matrices = new Hashtable<Integer,MatPTrans>();
        defEstadosComp = new ArrayList<int[]>() ;  
        int[] cotasInferiores = new int[cantVarEst];
        int[] cotasSuperiores = new int[cantVarEst];

        for(int i = 0; i<cantVarEst; i++){
            cotasInferiores[i]=0;
            cotasSuperiores[i]= cantCla[i]-1;
        }
        EnumeradorLexicografico eL = new EnumeradorLexicografico(cantVarEst, cotasInferiores, cotasSuperiores);
        int[] vector;
        cantEstadosComp = 0;   // cantEstComp es la cantidad de estados compuestos
        vector = eL.devuelveVector();        
        do{
            cantEstadosComp++;
            defEstadosComp.add(vector);                    
            vector = eL.devuelveVector();            
        }while (vector!=null);

        // Carga la definici�n de estados en la clase MatPTrans
        setDefEstadosComp(defEstadosComp);                       

        // Cuenta transiciones
        int est1, est2;  
        
        // Se cargar� en estadoComp el �ndice del estado compuesto (clase compuesta) para cada tiempo
        // Se acumulan las cantidades de observaciones por estaci�n y estado compuesto.
        // Se acumulan los valores de las observaciones por serie, estaci�n y estado compuesto.
        
        estadoComp = new int[cantDatos];
        // medias: primer �ndice estaci�n, segundo �ndice serie, tercer indice estado compuesto.
        medias = new double[cantEstac][cantSeries][cantEstadosComp];
        for(int iest = 0; iest<cantEstac; iest++){
        	for(int is = 0; is<cantSeries; is++){
                for(int iec = 0; iec<cantEstadosComp; iec++){
                    medias[iest][is][iec] = 0.0;
                }
            }
          
        }
        cantObsEstacEstadoComp = new int[cantEstac][cantEstadosComp];
        for(int t = 0; t<cantDatos-1; t++){
            est1 = estac[t];
            int[] clases1 = clases[t];
            int[] clases2 = clases[t+1];
            int ind1 = indiceEstadoCompuesto(clases1);  // el indice en defEstadosComp del estado compuesto dadas las clases en t 
            estadoComp[t] = ind1;
            cantObsEstacEstadoComp[est1-1][ind1]++;       // acumula una observaci�n por estaci�n y estado compuesto.
            for (int is=0; is<cantSeries; is++){
                medias[est1-1][is][ind1] += datos[t][is];
            }
            int ind2 = indiceEstadoCompuesto(clases2);  // �dem en t+1                         
            if(matrices.containsKey(est1)){
                // ya existen transiciones en est1
                MatPTrans mPT = (MatPTrans)matrices.get(est1);
                double[][] probs = mPT.getProbs();
                probs[ind1][ind2] = probs[ind1][ind2]+1;
            }else{
                // aparece la primera transici�n entre est1 y est2 y se crea la matriz inicialmente con ceros
                double[][] probs = new double[cantEstadosComp][cantEstadosComp];
                for(int i1=0; i1<cantEstadosComp; i1++){
                    double[] m1 = new double[cantEstadosComp];
                    Arrays.fill(m1, 0.0);                    
                }
                probs[ind1][ind2] = probs[ind1][ind2]+1;
                MatPTrans mpt = new MatPTrans(est1, probs);
                matrices.put(est1, mpt);                                
            }            
        }
        
        // Carga las medias de cada serie, para cada estaci�n y estado compuesto
        // divide el valor acumulado en medias por la cantidad de observaciones 
        for(int iest=0; iest<cantEstac; iest++){
        	for(int is=0; is<cantSeries; is++){
                for(int iec=0; iec<cantEstadosComp; iec++){
                    medias[iest][is][iec] = medias[iest][is][iec]/cantObsEstacEstadoComp[iest][iec];
                }
            }                                             
        }          
        
        // Divide cada fila entre la cantidad de datos de la fila.
        // Para recorrer las claves las pasa a un conjunto al que le crea un iterador.
        Set<Integer> set = matrices.keySet();
        Iterator<Integer> iter = set.iterator();
        while(iter.hasNext()){
            Integer i = (Integer)iter.next();
            MatPTrans mPT = (MatPTrans)matrices.get(i);
            double[][] probs = mPT.getProbs();
            double suma;
            for(int ifil=0; ifil<cantEstadosComp; ifil++){
                suma = 0.0;
                for(int icol=0; icol<cantEstadosComp; icol++){
                    suma += probs[ifil][icol];
                }
                for(int icol=0; icol<cantEstadosComp; icol++){
                    probs[ifil][icol] = probs[ifil][icol]/suma;
                }                
            }                                               
        }     	
    	
    	
    }
    
    /**
     * Crea la estructura datosEstacEstComp para cargar las observaciones de las series originales asociadas 
     * a cada estaci�n y estado compuesto.
	 * Para cada estaci�n y estado compuesto hay un ArrayList de double[] con las observaciones 
     */
    public void creaDatosEstacEstComp(){
  	    	
        /**
         * Crea la estructura datosEstacEstComp
         */
    	datosEstacEstComp = new Object[cantEstac][cantEstadosComp] ;
        for(int iest=0; iest<cantEstac; iest++){
            for(int iec=0; iec<cantEstadosComp; iec++){
                ArrayList<double[]> aux = new ArrayList<double[]>();
                datosEstacEstComp[iest][iec] = aux;                
            }
        }
        /**
         * Almacena las observaciones en datosEstacEstComp
         */
        for(int t=0; t<cantDatos; t++ ){
            int iest = estac[t]-1;
            int iec = estadoComp[t]; 
            ArrayList<double[]> aux = (ArrayList<double[]>)datosEstacEstComp[iest][iec];
            aux.add(datos[t]);                                    
        }                            	
    }
    
    
    
    /**
     * M�todo auxiliar que devuelve el �ndice en defEstadosComp empezando en 0, de un estado compuesto
     * cuyas clases son las de vectorClas. Las clases se numeran de 0 en adelante.
     */
    int indiceEstadoCompuesto(int[] vectorClas){
        int indice = 0;
        for(int i = 0; i<defEstadosComp.size(); i++){
            if(Arrays.equals(vectorClas, defEstadosComp.get(i))){
                indice = i;
                break;
            }            
        }
        return indice;                
    }
    
    
    
	//////////////////////////////////////////////////////////
	//
	//	METODOS DE IMPRESI�N DE RESULTADOS DE VERIFICACI�N EN ARCHIVOS
	//
	//////////////////////////////////////////////////////////        
    
    /**
     * Genera archivos de salida en el directorio especificado
     * incluso DatosProcHistorico.xlt que es entrada para el proceso hist�rico asociado al Markov
     * @param directorio 
     */
    public void imprimeResultVerif(String directorio) {
    	
        System.out.println("COMIENZA IMPRESI�N DE SALIDAS DE VERIFICACI�N");     
      
        // Genera salida de  datos, estaci�n, valor de las var. de Estado y clase 
        // para cada tiempo t
        String dirDatos = directorio + "/DatosProcHistorico.xlt";
        boolean existe = DirectoriosYArchivos.existeArchivo(dirDatos);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatos);
        
        StringBuilder sb = new StringBuilder();
        sb.append("t,");
        sb.append("Estacion,");               
        for(int is=0; is<cantSeries; is++){
            sb.append(nombreSeries[is]); 
            sb.append(",");                                      
        }
        for(int ive=0; ive<cantVarEst; ive++){
            sb.append(nombreVarEst[ive] + " - ");
        	sb.append("Valor V.Estado ");            
            sb.append(",");                                
            sb.append("Clase V.Estado ");            
            sb.append(",");                      
        }   
        sb.append("Ind.est.compuesto,");              
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
            
        sb = new StringBuilder();
        for(int t=0; t<cantDatos; t++){
        	System.out.println("t=" + t);
            
            sb.append(t);
            sb.append(","); 
            sb.append(estac[t]);
            sb.append(",");             
            for(int is=0; is<cantSeries; is++){
                sb.append(datos[t][is]);            
                sb.append(",");                            
            }
            for(int ive=0; ive<cantVarEst; ive++){
                sb.append(valVarEst[t][ive]);
                sb.append(",");                                
                sb.append(clases[t][ive]);            
                sb.append(",");  
            }             
            sb.append(estadoComp[t]);            
        }
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
                
        // Genera salida de l�mites de variables de estado
        String dirVarEst = directorio + "/LimSupClasesVarEst.xlt";
        existe = DirectoriosYArchivos.existeArchivo(dirVarEst);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirVarEst);
        
        for(int ive=0; ive<cantVarEst; ive++){
            sb = new StringBuilder();
            sb.append("Variable de estado,");            
            sb.append(nombreVarEst[ive]);
            sb.append(" - L�mites superiores de clases\n");            
            DirectoriosYArchivos.agregaTexto(dirVarEst, sb.toString());
            sb = new StringBuilder();
            for(int iest=0; iest<cantEstac; iest++){
                sb.append("Estaci�n,");
                sb.append(iest+1);
                sb.append(",");                
                for(int iclas = 0; iclas<cantCla[ive]; iclas++){
                    sb.append(limSupVE[iest][ive][iclas]);
                    sb.append(",");                      
                }
                sb.append("\n");
            }
            sb.append("\n");
            DirectoriosYArchivos.agregaTexto(dirVarEst, sb.toString());            
        }
        
        /**
         * Genera salida de las medias absolutas de cada serie en cada estaci�n
         */
        String dirMedAbs = directorio + "/MediasAbsolutas.xlt";
        existe = DirectoriosYArchivos.existeArchivo(dirMedAbs);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirMedAbs);           

        sb = new StringBuilder();
        sb.append(",");                    
        for (int is=0; is<cantSeries; is++){
            sb.append(nombreSeries[is]);
            sb.append(",");            
        }
        DirectoriosYArchivos.agregaTexto(dirMedAbs, sb.toString());
        for(int iest=0; iest<cantEstac; iest++){
            sb = new StringBuilder();            
            sb.append(iest+1);
            sb.append(",");
            for(int is=0; is<cantSeries; is++){
                sb.append(mediaAbs[iest][is]);
                sb.append(",");                
            }
            DirectoriosYArchivos.agregaTexto(dirMedAbs, sb.toString());
        }
        
        /**
         * Genera salida, por estaci�n y estado compuesto la cantidad de observaciones en cada estado compuesto
         */
        String dirObsE = directorio + "/cantObsEstEstadoComp.xlt";
        existe = DirectoriosYArchivos.existeArchivo(dirObsE);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirObsE);
        sb = new StringBuilder();
        sb.append(",");                    
        for (int iec=0; iec<cantEstadosComp; iec++){
            sb.append(iec);
            sb.append(",");            
        }
        DirectoriosYArchivos.agregaTexto(dirObsE, sb.toString());
        for(int iest=0; iest<cantEstac; iest++){
            sb = new StringBuilder();            
            sb.append(iest+1);
            sb.append(",");
            for(int iec=0; iec<cantEstadosComp; iec++){
                sb.append(cantObsEstacEstadoComp[iest][iec]);
                sb.append(",");                
            }
            DirectoriosYArchivos.agregaTexto(dirObsE, sb.toString());
        }        
         
        /**
         * Genera salida de los valores promedio de cada serie en cada estaci�n y estado compuesto         
         */
        for (int is=0; is<cantSeries; is++){
            String dirMed = directorio + "/Media" + nombreSeries[is] + ".xlt";
            existe = DirectoriosYArchivos.existeArchivo(dirMed);
            if(existe) DirectoriosYArchivos.eliminaArchivo(dirMed);   
            sb = new StringBuilder();
            sb.append("ESTACION,");
            for(int iec=0; iec<cantEstadosComp; iec++){
                sb.append(iec+1);
                sb.append(",");                
            }
            DirectoriosYArchivos.agregaTexto(dirMed, sb.toString());
            for(int iest = 0; iest<cantEstac; iest++){
                sb = new StringBuilder();
                sb.append(iest+1);
                sb.append(",");
                for(int iec=0; iec<cantEstadosComp; iec++){
                    sb.append(medias[iest][is][iec]);
                    sb.append(",");                    
                }
                DirectoriosYArchivos.agregaTexto(dirMed, sb.toString());                
            }            
        }    

        
        // Genera salida de comparaci�n de clases con EDF
        String dirComparaClasesEDF = directorio + "/ComparaClasesEDF.xlt";
        existe = DirectoriosYArchivos.existeArchivo(dirComparaClasesEDF);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirComparaClasesEDF);
        DirectoriosYArchivos.agregaTexto(dirComparaClasesEDF, "CLASES POR CRONICA Y SEMANA");
        int ind = 0; 
        while (ind<estadoComp.length){
        	int r = ind % 52; 
        	if(r == 0){
        		int numCron = ind/52 + 1;
        		DirectoriosYArchivos.agregaTexto(dirComparaClasesEDF, "Cronica " + numCron);
        	}
    		for(int ifil=1; ifil<=6; ifil++){        		
	        		int maxcol = 10;
	        		if(ifil == 6) maxcol = 2;
	        		sb = new StringBuilder();	        		
	        		for(int icol=0; icol<maxcol; icol++){
	        			sb.append((estadoComp[ind]+1) + "\t");
		        		ind++;
	        		}
	        		String texto = sb.toString();
	        		DirectoriosYArchivos.agregaTexto(dirComparaClasesEDF, texto);
    		}	
    		int kk = 0;
        }
        
        
        System.out.println("TERMINO LA IMPRESION DE SALIDAS DE VERIFICACION");
    }


	//////////////////////////////////////////////////////////
	//
	//	METODO QUE CREA TEXTOS PARA SER LE�DOS POR CargadorPEHistorico y CargadorPEMarkov
	//
	//////////////////////////////////////////////////////////           
    
	/**
	 * M�todo que crea los textos para ser le�dos por CargadorPEHistorico y CargadorPEMarkov
	 */
    private void imprimeTextosParaDatatypes(String directorio) {
    	    
    	
        System.out.println("COMIENZA IMPRESIÓN DE TEXTOS PARA DATATYPE");     
        
        /**
         *  Genera salida de datos de series y sus estados en DatosSal.xlt
         *  para cada tiempo t con datos:
         *  -cronica
         *  -estaci�n
         *  -valor de las series
         *  -valor de las VE (clases discretas)
         *  -valor de las VE (continuos)
         *  en DatosSal.xlt 
         */
              
        Date fecha1 = new Date();
        
        String dirDatos = directorio + "/datosProcHistorico.xlt";
        boolean existe = DirectoriosYArchivos.existeArchivo(dirDatos);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatos);
        
        StringBuilder sb = new StringBuilder();
        sb.append(identificadorEstimacion + " " + fecha1.toString());
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());        
        
        sb = new StringBuilder();
        sb.append("cantSeries" + " " + cantSeries);
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
        
        sb = new StringBuilder();
        sb.append("cantVarEst " + " " +  cantVarEst);
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());   
        
        for(int ive=0; ive<cantVarEst; ive++){
        	 sb = new StringBuilder();
        	 sb.append("VARIABLE_ESTADO  " + nombreVarEst[ive] + " CANT_VALORES " + cantCla[ive]);
        }
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
        
        sb = new StringBuilder();
        sb.append("cantCron" + " " + cantCron);
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
        
        sb = new StringBuilder();
        sb.append("nombreDurPaso" + " " + "SEMANA");  // nombre de la duraci�n del paso
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());     
        
        sb = new StringBuilder();
        sb.append("cantDatos" + " " + cantDatos);  // cantidad de datos 
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());             
                  
        
        sb = new StringBuilder();
        sb.append("Cronica ");
        sb.append("Estacion ");               
        for(int is=0; is<cantSeries; is++){
            sb.append(nombreSeries[is]); 
            sb.append(" ");                                      
        }
        for(int ive=0; ive<cantVarEst; ive++){
            sb.append(nombreVarEst[ive] + " ");                               
        }  
        for(int ive=0; ive<cantVarEst; ive++){
            sb.append(nombreVarEst[ive] + "-");
        	sb.append("V.Estado-continua");            
            sb.append(" ");                                                    
        }          
        sb.append("Ind.est.compuesto,");              
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
           
        sb = new StringBuilder();
        for(int t=0; t<cantDatos; t++){
            
            sb.append(etiquetaCronDatos[t]);
            sb.append(" "); 
            sb.append(estac[t]);
            sb.append(" ");             
            for(int is=0; is<cantSeries; is++){
                sb.append(datos[t][is]);            
                sb.append(" ");                            
            }
            for(int ive=0; ive<cantVarEst; ive++){
                sb.append(clases[t][ive]);
                sb.append(" ");                                
            }   
            for(int ive=0; ive<cantVarEst; ive++){
                sb.append(valVarEst[t][ive]);
                sb.append(" ");                                
            }                               
            sb.append(estadoComp[t]);
            sb.append("\n");
        }
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
        
        
        /**
         * Genera salida de las matrices de transici�n
         * en MatTrans.xlt
         */
        
        String dirMat = directorio + "/MatTrans.xlt";        
        
        existe = DirectoriosYArchivos.existeArchivo(dirMat);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirMat);        
        
        sb = new StringBuilder();
        sb.append(identificadorEstimacion + " " + fecha1.toString());
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());        
        
        sb = new StringBuilder();
        sb.append("cantSeries" + " " + cantSeries);
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());
        
        sb = new StringBuilder();
        sb.append("cantVarEst " + " " +  cantVarEst);
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString()); 
        
        sb = new StringBuilder();
        sb.append("cantVarEstOptim " + " " +  cantVarEst);
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());         
        
        sb = new StringBuilder();
        sb.append("cantEstac" + " " + cantEstac);
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());        

        sb = new StringBuilder();
        sb.append("cantCron" + " " + cantCron);
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());
        
        sb = new StringBuilder();
        sb.append("nombreDurPaso" + " " + "SEMANA");  // nombre de la duraci�n del paso
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());   
        
        sb = new StringBuilder();
        sb.append("cantEstadosCompuestos" + " " + cantEstadosComp);  // cantidad de estados compuestos
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());           
        
        sb = new StringBuilder();
        sb.append("cantDatos" + " " + cantDatos);  // cantidad de datos 
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());                

        sb = new StringBuilder();
        sb.append("Nombres-VA" + " ");
        for(int is=0; is<cantSeries; is++){
            sb.append(nombreSeries[is]); 
            sb.append(" ");                                      
        }
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString()); 
        
        sb = new StringBuilder();                 
        sb.append("Nombres-VE" + " ");        
        for(int ive=0; ive<cantVarEst; ive++){
            sb.append(nombreVarEst[ive] + " ");                               
        }          
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());  
        
        sb = new StringBuilder();                 
        sb.append("Nombres-VE-Optim" + " ");        
        for(int ive=0; ive<cantVarEst; ive++){
            sb.append(nombreVarEst[ive] + " ");                               
        }          
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());          
        
        sb = new StringBuilder(); 
        sb.append("Cant-Clases-VE" + " ");           
        for(int ive=0; ive<cantVarEst; ive++){
            sb.append(cantCla[ive] + " ");                               
        } 
        DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());   

        
        Set<Integer> set = matrices.keySet();
        // Hay que ordenar las matrices para imprimirlas
        ArrayList<Integer> lista = new ArrayList<Integer>();
        lista.addAll(set);
        Collections.sort(lista);
        for(int i: lista){
            MatPTrans mPT = (MatPTrans)matrices.get(i);  
            String st = mPT.toString();
            DirectoriosYArchivos.agregaTexto(dirMat, st);                        
        }         
        

        /**
         * Genera salida de observaciones por estaci�n y estado compuesto
         */
        String dirDatosEE = directorio + "/ObservPorEstacEC.xlt";
        existe = DirectoriosYArchivos.existeArchivo(dirDatosEE);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatosEE);  
        sb = new StringBuilder();
        sb.append("CANTIDAD_ESTADOS_COMPUESTOS ");
        sb.append(cantEstadosComp);
        DirectoriosYArchivos.agregaTexto(dirDatosEE, sb.toString());      
        sb = new StringBuilder();
        for(int iest=0; iest<cantEstac; iest++){
            for(int iec=0; iec<cantEstadosComp; iec++){               
                sb.append("Estacion ");
                sb.append(iest+1);
                sb.append(" ");                
                sb.append("Estado-compuesto= ");
                sb.append(iec);              
                sb.append("\n");
                for(int ive=0; ive<cantVarEst; ive++){       	
                	sb.append(nombreVarEst[ive]);
                	sb.append(" ");
                	sb.append(defEstadosComp.get(iec)[ive]);
                	sb.append(" ");
                	sb.append("\n");                	
                }                             
                for(int is=0; is<cantSeries; is++){
                	sb.append(nombreSeries[is]);
                	sb.append(" ");   
                }
                sb.append("\n");               
                Object aux = datosEstacEstComp[iest][iec];
                ArrayList<double[]> aux2 = (ArrayList<double[]>)aux;
                for(int i=0; i<aux2.size(); i++){
                    for(int iobs=0; iobs<cantSeries; iobs++){
                        sb.append(aux2.get(i)[iobs]);                        
                        sb.append(" ");   
                    }
                    sb.append("\n");                  
                }                
            }
        } 
        DirectoriosYArchivos.agregaTexto(dirDatosEE, sb.toString());
    	
        System.out.println("TERMINA IMPRESIÓN DE TEXTOS PARA DATATYPE"); 		
	}
    

	public String getNombreDurPaso() {
		return nombreDurPaso;
	}

	public void setNombreDurPaso(String nombreDurPaso) {
		this.nombreDurPaso = nombreDurPaso;
	}    
    
    
	//////////////////////////////////////////////////////////
	//
	//	METODO MAIN PARA PROBAR LA CLASE
	//
	//////////////////////////////////////////////////////////        
    

    /**
     * El programa 
     * - lee los datos de un conjunto de series y la definici�n de variables de estado
     * - estima las matrices de un proceso de Markov y las observaciones representativas
     * - crea los archivos para cargar los datatypes legibles por CargadorPEHistorico y CargadorPEMarkov
     */
    public static void main(String[] args) {              
        EstimadorMarkovDiscreto eM = new EstimadorMarkovDiscreto();
        
    	String dirArchConf = "resources/ESTIMADORES.conf";
    	String nombreProp = "rutaMarkov";
        
        // Elige directorios de entradas y salidas
        boolean soloDirectorio = true;  
        String titulo1 = "ELIJA EL DIRECTORIO DE ENTRADA DE DATOS DONDE LEER ARCHIVOS: datos.txt, defVE.txt, defEstac.txt ";
        String dirEntradas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo1, dirArchConf, nombreProp);
        
        String titulo2 ="ELIJA EL DIRECTORIO DE SALIDA DE LA ESTIMACION, ARCHIVOS: datosProcHistorico.xlt, MatTrans.xlt, ObservPorEstacEC.xlt, defEstac.txt"; 
        String dirSalidas =  LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo2, dirArchConf, nombreProp);
 

        String titulo3 = "ELIJA EL DIRECTORIO PARA COPIAR SALIDA PARA EL PROCESO HIST�RICO ASOCIADO, ARCHIVO datosProcHistorico.xlt";
        String dirHist =  LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo3, dirArchConf, nombreProp);
        
        // Copia la definici�n de estaciones al directorio de salida
        String origen = dirEntradas + "/defEstac.txt";
        String destino = dirSalidas + "/defEstac.txt";
        try{
        	DirectoriosYArchivos.copy2(origen, destino);
        }catch(Exception e){
        	System.out.println("Error al copiar el archivo " + origen);
        	System.exit(1);
        }
        

        eM.cargaTodo(dirEntradas);
        
        // Estima proceso de Markov
        eM.estimaMarkov();
        
        // Crea salidas de verificaci�n de resultados

        String dirSalVerif = dirSalidas + "/SalidasVerif";   
        if(!DirectoriosYArchivos.existeDirectorio(dirSalVerif)) DirectoriosYArchivos.creaDirectorio(dirSalidas,"SalidasVerif");
        eM.imprimeResultVerif(dirSalVerif);
        
        // Crea textos legibles por CargadorPEHistorico y CargadorPEMarkov
        eM.imprimeTextosParaDatatypes(dirSalidas);
        
        // Copia el archivo DatosSal.xlt al directorio dirHist
        origen = dirSalidas + "/datosProcHistorico.xlt";
        destino = dirHist + "/datosProcHistorico.xlt";                   
        try{
        	DirectoriosYArchivos.copy2(origen, destino);
        }catch(Exception e){
        	System.out.println("Error al copiar el archivo " + origen);
        	System.exit(1);
        }    
        
        System.out.println("TERMINA LA TOTALIDAD DEL PROCESO DE ESTIMACI�N");
                
    }


    public static Boolean estimar(String dirEntradas, String nombre, String nombreHist) {
        EstimadorMarkovDiscreto eM = new EstimadorMarkovDiscreto();


        // Copia la definición de estaciones al directorio de salida
        String origen = dirEntradas + "/defEstac.txt";
        String dirSalidas = "";
        String dirSalidasHist = "";
        String dirResources = "";

        LectorPropiedades lprop = new LectorPropiedades(".\\resources\\mop.conf");
        try {
            dirResources = lprop.getProp("rutaEntradas") + "\\resources\\";
            dirSalidas= dirResources+ nombre ;
            dirSalidasHist= dirResources + nombreHist ;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String destino = dirSalidas + "\\defEstac.txt";
        DirectoriosYArchivos.creaDirectorio(dirResources, nombre );
        DirectoriosYArchivos.creaDirectorio(dirResources, nombreHist );
        try{
            DirectoriosYArchivos.copy2(origen, destino);
        }catch(Exception e){
            System.out.println("Error al copiar el archivo " + origen);
            System.exit(1);
        }


        eM.cargaTodo(dirEntradas);

        // Estima proceso de Markov
        eM.estimaMarkov();

        // Crea salidas de verificación de resultados

        String dirSalVerif = dirSalidas + "/SalidasVerif";
        if(!DirectoriosYArchivos.existeDirectorio(dirSalVerif)) DirectoriosYArchivos.creaDirectorio(dirSalidas,"SalidasVerif");
        eM.imprimeResultVerif(dirSalVerif);

        // Crea textos legibles por CargadorPEHistorico y CargadorPEMarkov
        eM.imprimeTextosParaDatatypes(dirSalidas);

        // Copia el archivo DatosSal.xlt al directorio dirHist
        origen = dirSalidas + "/datosProcHistorico.xlt";
        destino = dirSalidasHist + "/datosProcHistorico.xlt";

        try{
            DirectoriosYArchivos.copy2(origen, destino);
        }catch(Exception e){
            System.out.println("Error al copiar el archivo " + origen);
            System.exit(1);
        }
        System.out.println("==========================================");
        System.out.println("TERMINA LA TOTALIDAD DEL PROCESO DE ESTIMACIÓN");
        System.out.println("==========================================");

        double [][] matAg = new double[1][1];
        matAg[0][0] = 1;

        String [] aux = new String[0];

        escribeTextoAgregadorLineal(dirSalidasHist, eM.identificadorEstimacion ,
                nombreHist,  nombre, eM.nombreVarEst,
                aux, eM.nombreVarEst,matAg);

        //ESCRIBE DATOS GENERALES DEL PROCESO MARKOV DE OPTIMIZACION
        escribeDatosGeneralesPE( dirSalidas, eM.identificadorEstimacion, false,  true,  "",  false,
        eM.nombreSeries,  eM.nombreVarEst,  true,  true,
         false,  1,  Constantes.SEMANA,  false,
                aux,  aux);


        //ESCRIBE DATOS GENERALES DEL PROCESO HISTORICO DE SIMULACION
        escribeDatosGeneralesPE( dirSalidasHist, eM.identificadorEstimacion, true,  false,  nombre,  false,
                eM.nombreSeries,  eM.nombreVarEst,  false,  false,
                false,  1,  Constantes.SEMANA,  false,
                aux,  aux);


        return true;
    }






}
