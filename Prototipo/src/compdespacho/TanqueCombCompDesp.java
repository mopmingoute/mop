/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TanqueCombCompDesp is part of MOP.
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

package compdespacho;

import java.util.Hashtable;

import compgeneral.CompGeneral;
import datatypes.DatosTanqueCombustibleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import parque.TanqueCombustible;
import utilitarios.Constantes;

/**
 * Clase encargada de modelar el comportamiento del tanque de combustible en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */
public class TanqueCombCompDesp extends CompDespacho {

	private Double cantIni;
	/** Cantidad inicial en unidad de combustible del tanque */
	private Double valComb;
	/** Valor del combustible */

	private TanqueCombustible tanque;

	private String nvarvol;
	private String ncantfin;

	public TanqueCombCompDesp(DatosTanqueCombustibleCorrida dT, TanqueCombustible tanqueCombustible) {
		super();
		this.cantIni = dT.getCantIni();
		this.valComb = dT.getValComb();

		this.variablesControl = new Hashtable<String, DatosVariableControl>();
		this.restricciones = new Hashtable<String, DatosRestriccion>();
		this.objetivo = new DatosObjetivo();
		this.participante = tanqueCombustible;
		this.tanque = tanqueCombustible;

		Hashtable<String, String> parametros = new Hashtable<String, String>();

		String compValoresBellman = CompGeneral.getCompsGlobales().get(Constantes.COMPVALORESBELLMAN);
		parametros.put(Constantes.COMPVALORESBELLMAN, compValoresBellman);

		this.setParametros(parametros);
	}

	@Override
	public void crearVariablesControl() {
		// Crear varVol variable libre (puede ser negativa)
		// cotaSuperior y cotaInferior segón 2.7.3
		nvarvol = generarNombre("varVol");
		DatosVariableControl nv = new DatosVariableControl(nvarvol, Constantes.VCCONTINUA, Constantes.VCLIBRE,
				-this.cantIni, tanque.getCapacidad() - this.cantIni);
		this.variablesControl.put(nvarvol, nv);

		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		if (compValoresBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			// Crear cantFin
			setNcantfin(generarNombre("cantFin"));
			nv = new DatosVariableControl(ncantfin, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
					Constantes.INFNUESTRO);
			this.variablesControl.put(ncantfin, nv);

		}
	}

	@Override
	public void cargarRestricciones() {
		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		if (compValoresBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			// restricción de definición de cantFin
			DatosRestriccion nr = new DatosRestriccion();
			String nombre = generarNombre("balance");
			nr.agregarTermino(ncantfin, 1.0);
			nr.agregarTermino(nvarvol, -1.0);
			nr.setSegundoMiembro(this.cantIni);
			nr.setNombre(nombre);
			nr.setTipo(Constantes.RESTIGUAL);
			this.restricciones.put(nombre, nr);
		}

	}

	@Override
	public void contribuirObjetivo() {
		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		if (compValoresBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			// contribución = -varVol*valComb
			DatosObjetivo no = new DatosObjetivo();
			no.agregarTermino(nvarvol, -this.valComb);
			this.objetivo.contribuir(no);
		}
	}

	public Double getCantIni() {
		return cantIni;
	}

	public void setCantIni(Double cantIni) {
		this.cantIni = cantIni;
	}

	public Double getValComb() {
		return valComb;
	}

	public void setValComb(Double valComb) {
		this.valComb = valComb;
	}

	public String getNvarvol() {
		return nvarvol;
	}

	public void setNvarvol(String nvarvol) {
		this.nvarvol = nvarvol;
	}

	public String getNcantfin() {
		return ncantfin;
	}

	public void setNcantfin(String ncantfin) {
		this.ncantfin = ncantfin;
	}

}
