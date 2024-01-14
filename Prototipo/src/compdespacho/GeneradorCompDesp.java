/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorCompDesp is part of MOP.
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

package compdespacho;

import java.util.Hashtable;
import parque.Generador;
import datatypesProblema.DatosSalidaProblemaLineal;

/**
 * Clase encargada de modelar el comportamiento de un generador en el problema de despacho
 * @author ut602614
 *
 */

public class GeneradorCompDesp extends CompDespacho{

	private Generador g;
	
	
	public GeneradorCompDesp() {
		setG((Generador)this.participante);

	}
	
	@Override
	public void crearVariablesControl() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cargarRestricciones() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contribuirObjetivo() {
				
	}

	public String[] getNpotp() {
		return npotp;
	}

	public void setNpotp(String[] npotp) {
		this.npotp = npotp;
	}
	
	
	/**
	 * Devuelve el resultado de potencia del poste p en MW dada una salida del problema lineal 
	 */
	public double getPot(DatosSalidaProblemaLineal resultados, int p){		
		Hashtable<String, Double> solucion = resultados.getSolucion();
		return solucion.get((this.npotp[p]));				
		
	}

	public double dameCoeficientePotencia(int p) {
		return 1.0;
	}

	public Generador getG() {
		return g;
	}

	public void setG(Generador g) {
		this.g = g;
	}

}
