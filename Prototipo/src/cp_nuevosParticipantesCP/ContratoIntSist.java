/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoIntSist is part of MOP.
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

package cp_nuevosParticipantesCP;

import java.util.ArrayList;
import java.util.Hashtable;

import cp_compdespProgEst.ContratoIntCompDespPE;
import cp_compdespProgEst.EolicoCompDespPE;
import cp_compdespProgEst.FallaCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import parque.Impacto;
import parque.Participante;
import procesosEstocasticos.ProcesoEstocastico;

/**
 * Participante que representa para el sistema eléctrico 
 * la regla de despacho de un contrato interrumpible en el problema de corto plazo.
 * @author ut469262
 *
 */
public class ContratoIntSist extends Participante {
	
	private ContratoIntCompDespPE cdPE;
	

	public ContratoIntSist(String nombre) {
		this.setNombre(nombre);
	}

	@Override
	public void asignaVAOptim() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void asignaVASimul() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void crearCompDespPE() {
		ContratoIntCompDespPE compDespPE = new ContratoIntCompDespPE();
		this.setCompDespPE(compDespPE);	
		compDespPE.setParticipante(this);
		
	}
	

	
}
