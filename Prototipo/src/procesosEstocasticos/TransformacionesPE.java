/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TransformacionesPE is part of MOP.
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
import java.util.Hashtable;

import datatypesProcEstocasticos.DatosTransformaciones;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import utilitarios.UtilArrays;

public class TransformacionesPE {
	
	
	/**
	 * Contiene todas las transformaciones del PE
	 * Clave: nombre de la variable aleatoria
	 * Valor: una lista de transformaciones, una para cada paso de tiempo del año
	 */
	private Hashtable<String, ArrayList<TransformacionVA>> transformaciones;
	
	private ArrayList<String> nombresSeries; // nombres de las series a las que se aplican las transformaciones
	
	// Las constantes de nombres de paso de PE que se usan en utilitarios.Constantes: PASOSEMANA, PASODIA, PASOHORA
	private String nombrePaso;
	
	
	public TransformacionesPE(DatosTransformaciones dt){
		this.nombresSeries = dt.getNombresSeries();
		this.nombrePaso = dt.getNombrePaso();
		transformaciones = new Hashtable<String, ArrayList<TransformacionVA>>();
		for(String ns: nombresSeries){
			ArrayList<TransformacionVA> al = new ArrayList<TransformacionVA>();
			if(dt.getTipoTransformaciones().get(ns).equalsIgnoreCase(utilitarios.Constantes.BOXCOX)){								
				for(int ip=0; ip<dt.getCantPasos(); ip++){					
					TransBoxCox tbc = new TransBoxCox(dt.getParametros().get(ns).get(ip).get(0), 
							dt.getParametros().get(ns).get(ip).get(1), 
							dt.getParametros().get(ns).get(ip).get(2),
							dt.getParametros().get(ns).get(ip).get(3));
					al.add(tbc);					
				}	
				transformaciones.put(ns, al);					
			}else if(dt.getTipoTransformaciones().get(ns).equalsIgnoreCase(utilitarios.Constantes.NQT)){			
				for(int ip=0; ip<dt.getCantPasos(); ip++){	
					TransNQT tnqt = new TransNQT(UtilArrays.dameArrayD(dt.getParametros().get(ns).get(ip)));
					al.add(tnqt);	
				}
			}else{
				System.out.println("Se pidió tipo de transformaci�n inexistente");
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(1);
			}
			transformaciones.put(ns, al);
		}
		
	}	
	
	/**
	 * Devuelve la transformación de la variable nombreVA en el pasoDelAnio
	 * @param nombreVA
	 * @param pasoDelAnio empezando en 0.
	 */
	public TransformacionVA dameTrans(String nombreVA, int pasoDelAnio){
		return transformaciones.get(nombreVA).get(pasoDelAnio);
	}

}
