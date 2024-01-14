/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombTopPasoFijoCompDesp is part of MOP.
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

import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import parque.ContratoCombustibleTopPasoFijo;
import utilitarios.Constantes;
import utilitarios.Recta;

public class ContratoCombTopPasoFijoCompDesp extends CompDespacho {

	private ContratoCombustibleTopPasoFijo contrato;
	private String nCaudalComb; // en unidades por hora
	private String nZCosto; // contribución al objetivo en USD
	private Double caudalMaxPaso; // en unidades por hora; atención que puede no coincidir con el del contrato
	// cuando se estó fuera del plazo top del contrato y se estó terminando el bien
	// prepago
	private ArrayList<Double> caudalMaxSubconjuntos;
	/**
	 * Rectas que limitan inferiormente el costo en la opción costos convexos del
	 * TOP La contribución de costo es zcosto >= ri, i=1,..cantidad de rectas.
	 */
	private ArrayList<Recta> rectasCosto;

	/**
	 * ATENCIóN Cuando se haga hiperplanos, el coeficiente de los volómenes prepagos
	 * de la variable dual se deberóa hacer monótono a prepo.
	 * 
	 * La ecuación de despacho tiene u1+u2+u3 = u volumen total ui volumen tomado
	 * del prepago i ui<total volumen prepago i (variable dual li
	 * 
	 * Las variables li son los coeficientes de los hiperplanos.
	 *
	 */

	// OJO CONSTRUCTOR

	@Override
	public void crearVariablesControl() {
		String compCostos = parametros.get(Constantes.COMPCOSTOSTOP);
		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		if (compValoresBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			if (compCostos.equalsIgnoreCase(Constantes.COSTOSCONVEXOS)) {
				// crear cantComb caudal en unidades de combustible por unidad de tiempo
				// Con cota superior caudalMax que puede ser menor que la nominal

				nCaudalComb = generarNombre("caudalComb");
				DatosVariableControl nv = new DatosVariableControl(this.nCaudalComb, Constantes.VCCONTINUA,
						Constantes.VCPOSITIVA, null, this.caudalMaxPaso);
				this.variablesControl.put(this.nCaudalComb, nv);
				this.contrato.getRedAsociada().getCompDesp().getVariablesControl().put(this.nCaudalComb, nv);

				nZCosto = generarNombre("costoTOP");
				DatosVariableControl nZ = new DatosVariableControl(this.nZCosto, Constantes.VCCONTINUA,
						Constantes.VCPOSITIVA, null, null);
				this.variablesControl.put(this.nCaudalComb, nZ);
				this.contrato.getRedAsociada().getCompDesp().getVariablesControl().put(this.nZCosto, nZ);

			}
		}
	}

	@Override
	public void cargarRestricciones() {
		/**
		 * Restricciones de los costos convexos, que son de la forma zCosto - ai*caudal
		 * >= bi donde la recta i es de la forma ai*x + bi
		 */
		String compCostos = parametros.get(Constantes.COMPCOSTOSTOP);
		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		if (compValoresBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			if (compCostos.equalsIgnoreCase(Constantes.COSTOSCONVEXOS)) {
				int ir = 1;
				for (Recta r : rectasCosto) {
					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(nZCosto, 1.0);
					nr.agregarTermino(nCaudalComb, -r.getA());
					nr.setNombre("Recta-" + ir);
					nr.setSegundoMiembro(r.getB());
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
					this.restricciones.put(nr.getNombre(), nr);
					ir++;
				}
			}
		}
	}

	@Override
	public void contribuirObjetivo() {
		// TODO Auto-generated method stub

	}

	public ContratoCombustibleTopPasoFijo getContrato() {
		return contrato;
	}

	public void setContrato(ContratoCombustibleTopPasoFijo contrato) {
		this.contrato = contrato;
	}

	public Double getCaudalMaxPaso() {
		return caudalMaxPaso;
	}

	public void setCaudalMax(Double caudalMaxPaso) {
		this.caudalMaxPaso = caudalMaxPaso;
	}

	public String getNCaudalComb() {
		return nCaudalComb;
	}

	public void setNCaudalComb(String nCaudalComb) {
		this.nCaudalComb = nCaudalComb;
	}

	public String getnZCosto() {
		return nZCosto;
	}

	public void setnZCosto(String nZCosto) {
		this.nZCosto = nZCosto;
	}

	public ArrayList<Recta> getRectasCosto() {
		return rectasCosto;
	}

	public void setRectasCosto(ArrayList<Recta> rectasCosto) {
		this.rectasCosto = rectasCosto;
	}

	public String getnCaudalComb() {
		return nCaudalComb;
	}

	public void setnCaudalComb(String nCaudalComb) {
		this.nCaudalComb = nCaudalComb;
	}

	public ArrayList<Double> getCaudalMaxSubconjuntos() {
		return caudalMaxSubconjuntos;
	}

	public void setCaudalMaxSubconjuntos(ArrayList<Double> caudalMaxSubconjuntos) {
		this.caudalMaxSubconjuntos = caudalMaxSubconjuntos;
	}

	public void setCaudalMaxPaso(Double caudalMaxPaso) {
		this.caudalMaxPaso = caudalMaxPaso;
	}

}
