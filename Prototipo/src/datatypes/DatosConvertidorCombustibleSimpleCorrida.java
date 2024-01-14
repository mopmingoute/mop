/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosConvertidorCombustibleSimpleCorrida is part of MOP.
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

/**
 * Datatype que representa un convertidor de combustible
 * @author ut602614
 *
 */
public class DatosConvertidorCombustibleSimpleCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	String nombre;
	Integer cantModulosDisponibles;
	Integer cantModulos;
	String combustibleOrigen;
	String combustibleTransformado;
	String barraOrigen;
	String barraDestino;
	Double flujoMaxOrigen;
	Double flujoMaxConvertido;
	Double relacion;

	public DatosConvertidorCombustibleSimpleCorrida(String nombre,
			Integer cantModulosDisponibles, Integer cantModulos,
			String combustibleOrigen, String combustibleTransformado,
			String barraOrigen, String barraDestino, Double flujoMaxOrigen,
			Double flujoMaxConvertido, Double relacion) {
		super();
		this.nombre = nombre;
		this.cantModulosDisponibles = cantModulosDisponibles;
		this.cantModulos = cantModulos;
		this.combustibleOrigen = combustibleOrigen;
		this.combustibleTransformado = combustibleTransformado;
		this.barraOrigen = barraOrigen;
		this.barraDestino = barraDestino;
		this.flujoMaxOrigen = flujoMaxOrigen;
		this.flujoMaxConvertido = flujoMaxConvertido;
		this.relacion = relacion;
	}
	public Integer getCantModulosDisponibles() {
		return cantModulosDisponibles;
	}
	public void setCantModulosDisponibles(Integer cantModulosDisponibles) {
		this.cantModulosDisponibles = cantModulosDisponibles;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Integer getCantModulos() {
		return cantModulos;
	}
	public void setCantModulos(Integer cantModulos) {
		this.cantModulos = cantModulos;
	}
	public String getCombustibleOrigen() {
		return combustibleOrigen;
	}
	public void setCombustibleOrigen(String combustibleOrigen) {
		this.combustibleOrigen = combustibleOrigen;
	}
	public String getCombustibleTransformado() {
		return combustibleTransformado;
	}
	public void setCombustibleTransformado(String combustibleTransformado) {
		this.combustibleTransformado = combustibleTransformado;
	}
	public String getBarraOrigen() {
		return barraOrigen;
	}
	public void setBarraOrigen(String barraOrigen) {
		this.barraOrigen = barraOrigen;
	}
	public String getBarraDestino() {
		return barraDestino;
	}
	public void setBarraDestino(String barraDestino) {
		this.barraDestino = barraDestino;
	}
	public Double getFlujoMaxOrigen() {
		return flujoMaxOrigen;
	}
	public void setFlujoMaxOrigen(Double flujoMaxOrigen) {
		this.flujoMaxOrigen = flujoMaxOrigen;
	}
	public Double getFlujoMaxConvertido() {
		return flujoMaxConvertido;
	}
	public void setFlujoMaxConvertido(Double flujoMaxConvertido) {
		this.flujoMaxConvertido = flujoMaxConvertido;
	}
	public Double getRelacion() {
		return relacion;
	}
	public void setRelacion(Double relacion) {
		this.relacion = relacion;
	}

	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) errores.add("DatosConvertidorCombustibleSimpleCorrida : Nombre vacío.");

		if(cantModulosDisponibles == null || cantModulosDisponibles == 0 ) { errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ":cantModulosDisponibles vacío."); }
		if(cantModulos == null || cantModulos == 0 ) { errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ":cantModulosDisponibles vacío."); }

		if(combustibleOrigen.trim().equals("")) errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": combustibleOrigen vacío.");
		if(combustibleTransformado.trim().equals("")) errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": combustibleTransformado vacío.");
		if(barraOrigen.trim().equals("")) errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": barraOrigen vacío.");
		if(barraDestino.trim().equals("")) errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": barraDestino vacío.");

		if(flujoMaxOrigen == null || flujoMaxOrigen == 0 ) { errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": flujoMaxOrigen vacío."); }
		if(flujoMaxConvertido == null || flujoMaxConvertido == 0 ) { errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": flujoMaxConvertido vacío."); }
		if(relacion == null || relacion == 0 ) { errores.add("DatosConvertidorCombustibleSimpleCorrida" +  nombre + ": relacion vacío."); }

		return errores;
	}
}
