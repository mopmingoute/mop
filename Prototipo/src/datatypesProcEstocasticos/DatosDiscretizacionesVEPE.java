/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDiscretizacionesVEPE is part of MOP.
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

import java.util.Hashtable;

import datatypes.DatosDiscretizacion;

/**
 * Datos de las discretizaciones de las VE continuas de procesos estocásticos
 * @author ut469262
 *
 */

public class DatosDiscretizacionesVEPE {

	// clave el nombre de la variable de estado, valor los datos de su discretizacion
	Hashtable<String, DatosDiscretizacion> discretizaciones;
	
	
	public DatosDiscretizacionesVEPE(){
		discretizaciones = new Hashtable<String, DatosDiscretizacion>(); 
	}
	
	public DatosDiscretizacion datosDiscDeVE(String nombreVE){
		return discretizaciones.get(nombreVE);
	}

	public void cargaDatosDisc(String nombreVE, DatosDiscretizacion dat){
		discretizaciones.put(nombreVE, dat);
	}
}
