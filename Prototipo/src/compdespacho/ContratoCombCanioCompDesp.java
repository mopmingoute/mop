/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombCanioCompDesp is part of MOP.
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

import parque.ContratoCombustibleCanio;
import utilitarios.Constantes;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;

/**
 * Clase encargada de modelar el comportamiento del contrato de combustible en
 * el problema de despacho
 * 
 * @author ut602614
 *
 */
public class ContratoCombCanioCompDesp extends CompDespacho {
	private Double precioComb;
	private Double caudalMax; // en unidades de combustible por hora
	private ContratoCombustibleCanio contrato;
	private Integer cantModDisp; // cantidad de modulos disponibles
	private String nCaudalComb;

	public ContratoCombCanioCompDesp(ContratoCombustibleCanio contratoCombustibleCanio) {
		this.contrato = contratoCombustibleCanio;
	}

	@Override
	public void crearVariablesControl() {
		// crear cantComb //caudal en unidades de combustible por unidad de tiempo
		// Con cota superior caudalMax
		nCaudalComb = generarNombre("caudalComb");
		DatosVariableControl nv = new DatosVariableControl(this.nCaudalComb, Constantes.VCCONTINUA,
				Constantes.VCPOSITIVA, null, this.caudalMax * this.cantModDisp);
		this.variablesControl.put(this.nCaudalComb, nv);
		this.contrato.getRedAsociada().getCompDesp().getVariablesControl().put(this.nCaudalComb, nv);
	}

	@Override
	public void cargarRestricciones() {
		// No tiene
	}

	@Override
	public void contribuirObjetivo() {
		// precioComb*durpaso*caudalComb
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		ContratoCombustibleCanio ccc = (ContratoCombustibleCanio) this.getParticipante();
		this.objetivo.agregarTermino(nCaudalComb, this.precioComb * ccc.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		this.contrato.getRedAsociada().getCompDesp().getObjetivo().agregarTermino(nCaudalComb,
				this.precioComb * ccc.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
		DatosObjetivo costo = new DatosObjetivo();
		costo.setTerminoIndependiente(contrato.getCostoFijo().getValor(instanteActual));
		this.objetivo.contribuir(costo);
	}

	public Double getPrecioComb() {
		return precioComb;
	}

	public void setPrecioComb(Double precioComb) {
		this.precioComb = precioComb;
	}

	public Integer getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(Integer cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public Double getCaudalMax() {
		return caudalMax;
	}

	public void setCaudalMax(Double caudalMax) {
		this.caudalMax = caudalMax;
	}

	public ContratoCombustibleCanio getContrato() {
		return contrato;
	}

	public void setContrato(ContratoCombustibleCanio contrato) {
		this.contrato = contrato;
	}

	public String getnCaudalComb() {
		return nCaudalComb;
	}

	public void setnCaudalComb(String nCaudalComb) {
		this.nCaudalComb = nCaudalComb;
	}

	public String getNCaudalComb() {
		return nCaudalComb;
	}

	public void setNCaudalComb(String nCaudalComb) {
		this.nCaudalComb = nCaudalComb;
	}

}
