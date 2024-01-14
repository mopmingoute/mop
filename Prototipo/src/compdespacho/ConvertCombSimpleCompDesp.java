/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConvertCombSimpleCompDesp is part of MOP.
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

import datatypes.DatosConvertidorCombustibleSimpleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import parque.ConvertidorCombustible;
import parque.ConvertidorCombustibleSimple;
import utilitarios.Constantes;

/**
 * Clase encargada de modelar el comportamiento del convertidor de combustible
 * en el problema de despacho
 * 
 * @author ut602614
 *
 */
public class ConvertCombSimpleCompDesp extends CompDespacho {
	private ConvertidorCombustible convertidor;

	private String nflujoOrigen;
	private String nflujoConvertido;

	public ConvertCombSimpleCompDesp(ConvertidorCombustibleSimple asociado) {
		setConvertidor(asociado);
		this.participante = asociado;

		this.variablesControl = new Hashtable<String, DatosVariableControl>();
		this.restricciones = new Hashtable<String, DatosRestriccion>();
		this.objetivo = new DatosObjetivo();
	}

	public ConvertCombSimpleCompDesp(DatosConvertidorCombustibleSimpleCorrida dccc,
			ConvertidorCombustibleSimple asociado) {
		setConvertidor(asociado);
		this.participante = asociado;

	}

	public Hashtable<String, DatosVariableControl> getVariablesControl() {
		return variablesControl;
	}

	public void setVariablesControl(Hashtable<String, DatosVariableControl> variablesControl) {
		this.variablesControl = variablesControl;
	}

	public void setRestricciones(Hashtable<String, DatosRestriccion> restricciones) {
		this.restricciones = restricciones;
	}

	public DatosObjetivo getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(DatosObjetivo objetivo) {
		this.objetivo = objetivo;
	}

	public String getNflujoOrigen() {
		return nflujoOrigen;
	}

	public void setNflujoOrigen(String nflujoOrigen) {
		this.nflujoOrigen = nflujoOrigen;
	}

	public String getNflujoConvertido() {
		return nflujoConvertido;
	}

	public void setNflujoConvertido(String nflujoConvertido) {
		this.nflujoConvertido = nflujoConvertido;
	}

	@Override
	public void crearVariablesControl() {
		// crear flujoOrigen y flujoConv
		String nombre = generarNombre("flujoOrigen");
		this.nflujoOrigen = nombre;
		DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
				Constantes.INFNUESTRO);
		this.variablesControl.put(nombre, nv);
		nombre = generarNombre("flujoConvertido");
		this.nflujoConvertido = nombre;
		nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
				convertidor.getFlujoMaxConvertido());
		this.variablesControl.put(nombre, nv);
	}

	@Override
	public void cargarRestricciones() {
		// crear restricción 2.6.3.1
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(nflujoConvertido, 1 / convertidor.getRelacion());
		nr.agregarTermino(nflujoOrigen, -1.0);
		nr.setTipo(Constantes.RESTIGUAL);
		String nombre = generarNombre("conversión");
		nr.setNombre(nombre);
		this.restricciones.put(nombre, nr);
	}

	@Override
	public void contribuirObjetivo() {

	}

	public ConvertidorCombustible getConvertidor() {
		return convertidor;
	}

	public void setConvertidor(ConvertidorCombustible convertidor) {
		this.convertidor = convertidor;
	}

}
