/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEEvolucion is part of MOP.
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

package persistencia;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;

import datatypesProcEstocasticos.DatosPEDemandaAnioBase;
import datatypesProcEstocasticos.DatosPEEvolucion;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import utilitarios.LeerDatosArchivo;
import utilitarios.Par;

public class CargadorPEEvolucion {
	
	
	/**
	 * Crea un Data type para ProcesoDemandaAnioBAse
	 */
	public static DatosPEEvolucion devuelveDatosPEEvolucion(DatosProcesoEstocastico dpe){
		DatosPEEvolucion dpdem = new DatosPEEvolucion();
	
	//	dpdem.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE("", dpe.getNombre()));
		ArrayList<String>vars = new ArrayList<String>();
		vars.add("val");
		dpdem.setNombresVA(vars);
		dpdem.setNombre(dpe.getNombre());
		dpdem.setRuta("");
		
		return dpdem;
	}
	
	

}

