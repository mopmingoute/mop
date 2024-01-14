/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDiscretizacion is part of MOP.
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

package datatypes;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatosDiscretizacion implements Serializable{
	private static final long serialVersionUID = 1L;
	private double[] particion;
	private double minimo;
	private double maximo;	
	
	public DatosDiscretizacion() {
		super();
	}

	public DatosDiscretizacion(double[] particion) {
		super();
		this.particion = particion;
	}

	public double[] getParticion() {
		return particion;
	}

	public void setParticion(double[] particion) {
		this.particion = particion;
	}

	public double getMinimo() {
		return minimo;
	}

	public void setMinimo(double minimo) {
		this.minimo = minimo;
	}

	public double getMaximo() {
		return maximo;
	}

	public void setMaximo(double maximo) {
		this.maximo = maximo;
	}

	public Element toXML(Document doc){
		Element res = doc.createElement("discretizacion");
		res.setAttribute("tipo", "equiespaciada");
		Element min = doc.createElement("minimo");
		min.appendChild(doc.createTextNode(String.valueOf(getMinimo())));
		res.appendChild(min);
		Element max = doc.createElement("maximo");
		max.appendChild(doc.createTextNode(String.valueOf(getMaximo())));
		res.appendChild(max);
		Element cantPuntos = doc.createElement("cantidadPuntos");
		cantPuntos.appendChild(doc.createTextNode(String.valueOf(getParticion().length)));
		res.appendChild(cantPuntos);
		return res;
	}

}
