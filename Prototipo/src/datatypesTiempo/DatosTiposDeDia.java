/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTiposDeDia is part of MOP.
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

import java.util.Hashtable;

import utilitarios.Par;

/**
 * Guarda la información sobre tipos de día aplicable a la demanda para un
 * horizonte de tiempo. Se considera un año base, por ejemplo 2020 y para cada
 * día de un horizonte de años consecutivos se determina el día más parecido del
 * año base, considerando el ordinal del día en el año y el tipo de día.
 * 
 * @author ut469262
 *
 */

public class DatosTiposDeDia {

	private int anioIniHorizonte; // primer año del horizonte

	private int anioFinHorizonte; // último año del horizonte

	private int anioBase; // Año base elegido

	/**
	 * Un entero para cada uno de los 7 días de la semana empezando en domingo Por
	 * ejemplo 3 1 1 1 1 1 2, quiere decir que los domingos son día tipo 3, los
	 * sábados tipo 2 y los días hábiles tipo 1.
	 */
	private int[] tiposDiasSemana;

	/**
	 * Lista de los feriados comunes a todos los años, que no cambian de fecha
	 * HashSet: Clave String con
	 * índice del mes empezando en 1 hasta 12 + "_" + índice del día del mes empezando en 1 hasta 31.
	 * la clave se crea con el método CargadorTiposDeDia.claveTiposDeDia
	 * 
	 * Valor: el Par de int (mes, dia del mes)
	 * 
	 */
	private Hashtable<String, Par> feriadosComunes;

	/**
	 * Tabla que contiene el ordinal en el año base empezando en cero de cada día
	 * especial, dado por su nombre, ejemplo "lunes_de_turismo"
	 * 
	 * Clave: año base + nombre del día especial
	 * 
	 * Valor: ordinal en el año base
	 */
	private Hashtable<String, Integer> ordinalDiasEspecialesEnAnioBase;

	/**
	 * Tabla que contiene el tipo de día de cada día especial, dado por su nombre,
	 * ejemplo "lunes_de_turismo"
	 * 
	 * Clave: nombre del día especial
	 * 
	 * Valor: tipo de día del día especial tomado de los tipos de día de los días de la semana
	 * es decir los valores que toma tiposDiasSemana
	 */
	private Hashtable<String, Integer> tipoDiaDiasEspeciales;

	/**
	 * Tabla que contiene todos los días especiales del horizonte en el que podrán
	 * buscarse los tipos de día y también del año base. Ej. desde 2016 hasta 2063.
	 * Los días especiales son los feriados móviles del año, por ejemplo lunes y
	 * martes de carnaval, jueves y viernes de Semana Santa, o los feriados que
	 * cambian de fecha.
	 * 
	 * La clave Integer es un código: año*10000+mes*100+dia
	 * 
	 * El valor String es una denominación de día especial, por ejemplo "viernes
	 * santo"; "lunes de turismo" Así por ejemplo una entrada puede ser (20170410,
	 * "lunes de turismo")
	 */
	Hashtable<Integer, String> diasEspecialesHorizonte;

	/**
	 * Clave para entrar ver si un día es un día especial
	 * 
	 * @param anio
	 * @param mes
	 * @param dia
	 */
	public int claveDiasEspeciales(int anio, int mes, int dia) {
		return anio * 10000 + mes * 100 + dia;
	}

	public int getAnioIniHorizonte() {
		return anioIniHorizonte;
	}

	public void setAnioIniHorizonte(int anioIniHorizonte) {
		this.anioIniHorizonte = anioIniHorizonte;
	}

	public int getAnioFinHorizonte() {
		return anioFinHorizonte;
	}

	public void setAnioFinHorizonte(int anioFinHorizonte) {
		this.anioFinHorizonte = anioFinHorizonte;
	}

	public int getAnioBase() {
		return anioBase;
	}

	public void setAnioBase(int anioBase) {
		this.anioBase = anioBase;
	}

	public int[] getTiposDiasSemana() {
		return tiposDiasSemana;
	}

	public void setTiposDiasSemana(int[] tiposDiasSemana) {
		this.tiposDiasSemana = tiposDiasSemana;
	}



	public Hashtable<String, Integer> getOrdinalDiasEspecialesEnAnioBase() {
		return ordinalDiasEspecialesEnAnioBase;
	}

	public void setOrdinalDiasEspecialesEnAnioBase(Hashtable<String, Integer> ordinalDiasEspecialesEnAnioBase) {
		this.ordinalDiasEspecialesEnAnioBase = ordinalDiasEspecialesEnAnioBase;
	}

	public Hashtable<Integer, String> getDiasEspecialesHorizonte() {
		return diasEspecialesHorizonte;
	}

	public void setDiasEspecialesHorizonte(Hashtable<Integer, String> diasEspecialesHorizonte) {
		this.diasEspecialesHorizonte = diasEspecialesHorizonte;
	}



	public Hashtable<String, Par> getFeriadosComunes() {
		return feriadosComunes;
	}

	public void setFeriadosComunes(Hashtable<String, Par> feriadosComunes) {
		this.feriadosComunes = feriadosComunes;
	}

	public Hashtable<String, Integer> getTipoDiaDiasEspeciales() {
		return tipoDiaDiasEspeciales;
	}

	public void setTipoDiaDiasEspeciales(Hashtable<String, Integer> tipoDiaDiasEspeciales) {
		this.tipoDiaDiasEspeciales = tipoDiaDiasEspeciales;
	}


}
