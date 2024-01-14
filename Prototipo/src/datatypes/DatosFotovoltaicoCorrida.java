/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosFotovoltaicoCorrida is part of MOP.
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

import tiempo.Evolucion;

/**
 * Datatype que representa los datos de un aerogenerador eólico
 * 
 * @author ut602614
 *
 */

public class DatosFotovoltaicoCorrida implements Serializable {

	private String nombre;
	/** Nombre del generador eólico */
	private String propietario;
	private String barra;
	/** Barra asociada al generador */
	private Evolucion<Integer> cantModInst;
	/** Cantidad de módulos del generador */

	private Evolucion<Double> potMin;
	/** Potencia mónima asociada al generador eólico */
	private Evolucion<Double> potMax;
	/** Potencia móxima asociada al generador eólico */

	private DatosVariableAleatoria factor;

	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private boolean salDetallada;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;
	private Evolucion<Double> costoVariable;

	public DatosFotovoltaicoCorrida(String nombre, String propietario, String barra, Evolucion<Integer> cantModInst,
			Evolucion<Double> potMin, Evolucion<Double> potMax, DatosVariableAleatoria factor, Integer cantModIni,
			Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo, boolean salDetallada,
			Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo, Evolucion<Double> costoVariable) {
		super();
		this.nombre = nombre;
		this.propietario = propietario;
		this.barra = barra;
		this.cantModInst = cantModInst;
		this.potMin = potMin;
		this.potMax = potMax;
		this.factor = factor;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.setSalDetallada(salDetallada);
		this.setMantProgramado(mantProgramado);
		this.costoFijo = costoFijo;
		this.costoVariable = costoVariable;
	}

	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}

	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getBarra() {
		return barra;
	}

	public void setBarra(String barra) {
		this.barra = barra;
	}

	public Evolucion<Double> getPotMin() {
		return potMin;
	}

	public void setPotMin(Evolucion<Double> potMin) {
		this.potMin = potMin;
	}

	public Evolucion<Double> getPotMax() {
		return potMax;
	}

	public void setPotMax(Evolucion<Double> potMax) {
		this.potMax = potMax;
	}

	public Integer getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
	}

	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}

	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}

	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}

	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}

	public DatosVariableAleatoria getFactor() {
		return factor;
	}

	public void setFactor(DatosVariableAleatoria factor) {
		this.factor = factor;
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public Evolucion<Integer> getMantProgramado() {
		return mantProgramado;
	}

	public void setMantProgramado(Evolucion<Integer> mantProgramado) {
		this.mantProgramado = mantProgramado;
	}

	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}

	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}

	public Evolucion<Double> getCostoVariable() {
		return costoVariable;
	}

	public void setCostoVariable(Evolucion<Double> costoVariable) {
		this.costoVariable = costoVariable;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if (nombre.trim().equals(""))
			errores.add("Fotovoltaico: Nombre vacío.");
		if (barra == null) {
			errores.add("Fotovoltaico " + nombre + ": Barra vacío.");
		}

		if (cantModInst == null) {
			errores.add("Fotovoltaico " + nombre + ": cantModInst vacío.");
		} else if (cantModInst.controlDatosCompletos().size() > 0) {
			errores.add("Fotovoltaico " + nombre + ": cantModInst vacío.");
		}
		if (potMax == null) {
			errores.add("Fotovoltaico " + nombre + ": potMax vacío.");
		} else if (potMax.controlDatosCompletos().size() > 0) {
			errores.add("Fotovoltaico " + nombre + ": potMax vacío.");
		}
		if (potMin == null) {
			errores.add("Fotovoltaico " + nombre + ": potMin vacío.");
		} else if (potMin.controlDatosCompletos().size() > 0) {
			errores.add("Fotovoltaico " + nombre + ": potMin vacío.");
		}

		if (factor.controlDatosCompletos().size() > 0) {
			errores.add("Fotovoltaico " + nombre + ": factor vacío.");
		}
		if (factor == null)
			errores.add("Fotovoltaico " + nombre + ": factor vacío.");

		return errores;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}
}
