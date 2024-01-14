/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CalculadorVE is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;

import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.LeerDatosArchivo;

/**
 * 
 * A partir de un conjunto de series, construye un conjunto de variables de estado
 * a partir de operaciones con ellas:
 * -Combinaciones lineales de valores coriente y rezagados
 * -Estandarizaciones para escalar los valores al rango 0,1.
 * @author ut469262
 *
 */
public class CalculadorVE {

    private int cantSeries;
    private String[] nombresSeries;  // las series a partir de las cuales se construyen las VE
	private String[] nombresVEAgregadas; // nombres de las variables de estado que se construyen 
    private int cantVEAg;
    /**
     * Coeficientes de los datos para crear la variable de estado.
     * Primer índice: variable de estado
     * Segundo índice: serie de datos
     * Tercer índice: rezago en la serie empezando de 0, -1, -2,....
     * Define la combinación lineal de datos rezagados que determina cada estado.
     */
    private double[][][] defVarEst;
    
    /**
     * Es lo mismo que defVarEst con otro formato
     * Coeficientes de los datos para crear la variable de estado.
     * Clave: nombre de VE + nombre de Serie + rezago en la Serie empezando de 0, 1, 2
     * (atencion que los rezagos son positivos) 
     * Valor: coeficiente lineal
     */
    private Hashtable<String, Double> defVEAg;
    
    
    /**
     * Cantidad de rezagos (incluso el valor corriente) en la definición de las VE
     * primer índice VE, segundo índice serie
     */
    private int[][] cantRezagos;   
    
    
    /**
     * True si la serie se estandariza 0-1 para crear la respectiva VE
     * primer índice VE, segundo índice serie
     */
    private boolean[][] estandarizada;   
    
    private Hashtable<String, Serie> seriesLeidas; // las series originales incluso las exogenas, clave el nombre de serie original
    private String nombreDurPaso; 
    private Hashtable<String, Serie> series01;  // las series estandarizadas 0,1, incluso las exogenas, clave el nombre de serie original
    private Hashtable<String, Serie> series;  // las series a emplear para hallar cada VE, clave el nombre de serie original
    	    
    private Hashtable<String, Serie> varsEstado; // para cada nombre de VE, la serie de sus valores continuos	    
    
    /**
     * Los nombres de los grupos de series de cada VE
     * Clave nombre de VE
     * Valor lista de nombres de grupos de la VE
     */
    private Hashtable<String, ArrayList<String>> gruposDeVEAg;
    
    /**
     * Nombre del grupo al que pertenece cada serie en cada VE.
     * Clave: String con nombre de VE + nombre de serie
     * Valor: nombre del grupo
     * No puede haber el mismo nombre de grupo en dos VE distintas
     * Dentro de un grupo de series, cada una tiene un ponderador, para formar una combinación
     * lineal a partir de los valores de la serie.
     * El valor resultante de cada grupo se estandariza 0,1 y se pondera por el
     * respectivo ponderador grupo
     */
    private Hashtable<String, String> grupoDeVESerie;
    
  
    /**
     * Operador para combinar los valores de los grupos dentro de una VE
     * puede ser COMBINACION_LINEAL, MINIMO, etc.
     * Clave: nombre de VE
     * Valor: constante del operador
     */
    private Hashtable<String, String> operadorEnVE;
    
    /**
     * Ponderador de un grupo dentro de la VE si el operador es COMBINACION_LINEAL
     * Clave: String con nombre de VE + nombre del grupo
     * Valor: ponderador del grupo dentro de la VE
     */
    private Hashtable<String, Double> ponderadorDeGruposEnVE;
    
    private int[] anios;
    private int[] pasos;
    private int cantDatos;
    
    /**
     * Este constructor se usa por el proceso MarkovAmpliado que lee los parámetros 
     * de construcción de las VE por sí mismo.
     * @param cantSeries incluso las exogenas
     * @param nombresSeries incluso las exogenas
     * @param nombresVE
     * @param cantVE
     * @param defVarEst coeficientes de rezagos
     * @param cantRezagos
     * @param estandarizada
     * @param seriesLeidas incluso las exogenas
     * @param gruposDeVE
     * @param grupoDeVESerie
     * @param operadorEnVE
     * @param ponderadorDeGruposEnVE
     */
    public CalculadorVE(int cantSeries, String[] nombresSeries, String nombreDurPaso, 
    		String[] nombresVEAgregadas, int cantVE, double[][][] defVarEst,
			int[][] cantRezagos, boolean[][] estandarizada, Hashtable<String, Serie> seriesLeidas,			
			Hashtable<String, ArrayList<String>> gruposDeVEAg, Hashtable<String, String> grupoDeVESerie,
			Hashtable<String, String> operadorEnVE, Hashtable<String, Double> ponderadorDeGruposEnVE) {
		super();
		this.cantSeries = cantSeries;
		this.nombresSeries = nombresSeries;
		this.nombreDurPaso = nombreDurPaso;
		this.nombresVEAgregadas = nombresVEAgregadas;
		this.cantVEAg = cantVEAg;
		this.defVarEst = defVarEst;
		this.cantRezagos = cantRezagos;
		this.estandarizada = estandarizada;
		this.seriesLeidas = seriesLeidas;
		this.gruposDeVEAg = gruposDeVEAg;
		this.grupoDeVESerie = grupoDeVESerie;
		this.operadorEnVE = operadorEnVE;
		this.ponderadorDeGruposEnVE = ponderadorDeGruposEnVE;
		ayudaConstructor();
	}
    
    /**
     * Este constructor se usa por los procesos que no cargan los parámetros de
     * construcción de las VE sino que usan cargaParamVE de esta clase
     * y los datos de las series los leen separadamente y deben pasarlos en 
     * este constructor.
     */
    public CalculadorVE(String[] nombresSeries, String[] nombresVEAgregadas,
    		String nombreDurPaso, Hashtable<String, Serie> seriesLeidas){
    	
//		calculadorVE = new CalculadorV(nombresVariablesYExo,
//				nombresVEAgregadas, nombreDurPaso, seriesLeidas);

    	this.nombresSeries = nombresSeries;
    	this.cantSeries = nombresSeries.length;
    	this.nombresVEAgregadas = nombresVEAgregadas;
    	this.cantVEAg = nombresVEAgregadas.length;
    	this.nombreDurPaso = nombreDurPaso;
    	this.seriesLeidas = seriesLeidas;
    	ayudaConstructor();
    }


    public void ayudaConstructor(){
    	cantDatos = seriesLeidas.get(nombresSeries[0]).getDatos().length;
    	pasos = seriesLeidas.get(nombresSeries[0]).getPaso();
    	anios = seriesLeidas.get(nombresSeries[0]).getAnio();
    }




    /**
     * Lee los parámetros de las variables de estado del archivo dirArchivo
     * incluyendo los grupos de series
     * @param dirArchivo 
     * 
     * 	LOS NOMBRES DE VARIABLES DE ESTADO DEL VAR 
	 *	DE VARIABLES QUE SE EMPLEAN PARA CONSTRUIR LAS VE Y DE LAS PROPIAS VE, QUE SE LEEN 
	 *	EN ESTE METODO DEBEN SER LOS MISMOS QUE LOS QUE
	 *	SE CARGARON EN EL CONSTRUCTOR DE this.
     */
    public void cargaParamVE(String dirArchivo){
        ArrayList<ArrayList<String>> texto;
        texto = LeerDatosArchivo.getDatos(dirArchivo);
        AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirArchivo);	        

        int i=0;
        defVarEst = new double[cantVEAg][cantSeries][];
        defVEAg = new Hashtable<String, Double>();
        cantRezagos = new int[cantVEAg][cantSeries];
        estandarizada = new boolean[cantVEAg][cantSeries];
        gruposDeVEAg = new Hashtable<String, ArrayList<String>>();
        grupoDeVESerie = new Hashtable<String, String>();
	    operadorEnVE = new Hashtable<String, String>();
	    ponderadorDeGruposEnVE = new Hashtable<String, Double>();	  	        
        double[] aux;
        boolean[] auxb;
        i++;
        for(int ive=0; ive< cantVEAg; ive++){	        	
            String nleido =texto.get(i).get(1);
            if(!nombresVEAgregadas[ive].equalsIgnoreCase(nleido)){
            	System.out.println("Variable no coincide en calculador de VE - Variable " + nleido);
            	System.exit(1);
            }
            i++;
        	ArrayList<String> auxG = lector.cargaLista(i, "GRUPOS");
        	gruposDeVEAg.put(nombresVEAgregadas[ive], auxG);
        	i++;
        	String oper = lector.cargaPalabra(i, "OPERADOR_GRUPOS");
        	operadorEnVE.put(nombresVEAgregadas[ive], oper);
        	i++;
        	if(oper.equalsIgnoreCase(utilitarios.Constantes.COMBINACION_LINEAL)){
        		ArrayList<Double> auxD = lector.cargaListaReales(i, "PONDERADORES_GRUPOS");
        		for(int g=0; g<auxD.size(); g++){
        			ponderadorDeGruposEnVE.put(nombresVEAgregadas[ive] + auxG.get(g), auxD.get(g));
        		}
        	}
        	i++;            	
            for(int iserie = 0; iserie< cantSeries; iserie++){
            	// verifica que cada serie aparezca en su orden
            	if(!texto.get(i).get(1).equalsIgnoreCase(nombresSeries[iserie])){
            		System.out.println("Error en lectura de serie en calculador de VE - serie " + nombresSeries[iserie]);
            		System.exit(1);
            	}
            	i++;
            	String sg = lector.cargaPalabra(i, "GRUPO");
            	grupoDeVESerie.put(nombresVEAgregadas[ive]+nombresSeries[iserie], sg);
            	i++;

            	double pondSerie = Double.parseDouble(texto.get(i).get(1));
            	i++;
            	auxb = new boolean[cantSeries];
            	boolean b=false;
            	if(texto.get(i).get(1).equals("SI")){
            		b = true;
            	}
            	estandarizada[ive][iserie] = b;
            	i++;
            	int cr = Integer.parseInt(texto.get(i).get(1));
            	cantRezagos[ive][iserie] = cr;
            	i++;
	            aux = new double[cr];
	            defVarEst[ive][iserie]=aux;	            
	            for(int j=1; j<texto.get(i).size(); j++){
	            	double dd = Double.parseDouble(texto.get(i).get(j))*pondSerie;
	                defVarEst[ive][iserie][j-1] = dd;
	                String clave = nombresVEAgregadas[ive] + nombresSeries[iserie] + "-L" + (j-1);
    				defVEAg.put(clave, dd);
	            }
	            i++;
            }
//            aux = new double[texto.get(i).size()-1];     
        }
        System.out.println("TERMINO LA LECTURA DE PARAMETROS DE VE AGREGADAS");
        
    }
    	    	    
    
    
    /**
     * Para emplearlas en la forma de estimacion VAR_Y_PVA
     * calcula los valores continuos de cada VE en las series de varsEstado
     * Cada VE puede tener más de un grupo de series que contribuyen a ella
     * La contribución de cada grupo se estandariza a 0,1 por paso del año, antes
     * de ponderarse con el ponderador del grupo.
     */    
	public Hashtable<String, Serie> construyeVE(){
		// ATENCION: el agregador de estados lo construira el EstimadorVAR
	  	varsEstado = new Hashtable<String, Serie>();
		for(int ive=0; ive<cantVEAg; ive ++){	    			
			series = new Hashtable<String, Serie>();
			String nombreVE = nombresVEAgregadas[ive];
    		Serie sve = new Serie(nombreDurPaso);
    		sve.setAnio(anios);
    		sve.setPaso(pasos);
    		sve.setDatos(new double[cantDatos]);    			
			if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.COMBINACION_LINEAL)){
				sve.inicializa(0.0);
			}else if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.MINIMO)){
				sve.inicializa(Double.MAX_VALUE);
			}else{
				System.out.println("Error en VARenVariablesNormalizdas en operador de variable de estado " + nombreVE);
			}
			ArrayList<String> grupos = gruposDeVEAg.get(nombreVE);	    			
			for(String ng: grupos){
				Serie sg = new Serie(ng, nombreDurPaso, new double[cantDatos], anios, pasos);		
    			for(int is=0; is<cantSeries; is++){
    				String nombreS = nombresSeries[is];
    				if(grupoDeVESerie.get(nombreVE+nombreS).equalsIgnoreCase(ng)){
	    				if(estandarizada[ive][is]==true){
	    					series.put(nombreS, series01.get(nombreS));
	    				}else{
	    					series.put(nombreS, seriesLeidas.get(nombreS));
	    				}
						for(int t=0; t<cantDatos; t++ ){
		    				for(int ir=0; ir< cantRezagos[ive][is]; ir++){
		    					if(t-ir >= 0){
		    						sg.getDatos()[t] += series.get(nombreS).getDatos()[t-ir]*defVarEst[ive][is][ir];
		    					}else{
		    						sg.getDatos()[t] += series.get(nombreS).getDatos()[t]*defVarEst[ive][is][ir];
		    					}
		    				}
						}
    				}	
    			}
				if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.COMBINACION_LINEAL)){
	    			double epsi = 0.0;
	    			sve = MetodosSeries.estandariza01(sve, epsi);
					double pondg = ponderadorDeGruposEnVE.get(nombreVE+ng);
					sve = MetodosSeries.combilin2S(sve, sg, 1.0, pondg);
    			}else if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.MINIMO)){
					sve = MetodosSeries.min2S(sve, sg);	    				
    			}
    		}

    		varsEstado.put(nombreVE, sve);
    	}
		return varsEstado;
    }
	
    /**
     * Metodo para ser empleardo en la forma de estimacion SOLO_VAR
     * construye la matrizAgregacion de un AgregadorLineal que permite agregar las normalizadas 
     * VE de la simulacion del VAR para construir las de la optimizacion.
     * 
     * El vector V de variables de estado del VAR en la simulacion se forma por 
     * 
     * V(t) = [y(t-1)* |...|y(t-NA)* | x(t)*]*
     * 
     * El vector H de variables de estado en la optimizacion, resulta 
     * de los coeficientes defVAREst que son atributos de this
     * 
     * El operador * significa transpuesto de un vector.
     * 
     * 
     * La dimension de V es NA*N+NE donde 
     * NA es el orden del VAR
     * N es la cantidad de variables del VAR (excluyendo las exogenas)
     * NE es la cantidad de variables exogenas del VAR
     * 
     * La dimension de H es la cantidad de variables de estado del VAR en la optimizacion
     * 
     * La matriz de agregacion es S, tal que H=SV
     * 
     */     
    public double[][] construyeAgregadorLinealParaVAR(int ordenDelVAR, String[] nombresVariablesVAR,
    		String[] nombresVariablesExogenas){
    	int n = nombresVariablesVAR.length;   // dimension del VAR
    	int ne = nombresVariablesExogenas.length;  // cantidad de exogenas
    	int na = ordenDelVAR;
    	int dimV = na*n+ne;
    	int dimH = cantVEAg;
    	// Se halla una matriz S que luego se estandarizará produciendo otra matriz T
    	// para las VE agregadas tengan varianza 1
    	// La estandarizacion requiere usar Varianza(V) por lo que 
    	// se hace en el Estimador y no aquí
    	double[][] matAgS = new double[dimH][dimV];	
    	for(int ive=0; ive<cantVEAg; ive++){
    		String nomVE = nombresVEAgregadas[ive];
    		for(int p=1; p<=na; p++){
    			for(int in=0; in<n; in++){
    				String clave = nomVE + nombresVariablesVAR[in] + "-L" + p;
    				if(defVEAg.get(clave)!=null) matAgS[ive][(p-1)*n+in] = defVEAg.get(clave);
    			}
    		}
    		for(int ivx = 0; ivx<ne; ive++){
				String clave = nomVE + nombresVariablesExogenas[ivx] + 0;
				matAgS[ive][na*n+ivx] = defVEAg.get(clave);   			    			
    		}	
    	}    	
    	return matAgS;		
	}

	public int getCantSeries() {
		return cantSeries;
	}

	public void setCantSeries(int cantSeries) {
		this.cantSeries = cantSeries;
	}

	public String[] getNombresSeries() {
		return nombresSeries;
	}

	public void setNombresSeries(String[] nombresSeries) {
		this.nombresSeries = nombresSeries;
	}

	public String[] getNombresVEAgregadas() {
		return nombresVEAgregadas;
	}

	public void setNombresVEAgregadas(String[] nombresVEAgregadas) {
		this.nombresVEAgregadas = nombresVEAgregadas;
	}

	public int getCantVEAg() {
		return cantVEAg;
	}

	public void setCantVEAg(int cantVEAg) {
		this.cantVEAg = cantVEAg;
	}

	public double[][][] getDefVarEst() {
		return defVarEst;
	}

	public void setDefVarEst(double[][][] defVarEst) {
		this.defVarEst = defVarEst;
	}

	public int[][] getCantRezagos() {
		return cantRezagos;
	}

	public void setCantRezagos(int[][] cantRezagos) {
		this.cantRezagos = cantRezagos;
	}

	public boolean[][] getEstandarizada() {
		return estandarizada;
	}

	public void setEstandarizada(boolean[][] estandarizada) {
		this.estandarizada = estandarizada;
	}

	public Hashtable<String, Serie> getSeriesLeidas() {
		return seriesLeidas;
	}

	public void setSeriesLeidas(Hashtable<String, Serie> seriesLeidas) {
		this.seriesLeidas = seriesLeidas;
	}

	public String getNombreDurPaso() {
		return nombreDurPaso;
	}

	public void setNombreDurPaso(String nombreDurPaso) {
		this.nombreDurPaso = nombreDurPaso;
	}

	public Hashtable<String, Serie> getSeries01() {
		return series01;
	}

	public void setSeries01(Hashtable<String, Serie> series01) {
		this.series01 = series01;
	}

	public Hashtable<String, Serie> getSeries() {
		return series;
	}

	public void setSeries(Hashtable<String, Serie> series) {
		this.series = series;
	}

	public Hashtable<String, Serie> getVarsEstado() {
		return varsEstado;
	}

	public void setVarsEstado(Hashtable<String, Serie> varsEstado) {
		this.varsEstado = varsEstado;
	}

	public Hashtable<String, ArrayList<String>> getGruposDeVE() {
		return gruposDeVEAg;
	}

	public void setGruposDeVE(Hashtable<String, ArrayList<String>> gruposDeVE) {
		this.gruposDeVEAg = gruposDeVE;
	}

	public Hashtable<String, String> getGrupoDeVESerie() {
		return grupoDeVESerie;
	}

	public void setGrupoDeVESerie(Hashtable<String, String> grupoDeVESerie) {
		this.grupoDeVESerie = grupoDeVESerie;
	}

	public Hashtable<String, String> getOperadorEnVE() {
		return operadorEnVE;
	}

	public void setOperadorEnVE(Hashtable<String, String> operadorEnVE) {
		this.operadorEnVE = operadorEnVE;
	}

	public Hashtable<String, Double> getPonderadorDeGruposEnVE() {
		return ponderadorDeGruposEnVE;
	}

	public void setPonderadorDeGruposEnVE(Hashtable<String, Double> ponderadorDeGruposEnVE) {
		this.ponderadorDeGruposEnVE = ponderadorDeGruposEnVE;
	}

	public int[] getAnios() {
		return anios;
	}

	public void setAnios(int[] anios) {
		this.anios = anios;
	}

	public int[] getPasos() {
		return pasos;
	}

	public void setPasos(int[] pasos) {
		this.pasos = pasos;
	}

	public int getCantDatos() {
		return cantDatos;
	}

	public void setCantDatos(int cantDatos) {
		this.cantDatos = cantDatos;
	}	    	

	

}
