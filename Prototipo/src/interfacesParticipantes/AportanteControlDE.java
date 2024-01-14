/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AportanteControlDE is part of MOP.
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

package interfacesParticipantes;

import java.util.ArrayList;

import control.VariableControlDE;



public interface AportanteControlDE {

	/**
	 * Carga en la colección del participante las variables de control DE que se usan en el paso corriente
	 * de la simulación y de la optimización
	 * 	
	 */	
	public void actualizarVarsControlDE();
		
	
	/**
	 * Devuelve las variables de control DE que usa el participante en el paso corriente
	 * de la simulación y de la optimización
	 * @return
	 */
	public ArrayList<VariableControlDE> aportarVarsControlDE();	
	
	/**
	 * Carga en cada variable de control DE del AportanteControlDE, la lista de óndices en su discretización de los 
	 * controles factibles de esa variable.
	 * Si el AportanteControlDe tiene variables de estado, los controles DE factibles pueden depender
	 * de ese estado.
	 * Por ejemplo: la totalidad de los controles de un participante son:
	 * 0-encender, 1-apagar, 2-dejar apagado, 3-dejar encendido. 
	 * En el estado apagado los controles factibles son 0-encender y 2-dejar apagado
	 * el mótodo carga 0, 3 en los controles factibles de la variable.
	 * vc una variable de control del AportanteControlDE
	 * 
	 */
	public void cargaControlesDEFactibles();
	
}
