/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTransformaciones is part of MOP.
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

package datatypesProcEstocasticos;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Datatype para el conjunto de las transformaciones Box-Cox, una por paso de
 * tiempo del a�o, para un conjunto de series de datos
 * 
 * @author ut469262
 *
 */
public class DatosTransformaciones {

	private String nombreEstimacion;

	private ArrayList<String> nombresSeries; // nombres de las series a las que se aplican las transformaciones

	/**
	 * Clave nombre de las serie, valor tipo de transformación, con los nombres de
	 * utilitarios.Constantes BOXCOX, NQT
	 */
	private Hashtable<String, String> tipoTransformaciones;

	// Las constantes de nombres de paso de PE que se usan en
	// utilitarios.Constantes: PASOSEMANA, PASODIA, PASOHORA
	private String nombrePaso;

	private int cantParametros; // cantidad de parámetros por paso de tiempo, SE IGNORA PARA NQT
	private int cantPasos; // cantidad de pasos anuales cada uno con su transformación

	/**
	 * Clave de la tabla: nombre de la serie ArrayList: Primer índice: recorre los
	 * pasos de PE a lo largo del año para un parámetro Segundo índice: recorre
	 * parámetros de la transformación de una serie dada
	 */
	private Hashtable<String, ArrayList<ArrayList<Double>>> parametros;

	public Hashtable<String, String> getTipoTransformaciones() {
		return tipoTransformaciones;
	}

	public void setTipoTransformaciones(Hashtable<String, String> tipoTransformaciones) {
		this.tipoTransformaciones = tipoTransformaciones;
	}

	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	public String getNombreEstimacion() {
		return nombreEstimacion;
	}

	public void setNombreEstimacion(String nombreEstimacion) {
		this.nombreEstimacion = nombreEstimacion;
	}

	public ArrayList<String> getNombresSeries() {
		return nombresSeries;
	}

	public void setNombresSeries(ArrayList<String> nombresSeries) {
		this.nombresSeries = nombresSeries;
	}

	public Hashtable<String, ArrayList<ArrayList<Double>>> getParametros() {
		return parametros;
	}

	public void setParametros(Hashtable<String, ArrayList<ArrayList<Double>>> parametros) {
		this.parametros = parametros;
	}

	public int getCantParametros() {
		return cantParametros;
	}

	public void setCantParametros(int cantParametros) {
		this.cantParametros = cantParametros;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

}
