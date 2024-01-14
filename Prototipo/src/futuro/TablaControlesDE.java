/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaControlesDE is part of MOP.
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

package futuro;

import java.io.Serializable;

public abstract class TablaControlesDE implements Serializable{
	private int cantPasos;

	
	public int getCantPasos() {
		return cantPasos;
	}


	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}


	
	
	/**
	 * Devuelve el código del control discreto exhaustivo óptimo dado el código de estado
	 * ATENCIóN: EL CóDIGO DE ESTADO CORRESPONDE AL ESTADO DESPUóS DEL SALTO
	 * DE LAS VARIABLES DE ESTADO DE LOS PROCESOS DISCRETOS EXHAUSTIVOS
	 * 
	 * @param paso el paso empezando en 0
	 * @param sInit estado inicial en el paso paso, para el que se pide el control
	 * 
	 * @return el código entero de controles óptimos
	 */
	public abstract int[] devuelveCodigoControlesDEOpt(int paso, int[] sInit);
	
	
	/**
	 * Carga el código del control DE óptimo para un código de estado dado.
	 * 
	 * @param paso el paso empezando en 0
	 * @param sInit estado inicial para el que se guarda el control
	 * @param codigoOpt es el código discreto de los controles óptimos para el estado sInit
	 */
	public abstract void cargaCodigoControlesDEOpt(int paso, int[] sInit, int[] codigoOpt);
	
	


	public abstract void cargaTabla(int paso);


	public abstract void devuelveTabla(int paso, int cantPaquetes, int cantEstados);


	public abstract void devuelveTablaEnteraDE(int paso);
	

}
