/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosVariableControl is part of MOP.
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

package datatypesProblema;

import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

/**
 * Datatype que representa los datos de una variable de control asociada al problema lineal
 * @author ut602614
 *
 */ 
public class DatosVariableControl implements Comparable{
	private String nombre;
	private Integer ordinal;
	private Integer tipo;			/**BINARIA, ENTERA, CONTINUA*/
	private Integer dominio;			/**POSITIVA, LIBRE, SEMICONTINUA*/
	private Double cotaInferior;
	private Double cotaSuperior;
	
	
	
	public DatosVariableControl(String nombre, Integer tipo, Integer dominio, Double cotaInferior, Double cotaSuperior) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.cotaInferior = cotaInferior;
		this.cotaSuperior = cotaSuperior;
	
		this.dominio = dominio;
	}
	public Integer getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
	public Integer getTipo() {
		return tipo;
	}
	public void setTipo(Integer tipo) {
		this.tipo = tipo;
	}
	public Double getCotaInferior() {
		return cotaInferior;
	}
	public void setCotaInferior(Double cotaInferior) {
		this.cotaInferior = cotaInferior;
	}
	public Double getCotaSuperior() {
		return cotaSuperior;
	}
	public void setCotaSuperior(Double cotaSuperior) {
		this.cotaSuperior = cotaSuperior;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Integer getDominio() {
		return dominio;
	}
	public void setDominio(Integer dominio) {
		this.dominio = dominio;
	}
	public void imprimir() {
	
		if (cotaInferior == null && cotaSuperior!= null)
			System.out.println(  nombre + " <= " + cotaSuperior + ";");
		else if (cotaInferior!= null && cotaSuperior == null)
			System.out.println(  cotaInferior + " <= " + nombre + ";");
		else if (cotaInferior== null && cotaSuperior==null)
			System.out.println(nombre + ";");
		else
			System.out.println(  cotaInferior + " <= " + nombre + " <= " + cotaSuperior + ";");
	}
	
	public void guardar(String ruta) {
		if (cotaInferior == null && cotaSuperior!= null)
			DirectoriosYArchivos.agregaTexto(ruta, nombre + " <= " + cotaSuperior + ";");
		else if (cotaInferior!= null && cotaSuperior == null)
			DirectoriosYArchivos.agregaTexto(ruta,  cotaInferior + " <= " + nombre + ";");
		else if (cotaInferior== null && cotaSuperior==null)
			DirectoriosYArchivos.agregaTexto(ruta,nombre + ";");
		else
			DirectoriosYArchivos.agregaTexto(ruta, cotaInferior + " <= " + nombre + " <= " + cotaSuperior + ";");
		
	}
	
	
	public String creaSalida() {
		String result = "";
		if (cotaInferior == null && cotaSuperior!= null)
			result = nombre + " <= " + cotaSuperior + ";";
		else if (cotaInferior!= null && cotaSuperior == null)
			result = cotaInferior + " <= " + nombre + ";";
		else if (cotaInferior== null && cotaSuperior==null)
			result = nombre + ";";
		else
			result = cotaInferior + " <= " + nombre + " <= " + cotaSuperior + ";";
		return result;
	}
	
	public String creaCotasLpSolve() {
		String result = null;
		if (cotaSuperior!= null)
			result = nombre + " <= " + cotaSuperior + ";";
		if (cotaInferior!= null && cotaInferior!=0 )
			result = cotaInferior + " <= " + nombre + ";";
		return result;
	}
	

	public boolean mismaVar(DatosVariableControl dvar) {
		return nombre.equals(dvar.nombre);
	}

	public boolean cambioVar(DatosVariableControl dvar) {
		if (mismaVar(dvar)) {
			boolean tipoDom = !tipo.equals(dvar.tipo) || !dominio.equals(dvar.dominio);
			boolean cinf = (cotaInferior != null && dvar.cotaInferior != null) ? !cotaInferior.equals(dvar.cotaInferior) : false;
			boolean csup = (cotaSuperior != null && dvar.cotaSuperior != null) ? !cotaSuperior.equals(dvar.cotaSuperior) : false;
			return tipoDom || cinf || csup;
		}
		return false;
	}
	@Override
	public int compareTo(Object o) {
		DatosVariableControl dvc = (DatosVariableControl)o;
		return nombre.compareTo(dvc.getNombre());
	}
	
	
	
	@Override
	public String toString() {
		String result = nombre + "\t" + "ordinal =" + ordinal + "\t" +
				"tipo = " + Constantes.TIPOSVC[tipo] + "\t" +
				"dominio = " + Constantes.TIPOSVC[dominio] + "\t" +
				"cotaInferior = " + cotaInferior + "\t" +
				"cotaSuperior = " + cotaSuperior;				
	
		return result;
				
		
		
		
	}

}
