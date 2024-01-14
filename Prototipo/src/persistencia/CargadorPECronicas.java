/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPECronicas is part of MOP.
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

import utilitarios.LeerDatosArchivo;
import datatypesProcEstocasticos.DatosPECronicas;
import datatypesProcEstocasticos.DatosPEEscenarios;
import datatypesProcEstocasticos.DatosProcesoEstocastico;

public class CargadorPECronicas {
	
	public static DatosPECronicas devuelveDatosPECronicas(DatosProcesoEstocastico dpe){
		String ruta = dpe.getRuta();
		DatosPECronicas dpcron = new DatosPECronicas();

	
		DatosPEEscenarios dpEsc = CargadorPEEscenarios.devuelveDatosPEEscenarios(dpe);
		dpcron.setDpesc(dpEsc);

        String dirProcAsoc = ruta + "/procesoAsociado.txt";
        ArrayList<ArrayList<String>> texto;
        texto = LeerDatosArchivo.getDatos(dirProcAsoc);
        dpcron.setNombreProcesoOrigen(texto.get(0).get(1));
        if(dpEsc.getCantVE()>0){
        	System.out.println("ERROR: UN PROCESO POR CRóNICAS TIENE VARIABLES DE ESTADO PROPIAS");
        	System.exit(1);
        }
        return dpcron;
	}

}
