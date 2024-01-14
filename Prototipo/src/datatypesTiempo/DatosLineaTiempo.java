/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosLineaTiempo is part of MOP.
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

package datatypesTiempo;

import java.io.Serializable;
import java.util.ArrayList;

import tiempo.SentidoTiempo;

/**
 * Datatype que representa la información de la lónea de tiempo
 * @author ut602614
 *
 */

public class DatosLineaTiempo implements Serializable{
	private static final long serialVersionUID = 1L;
	/*
	 * Representa el tiempo inicial que será parseado a GregorianCalendar
	 */
	private String tiempoInicial;
	private String tiempoInicialEvoluciones;
	private String tiempoFinal;
	
	private int instanteInicial;
	private SentidoTiempo sentido;
	private int cantBloques;
	private ArrayList<Integer> duracionPasoPorBloque;
	private ArrayList<Integer> periodoPasoPorBloque;
	private ArrayList<Boolean> cronologicos;
	private ArrayList<ArrayList<Integer>> durPostesPorBloque;
	private ArrayList<Integer> pasosPorBloque;
	private ArrayList<Integer> intMuestreoPorBloque; 
	private int periodoIntegracion;
	/*
	 * Toma el valor entero representado por las constantes Calendar.MONTH, Calendar.DAY, etc.
	 */

	private boolean usarPeriodoIntegracion; 
	
	public DatosLineaTiempo() {
		duracionPasoPorBloque = new ArrayList<Integer>();
		periodoPasoPorBloque = new ArrayList<Integer>();
		pasosPorBloque = new ArrayList<Integer>();
		intMuestreoPorBloque = new ArrayList<Integer>();
		durPostesPorBloque = new ArrayList<ArrayList<Integer>>();
		this.cronologicos = new ArrayList<Boolean>();
		cantBloques = 0;
		instanteInicial = 0;
		setSentido(new SentidoTiempo(-1));
	}
	
	public DatosLineaTiempo(int instanteInicial, int cantBloques,
			ArrayList<Integer> duracionPasoPorBloque,
			ArrayList<Integer> pasosPorBloque, int periodoIntegracion, boolean usarPeriodoIntegracion) {
		super();
		this.instanteInicial = instanteInicial;
		this.cantBloques = cantBloques;
		this.duracionPasoPorBloque = duracionPasoPorBloque;
		this.pasosPorBloque = pasosPorBloque;
		this.usarPeriodoIntegracion = usarPeriodoIntegracion;
		this.cronologicos = new ArrayList<Boolean>();
		setSentido(new SentidoTiempo(-1));
	}
	
	
	public int getInstanteInicial() {
		return instanteInicial;
	}
	public void setInstanteInicial(int instanteInicial) {
		this.instanteInicial = instanteInicial;
	}
	public int getCantBloques() {
		return cantBloques;
	}
	public void setCantBloques(int cantBloques) {
		this.cantBloques = cantBloques;
	}
	public ArrayList<Integer> getDuracionPasoPorBloque() {
		return duracionPasoPorBloque;
	}
	public void setDuracionPasoPorBloque(ArrayList<Integer> duracionPasoPorBloque) {
		this.duracionPasoPorBloque = duracionPasoPorBloque;
	}
	public ArrayList<Integer> getPasosPorBloque() {
		return pasosPorBloque;
	}
	public void setPasosPorBloque(ArrayList<Integer> pasosPorBloque) {
		this.pasosPorBloque = pasosPorBloque;
	}


	public String getTiempoInicial() {
		return tiempoInicial;
	}


	public void setTiempoInicial(String tiempoInicial) {
		this.tiempoInicial = tiempoInicial;
	}

	public String getTiempoInicialEvoluciones() {
		return tiempoInicialEvoluciones;
	}

	public void setTiempoInicialEvoluciones(String tiempoInicialEvoluciones) {
		this.tiempoInicialEvoluciones = tiempoInicialEvoluciones;
	}


	public int getPeriodoIntegracion() {
		return periodoIntegracion;
	}

	public ArrayList<Integer> getIntMuestreoPorBloque() {
		return intMuestreoPorBloque;
	}

	public void setIntMuestreoPorBloque(ArrayList<Integer> intMuestreoPorBloque) {
		this.intMuestreoPorBloque = intMuestreoPorBloque;
	}


	public void setPeriodoIntegracion(int periodoIntegracion) {
		this.periodoIntegracion = periodoIntegracion;
	}


	public boolean isUsarPeriodoIntegracion() {
		return usarPeriodoIntegracion;
	}


	public void setUsarPeriodoIntegracion(boolean usarPeriodoIntegracion) {
		this.usarPeriodoIntegracion = usarPeriodoIntegracion;
	}


	public ArrayList<Integer> getPeriodoPasoPorBloque() {
		return periodoPasoPorBloque;
	}


	public void setPeriodoPasoPorBloque(ArrayList<Integer> periodoPasoPorBloque) {
		this.periodoPasoPorBloque = periodoPasoPorBloque;
	}

	public void agregarBloque(String cantPasos, String durPaso,	String intervaloMuestreo, String cantPostes, ArrayList<Integer> durPos, String periodoBloque, String cronologico) {
		this.duracionPasoPorBloque.add(Integer.parseInt(durPaso));
		this.intMuestreoPorBloque.add(Integer.parseInt(intervaloMuestreo));
		this.pasosPorBloque.add(Integer.parseInt(cantPasos));
		this.periodoPasoPorBloque.add(Integer.parseInt(periodoBloque));
		this.durPostesPorBloque.add(durPos);		
		this.cronologicos.add(Boolean.parseBoolean(cronologico));			
		cantBloques++;
	}

	public void agregarBloqueDatosInt(Integer cantPasos, Integer durPaso,	Integer intervaloMuestreo, Integer cantPostes, ArrayList<Integer> durPos, Integer periodoBloque, Boolean cronologico) {
		this.duracionPasoPorBloque.add(durPaso);
		this.intMuestreoPorBloque.add(intervaloMuestreo);
		this.pasosPorBloque.add(cantPasos);
		this.periodoPasoPorBloque.add(periodoBloque);
		this.durPostesPorBloque.add(durPos);
		this.cronologicos.add(cronologico);
		cantBloques++;
	}

	public ArrayList<ArrayList<Integer>> getDurPostesPorBloque() {
		return durPostesPorBloque;
	}

	public void setDurPostesPorBloque(ArrayList<ArrayList<Integer>> durPostesPorBloque) {
		this.durPostesPorBloque = durPostesPorBloque;
	}

	public SentidoTiempo getSentido() {
		return sentido;
	}

	public void setSentido(SentidoTiempo sentido) {
		this.sentido = sentido;
	}

	public String getTiempoFinal() {
		return tiempoFinal;
	}

	public void setTiempoFinal(String tiempoFinal) {
		this.tiempoFinal = tiempoFinal;
	}

	public ArrayList<Boolean> getCronologicos() {
		return cronologicos;
	}

	public void setCronologicos(ArrayList<Boolean> cronologicos) {
		this.cronologicos = cronologicos;
	}


	public void print(){
		for(int i=0;i<cantBloques;i++) {
			System.out.println("=================================================================");
			System.out.println("Bloque: "+(i+1));
			System.out.println("-----------------------------------------------------------------");
			System.out.println("Cant Pasos: "+pasosPorBloque.get(i));
			System.out.println("Dur Pasos: "+duracionPasoPorBloque.get(i));
			System.out.println("Intervalo Muestreo: "+intMuestreoPorBloque.get(i));
			System.out.println("Cronologico: "+cronologicos.get(i));
			System.out.println("Cant Postes: "+durPostesPorBloque.get(i).size());
			System.out.println("Dur Postes: "+durPostesPorBloque.get(i));
		}
	}

	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();

		if(tiempoInicial.trim().equals("")) { errores.add("Linea de tiempo: tiempoInicial vacío."); }
		if(tiempoFinal.trim().equals("")) { errores.add("Linea de tiempo: tiempoFinal vacío."); }
		// if(instanteInicial == 0 ) { errores.add("Combustible: instanteInicial vacío."); }
		if(sentido == null ) { errores.add("Linea de tiempo: sentido vacío."); }
		if(cantBloques == 0 ) { errores.add("Linea de tiempo: cantBloques vacío."); }

		if(duracionPasoPorBloque == null){ errores.add("Linea de tiempo: duracionPasoPorBloque vacío."); }
		if(periodoPasoPorBloque == null){ errores.add("Linea de tiempo: periodoPasoPorBloque vacío."); }
		if(cronologicos == null){ errores.add("Linea de tiempo: cronologicos vacío."); }
		if(durPostesPorBloque == null){ errores.add("Linea de tiempo: durPostesPorBloque vacío."); }
		if(pasosPorBloque == null){ errores.add("Linea de tiempo: pasosPorBloque vacío."); }
		if(intMuestreoPorBloque == null){ errores.add("Linea de tiempo: intMuestreoPorBloque vacío."); }

		//if(periodoIntegracion == 0 ) { errores.add("Combustible: periodoIntegracion vacío."); }

		return errores;
    }
}
