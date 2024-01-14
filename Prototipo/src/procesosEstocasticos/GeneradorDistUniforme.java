/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorDistUniforme is part of MOP.
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


/**
 * Genera un valor con distribución uniforme [0,1]
 * @author ut602614
 *
 */
public abstract class GeneradorDistUniforme {
	protected long semilla;

	public GeneradorDistUniforme(long semilla) {
		super();
		this.semilla = semilla;
	}

	public Long getSemilla() {
		return semilla;
	}

	public void setSemilla(long semilla) {
		this.semilla = semilla;
	}
	
	public abstract Double generarValor();
	
	/**
	 * Devuelve un ordinal aleatorio entre 0 y cantidad-1 equidistribuido
	 * a partir de un valor en [0,1], generado a partir de la uniforme
	 * de este proceso
	 * @param cantidad la cantidad de ordinales
	 * @param valor el real en [0,1]
	 */
	public int devuelveOrdinal(int cantidad){
		double valor = this.generarValor();
		return (int)Math.floor(valor*cantidad);			
	}
		
}
