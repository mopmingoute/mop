/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosAcumuladorCorrida is part of MOP.
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
import java.util.Hashtable;

import tiempo.Evolucion;
import utilitarios.Constantes;

public class DatosAcumuladorCorrida implements Serializable {
	private String nombre;
	/** Nombre del acumulador */
	private String propietario;
	private String barra;
	/** Barra asociada al acumulador */
	private Evolucion<Integer> cantModInst;
	/** Cantidad de módulos del acumulador */

	private Evolucion<Double> potMin;
	/** Potencia mónima asociada al acumulador (inyección) */
	private Evolucion<Double> potMax;
	/** Potencia móxima asociada al acumulador (inyección) */

	private Evolucion<Double> potAlmacenadaMin;
	/** Potencia mónima asociada al acumulador (almacenamiento) */
	private Evolucion<Double> potAlmacenadaMax;
	/** Potencia móxima asociada al acumulador (almacenamiento) */

	private Evolucion<Double> rendIny;
	private Evolucion<Double> rendAlmac;
	private Evolucion<Double> factorUso;
	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private boolean salDetallada;
	private Evolucion<Boolean> hayPotObligatoria;
	private Evolucion<Double> costoFallaPotOblig;
	private double[] potOblig;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;
	private Evolucion<Double> costoVariable;
	private Hashtable<String, DatosVariableEstado> varsEstado;
	private Evolucion<Double> energAlmacMax;
	private Evolucion<Double> energIniPaso; // energóa al inicio de cada paso para el comportamiento cierraPaso con
											// Balance Cronológico
	private Hashtable<String, Evolucion<String>> valoresComportamientos;

	public DatosAcumuladorCorrida(String nombre, String propietario, String barra, Evolucion<Integer> cantModInst,
			Evolucion<Double> potMin, Evolucion<Double> potMax, Evolucion<Double> potAlmacenadaMin,
			Evolucion<Double> potAlmacenadaMax, Evolucion<Double> energAlmacMax, Evolucion<Double> rendIny,
			Evolucion<Double> rendAlmac, Integer cantModIni, Evolucion<Double> dispMedia,
			Evolucion<Double> tMedioArreglo, boolean salDetallada, Evolucion<Integer> mantProgramado,
			Evolucion<Double> costoFijo, Evolucion<Double> costoVariable,
			Hashtable<String, DatosVariableEstado> varsEstado, Evolucion<Double> factorUso,
			Evolucion<Double> energIniPaso, boolean salDet, Evolucion<Boolean> hayPotOblig,
			Evolucion<Double> costoFallaPotOblig, double[] potOblig) {
		super();
		this.nombre = nombre;
		this.propietario = propietario;
		this.barra = barra;
		this.cantModInst = cantModInst;
		this.potMin = potMin;
		this.potMax = potMax;
		this.potAlmacenadaMin = potAlmacenadaMin;
		this.potAlmacenadaMax = potAlmacenadaMax;
		this.rendIny = rendIny;
		this.rendAlmac = rendAlmac;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.salDetallada = salDetallada;
		this.mantProgramado = mantProgramado;
		this.costoFijo = costoFijo;
		this.costoVariable = costoVariable;
		this.setVarsEstado(varsEstado);
		this.energAlmacMax = energAlmacMax;
		this.setValoresComportamientos(new Hashtable<String, Evolucion<String>>());
		this.factorUso = factorUso;
		this.energIniPaso = energIniPaso;
		this.salDetallada = salDet;
		this.hayPotObligatoria = hayPotOblig;
		this.costoFallaPotOblig = costoFallaPotOblig;
		this.potOblig = potOblig;

	}

	public String getNombre() {
		return nombre;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
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

	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}

	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
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

	public Evolucion<Double> getPotAlmacenadaMin() {
		return potAlmacenadaMin;
	}

	public void setPotAlmacenadaMin(Evolucion<Double> potAlmacenadaMin) {
		this.potAlmacenadaMin = potAlmacenadaMin;
	}

	public Evolucion<Double> getPotAlmacenadaMax() {
		return potAlmacenadaMax;
	}

	public void setPotAlmacenadaMax(Evolucion<Double> potAlmacenadaMax) {
		this.potAlmacenadaMax = potAlmacenadaMax;
	}

	public Evolucion<Double> getRendIny() {
		return rendIny;
	}

	public void setRendIny(Evolucion<Double> rendIny) {
		this.rendIny = rendIny;
	}

	public Evolucion<Double> getRendAlmac() {
		return rendAlmac;
	}

	public void setRendAlmac(Evolucion<Double> rendAlmac) {
		this.rendAlmac = rendAlmac;
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

	public Hashtable<String, DatosVariableEstado> getVarsEstado() {
		return varsEstado;
	}

	public void setVarsEstado(Hashtable<String, DatosVariableEstado> varsEstado) {
		this.varsEstado = varsEstado;
	}

	public Evolucion<Double> getEnergIniPaso() {
		return energIniPaso;
	}

	public void setEnergIniPaso(Evolucion<Double> energIniPaso) {
		this.energIniPaso = energIniPaso;
	}

	public Evolucion<Double> getEnergAlmacMax() {
		return energAlmacMax;
	}

	public Hashtable<String, Evolucion<String>> getValoresComportamientos() {
		return valoresComportamientos;
	}

	public void setValoresComportamientos(Hashtable<String, Evolucion<String>> valoresComportamientos) {
		this.valoresComportamientos = valoresComportamientos;
	}

	public Evolucion<Double> getFactorUso() {
		return factorUso;
	}

	public void setFactorUso(Evolucion<Double> factorUso) {
		this.factorUso = factorUso;
	}

	public Evolucion<Boolean> getHayPotObligatoria() {
		return hayPotObligatoria;
	}

	public void setHayPotObligatoria(Evolucion<Boolean> hayPotObligatoria) {
		this.hayPotObligatoria = hayPotObligatoria;
	}

	public Evolucion<Double> getCostoFallaPotOblig() {
		return costoFallaPotOblig;
	}

	public void setCostoFallaPotOblig(Evolucion<Double> costoFallaPotOblig) {
		this.costoFallaPotOblig = costoFallaPotOblig;
	}

	public double[] getPotOblig() {
		return potOblig;
	}

	public void setPotOblig(double[] potOblig) {
		this.potOblig = potOblig;
	}

	public void setEnergAlmacMax(Evolucion<Double> energAlmacMax) {
		this.energAlmacMax = energAlmacMax;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if (nombre.trim().equals(""))
			errores.add("Acumulador " + nombre + ": Nombre vacío.");
		if (barra == null)
			errores.add("Acumulador " + nombre + ": Barra vacío.");
		if (cantModInst == null) {
			errores.add("Acumulador " + nombre + ": cantModInst vacío.");
		} else {
			if (cantModInst.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": cantModInst vacío.");
		}
		if (potMax == null)
			errores.add("Acumulador " + nombre + ": potMax vacío.");
		else {
			if (potMax.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": potMax vacío.");
		}
		if (potMin == null) {
			errores.add("Acumulador " + nombre + ": potMin vacío.");
		} else {
			if (potMin.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": potMin vacío.");
		}
		if (potAlmacenadaMin == null) {
			errores.add("Acumulador " + nombre + ": potAlmacenadaMin vacío.");
		} else {
			if (potAlmacenadaMin.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": potAlmacenadaMin vacío.");
		}
		if (potAlmacenadaMax == null) {
			errores.add("Acumulador " + nombre + ": potAlmacenadaMax vacío.");
		} else {
			if (potAlmacenadaMax.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": potAlmacenadaMax vacío.");
		}
		if (rendIny == null) {
			errores.add("Acumulador " + nombre + ": rendIny vacío.");
		} else {
			if (rendIny.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": rendIny vacío.");
		}
		if (rendAlmac == null) {
			errores.add("Acumulador " + nombre + ": rendAlmac vacío.");
		} else {
			if (rendAlmac.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": rendAlmac vacío.");
		}
		if (factorUso == null) {
			errores.add("Acumulador " + nombre + ": factorUso vacío.");
		} else {
			if (factorUso.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": factorUso vacío.");
		}

		if (cantModIni == null) {
			errores.add("Acumulador " + nombre + ": cantModIni vacío.");
		}

		if (dispMedia == null) {
			errores.add("Acumulador " + nombre + ": dispMedia vacío.");
		} else {
			if (dispMedia.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": dispMedia vacío.");
		}
		if (tMedioArreglo == null) {
			errores.add("Acumulador " + nombre + ": dispMedia vacío.");
		} else {
			if (tMedioArreglo.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": tMedioArreglo vacío.");
		}
		if (mantProgramado == null) {
			errores.add("Acumulador " + nombre + ": mantProgramado vacío.");
		} else {
			if (mantProgramado.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": mantProgramado vacío.");
		}
		if (hayPotObligatoria == null)
			errores.add("Acumulador " + nombre + ": hayPotObligatoria vacío.");
		else {
			if (hayPotObligatoria.controlDatosCompletos().size() > 0) {
				errores.add("Acumulador " + nombre + ": hayPotObligatoria vacío.");
			}
		}
		if (costoFallaPotOblig == null) {
			errores.add("Acumulador " + nombre + ": costoFallaPotOblig vacío.");
		} else {
			if (costoFallaPotOblig.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": costoFallaPotOblig vacío.");
		}
		if (potOblig == null) {
			errores.add("Acumulador " + nombre + ": potOblig vacío.");
		}
		if (costoFijo == null) {
			errores.add("Acumulador " + nombre + ": costoFijo vacío.");
		} else {
			if (costoFijo.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": costoFijo vacío.");
		}
		if (costoVariable == null) {
			errores.add("Acumulador " + nombre + ": costoVariable vacío.");
		} else {
			if (costoVariable.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": costoVariable vacío.");
		}

		if (energAlmacMax == null) {
			errores.add("Acumulador " + nombre + ": energAlmacMax vacío.");
		} else {
			if (energAlmacMax.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": energAlmacMax vacío.");
		}
		if (energIniPaso == null) {
			errores.add("Acumulador " + nombre + ": energIniPaso vacío.");
		} else {
			if (energIniPaso.controlDatosCompletos().size() > 0)
				errores.add("Acumulador " + nombre + ": energIniPaso vacío.");
		}
		if (valoresComportamientos == null || valoresComportamientos.size() == 0)
			errores.add("Acumulador " + nombre + ": valoresComportamientos vacío.");

		if (valoresComportamientos.get(Constantes.COMPPASO).equals(Constantes.ACUMULTIPASO)) {
			if (varsEstado == null) {
				errores.add("Acumulador " + nombre + ": varsEstado vacío.");
			} else if (varsEstado.get(Constantes.COMPPASO).controlDatosCompletos().size() > 0) {
				errores.add("Acumulador " + nombre + ": varsEstado vacío.");
			}
		}
		return errores;
	}

}
