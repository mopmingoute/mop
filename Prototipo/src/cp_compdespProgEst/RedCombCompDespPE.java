/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCombCompDespPE is part of MOP.
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

package cp_compdespProgEst;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import compdespacho.RedCombCompDesp;
import compdespacho.TermicoCompDesp;
import compsimulacion.RedCombCompSim;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_despacho.GrafoEscenarios;
import datatypesProblema.DatosEntradaProblemaLineal;
import parque.BarraCombustible;
import parque.Corrida;
import parque.DuctoCombustible;
import parque.GeneradorTermico;
import parque.Participante;
import parque.RedCombustible;
import parque.RedElectrica;

public class RedCombCompDespPE extends CompDespPE{
	
	private boolean uninodal;
	private RedCombustible red;
	private RedCombCompDesp compDesp;
	
	



	@Override
	public void completaConstruccion() {
		red = (RedCombustible)participante;
		if(uninodal) red.getBarraunica().getCompDespPE().completaConstruccion();
	}

	
	@Override
	public void crearBasesVar() {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public void crearBasesRest() {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public void crearBaseObj() {
		// DELIBERADAMENTE EN BLANCO
		
	}

	@Override
	public void aportarVarAEntrada(DatosEntradaProblemaLineal entrada){
		// DELIBERADAMENTE EN BLANCO
	}

	@Override
	public void aportarRestAEntrada(DatosEntradaProblemaLineal entrada) {
		// DELIBERADAMENTE EN BLANCO
	}
	

	@Override
	public void aportarObjAEntrada(DatosEntradaProblemaLineal entrada) {
		// DELIBERADAMENTE EN BLANCO
	}

	public boolean isUninodal() {
		return uninodal;
	}

	public RedCombustible getRed() {
		return red;
	}

	public void setRed(RedCombustible red) {
		this.red = red;
	}

	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}



	public RedCombCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(RedCombCompDesp compDesp) {
		this.compDesp = compDesp;
	}


	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
		if(uninodal) red.getBarraunica().getCompDespPE().setCorrida(corrida);
	}

	public void setParticipantes(ArrayList<Participante> participantes) {
		this.participantes = participantes;
		if(uninodal) red.getBarraunica().getCompDespPE().setParticipantes(participantes);
	}


	public void setGrafo(GrafoEscenarios grafo) {
		this.grafo = grafo;
		if(uninodal) red.getBarraunica().getCompDespPE().setGrafo(grafo);
	}


	public void setdGCP(DatosGeneralesCP dGCP) {
		this.dGCP = dGCP;
		if(uninodal) red.getBarraunica().getCompDespPE().setdGCP(dGCP);
	}


	
	

}
