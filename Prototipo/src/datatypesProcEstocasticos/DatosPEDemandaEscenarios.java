/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEDemandaEscenarios is part of MOP.
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
import java.util.Hashtable;

import datatypesTiempo.DatosTiposDeDia;

public class DatosPEDemandaEscenarios {
	
	private DatosGeneralesPE datGen;
	private String nombre;
	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String tipo;	
	private String ruta;
	
	private String estimacionVE;   // identificación de la estimación de las VE que se empleó
	private ArrayList<String>  nombresVA;   // nombres de las VA 
	
	private boolean sumaVar; // se genera una única variable con el nombre contenido en nombre_var_suma
	private String nombre_var_suma;
	
	private ArrayList<String>  nombresVE;   // nombres de las VE		
	private ArrayList<String> nombresVEOptim; // nombres de las VE de optimización
	private boolean varEstadoEnOptim = false; // no tiene la VE estado compuesto en la optimización
	private int cantVA;   		// cantidad de variables aleatorias, en la opción suma de variables es 1
	private int cantVarLeidas;
	
	private int[] cantEscTot;  // cantidad de escenarios totales
	private int[] cantEsc;   // Se usan los primeros cantEsc escenarios de los totales
	
	private int anioBaseInicial;   // año del primer paso de los escenarios, ejemplo: 2016.
	private int anioBaseFinal;   // año del primer paso de los escenarios, ejemplo: 2016.

	private int anioSimInicial;
	private int anioSimFinal;
	private boolean corrigeTiposDia;  // False: elige el dia de igual ordinal; True: elige el día del mismo tipo más cercano en ordinal
	
	/**
	 * clave: nombre de la VA
	 * 
	 * Valor:
	 * primer índice año a partir de anioBaseInicial
	 * segundo índice escenario 
	 * tercer índice recorre el ordinal del paso en el año, por ejemplo las semanas, horas, etc.
	 * 
	 */
	private static Hashtable<String, double[][][]>  potencias;  // potencias en MW
	
	
	/**
	 * Las energias esperadas del proceso en años con realizaciones para cada variable aleatoria y año
	 * clave:  String: nombre de la V.A + String(año)
	 * valor: energía esperada
	 */
	private Hashtable<String, Double> energias; 
	
	/**
	 * Para cada año, la energía esperada de la demanda en el año.
	 * Las energias esperadas del proceso en años con realizaciones para cada variable aleatoria y año
	 * clave:  String: nombre de la V.A + String(año)
	 * valor: energía esperada.
	 */	
	private Hashtable<String, Double> energiaEsperadaDatos;
	
	private DatosTiposDeDia tiposDeDia;
	

//	public DatosPEDemandaEscenarios(int[] cantEscTot, int cantEsc[], ArrayList<String> nombresVA, int anioBaseInicial, int anioBaseFinal, int anioSimInicial, int anioSimFinal, int pasoInicialPE,
//			double[][][][] datos, Hashtable<String, Double> energiaEsperadaDatos, boolean corrigeTiposDia, DatosTiposDeDia tiposDeDia) {
//		super();
//		this.cantEscTot = cantEscTot;
//		this.cantEsc = cantEsc;
//		this.anioBaseInicial = anioBaseInicial;
//		this.anioBaseFinal = anioBaseFinal;
//		this.anioSimInicial = anioSimInicial;
//		this.anioSimFinal = anioSimFinal;
//		this.energiaEsperadaDatos = energiaEsperadaDatos;
//		this.corrigeTiposDia = corrigeTiposDia;
//		this.tiposDeDia = tiposDeDia;
//		this.nombresVA = nombresVA;
//		 
//	}

	
	
	
	public DatosTiposDeDia getTiposDeDia() {
		return tiposDeDia;
	}




	public void setTiposDeDia(DatosTiposDeDia tiposDeDia) {
		this.tiposDeDia = tiposDeDia;
	}




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


	public boolean isCorrigeTiposDia() {
		return corrigeTiposDia;
	}


	public void setCorrigeTiposDia(boolean corrigeTiposDia) {
		this.corrigeTiposDia = corrigeTiposDia;
	}




	public void setRuta(String ruta) {
		this.ruta = ruta;
	}




	public DatosPEDemandaEscenarios() {
		// TODO Auto-generated constructor stub
	}




	public int[] getCantEscTot() {
		return cantEscTot;
	}




	public void setCantEscTot(int[] cantEscTot) {
		this.cantEscTot = cantEscTot;
	}




	public int[] getCantEsc() {
		return cantEsc;
	}




	public void setCantEsc(int[] cantEsc) {
		this.cantEsc = cantEsc;
	}




	public int getAnioBaseInicial() {
		return anioBaseInicial;
	}


	public void setAnioBaseInicial(int anioBaseInicial) {
		this.anioBaseInicial = anioBaseInicial;
	}


	public int getAnioBaseFinal() {
		return anioBaseFinal;
	}


	public void setAnioBaseFinal(int anioBaseFinal) {
		this.anioBaseFinal = anioBaseFinal;
	}


	public int getAnioSimInicial() {
		return anioSimInicial;
	}


	public void setAnioSimInicial(int anioSimInicial) {
		this.anioSimInicial = anioSimInicial;
	}


	public int getAnioSimFinal() {
		return anioSimFinal;
	}


	public void setAnioSimFinal(int anioSimFinal) {
		this.anioSimFinal = anioSimFinal;
	}


	public Hashtable<String, double[][][]> getPotencias() {
		return potencias;
	}

	public void setPotencias(Hashtable<String, double[][][]> potencias) {
		DatosPEDemandaEscenarios.potencias = potencias;
	}


	public Hashtable<String, Double> getEnergias() {
		return energias;
	}


	public void setEnergias(Hashtable<String, Double> energias) {
		this.energias = energias;
	}


	public Hashtable<String, Double> getEnergiaEsperadaDatos() {
		return energiaEsperadaDatos;
	}


	public void setEnergiaEsperadaDatos(Hashtable<String, Double> energiaEsperadaDatos) {
		this.energiaEsperadaDatos = energiaEsperadaDatos;
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



	public int getCantVarLeidas() {
		return cantVarLeidas;
	}




	public void setCantVarLeidas(int cantVarLeidas) {
		this.cantVarLeidas = cantVarLeidas;
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

	

}
