/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosLocalizadorSalida is part of MOP.
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
import java.util.Hashtable;

import datatypesProblema.DatosHidroSalida;

/**
 * Datatype asociado a la relación localizador salida representando una tabla en el esquema de base de datos general
 * @author ut602614
 *
 */

public class DatosLocalizadorSalida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer poste;
	private String barra;
	private Double marginal;
	private Integer duracion;
	private Hashtable<String,DatosFuenteSalida> fuentes;
	private Hashtable<String,DatosHidroSalida> hidros;
	public Integer getPoste() {
		return poste;
	}
	public void setPoste(Integer poste) {
		this.poste = poste;
	}
		
	public String getBarra() {
		return barra;
	}
	public void setBarra(String barra) {
		this.barra = barra;
	}
	public Double getMarginal() {
		return marginal;
	}
	public void setMarginal(Double marginal) {
		this.marginal = marginal;
	}

	
	
	public Integer getDuracion() {
		return duracion;
	}
	public void setDuracion(Integer duracion) {
		this.duracion = duracion;
	}
	public Hashtable<String, DatosFuenteSalida> getFuentes() {
		return fuentes;
	}
	public void setParticipantes(
			Hashtable<String, DatosFuenteSalida> fuentes) {
		this.fuentes = fuentes;
	}
	public Hashtable<String, DatosHidroSalida> getHidros() {
		return hidros;
	}
	public void setHidros(Hashtable<String, DatosHidroSalida> hidros) {
		this.hidros = hidros;
	}
	
}
