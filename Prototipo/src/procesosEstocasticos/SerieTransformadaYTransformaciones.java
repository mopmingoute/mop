/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * SerieTransformadaYTransformaciones is part of MOP.
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

public class SerieTransformadaYTransformaciones {
	
	
	private Serie serie;
	
	// El ArrayList recorre los pasos del año
	private ArrayList<TransformacionVA> transformaciones;
	
	public SerieTransformadaYTransformaciones(Serie serie, ArrayList<TransformacionVA> transformaciones) {
		super();
		this.serie = serie;
		this.transformaciones = transformaciones;
	}
	public Serie getSerie() {
		return serie;
	}
	public void setSerie(Serie serie) {
		this.serie = serie;
	}
	public ArrayList<TransformacionVA> getTransformaciones() {
		return transformaciones;
	}
	public void setTransformaciones(ArrayList<TransformacionVA> transformaciones) {
		this.transformaciones = transformaciones;
	}
	
	

}
