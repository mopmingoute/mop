/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Variable is part of MOP.
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

package estado;

import tiempo.Evolucion;

/*
 * Las variables pueden ser continuas o discretas
 * Si son discretas pueden ser discretas incrementales (tienen un tratamiento no exhaustivo en los despachos)
 * Si son discretas pueden ser ordinales (toman valores enteros no negativos 0, 1, 2,..)
 */
public abstract class Variable{
	private String nombre;
	protected Evolucion<Discretizacion> evolDiscretizacion;
	protected boolean discreta;    // True si la variable es discreta
	protected boolean ordinal; // Si ademós de ser discreta, tiene sólo valores enteros sucesivos: 0, 1, 2,...
	
	/*
	 * Para las variables discretas, true si no es exhaustiva es decir:
	 * - si la variable es de control, es un control discreto incremental
	 * - si la variable es de estado, el valor de sus recursos estó dado por incrementos
	 * y false si es exhaustiva.
	 */
	protected boolean discretaIncremental; 

	public Variable() {
		
	}
	
	public Variable(String nombre) {
		this.nombre = nombre;
	}
	
	public Variable(String nombre, boolean discreta, boolean discretaInc, boolean ordinal) {
		this.nombre = nombre;
		this.discreta = discreta;
		this.ordinal = ordinal;
		this.discretaIncremental = discretaInc;
	}
	 
	
	

	
	/*
	 * Devuelve la cantidad de valores que puede tomar la variable en el instante
	 */
	public int cantValoresPosibles(long instante){
		Discretizacion dis = this.getEvolDiscretizacion().getValor(instante);
		return dis.getValores().size();
	}
	
	
	/*
	 * Devuelve la Discretizacion que vale en un instante dado
	 * @param intante
	 */
	public Discretizacion devuelveDiscretizacion(long instante){
		return evolDiscretizacion.getValor(instante);	
	}
	
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}



	public Evolucion<Discretizacion> getEvolDiscretizacion() {
		return evolDiscretizacion;
	}

	public void setEvolDiscretizacion(Evolucion<Discretizacion> evolDiscretizacion) {
		this.evolDiscretizacion = evolDiscretizacion;
	}

	public boolean isDiscreta() {
		return discreta;
	}

	public void setDiscreta(boolean discreta) {
		this.discreta = discreta;
	}

	public boolean isOrdinal() {
		return ordinal;
	}

	public void setOrdinal(boolean ordinal) {
		this.ordinal = ordinal;
	}

	public boolean isDiscretaIncremental() {
		return discretaIncremental;
	}

	public void setDiscretaIncremental(boolean discretaIncremental) {
		this.discretaIncremental = discretaIncremental;
	}


	
	
	
	
}
