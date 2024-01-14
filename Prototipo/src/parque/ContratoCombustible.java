/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombustible is part of MOP.
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

package parque;

import tiempo.Evolucion;
import compdespacho.ContratoCombCanioCompDesp;
import compdespacho.ContratoCombTopPasoFijoCompDesp;
import datatypes.DatosContratoCombustibleCorrida;


/**
 * Clase que representa el contrato de un combustible
 * @author ut602614
 *
 */
public abstract class ContratoCombustible extends Recurso {
	private Combustible combustible;		/**Combustible asociado al contrato*/
	private BarraCombustible barra;			/**Barra de combustible en donde el contrato inyecta combustible*/
	private Evolucion<Double> caudalMaximo; 			/**Caudal m�ximo de combustible que entrega el contrato por unidad de tiempo*/
	
	
	/**Constructor del contrato a partir de sus datos, el combustible y la barra asociados*/
	public ContratoCombustible(DatosContratoCombustibleCorrida datos, Combustible comb, BarraCombustible ba) {
		this.setNombre(datos.getNombre());
		this.combustible = comb;
		this.barra = ba;
		this.setCaudalMaximo(datos.getCaudalMax());
		this.setMantProgramado(datos.getMantProgramado());
//		this.agregarEvolucionAColeccion(caudalMaximo);
	
	}
	
	/**
	 * Devuelve el costo medio del contrato en USD/unidad para obtener un costo total
	 * del tórmico en el paso asociado al intante
	 */
	public abstract double costoMedio(long instante);
	
	
	
	
	
	public Combustible getCombustible() {
		return combustible;
	}
	public void setCombustible(Combustible combustible) {
		this.combustible = combustible;
	}
	public BarraCombustible getBarra() {
		return barra;
	}
	public void setBarra(BarraCombustible barra) {
		this.barra = barra;
	}


	public abstract String getNCaudalComb();   
	

	public Evolucion<Double> getCaudalMaximo() {
		return caudalMaximo;
	}

	public void setCaudalMaximo(Evolucion<Double> caudalMaximo) {
		this.caudalMaximo = caudalMaximo;
	}

	
	
	
}
