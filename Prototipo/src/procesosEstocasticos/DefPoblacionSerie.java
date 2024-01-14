/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DefPoblacionSerie is part of MOP.
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

/**
 * Define las poblaciones de una series en cada paso de tiempo, que se usan por ejemplo
 * en las transformaciones de normalizacion
 * @author ut469262
 * 
 * Los atributos de la definicion son:
 *  radioEntorno
 *  periodo
 *  cantPeriodos, si es par incluso 0 se toma el impar inmediato mayor.
 *  
 * EJEMPLO:
 * 1  2  3  4  5  6  7  …….24 25 26 27 ……….48 49 50 51 
 * Para la observación 25, si se toma radioEntorno =1, periodo=24, cantPeriodos=3, las observaciones que constituyen una población son la 1,2,3, 24,25,26,48,49,50.
 * Como radioEntorno=1, se toma la observación 24 y 26 además de la 25. 
 * periodo es la cantidad de observaciones entre el centro de dos entornos sucesivos.
 * Como cantPeriodos=3, se toman entornos de observaciones en tres períodos, el que contiene la observación 25, el anterior y el posterior.
 */

public class DefPoblacionSerie {
	
	private int radioEntorno;
	private int periodo;
	private int cantPeriodos;
	
	public DefPoblacionSerie(int radioEntorno, int periodo, int cantPeriodos) {
		super();
		this.radioEntorno = radioEntorno;
		this.periodo = periodo;
		this.cantPeriodos = cantPeriodos;
	}
	
	/**
	 * Construye a partir de la lista de Strings leidos con los parametros
	 * @param al
	 */
	public DefPoblacionSerie(ArrayList<String> al) {
		super();
		this.radioEntorno = Integer.parseInt(al.get(0));
		this.periodo = Integer.parseInt(al.get(1));
		this.cantPeriodos = Integer.parseInt(al.get(2));
	}

	public int getRadioEntorno() {
		return radioEntorno;
	}

	public void setRadioEntorno(int radioEntorno) {
		this.radioEntorno = radioEntorno;
	}

	public int getPeriodo() {
		return periodo;
	}

	public void setPeriodo(int periodo) {
		this.periodo = periodo;
	}

	public int getCantPeriodos() {
		return cantPeriodos;
	}

	public void setCantPeriodos(int cantPeriodos) {
		this.cantPeriodos = cantPeriodos;
	}
	
	

}
