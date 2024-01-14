/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorCompSim is part of MOP.
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

package compsimulacion;

import java.util.Hashtable;

import compdespacho.GeneradorCompDesp;

import parque.Generador;
import datatypesProblema.DatosSalidaProblemaLineal;

public class GeneradorCompSim extends CompSimulacion{
	private Generador generador;
	private GeneradorCompDesp compD;


	public GeneradorCompSim() {
		super();
		generador = (Generador)this.getParticipante();
		compD = (GeneradorCompDesp)this.getCompdespacho();
	}


	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cargarDatosParaUnaIteracion(int iter,
			DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean aceptaDetenerIteracion(int iter,DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void inicializarParaEscenario() {
		compD.setNpotp(new String[generador.getCantPostes()]);
	}


	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String,String> comps) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		compD.setNpotp(new String[generador.getCantPostes()]);		
	}
	
	public GeneradorCompDesp getCompD() {
		return compD;
	}


	public void setCompD(GeneradorCompDesp compD) {
		this.compD = compD;
	}


	public Generador getGenerador() {
		return generador;
	}


	public void setGenerador(Generador generador) {
		this.generador = generador;
	}
	
	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////	
	
	@Override
	public void actualizarOtrosDatosIniciales() {
		// No hace nada deliberadamente
		// Puede ser sobre escrito por métodos de las clases hijas
		
	}

	
	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		return 0;
	}


	
	
	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);
		
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		cargarDatosCompDespacho(instante);
		
	}
	
	////////////////////////////////////////////////////////////////////////////////	


}
