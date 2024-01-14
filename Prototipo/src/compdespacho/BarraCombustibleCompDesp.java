/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCombustibleCompDesp is part of MOP.
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

import datatypes.DatosBarraCombCorrida;
import datatypesProblema.DatosRestriccion;
import parque.BarraCombustible;
import parque.CicloCombinado;
import parque.Combustible;
import parque.ContratoCombustible;
import parque.ConvertidorCombustible;
import parque.DuctoCombustible;
import parque.GeneradorTermico;
import parque.TanqueCombustible;
import utilitarios.Constantes;

/**
 * Clase encargada de modelar el comportamiento de la barra de combustible en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */

public class BarraCombustibleCompDesp extends CompDespacho {

	private BarraCombustible barra;
	private String nombreRestBalance;
	// private String compRedComb; // atención que es un comportamiento de la red de
	// combustible y no de la barra

	public BarraCombustibleCompDesp(DatosBarraCombCorrida db, BarraCombustible asociada) {
		super();
		setBarra(asociada);
		this.participante = asociada;
		String nombreComb = barra.getComb().getNombre();
		nombreRestBalance = generarNombre("BalanceBarra", nombreComb);
	}

	public BarraCombustibleCompDesp(BarraCombustible asociada) {
		super();
		this.barra = asociada;
		this.participante = asociada;
		String nombreComb = barra.getComb().getNombre();
		nombreRestBalance = generarNombre("BalanceBarra", nombreComb);

	}

	public BarraCombustibleCompDesp() {
		super();
		String nombreComb = barra.getComb().getNombre();
		nombreRestBalance = generarNombre("BalanceBarra", nombreComb);
	}

	@Override
	public void crearVariablesControl() {
	}

	@Override
	public void cargarRestricciones() {

		// cargar 2.4.1
		DatosRestriccion nr = new DatosRestriccion();
		Combustible comb = this.barra.getComb();
		for (DuctoCombustible de : this.barra.getDuctosEntrantes()) {
			nr.agregarTermino(de.getNFlujo(), (double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		}

		for (DuctoCombustible ds : this.barra.getDuctosSalientes()) {
			nr.agregarTermino(ds.getNFlujo(), -(double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		}

		for (TanqueCombustible t : this.barra.getTanques()) {
			nr.agregarTermino(t.getNVarvol(), -1.0);
		}

		for (GeneradorTermico g : this.barra.getGeneradoresConectados()) {
			for (int p = 0; p < participante.getCantPostes(); p++) {
				nr.agregarTermino(g.getNEnerT(p, comb.getNombre()), -1 / comb.getPci());
			}
		}

		for (CicloCombinado c : this.barra.getCiclosCombConectados()) {
			for (int p = 0; p < participante.getCantPostes(); p++) {
				nr.agregarTermino(c.getNEnerT(p, comb.getNombre()), -1 / comb.getPci());
			}
		}

		for (ContratoCombustible cc : this.barra.getContratos()) {
			nr.agregarTermino(cc.getNCaudalComb(), (double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		}

		for (ConvertidorCombustible cve : this.barra.getConvertidoresEntrantes()) {
			nr.agregarTermino(cve.getNFlujoConv(), (double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		}

		for (ConvertidorCombustible cvs : this.barra.getConvertidoresSalientes()) {
			nr.agregarTermino(cvs.getNFlujoOrigen(), -(double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		}
		nr.setSegundoMiembro(0.0);
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		nr.setNombre(nombreRestBalance);
		this.restricciones.put(nr.getNombre(), nr);

		this.barra.getRedAsociada().getCompDesp().getRestricciones().put(nr.getNombre(), nr);

	}

	@Override
	public void contribuirObjetivo() {
		// Deliberadamente en blanco
	}

	public BarraCombustible getBarra() {
		return barra;
	}

	public void setBarra(BarraCombustible barra) {
		this.barra = barra;
	}

	public String getNombreRestBalance() {
		return nombreRestBalance;
	}

	public void setNombreRestBalance(String nombreRestBalance) {
		this.nombreRestBalance = nombreRestBalance;
	}

}
