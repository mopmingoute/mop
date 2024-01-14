/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCombCompDesp is part of MOP.
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

import java.util.Iterator;
import java.util.Set;

import compsimulacion.RedCombCompSim;
import parque.BarraCombustible;
import parque.DuctoCombustible;
import parque.RedCombustible;

/**
 * Clase encargada de modelar el comportamiento de la red de combustible en el
 * problema de despacho
 * 
 * @author ut602614
 * 
 *         En las ecuaciones de despacho generadas por la red la energóa tórmica
 *         de las centrales tórmicas se expresa en MWh y los caudales en m3/h
 *
 */
public class RedCombCompDesp extends CompDespacho {

	private boolean uninodal;
	private RedCombustible red;
	private RedCombCompSim compS;

	public RedCombCompDesp(RedCombustible red) {
		this.red = red;

	}

	@Override
	public void crearVariablesControl() {
		String clave;
		Set<String> claves;
		Iterator<String> it;
		if (!uninodal) {
			claves = this.red.getBarras().keySet();
			it = claves.iterator();

			while (it.hasNext()) {
				clave = it.next();
				BarraCombustible barra = this.red.getBarras().get(clave);
				barra.getCompDesp().crearVariablesControl();

			}

			claves = this.red.getDuctos().keySet();
			it = claves.iterator();

			while (it.hasNext()) {
				clave = it.next();
				DuctoCombustible ducto = this.red.getDuctos().get(clave);
				ducto.getCompDesp().crearVariablesControl();
			}

		} else {
			this.red.getBarraunica().getCompDesp().crearVariablesControl();
		}

		claves = this.red.getContratos().keySet();
		it = claves.iterator();

		while (it.hasNext()) {
			clave = it.next();
			this.red.getContratos().get(clave).getCompDesp().crearVariablesControl();

		}

		claves = this.red.getTanques().keySet();
		it = claves.iterator();

		while (it.hasNext()) {
			clave = it.next();
			this.red.getTanques().get(clave).getCompDesp().crearVariablesControl();

		}

	}

	@Override
	public void cargarRestricciones() {
		Set<String> claves;
		Iterator<String> it;
		String clave;
		if (!uninodal) {
			claves = this.red.getBarras().keySet();
			it = claves.iterator();

			while (it.hasNext()) {
				clave = it.next();
				BarraCombustible barra = this.red.getBarras().get(clave);
				barra.getCompDesp().cargarRestricciones();

			}

			claves = this.red.getDuctos().keySet();
			it = claves.iterator();

			while (it.hasNext()) {
				clave = it.next();
				DuctoCombustible ducto = this.red.getDuctos().get(clave);
				ducto.getCompDesp().cargarRestricciones();

			}

		} else {
			this.red.getBarraunica().getCompDesp().cargarRestricciones();

		}
		claves = this.red.getContratos().keySet();
		it = claves.iterator();

		while (it.hasNext()) {
			clave = it.next();
			this.red.getContratos().get(clave).getCompDesp().cargarRestricciones();

		}

		claves = this.red.getTanques().keySet();
		it = claves.iterator();

		while (it.hasNext()) {
			clave = it.next();
			this.red.getTanques().get(clave).getCompDesp().cargarRestricciones();

		}

	}

	@Override
	public void contribuirObjetivo() {
		Set<String> claves;
		Iterator<String> it;
		String clave;
		if (!uninodal) {
			claves = this.red.getBarras().keySet();
			it = claves.iterator();

			while (it.hasNext()) {
				clave = it.next();
				BarraCombustible barra = this.red.getBarras().get(clave);
				barra.getCompDesp().contribuirObjetivo();

			}

			claves = this.red.getDuctos().keySet();
			it = claves.iterator();

			while (it.hasNext()) {
				clave = it.next();
				DuctoCombustible ducto = this.red.getDuctos().get(clave);
				ducto.getCompDesp().contribuirObjetivo();

			}
		} else {
			this.red.getBarraunica().getCompDesp().contribuirObjetivo();
		}
		claves = this.red.getContratos().keySet();
		it = claves.iterator();

		while (it.hasNext()) {
			clave = it.next();
			this.red.getContratos().get(clave).getCompDesp().contribuirObjetivo();

		}

		claves = this.red.getTanques().keySet();
		it = claves.iterator();

		while (it.hasNext()) {
			clave = it.next();
			this.red.getTanques().get(clave).getCompDesp().contribuirObjetivo();

		}

	}

	public boolean isUninodal() {
		return uninodal;
	}

	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}

	public RedCombCompSim getCompS() {
		return compS;
	}

	public void setCompS(RedCombCompSim compS) {
		this.compS = compS;
	}

}
