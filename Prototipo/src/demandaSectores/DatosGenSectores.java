/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosGenSectores is part of MOP.
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

package demandaSectores;

import java.util.HashSet;
import java.util.Hashtable;

import utilitarios.Par;

public class DatosGenSectores {
	
	/**
	 * El tipo de día es un entero mayor que cero.
	 */
	private int anioGen; // año de los datos de generación de recursos
	private int anioFac; // año de los datos de facturación mensual de los sectores
	private int cantTiposDia; // cantidad de tipos de día, el tipo de día 1 es la base, típicamente el día hábil de la semana
	private int[] tiposDiasSemana; // tipos de día de los días 7 de la semana empezando en DOMINGO=1
	private int cantHoras; // cantidad de horas, normalmente 24 del 0 al 23
	private int cantEstac;  // cantidad de estaciones del año
	private int[] defEstac; // para cada semana del año, el número de la estación a la que pertenece a partir de 1 en el orden de nombresEstac 
	private int[] defEstacMeses; // para cada mes del año, el número de la estación a la que pertenece a partir de 1 en el orden de nombresEstac 
	private String[] nombresEstac;
	private int cantSectores;
	private String[] nombresSectores;
	private int cantCronicas; 
	
	/**
	 * Tabla que contiene todos los días especiales del horizonte en el
	 * que podrán ser invocadas realizaciones del proceso
	 * Los días especiales son los feriados móviles del año, por ejemplo lunes y martes
	 * de carnaval, jueves y viernes de Semana Santa, o los feriados que cambian de fecha.
	 * 
	 * La clave Integer es un código: año*10000+mes*100+dia
	 * 
	 * El valor es el número de tipo de día mayor que cero
	 * Así por ejemplo una entrada puede ser (20170410, 2)
	 */
	Hashtable<Integer, Integer> diasEspecialesHorizonte;
	
	/**
	 * Lista de los feriados comunes a todos los años, que no cambian de fecha
	 * 
	 * Clave
	 * -Primer elemento del par: índice del mes empezando en 1 hasta 12
	 * -Segundo elemento del par: índice del día del mes empezando en 1 hasta 31.
	 * 
	 * Valor el tipo de día del feriado
	 * 
	 */
	private Hashtable<Par, Integer> feriadosComunes;




	public int getAnioGen() {
		return anioGen;
	}

	public void setAnioGen(int anioGen) {
		this.anioGen = anioGen;
	}

	public int getAnioFac() {
		return anioFac;
	}

	public void setAnioFac(int anioFac) {
		this.anioFac = anioFac;
	}

	public int getCantTiposDia() {
		return cantTiposDia;
	}

	public void setCantTiposDia(int cantTiposDia) {
		this.cantTiposDia = cantTiposDia;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}

	public int getCantEstac() {
		return cantEstac;
	}

	public void setCantEstac(int cantEstac) {
		this.cantEstac = cantEstac;
	}

	public int[] getDefEstac() {
		return defEstac;
	}
	

	public int[] getDefEstacMeses() {
		return defEstacMeses;
	}

	public void setDefEstacMeses(int[] defEstacMeses) {
		this.defEstacMeses = defEstacMeses;
	}

	public void setDefEstac(int[] defEstac) {
		this.defEstac = defEstac;
	}

	public String[] getNombresEstac() {
		return nombresEstac;
	}

	public void setNombresEstac(String[] nombresEstac) {
		this.nombresEstac = nombresEstac;
	}

	public int getCantSectores() {
		return cantSectores;
	}

	public void setCantSectores(int cantSectores) {
		this.cantSectores = cantSectores;
	}

	public String[] getNombresSectores() {
		return nombresSectores;
	}

	public void setNombresSectores(String[] nombresSectores) {
		this.nombresSectores = nombresSectores;
	}

	public int[] getTiposDiasSemana() {
		return tiposDiasSemana;
	}

	public void setTiposDiasSemana(int[] tiposDiasSemana) {
		this.tiposDiasSemana = tiposDiasSemana;
	}

	public Hashtable<Integer, Integer> getDiasEspecialesHorizonte() {
		return diasEspecialesHorizonte;
	}

	public void setDiasEspecialesHorizonte(Hashtable<Integer, Integer> diasEspecialesHorizonte) {
		this.diasEspecialesHorizonte = diasEspecialesHorizonte;
	}

	public Hashtable<Par, Integer> getFeriadosComunes() {
		return feriadosComunes;
	}

	public void setFeriadosComunes(Hashtable<Par, Integer> feriadosComunes) {
		this.feriadosComunes = feriadosComunes;
	}


	public int getCantCronicas() {
		return cantCronicas;
	}

	public void setCantCronicas(int cantCronicas) {
		this.cantCronicas = cantCronicas;
	}


	
	
	
	
}
