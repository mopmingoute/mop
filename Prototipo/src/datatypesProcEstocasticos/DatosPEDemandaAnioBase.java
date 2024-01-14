/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEDemandaAnioBase is part of MOP.
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
import java.util.HashSet;
import java.util.Hashtable;

import datatypesTiempo.DatosTiposDeDia;
import utilitarios.Par;

public class DatosPEDemandaAnioBase {

	private DatosGeneralesPE datGen;
	private String nombre;
	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String tipo;
	private String ruta;
	private String archPotencias; // nombre del archivo de potencias en años base

	private String nombrePasoPE; // es el nombre del paso en el lenguaje de la clase ProcesoEstocastico
	// para el caso de muestrear días de 24 horas el paso del PE es la hora

	private boolean sumaVar; // se genera una única variable con el nombre contenido en nombre_var_suma
	private String nombre_var_suma;

	private String estimacionVE; // identificación de la estimación de las VE que se empleó
	private ArrayList<String> nombresVA; // nombres de las VA
	private ArrayList<String> nombresVE; // nombres de las VE
	private ArrayList<String> nombresVEOptim; // nombres de las VE de optimización
	private boolean varEstadoEnOptim = false; // no tiene la VE estado compuesto en la optimización
	private int cantVA = 1; // cantidad de variables aleatorias
	private int cantVE = 0; // cantidad de variables de estado
	private int cantVEOptim = 0; // cantidad de variables de estado de optimización

	private int anioInicialHorizonte;
	private int anioFinalHorizonte;

	/**
	 * Energías relativas leídas de los distintos años que sirven para incrementar
	 * la potencia del año base en el cociente, para cada una de las variables de
	 * demanda, en el factor (energía(año t, demanda k)/energía(año base, demanda
	 * k))
	 */
	private Hashtable<Integer, double[]> energias;

	private boolean ajusteEnergiasAnuales;

	/**
	 * Lista de los feriados comunes a todos los años, que no cambian de fecha
	 * HashSet: Clave -Primer elemento del par: índice del mes empezando en 1 hasta
	 * 12 -Segundo elemento del par: índice del día del mes empezando en 1 hasta 31.
	 * 
	 */
	private HashSet<Par> feriadosComunes;

	/**
	 * 
	 * Potencias de los años base Clave del hashtable el año base, por ejemplo 2016.
	 * Valor: primer índice recorre días del año base segundo índice recorre "horas"
	 * del año base (podrían ser medias horas, etc.) tercer índice recorre variable
	 * de demanda (por ejemplo de distintos puntos)
	 */
	private Hashtable<Integer, double[][][]> potenciasAniosBase;

	/**
	 * El paso del proceso estocástico es la "hora". En adelante las llamamos
	 * simplemente horas. Los días son siempre los días del año.
	 */
	private int cantHoras; // cantidad de horas de un día.

	private int[] aniosBase; // lista de los años base

	private int anioBaseElegido;

	private DatosTiposDeDia tiposDeDia;



	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public DatosGeneralesPE getDatGen() {
		return datGen;
	}

	public void setDatGen(DatosGeneralesPE datGen) {
		this.datGen = datGen;
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

	public String getArchPotencias() {
		return archPotencias;
	}

	public void setArchPotencias(String archPotencias) {
		this.archPotencias = archPotencias;
	}

	public String getNombrePasoPE() {
		return nombrePasoPE;
	}

	public void setNombrePasoPE(String nombrePasoPE) {
		this.nombrePasoPE = nombrePasoPE;
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

	public Hashtable<Integer, double[][][]> getPotenciasAniosBase() {
		return potenciasAniosBase;
	}

	public void setPotenciasAniosBase(Hashtable<Integer, double[][][]> potenciasAniosBase) {
		this.potenciasAniosBase = potenciasAniosBase;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}


	public int[] getAniosBase() {
		return aniosBase;
	}

	public void setAniosBase(int[] aniosBase) {
		this.aniosBase = aniosBase;
	}

	public int getAnioInicialHorizonte() {
		return anioInicialHorizonte;
	}

	public void setAnioInicialHorizonte(int anioInicialHorizonte) {
		this.anioInicialHorizonte = anioInicialHorizonte;
	}

	public int getAnioFinalHorizonte() {
		return anioFinalHorizonte;
	}

	public void setAnioFinalHorizonte(int anioFinalHorizonte) {
		this.anioFinalHorizonte = anioFinalHorizonte;
	}

	public Hashtable<Integer, double[]> getEnergias() {
		return energias;
	}

	public void setEnergias(Hashtable<Integer, double[]> energias) {
		this.energias = energias;
	}

	public boolean isAjusteEnergiasAnuales() {
		return ajusteEnergiasAnuales;
	}

	public void setAjusteEnergiasAnuales(boolean ajusteEnergiasAnuales) {
		this.ajusteEnergiasAnuales = ajusteEnergiasAnuales;
	}

	public HashSet<Par> getFeriadosComunes() {
		return feriadosComunes;
	}

	public void setFeriadosComunes(HashSet<Par> feriadosComunes) {
		this.feriadosComunes = feriadosComunes;
	}

	public int getAnioBaseElegido() {
		return anioBaseElegido;
	}

	public void setAnioBaseElegido(int anioBaseElegido) {
		this.anioBaseElegido = anioBaseElegido;
	}


	public boolean isSumaVar() {
		return sumaVar;
	}

	public void setSumaVar(boolean sumaVar) {
		this.sumaVar = sumaVar;
	}

	public String getNombre_var_suma() {
		return nombre_var_suma;
	}

	public void setNombre_var_suma(String nombre_var_suma) {
		this.nombre_var_suma = nombre_var_suma;
	}

	public DatosTiposDeDia getTiposDeDia() {
		return tiposDeDia;
	}

	public void setTiposDeDia(DatosTiposDeDia tiposDeDia) {
		this.tiposDeDia = tiposDeDia;
	}

	
}
