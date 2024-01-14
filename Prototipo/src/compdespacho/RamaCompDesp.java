/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RamaCompDesp is part of MOP.
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

import parque.Barra;
import parque.Rama;
import utilitarios.Constantes;
import datatypes.Peaje;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;

/**
 * Clase encargada de modelar el comportamiento de la rama elóctrica en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */

public class RamaCompDesp extends CompDespacho {
	private Rama rama;
	private static String compRamaPorDefecto;
	private String compRama;

	private double potMax12;
	private double potMax21;
	private Barra barra1;
	private Barra barra2;
	private Peaje peaje12;
	private Peaje peaje21;
	private double perdidas12;
	private double perdidas21;
	private String nombreBarra1;
	private String nombreBarra2;
	private double X;

	private String[] npot12p;
	private String[] npot21p;
	private String[] npotp;

	public RamaCompDesp(Rama rama) {
		super();
		this.setRama(rama);
	}

	@Override
	public void crearVariablesControl() {
		if (compRama.equalsIgnoreCase(Constantes.RAMASIMPLE)) {
			// se crean pot12p y pot21p
			for (int p = 0; p < participante.getCantPostes(); p++) {
				this.npot12p[p] = generarNombre("pot12", Integer.toString(p));
				DatosVariableControl nv = new DatosVariableControl(npot12p[p], Constantes.VCCONTINUA,
						Constantes.VCPOSITIVA, null, this.potMax12);
				this.npot21p[p] = generarNombre("pot21p", Integer.toString(p));
				this.variablesControl.put(nv.getNombre(), nv);
				nv = new DatosVariableControl(npot21p[p], Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
						this.potMax21);
				this.variablesControl.put(nv.getNombre(), nv);
			}
		} else if (compRama.equalsIgnoreCase(Constantes.RAMADC)) {
			// se crea potp
			for (int p = 0; p < participante.getCantPostes(); p++) {
				this.npotp[p] = generarNombre("pot", Integer.toString(p));
				DatosVariableControl nv = new DatosVariableControl(npotp[p], Constantes.VCCONTINUA, Constantes.VCLIBRE,
						-this.potMax21, this.potMax12);
				this.variablesControl.put(nv.getNombre(), nv);
			}
		}

	}

	@Override
	public void cargarRestricciones() {
		if (compRama.equalsIgnoreCase(Constantes.RAMADC)) {
			// crear 2.17.1.2.3
			DatosRestriccion dr = new DatosRestriccion();
			String nombre = generarNombre("2daLeyK");
			dr.setNombre(nombre);
			BarraCompDesp barra1comp = (BarraCompDesp) this.barra1.getCompDesp();
			BarraCompDesp barra2comp = (BarraCompDesp) this.barra2.getCompDesp();
			for (int p = 0; p < participante.getCantPostes(); p++) {
				dr.agregarTermino(npotp[p], 1.0);
				dr.agregarTermino(barra1comp.getNdeltap()[p], -this.X);
				dr.agregarTermino(barra2comp.getNdeltap()[p], this.X);
			}
			dr.setTipo(Constantes.RESTIGUAL);
			dr.setSegundoMiembro(0.0);
			this.restricciones.put(dr.getNombre(), dr);
		}

	}

	@Override
	public void contribuirObjetivo() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		if (compRama.equalsIgnoreCase(Constantes.RAMASIMPLE)) {
			for (int p = 0; p < participante.getCantPostes(); p++) {
				DatosObjetivo no = new DatosObjetivo();
				no.agregarTermino(this.npot12p[p], participante.getDuracionPostes(p) * this.peaje12.getValor());
				no.agregarTermino(this.npot21p[p], participante.getDuracionPostes(p) * this.peaje21.getValor());
				this.objetivo.contribuir(no);
			}
		}
		DatosObjetivo costo = new DatosObjetivo();
		costo.setTerminoIndependiente(this.rama.getCostoFijo().getValor(instanteActual));
	}

	public static String getCompRamaPorDefecto() {
		return compRamaPorDefecto;
	}

	public static void setCompRamaPorDefecto(String compRamaPorDefecto) {
		RamaCompDesp.compRamaPorDefecto = compRamaPorDefecto;
	}

	public String getCompRama() {
		return compRama;
	}

	public void setCompRama(String compRama) {
		this.compRama = compRama;
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

	/*
	 * Devuelve la contribución de la rama this a la Barra barra para el Poste
	 * poste. Ecuación 2.16.13
	 */
	public DatosRestriccion dameAporteABarra(Barra barra, int poste) {
		DatosRestriccion nr = null;
		BarraCompDesp bc = null;
		if (!barra.isUnica()) {
			nr = new DatosRestriccion();
			if (compRama.equalsIgnoreCase(Constantes.RAMASIMPLE)) {
				if (this.nombreBarra1 == barra.getNombre()) {
					// la rama es saliente de la barra
					nr.agregarTermino(this.npot12p[poste],-1.0);
					nr.agregarTermino(this.npot21p[poste], 1.0 - this.perdidas21);
				} else if (this.nombreBarra2 == barra.getNombre()) {
					// la rama es entrante de la barra
					nr.agregarTermino(this.npot21p[poste], -1.0);
					nr.agregarTermino(this.npot12p[poste], 1.0 - this.perdidas12);
				}
			} else if (compRama.equalsIgnoreCase(Constantes.RAMADC)) {
				for (Barra b : barra.getRedAsociada().listaBarrasConectadasDC(barra)) {
					bc = (BarraCompDesp) b.getCompDesp();
					Double betaPrima = b.getRedAsociada().getCoeficienteBPrima(barra, b);
					nr.agregarTermino(bc.getNdeltap()[poste], -betaPrima);
				}
			}
		}
		return nr;
	}

	public boolean isDC() {
		return this.compRama.equalsIgnoreCase(Constantes.RAMADC);
	}

	public Rama getRama() {
		return rama;
	}

	public void setRama(Rama rama) {
		this.rama = rama;
	}

}
