/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BuscadorResult is part of MOP.
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

package cp_salidas;

import java.util.Hashtable;

import cp_datatypesEntradas.DatosGeneralesCP;
import cp_despacho.GrafoEscenarios;
import datatypesProblema.DatosSalidaProblemaLineal;
import parque.Corrida;

public abstract class BuscadorResult {
	
	protected Corrida corrida;
	
	protected String origenResultadosPL;
	
	protected Object objetoOrigen;
	
	
	protected GrafoEscenarios ge;
	
	protected DatosGeneralesCP dGCP;
		
	
	
	public BuscadorResult(String origenResultadosPL, Object objetoOrigen, Corrida corrida, GrafoEscenarios ge,
			DatosGeneralesCP dGCP) {
		this.origenResultadosPL = origenResultadosPL;
		this.objetoOrigen = objetoOrigen;
		this.corrida = corrida;
		this.ge = ge;
		this.dGCP = dGCP;
	}

	
	
	/**
	 * Devuelve true si existe la variables con valores nomVar y nomPar en los resultados
	 */
	public abstract boolean existeResult(String nomVar, String nomPar);
		
	
	

	/**
	 * Devuelve para los valores de nomVar y nomPar UN VECTOR con los resultados por poste o por día
	 * en el escenario desde el origen dado por vecEsc.
	 * Si la variable no existe devuelve null
	 * 
	 * @param nomVar  nombre de la BaseVar según las constantes de ConCP
	 * @param nomPar  nombre del participante que genera la BaseVar
	 * @param vecEsc  vector con el número de escenario por etapa desde el origen hasta el final
	 * @return
	 */
	public abstract ResultadoEsc1V dameVariable(String nomVar, String nomPar, int[] vecEsc);
	
	
	/**
	 * Devuelve el valor de una variable de control para un poste y escenario particular
	 * @param nomVC nombre de la variable de control en el problema lineal
	 * @return
	 */
	public abstract double dame1ValorVariable(String nomVC);
		
	
	
	
	
	/**
	 * Devuelve los valores de la variable dual de una restricción excepto restricciones de caja
	 * poste a poste desde el inicil hasta el final del horizonte
	 * 
	 * @param nomRest nombre de la BaseRest según las constantes de ConCP
	 * @param nomPar  nombre del participante que genera la BaseRest
	 * @param vecEsc vector con el número de escenario por etapa desde el origen hasta el final del escenario
	 * @return
	 */
	public abstract ResultadoEsc1V dameDuales(String nomRest, String nomPar, int[] vecEsc);
	
	
	
	/**
	 * Devuelve el valor de una variable dual para una restricción en un poste y escenario particular
	 * @param nomVC nombre de la restricción en el problema lineal
	 * @return
	 */
	public abstract double dame1ValorDual(String nomRest);
	
	
	/**
	 * Devuelve el objetivo del problema lineal en el óptimo
	 * @return
	 */
	public abstract double dameObjetivo();
	
	public String getOrigenResultadosPL() {
		return origenResultadosPL;
	}


	public void setOrigenResultadosPL(String origenResultadosPL) {
		this.origenResultadosPL = origenResultadosPL;
	}





	public Corrida getCorrida() {
		return corrida;
	}



	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}



	public Object getObjetoOrigen() {
		return objetoOrigen;
	}



	public void setObjetoOrigen(Object objetoOrigen) {
		this.objetoOrigen = objetoOrigen;
	}



	public GrafoEscenarios getGe() {
		return ge;
	}


	public void setGe(GrafoEscenarios ge) {
		this.ge = ge;
	}


	public DatosGeneralesCP getdGCP() {
		return dGCP;
	}


	public void setdGCP(DatosGeneralesCP dGCP) {
		this.dGCP = dGCP;
	}



	

	
	
	

}
