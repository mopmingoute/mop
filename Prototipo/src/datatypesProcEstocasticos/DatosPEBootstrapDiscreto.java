/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEBootstrapDiscreto is part of MOP.
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

package datatypesProcEstocasticos;

import java.util.ArrayList;

public class DatosPEBootstrapDiscreto {
	

	/**
	 * 
	 * ATENCIÓN:
	 * En esta clase se emplea la palabra hora como el intervalo para 
	 * el que hay un valor y la palabra d�a como el conjunto de horas que se muestrean todas
	 * juntas sucesivamente.
	 * Supongamos que se muestrean dias de 24 horas, con un valor por hora.
	 * En el lenguaje de la clase ProcesoEstocastico, el paso del PE ser�a la hora.
	 * 
	 * Si se muestreas en días con 48 medias horas, la "hora" sería la media hora
	 */ 

	private DatosGeneralesPE datGen;
	private String nombre;
	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String tipo;	
	private String ruta;	
	private String archDatos;
	private String nombrePasoPE;  // es el nombre del paso en el lenguaje de la clase ProcesoEstocastico
	// para el caso de muestrear d�as de 24 horas el paso del PE es la hora
	private int cantDiasDatos;
		
	private int cantHoras;  // cantidad de intervalos por paso
	private int durHora;   // duración del intervalo en segundos
	private int cantMaxDias;
	private String estimacionVE;   // identificaci�n de la estimación de las VE que se empleo
	private ArrayList<String>  nombresVA;   // nombres de las VA 
	private ArrayList<String>  nombresVE;   // nombres de las VE		
	private ArrayList<String> nombresVEOptim; // nombres de las VE de optimización
	private boolean varEstadoEnOptim; // si es false no tiene la VE estado compuesto en la optimizaci�n
	private int cantVA;   		// cantidad de variables aleatorias
	private int cantVE;			// cantidad de variables de estado
	private int cantVEOptim;	// cantidad de variables de estado de optimización
	
	
	/**
	 * Datos de las variables aleatorias observados hist�ricamente
	 * Primer �ndice: ordinal del paso, por ejemplo d�a, de cada dato hist�rico. Ejemplo el d�a 560 de los datos
	 * Segundo �ndice: poste dentro del paso por ejemplo hora.
	 * Tercer �ndice: variable aleatoria, por ejemplo factor e�lico, factor solar, etc.
	 */
//	private double[][][] datosHistoricos;
	
	
    private int cantEstadosComp; // cantidad de estados compuestos	
	
	/*
	 * Valor de la variable de estado estadoCompuesto para cada d�a de medidas de datosHistoricos
	 */
	private int[] estadosCompuestos;
	
	
	/**
	 * Poblaci�n de la que se extraen los sucesores en el bootstrap
	 * 
	 * Primer �ndice: d�a del a�o (Ejemplo d�as de 0 a 366)
	 * Segundo �ndice: estado discreto compuesto
	 * 
	 * El object es un ArrayList<Integer>
	 * Tercer �ndice sobre el ArrayList: observaciones para el d�a y estado discreto 
	 * 
	 * A PARTIR DE ESTE ATRIBUTO SE CREAN LAS REALIZACIONES DEL PE 
	 * SE TOMA EL Integer que apunta a datosHistoricos
	 *  
	 */
	private  Object[][] poblacionesSucesores;
	
	/**
	 * Probabilidades de los estados compuestos en cada "dia" del a�o
	 * primer �ndice dia del a�o
	 * segundo �ndice estado compuesto
	 */
	private double[][] probabilidadesEstadosCompuestos;
	
	
	/**
     * Ponderadores para construir las variable de estado parciales.
     * primer indice variable de estado parcial
     * segundo �ndice intervalo dentro del paso.
     * Las variables de estado se numeran del 0 en adelante.
     * 
     */    
    private double[][] ponderadores;
    
    
    /**
     * Probabilidades de las clases de cada VA
     */
    private double[][] probCla;
    
    /**
     * Cantidad de clases en la variable de estado continua para cada variable de datos;
     */
    private int[] cantCla;
    
    
    
	/**
	 * L�mites superiores de las clases de las variables de estado continuas
	 * primer �ndice: ordinal de dia del a�o
	 * segundo �ndice: ordinal de variable aleatoria
	 * tercer �ndice �ndice: ordinal de clase de la variable de estado continua
	 */
	private double[][][] limitesSupClases;	
    
	
	
	public DatosGeneralesPE getDatGen() {
		return datGen;
	}

	public void setDatGen(DatosGeneralesPE datGen) {
		this.datGen = datGen;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}

	public void setDiscretoExhaustivo(boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
	}

	

	public boolean isMuestreado() {
		return muestreado;
	}

	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public String getNombrePasoPE() {
		return nombrePasoPE;
	}

	public void setNombrePasoPE(String nombrePasoPE) {
		this.nombrePasoPE = nombrePasoPE;
	}

	public int getCantMaxDias() {
		return cantMaxDias;
	}

	public void setCantMaxDias(int cantMaxDias) {
		this.cantMaxDias = cantMaxDias;
	}

	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}

	public ArrayList<String> getNombresVA() {
		return nombresVA;
	}

	public void setNombresVA(ArrayList<String> nombresVA) {
		this.nombresVA = nombresVA;
	}

	public ArrayList<String> getNombresVE() {
		return nombresVE;
	}

	public void setNombresVE(ArrayList<String> nombresVE) {
		this.nombresVE = nombresVE;
	}

	public ArrayList<String> getNombresVEOptim() {
		return nombresVEOptim;
	}

	public void setNombresVEOptim(ArrayList<String> nombresVEOptim) {
		this.nombresVEOptim = nombresVEOptim;
	}
	
	

	public boolean isVarEstadoEnOptim() {
		return varEstadoEnOptim;
	}

	public void setVarEstadoEnOptim(boolean varEstadoEnOptim) {
		this.varEstadoEnOptim = varEstadoEnOptim;
	}

	public int getCantVA() {
		return cantVA;
	}

	public void setCantVA(int cantVA) {
		this.cantVA = cantVA;
	}

	public int getCantVE() {
		return cantVE;
	}

	public void setCantVE(int cantVE) {
		this.cantVE = cantVE;
	}

	public int getCantVEOptim() {
		return cantVEOptim;
	}

	public void setCantVEOptim(int cantVEOptim) {
		this.cantVEOptim = cantVEOptim;
	}

//	public double[][][] getDatosHistoricos() {
//		return datosHistoricos;
//	}
//
//	public void setDatosHistoricos(double[][][] datosHistoricos) {
//		this.datosHistoricos = datosHistoricos;
//	}

	public int getCantEstadosComp() {
		return cantEstadosComp;
	}

	public void setCantEstadosComp(int cantEstadosComp) {
		this.cantEstadosComp = cantEstadosComp;
	}

	public int[] getEstadosCompuestos() {
		return estadosCompuestos;
	}

	public void setEstadosCompuestos(int[] estadosCompuestos) {
		this.estadosCompuestos = estadosCompuestos;
	}

	public Object[][] getPoblacionesSucesores() {
		return poblacionesSucesores;
	}

	public void setPoblacionesSucesores(Object[][] poblacionesSucesores) {
		this.poblacionesSucesores = poblacionesSucesores;
	}
	

	public double[][] getProbabilidadesEstadosCompuestos() {
		return probabilidadesEstadosCompuestos;
	}

	public void setProbabilidadesEstadosCompuestos(double[][] probabilidadesEstadosCompuestos) {
		this.probabilidadesEstadosCompuestos = probabilidadesEstadosCompuestos;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}

	public int getDurHora() {
		return durHora;
	}

	public void setDurHora(int durHora) {
		this.durHora = durHora;
	}

	public double[][] getPonderadores() {
		return ponderadores;
	}

	public void setPonderadores(double[][] ponderadores) {
		this.ponderadores = ponderadores;
	}

	public int[] getCantCla() {
		return cantCla;
	}

	public void setCantCla(int[] cantCla) {
		this.cantCla = cantCla;
	}

	public double[][][] getLimitesSupClases() {
		return limitesSupClases;
	}

	public void setLimitesSupClases(double[][][] limitesSupClases) {
		this.limitesSupClases = limitesSupClases;
	}

	public String getArchDatos() {
		return archDatos;
	}

	public void setArchDatos(String archDatos) {
		this.archDatos = archDatos;
	}

	public int getCantDiasDatos() {
		return cantDiasDatos;
	}

	public void setCantDiasDatos(int cantDiasDatos) {
		this.cantDiasDatos = cantDiasDatos;
	}

	public double[][] getProbCla() {
		return probCla;
	}

	public void setProbCla(double[][] probCla) {
		this.probCla = probCla;
	}	
	
	
	

 

}

