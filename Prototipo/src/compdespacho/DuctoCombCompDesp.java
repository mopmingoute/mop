/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DuctoCombCompDesp is part of MOP.
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

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;
import parque.DuctoCombustible;
import utilitarios.Constantes;

/**
 * Clase encargada de modelar el comportamiento del ducto de combustible en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */

public class DuctoCombCompDesp extends CompDespacho {

	private double capacidad12;
	private double capacidad21;
	private String nflujo;
	private DuctoCombustible ducto;

	public DuctoCombCompDesp(DuctoCombustible asociado) {

		this.variablesControl = new Hashtable<String, DatosVariableControl>();
		ducto = asociado;
	}

	@Override
	public void crearVariablesControl() {
		// crear flujo en unidad de combustible por unidad de tiempo, constante en el
		// paso de barra 1 a barra 2
		// con cotas superior e inferior 2.5.2 2.5.3
		this.nflujo = generarNombre("flujo");
		DatosVariableControl nv = new DatosVariableControl(nflujo, Constantes.VCLIBRE, Constantes.VCCONTINUA,
				-this.capacidad12, this.capacidad21);
		this.variablesControl.put(nflujo, nv);

	}

	@Override
	public void cargarRestricciones() {
		// No hay restricciones

	}

	@Override
	public void contribuirObjetivo() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosObjetivo costo = new DatosObjetivo();
		costo.setTerminoIndependiente(ducto.getCostoFijo().getValor(instanteActual));
		this.objetivo.contribuir(costo);

	}

	public String getNflujo() {
		return nflujo;
	}

	public void setNflujo(String nflujo) {
		this.nflujo = nflujo;
	}

}
