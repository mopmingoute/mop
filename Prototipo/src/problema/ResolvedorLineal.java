/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResolvedorLineal is part of MOP.
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

package problema;

import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosSalidaProblemaLineal;

/**
 * Interfaz que contiene los mótodos de resolución del problema lineal
 * @author ut602614
 *
 */

public interface ResolvedorLineal {

	DatosSalidaProblemaLineal resolver(DatosEntradaProblemaLineal entrada, DatosEntradaProblemaLineal actualizado, int escenario, int paso, String dirInfactible, String dirSalidaLP);
	
	

}
