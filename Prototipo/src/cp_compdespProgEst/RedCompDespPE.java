/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCompDespPE is part of MOP.
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

import compdespacho.DemandaCompDesp;
import compdespacho.RedCompDesp;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_despacho.GrafoEscenarios;
import datatypesProblema.DatosEntradaProblemaLineal;
import parque.Barra;
import parque.Corrida;
import parque.Demanda;
import parque.Participante;
import parque.Rama;
import parque.RedElectrica;

public class RedCompDespPE extends CompDespPE{
	
	private RedCompDesp compDesp;
	private RedElectrica red;
	
	private String compRed;
	private boolean uninodal; 
	
	
	@Override
	public void completaConstruccion() {
		red = (RedElectrica)participante;
		if(uninodal) red.getBarraUnica().getCompDespPE().completaConstruccion();
	}

	public String getCompRed() {
		return compRed;
	}

	public void setCompRed(String compRed) {
		this.compRed = compRed;
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
	public void aportarVarAEntrada(DatosEntradaProblemaLineal entrada) {
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

	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}

	public RedElectrica getRed() {
		return red;
	}

	public void setRed(RedElectrica red) {
		this.red = red;
	}

	public RedCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(RedCompDesp compDesp) {
		this.compDesp = compDesp;
	}


	/**
	 * Metodos de seteo de la clase padre sobreeescritos para que la barra única reciba
	 * los atributos necesarios
	 */


	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
		if(uninodal) red.getBarraUnica().getCompDespPE().setCorrida(corrida);
	}

	public void setParticipantes(ArrayList<Participante> participantes) {
		this.participantes = participantes;
		if(uninodal) red.getBarraUnica().getCompDespPE().setParticipantes(participantes);
	}


	public void setGrafo(GrafoEscenarios grafo) {
		this.grafo = grafo;
		if(uninodal) red.getBarraUnica().getCompDespPE().setGrafo(grafo);
	}


	public void setdGCP(DatosGeneralesCP dGCP) {
		this.dGCP = dGCP;
		if(uninodal) red.getBarraUnica().getCompDespPE().setdGCP(dGCP);
	}





	

}
