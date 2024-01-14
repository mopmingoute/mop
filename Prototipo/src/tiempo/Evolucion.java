/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Evolucion is part of MOP.
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

package tiempo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Clase evolución abstracta
 * @author ut602614
 *
 */

public abstract class Evolucion<T> implements Serializable{
	

	
	public SentidoTiempo getSentido() {
		return sentido;
	}

	public void setSentido(SentidoTiempo sentido) {
		this.sentido = sentido;
	}

	protected T valor; //es el valor corriente
	protected SentidoTiempo sentido;

	private boolean editoValores;
		
	
	public Evolucion(SentidoTiempo sentido) {
		super();
		this.sentido = sentido;
	}

	public abstract T getValor(long instante);
	
	public abstract void inicializarParaSimulacion();

//	public T getValor() {
//		return valor;
//	}

	public void setValor(T valor) {
		this.valor = valor;
	}

	public boolean isEditoValores() {   return editoValores;	}
	public void setEditoValores(boolean editoValores) {	this.editoValores = editoValores;   }
	
	public abstract ArrayList<String> controlDatosCompletos();

	public abstract ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err );
	public abstract ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err );

}
