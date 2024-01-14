/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPostizacion is part of MOP.
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
import java.util.GregorianCalendar;
import utilitarios.LeerDatosArchivo;
import datatypes.DatosPostizacion;

public class CargadorPostizacion {
	public static DatosPostizacion devuelveDatosPostizacion(String ruta, GregorianCalendar inicioCorrida) {
		
		
		DatosPostizacion dp = new DatosPostizacion();


		
		ArrayList<ArrayList<String>> texto;

		GregorianCalendar fechaInicial = null;
		ruta = "./resources/NUMPOSLINEAL.txt";
		texto = LeerDatosArchivo.getDatos(ruta);

		String fecha;
		Integer inter;
		Integer paso;
		Integer poste;
		Integer pasoAnterior = 0;

		ArrayList<Integer> unNumpos = new ArrayList<Integer>();

		boolean empezo = false;

		for (int i = 0; i < texto.size(); i++) {
			fecha = texto.get(i).get(0);

			if (!empezo) {
				Integer anio =Integer.parseInt(fecha.substring(0, 4));
				Integer mes = Integer.parseInt(fecha.substring(4, 6))-1;
				Integer dia = Integer.parseInt(fecha.substring(6, 8));
				fechaInicial = new GregorianCalendar(anio, mes, dia);
				pasoAnterior = Integer.parseInt(texto.get(i).get(2));
				empezo = fechaInicial.equals(inicioCorrida);				
				
			} 
			if (empezo)  {
				paso = Integer.parseInt(texto.get(i).get(2));

				if (paso != pasoAnterior) {
					dp.agregarNumpos(unNumpos);
					unNumpos = new ArrayList<Integer>();
					pasoAnterior = paso;
				}
				inter = Integer.parseInt(texto.get(i).get(1));
				poste = Integer.parseInt(texto.get(i).get(3));

				unNumpos.add(poste);
			}

		}
		dp.agregarNumpos(unNumpos);

		dp.setFechaIni(fechaInicial);
		return dp;

	}

}
