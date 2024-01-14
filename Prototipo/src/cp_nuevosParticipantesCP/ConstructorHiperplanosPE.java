/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConstructorHiperplanosPE is part of MOP.
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

import cp_compdespProgEst.ConstHiperCompDespPE;
import cp_compdespProgEst.ContratoIntCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import cp_compdespProgEst.TermicoCompDespPE;
import cp_despacho.NombreBaseVar;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import parque.Impacto;
import parque.Participante;
import procesosEstocasticos.ProcesoEstocastico;

/**
 * Se crea esta clase para poder emplear hiperplanos en el problema de corto plazo
 * aunque no se disponga de un Participante ConstructorHiperplanos de la corrida, 
 * porque no se corrió la optimización, o porque se obtienen los hiperplanos por otro
 * método y se leen de un archivo Hiperplanos.txt para el corto plazo.
 * 
 * @author ut469262
 *
 */
public class ConstructorHiperplanosPE extends Participante{

	
	/**
	 * 
	 * Clave: nombre de la variable leída en el texto de hiperplanos
	 * 
	 * Valor:
	 * Nombrebase de una variable del problema de control aportada por un participante distinto del 
	 * ConstructorHiperplanosCP, por ejemplo el hidráulico que aporta su volumen final
	 * nombreBase = NOMBRE DE LA VARIABLE  + "_" + NOMBRE DEL PARTICIPANTE + "_"  + (cantPos-1)
	 * 
	 * 
	 *  
	 */
	private Hashtable<String, NombreBaseVar> nombresVEParticipantes;  
	
	
	public ConstructorHiperplanosPE(String nomPar, String tipo) {
		this.setNombre(nomPar);
		this.setTipo(tipo);		
		nombresVEParticipantes = new Hashtable<String, NombreBaseVar>();
	}
	
	

	@Override
	public void asignaVAOptim() {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public void asignaVASimul() {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public void inicializarParaEscenario() {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// DELIBERADAMENTE EN BLANCO
		return null;
	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// DELIBERADAMENTE EN BLANCO
		return null;
	}

	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// DELIBERADAMENTE EN BLANCO
		return null;
	}
	
	@Override
	public void crearCompDespPE() {
		ConstHiperCompDespPE compDespPE = new ConstHiperCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setParticipante(this);		
	}




	public Hashtable<String, NombreBaseVar> getNombresVEParticipantes() {
		return nombresVEParticipantes;
	}

	public void setNombresVEParticipantes(Hashtable<String, NombreBaseVar> nombresVEParticipantes) {
		this.nombresVEParticipantes = nombresVEParticipantes;
	}


	
	

}
