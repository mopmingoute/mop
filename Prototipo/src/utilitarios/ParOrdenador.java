/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ParOrdenador is part of MOP.
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

package utilitarios;

/**
 * Se usa para ordenar objetos según el valor del real ordenador
 * @author ut469262
 *
 */
public class ParOrdenador implements Comparable{
	private double ordenador;
	private Object objeto;
	
	public ParOrdenador(double ordenador, Object objeto) {
		super();
		this.ordenador = ordenador;
		this.objeto = objeto;
	}

	public double getOrdenador() {
		return ordenador;
	}


	public void setOrdenador(double ordenador) {
		this.ordenador = ordenador;
	}


	public Object getObjeto() {
		return objeto;
	}


	public void setObjeto(Object objeto) {
		this.objeto = objeto;
	}





	@Override
	public int compareTo(Object o) {
		int result=0;
		if(this.ordenador>((ParOrdenador)o).ordenador) result = 1;
		if(this.ordenador<((ParOrdenador)o).ordenador) result = -1;
		return result;
	}
	
}
