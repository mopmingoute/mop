/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCompDesp is part of MOP.
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

import logica.CorridaHandler;
import parque.Acumulador;
import parque.Barra;
import parque.Demanda;
import parque.Generador;
import parque.ImpoExpo;

import parque.Rama;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import datatypes.DatosBarraCorrida;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;

/**
 * Clase encargada de modelar el comportamiento de la barra eléctrica en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */
public class BarraCompDesp extends CompDespacho {
	private Barra barra;
	private boolean flotante; // ángulo delta igual a 0 por definición

	private String[] ndeltap;

	public BarraCompDesp(DatosBarraCorrida db, Barra asociada) {
		super();
		this.barra = asociada;
		this.participante = asociada;
		this.flotante = db.isFlotante();
	}

	public BarraCompDesp(Barra asociada) {
		super();
		this.barra = asociada;
		this.participante = asociada;
		this.flotante = false;
	}

	@Override
	public void crearVariablesControl() {
		DatosVariableControl nv = null;
		CorridaHandler ch = CorridaHandler.getInstance();
		RedCompDesp rcd = (RedCompDesp) ch.getCorridaActual().getRed().getCompDesp();
		boolean uninodal = rcd.isUninodal();
		if (!uninodal) {
			for (int p = 0; p < participante.getCantPostes(); p++) {
				this.ndeltap[p] = generarNombre("delta", Integer.toString(p));
				nv = new DatosVariableControl(ndeltap[p], Constantes.VCCONTINUA, Constantes.VCLIBRE, null,
						Constantes.INFNUESTRO);
				this.variablesControl.put(nv.getNombre(), nv);
			}
			this.variablesControl.put(nv.getNombre(), nv);
			this.barra.getRedAsociada().getCompDesp().getVariablesControl().put(nv.getNombre(), nv);
		}

	}

	@Override
	public void cargarRestricciones() {
		// cargar 2.16.3  POTENCIA NETA ENTRANTE A LA BARRA = POTENCIA DEMANDADA

		DatosRestriccion nr = null;
		GeneradorCompDesp gc;
		double coef;
		for (int p = 0; p < participante.getCantPostes(); p++) {
			nr = new DatosRestriccion();

			for (Rama r : this.barra.getRamas()) {
				nr.contribuir(r.dameAporteABarra(this.barra, p));
			}

			for (Generador g : this.barra.getGeneradores()) {
				gc = (GeneradorCompDesp) g.getCompDesp();
				coef = gc.dameCoeficientePotencia(p);
				nr.agregarTermino(gc.getNpotp()[p], coef);
				if (g instanceof Acumulador) {
					Acumulador acum = (Acumulador) g;
					AcumuladorCompDesp acd = acum.getCompD();
					nr.agregarTermino(acd.getNpotAlmacp()[p], -coef);
				}
			}
			Double segundoMiembro = 0.0;
			for (Demanda d : this.barra.getDemandas()) {
				DemandaCompDesp dc = (DemandaCompDesp) d.getCompDesp();
				FallaCompDesp fc = (FallaCompDesp) d.getFalla().getCompDesp();
				for (int e = 0; e < d.getFalla().getCantEscalones(); e++) {
					nr.agregarTermino(fc.getNpotpe()[p][e], 1.0);
				}

				segundoMiembro += dc.getPotActivaPorPoste()[p];
			}
			for (ImpoExpo ie : this.barra.getImpoExpos()) {
				ImpoExpoCompDesp iec = (ImpoExpoCompDesp) ie.getCompDesp();
				if (ie.getOperacionCompraVenta().equalsIgnoreCase(Constantes.PROVCOMPRA)) {
					for (int ib = 0; ib < ie.getCantBloques(); ib++) {
						nr.agregarTermino(iec.getNpotbp()[ib][p], 1.0);
					}
				} else if (ie.getOperacionCompraVenta().equalsIgnoreCase(Constantes.PROVVENTA)) {
					for (int ib = 0; ib < ie.getCantBloques(); ib++) {
						nr.agregarTermino(iec.getNpotbp()[ib][p], -1.0);
					}
				}
			}

		
			nr.setTipo(Constantes.RESTIGUAL);
			nr.setSegundoMiembro(segundoMiembro);
			nr.setNombre(generarNombre("demandaPoste", Integer.toString(p)));
			this.restricciones.put(nr.getNombre(), nr);
			this.barra.getRedAsociada().getCompDesp().getRestricciones().put(nr.getNombre(), nr);

		}
		// TODO setear flotante
	}

	@Override
	public void contribuirObjetivo() {
		// No contribuye

	}

	public Barra getBarra() {
		return barra;
	}

	public void setBarra(Barra barra) {
		this.barra = barra;
	}

	public boolean isFlotante() {
		return flotante;
	}

	public void setFlotante(boolean flotante) {
		this.flotante = flotante;
	}

	public String[] getNdeltap() {
		return ndeltap;
	}

	public void setNdeltap(String[] ndeltap) {
		this.ndeltap = ndeltap;
	}

	/**
	 * Devuelve el nombre de la restricción de balance del poste p
	 * 
	 * @param poste
	 * @return
	 */
	public String nombreRestPoste(int p) {
		return generarNombre("demandaPoste", Integer.toString(p));
	}

}
