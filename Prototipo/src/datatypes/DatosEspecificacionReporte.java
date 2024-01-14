/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosEspecificacionReporte is part of MOP.
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
import java.util.ArrayList;

public class DatosEspecificacionReporte implements Serializable {
	private static final long serialVersionUID = 1L;
	// Constantes tipo de reportes
	public static Integer REP_RES_ENER = 0;
	public static Integer REP_RES_COSTO = 1;
	public static Integer REP_COSTOS_MARG = 2;
	public static Integer REP_HIDROS = 3;
	public static Integer REP_TERS = 4;
	public static Integer REP_FALLAS = 5;
	public static Integer REP_VAL_RENOVABLES = 6;
	public static Integer REP_AMBIENTAL = 7;
	public static Integer REP_CONTRATOS = 8;
	public static Integer REP_OPTIMIZACION = 9;

	// Constantes tipo de simplificacion
	public static Integer SIMP_PROM = 10;
	public static Integer SIMP_PERCENTIL = 11;
	public static Integer SIMP_CRONICAS = 12;
	public static Integer SIMP_PERC_GEN_HIDRO = 13;

	// Constantes tipo de fuente
	public static Integer FUENTE_HIDRO = 14;
	public static Integer FUENTE_TER = 15;
	public static Integer FUENTE_SOL = 16;
	public static Integer FUENTE_EOL = 17;
	public static Integer FUENTE_ACUM = 18;
	public static Integer FUENTE_IMPOEXPO = 19;

	/*
	 * Hay que tener en cuenta que se usan algunos atributos dependiendo del tipo de
	 * reporte
	 */
	private String nombre; // nombre del reporte
	private Integer tipoReporte; // tipo del reporte: los vistos en el documento, ResumenEnergia,ResumenCosto,
									// etc... en orden desde 0
	private Integer anioInicio; // inicio del Integerervalo a reportar (graficar)
	private Integer anioFin; // fin del Integerervalo
	private Boolean porTecnologia; // si es true se agrupa por tecnologia
	private Integer simplificacion; // promedio, percentiles, porCronica, percentilSegunHidro
	private Boolean porPoste; // si se devuelven graficos por poste
	private Double criterio; // valor límite a graficar en algunos casos con una recta horizontal
	private Integer fuente; // tipo de fuente
	private Integer paso; // paso a mostrar, se usa sólo para el reporte de optimización
	private ArrayList<Integer> variablesEstadoActivas; // variables de estado para las que se grafica el valor del agua
														// en la salida optimizacion
	private Boolean todasEshys; // si en el gráfico se muestran todas las eshys
	private Integer eshy; // si se elige una eshy viene acá
	private Boolean porDefecto;

	public DatosEspecificacionReporte(Integer tipoReporte, Boolean porDefecto) {
		this.tipoReporte = tipoReporte;
		this.porDefecto = porDefecto;
	}

	public DatosEspecificacionReporte(String nombre, Integer tipoReporte, Integer anioInicio, Integer anioFin,
			Boolean porTecnologia, Integer simplificacion, Boolean porPoste, Double criterio, Integer fuente,
			Integer paso, ArrayList<Integer> variablesEstadoActivas, Boolean todasEshys, Integer eshy) {
		super();
		this.nombre = nombre;
		this.tipoReporte = tipoReporte;
		this.anioInicio = anioInicio;
		this.anioFin = anioFin;
		this.porTecnologia = porTecnologia;
		this.simplificacion = simplificacion;
		this.porPoste = porPoste;
		this.criterio = criterio;
		this.fuente = fuente;
		this.paso = paso;
		this.variablesEstadoActivas = variablesEstadoActivas;
		this.todasEshys = todasEshys;
		this.eshy = eshy;
		this.porDefecto = false;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getTipoReporte() {
		return tipoReporte;
	}

	public void setTipoReporte(Integer tipoReporte) {
		this.tipoReporte = tipoReporte;
	}

	public Integer getAnioInicio() {
		return anioInicio;
	}

	public void setAnioInicio(Integer anioInicio) {
		this.anioInicio = anioInicio;
	}

	public Integer getAnioFin() {
		return anioFin;
	}

	public void setAnioFin(Integer anioFin) {
		this.anioFin = anioFin;
	}

	public Boolean isPorTecnologia() {
		return porTecnologia;
	}

	public void setPorTecnologia(Boolean porTecnologia) {
		this.porTecnologia = porTecnologia;
	}

	public Integer getSimplificacion() {
		return simplificacion;
	}

	public void setSimplificacion(Integer simplificacion) {
		this.simplificacion = simplificacion;
	}

	public Boolean isPorPoste() {
		return porPoste;
	}

	public void setPorPoste(Boolean porPoste) {
		this.porPoste = porPoste;
	}

	public Double getCriterio() {
		return criterio;
	}

	public void setCriterio(Double criterio) {
		this.criterio = criterio;
	}

	public Integer getFuente() {
		return fuente;
	}

	public void setFuente(Integer fuente) {
		this.fuente = fuente;
	}

	public Integer getPaso() {
		return paso;
	}

	public void setPaso(Integer paso) {
		this.paso = paso;
	}

	public ArrayList<Integer> getVariablesEstadoActivas() {
		return variablesEstadoActivas;
	}

	public void setVariablesEstadoActivas(ArrayList<Integer> variablesEstadoActivas) {
		this.variablesEstadoActivas = variablesEstadoActivas;
	}

	public Boolean isTodasEshys() {
		return todasEshys;
	}

	public void setTodasEshys(Boolean todasEshys) {
		this.todasEshys = todasEshys;
	}

	public Integer getEshy() {
		return eshy;
	}

	public void setEshy(Integer eshy) {
		this.eshy = eshy;
	}

	public Boolean isPorDefecto() {
		return porDefecto;
	}

	public void setPorDefecto(Boolean porDefecto) {
		this.porDefecto = porDefecto;
	}

}
