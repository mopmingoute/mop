/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCosMargSP is part of MOP.
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

package datatypesSalida;

import java.io.Serializable;

/**
 * Almacena los costos marginales por barra, escenario, paso y poste resultantes
 * de una corrida
 * 
 * @author ut469262
 *
 */
public class DatosCosMargSP implements Serializable {

	private double[][][][] cosmar_cron; // índices barra, paso, escenario, poste

	public DatosCosMargSP(double[][][][] cosmar_cron) {
		super();
		this.cosmar_cron = cosmar_cron;
	}

	public double[][][][] getCosmar_cron() {
		return cosmar_cron;
	}

	public void setCosmar_cron(double[][][][] cosmar_cron) {
		this.cosmar_cron = cosmar_cron;
	}

}
