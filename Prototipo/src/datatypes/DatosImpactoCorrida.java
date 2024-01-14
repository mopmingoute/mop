/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosImpactoCorrida is part of MOP.
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
import java.util.Collection;

import tiempo.Evolucion;


public class DatosImpactoCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;
	private Evolucion<Double> costoUnit;
	private Evolucion<Double> limite;
	private boolean porPoste;
	private ArrayList<String> involucrados; /*Lista de participantes que contribuyen al impacto*/
	private boolean salDetallada;
	private int tipoImpacto;
	private boolean porUnidadTiempo;
	private Evolucion<Boolean> activo;



	public DatosImpactoCorrida(String nombre, Evolucion<Boolean> activo, Evolucion<Double> costoUnit, Evolucion<Double> limite, boolean porPoste,
			ArrayList<String> involucrados, int tipoImpacto, boolean porUnidadTiempo, boolean saldet) {
		super();
		this.nombre = nombre;
		this.setActivo(activo);
		this.costoUnit = costoUnit;
		this.limite = limite;
		this.involucrados = involucrados;
		this.salDetallada = saldet;
		this.setPorUnidadTiempo(porUnidadTiempo);
		this.tipoImpacto = tipoImpacto;
		this.porPoste = porPoste;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Evolucion<Double> getCostoUnit() {
		return costoUnit;
	}
	public void setCostoUnit(Evolucion<Double> costoUnit) {
		this.costoUnit = costoUnit;
	}
	public Evolucion<Double> getLimite() {
		return limite;
	}
	public void setLimite(Evolucion<Double> limite) {
		this.limite = limite;
	}
	public ArrayList<String> getInvolucrados() {
		return involucrados;
	}
	public void setInvolucrados(ArrayList<String> involucrados) {
		this.involucrados = involucrados;
	}
	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public int getTipoImpacto() {		
		return tipoImpacto;
	}

	public void setTipoImpacto(int tipoImpacto) {
		this.tipoImpacto = tipoImpacto;
	}

	public boolean isPorPoste() {
		return porPoste;
	}

	public void setPorPoste(boolean porPoste) {
		this.porPoste = porPoste;
	}

	public boolean isPorUnidadTiempo() {
		return porUnidadTiempo;
	}

	public void setPorUnidadTiempo(boolean porUnidadTiempo) {
		this.porUnidadTiempo = porUnidadTiempo;
	}

	public Evolucion<Boolean> getActivo() {
		return activo;
	}

	public void setActivo(Evolucion<Boolean> activo) {
		this.activo = activo;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) { errores.add("Impacto: Nombre vacío."); }

		if(activo == null) { errores.add("Impacto: activo vacío."); }
		else if(activo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": activo vacío."); }

		if(costoUnit == null) { errores.add("Impacto: costoUnit vacío."); }
		else if(costoUnit.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": costoUnit vacío."); }

		if(limite == null) { errores.add("Impacto: limite vacío."); }
		else if(limite.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": limite vacío."); }

		if(tipoImpacto == 0)  { errores.add("Impacto: tipoImpacto vacío."); }
		if(involucrados == null) { errores.add("Impacto: involucrados vacío."); }
		else {
			involucrados.forEach((v) -> { if (v.trim().equals("")) errores.add("Impacto: involucrados vacío."); });
		}


		return errores;
	}


}
