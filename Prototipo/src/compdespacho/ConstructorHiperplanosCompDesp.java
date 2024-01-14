/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConstructorHiperplanosCompDesp is part of MOP.
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

import java.util.ArrayList;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import futuro.AFHiperplanos;
import futuro.Hiperplano;
import parque.ConstructorHiperplanos;
import utilitarios.Constantes;

public class ConstructorHiperplanosCompDesp extends CompDespacho {

	ConstructorHiperplanos ch;
	private AFHiperplanos afh;
	String nVBellman = "zVB"; // nombre de la variable valor de Bellman z en las ecuaciones de los hiperplanos
	ArrayList<String> nombresRestHiperplanos; // nombres de los hiperplanos como restricciones en el problema de
												// despacho

	public ConstructorHiperplanosCompDesp(ConstructorHiperplanos ch) {
		super();
		this.ch = ch;
	}

	@Override
	public void crearVariablesControl() {
		DatosVariableControl nv = new DatosVariableControl(nVBellman, Constantes.VCCONTINUA, Constantes.VCLIBRE, null,
				null);
		this.variablesControl.put(nVBellman, nv);
	}

	@Override
	/**
	 * Son las restricciones z >= valor del hiperplano en las VE continuas
	 */
	public void cargarRestricciones() {
		int ih = 0;
		nombresRestHiperplanos = new ArrayList<String>();
		for (Hiperplano hp : afh.getHiperplanos()) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(nVBellman, 1.0);
			int ivc = 0;
			for (String nVEC : afh.getNombresVECont()) {
				nr.agregarTermino(nVEC, -hp.getCoefs()[ivc]);
				ivc++;
			}
			nr.setNombre("Hiperplano-" + ih);
			nr.setSegundoMiembro(hp.getTind());
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			this.restricciones.put(nr.getNombre(), nr);
			nombresRestHiperplanos.add(nr.getNombre());
			ih++;
		}
	}

	@Override
	public void contribuirObjetivo() {
		double pasosPorAnio = Constantes.SEGUNDOSXANIO / this.ch.getDuracionPaso();
		double tasaAnual = ch.devuelveTasaDescuento();
		double factorAnual = 1 / (1 + tasaAnual);
		double factorDescuentoPaso = Math.pow(factorAnual, 1 / pasosPorAnio);
		double raizFac = Math.pow(factorDescuentoPaso, 0.5);
		DatosObjetivo costo = new DatosObjetivo();
		costo.agregarTermino(nVBellman, raizFac); // Se descuenta desde el instante final hasta el instante medio del
													// paso
		costo.setTerminoIndependiente(0.0);
		this.objetivo.contribuir(costo);
	}

	public AFHiperplanos getAfh() {
		return afh;
	}

	public void setAfh(AFHiperplanos afh) {
		this.afh = afh;
	}

	public String getnVBellman() {
		return nVBellman;
	}

	public void setnVBellman(String nVBellman) {
		this.nVBellman = nVBellman;
	}

	public ArrayList<String> getNombresRestHiperplanos() {
		return nombresRestHiperplanos;
	}

	public void setNombresRestHiperplanos(ArrayList<String> nombresRestHiperplanos) {
		this.nombresRestHiperplanos = nombresRestHiperplanos;
	}

	public ConstructorHiperplanos getCh() {
		return ch;
	}

	public void setCh(ConstructorHiperplanos ch) {
		this.ch = ch;
	}

}
