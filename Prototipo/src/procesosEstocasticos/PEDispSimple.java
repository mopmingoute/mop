/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PEDispSimple is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import estado.VariableEstado;
import tiempo.PasoTiempo;


/**
 * Proceso estoc�stico cuya VA tiene una distribuci�n uniforme, independiente entre realizaciones
 * sucesivas
 * @author ut469262
 *
 */
public class PEDispSimple extends ProcesoEstocastico{
	
	private VariableAleatoria unif;  

	public PEDispSimple(String nombre) {		
		super();
		this.setNombre(nombre);
		this.setMuestreado(false);
		this.setDiscretoExhaustivo(false);
	}

	@Override
	public void producirRealizacion(long instante) {
		unif.setValor(this.getGeneradoresAleatorios().get(0).generarValor());
		
	}
	
	@Override
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		// Deliberadamente en blanco
	}
	
	@Override
	public void inicializar(Semilla semGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida,
			int escenario) {
		super.setSemGeneral(semGeneral);
		super.setInicioSorteos(inicioSorteos);
		super.setInicioCorrida(inicioCorrida);
		super.setEscenario(escenario);
		// TODO: ATENCION QUE EL FOR NO SE TIENE QUE HACER EN LOS PROCESOS HIST�RICOS. 
		super.getGeneradoresAleatorios().clear();
		super.getInnovacionesCorrientes().clear();
		GeneradorDistUniformeLCXOr gen = new GeneradorDistUniformeLCXOr(generarInnovacionInicial(semGeneral,unif.getNombre(), inicioSorteos, escenario, 0));
		super.getGeneradoresAleatorios().add(gen);
		super.getInnovacionesCorrientes().add(gen.generarValor());	
	}			
		
		
	
	@Override
	public int pasoDelAnio(long instante){
		return -1;		
	}
	
	
//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean tieneVEOptim() {
		return false;
	}


	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// Deliberadamente en blanco
	}
	
	/**
	 * ATENCION: se devuelve un valor cualquiera porque en este proceso
	 * no se usa el resultado.
	 */
	public int pasoDelAnio(int instante){
		return 0;
	}

	public VariableAleatoria getUnif() {
		return unif;
	}

	public void setUnif(VariableAleatoria unif) {
		this.unif = unif;
	}

	@Override
	public void producirRealizacionSinPronostico(long instante) {
		// DELIBERADAMENTE EN BLANCO
		
	}

	

	
	
	
	
	
	
	
}
