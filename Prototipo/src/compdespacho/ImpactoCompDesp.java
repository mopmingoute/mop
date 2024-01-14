/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpactoCompDesp is part of MOP.
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
import parque.Impacto;
import parque.Participante;
import utilitarios.Constantes;

public class ImpactoCompDesp extends CompDespacho {
	private Impacto impacto;
	private String nExceso;
	private String[] nExcesop;

	public ImpactoCompDesp(Impacto impacto) {
		super();
		this.setParticipante(impacto);
		this.setImpacto(impacto);
		this.parametros = new Hashtable<String, String>();

	}

	public void crearVariablesControl() {
		long instante = impacto.getSimPaso().getCorrida().getLineaTiempo().pasoActual().getInstanteFinal();
		if (impacto.getActivo().getValor(instante)) {
			if (this.impacto.isPorPoste()) {
				for (int ip = 0; ip < this.impacto.getCantPostes(); ip++) {
					String nombre = generarNombre("exceso", Integer.toString(ip));
					DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA,
							Constantes.VCPOSITIVA, null, null);
					this.variablesControl.put(nombre, nv);
					getnExcesop()[ip] = nombre;
				}
			} else {
				String nombre = generarNombre("exceso");
				DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
						null, null);
				this.variablesControl.put(nombre, nv);
				setnExceso(nombre);
			}
		}
	}

	public void cargarRestricciones() {
		long instante = impacto.getSimPaso().getCorrida().getLineaTiempo().pasoActual().getInstanteFinal();
		if (impacto.getActivo().getValor(instante)) {
			for (Participante p : impacto.getInvolucrados()) {
				this.restricciones.putAll(p.cargarRestriccionesImpacto(impacto));
			}
		}

	}

	public void contribuirObjetivo() {
		long instante = impacto.getSimPaso().getCorrida().getLineaTiempo().pasoActual().getInstanteFinal();
		if (impacto.getActivo().getValor(instante)) {
			DatosObjetivo costo = new DatosObjetivo();

			for (Participante p : impacto.getInvolucrados()) {
				p.aportarImpacto(impacto, costo);
			}

			this.objetivo.contribuir(costo);
		}
	}

	public Impacto getImpacto() {
		return impacto;
	}

	public void setImpacto(Impacto impacto) {
		this.impacto = impacto;
	}

	public String getnExceso() {
		return nExceso;
	}

	public void setnExceso(String nExceso) {
		this.nExceso = nExceso;
	}

	public String[] getnExcesop() {
		return nExcesop;
	}

	public void setnExcesop(String[] nExcesop) {
		this.nExcesop = nExcesop;
	}

}
