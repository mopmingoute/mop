/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCicloCombCP is part of MOP.
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

package cp_datatypesEntradas;

import java.util.ArrayList;
import java.util.Hashtable;

public class DatosCicloCombCP extends DatosPartCP{
	

	//	Arranques iniciales

	private ArrayList<String> listaAIni;  // lista de arranques iniciales (que encienden el ciclo de vapor)
	private Hashtable<String, Double> horasMax;   // cantidad máxima de horas que permiten un arranque de cada tipo, permite decimales de hora
	private Hashtable<String, Integer> postesMax;   // cantidad máxima de postes que permiten un arranque de cada tipo
	private Hashtable<String, Integer> nmodAIni;  //	cantidad de módulos de TGs que arrancan en el arranque a.
	private Hashtable<String, Double> costAIni; //	costo del arranque inicial de turbinas combinadas
	private Hashtable<String, double[]> potAIni; //	potencia de rampa del arranque inicial a en el poste h-esimo a partir del poste que comienza el arranque, para todo poste h=0, …durinia-1,  del arranque a, para el conjunto de las TGs que arrancan
	private Hashtable<String, Integer> durAIni;        //  duración en postes del arranque ATENCIÓN NO INCLUYE LOS POSTES LUEGO DE QUE SE ALCANZA EL MÍNIMO TÉCNICO
	private Hashtable<String, ArrayList<Integer>> postesAIniD;    //    conjunto de postes del DIA, con los postes numerados a partir de cero, en las que puede empezar un arranque inicial a. 

	
	private int cantArrTGAbMax;  // DE TGS EN CICLO ABIERTO
	private int cantArrIniMax;   // DEL CV EN ARRANQUES INICIALES DE TGS EN CICLO COMBINADO
	private int cantArrSimMax;   // DE TGS EN ARRANQUES SIMPLES DE TGS EN CICLO COMBINADO 

	
	
	/**
	 * Para cada arranque inicial lista de postes del horizonte en los que puede empezar
	 * ese arranque inicial, se carga en completaConstruccion 
	 */
	private Hashtable<String, ArrayList<Integer>> postesAIni; 

	//	Arranques simples
	private ArrayList<String> listaASim; // lista de arranques simples (que aumentan la cantidad de TG que ya estaban combinadas (El CV ya estaba encendido)
	private Hashtable<String, Integer>  nmodASim;   // scantidad de módulos que arrancan, del arranque simple s
	private Hashtable<String, Double> costASim; //	cossimple^s  costo de arranque de una TG en ciclo abierto
	private Hashtable<String, double[]> potASim;   //  potencia del conjunto de las TG que arrancan, en el poste h-esimo a partir del poste que comienza el arranque simple, para h=0, …dursims-1,  del arranque s.
	private Hashtable<String, Integer> durASim;  // duración en postes del arranque, resulta de potAsim
	private Hashtable<String, ArrayList<Integer>> postesASimD; // postes del DIA en los que puede haber un arranque simple

	/**
	 * Para cada arranque simple lista de postes del horizonte en los que puede empezar
	 * ese arranque simple, se carga en completaConstruccion 
	 */		
	private Hashtable<String, ArrayList<Integer>> postesASim; // para cada arranque simple lista de postes del horizonte en los que puede empezar ese arranque simple, se carga en completaConstruccion 

	
	// Paradas
	private ArrayList<String> listaPar;
	private Hashtable<String, Integer> nmodPar;
	private Hashtable<String, double[]> potPar;
	private Hashtable<String, Integer> durPar;
	private Hashtable<String, ArrayList<Integer>> postesParD; // postes del DIA en los que puede haber una parada
	/**
	 * Para cada parada lista de postes del horizonte en los que puede empezar
	 * esa parada, se carga en completaConstruccion 
	 */		
	private Hashtable<String, ArrayList<Integer>> postesPar; 

	// Turbinas
	private double costArrTGAb;  // costo de arranque de una TG en ciclo abierto
	
	// Módulos iniciales
	private int cantTGDispCP;    // EVENTUALMENTE SE PUEDE SUSTITUIR POR UNA VARIABLE ALEATORIA DEL GRAFO DE ESCENARIOS
	private int cantCVDispCP;
	private double horasApagadoCV; // horas que el CV permaneció apagado antes del poste 0
	private int postesApagadoCV;
	private int nmodCCIni;     // LAS TGs QUE PERMANECIERON COMBINADAS AL FIN DEL POSTE ANTERIOR AL POSTE 0, PRIMERO DEL HORIZONTE
	private int nmodTGIni;     // LAS TGs QUE PERMANECIERON FUNCIONANDO EN CICLO ABIERTO AL FIN DEL POSTE ANTERIOR AL POSTE 0, PRIMERO DEL HORIZONTE

	// Arranques y paradas en ejecución al inicio del horizonte
	private ArrayList<String> listaEnEjec;
	private Hashtable<String, Integer> posteIniEE;  // Rezago respecto al poste inicial en que se inició el arranque o parada en ejecución. Debe ser un número negativo
	
	
	
	public DatosCicloCombCP(String nombrePart, String tipoPart, ArrayList<String> listaAIni,
			Hashtable<String, Double> horasMax, Hashtable<String, Integer> nmodAIni,
			Hashtable<String, Double> costAIni, Hashtable<String, double[]> potAIni, Hashtable<String, Integer> durAIni,
			Hashtable<String, ArrayList<Integer>> postesAIniD, ArrayList<String> listaASim, Hashtable<String, Integer> nmodASim,
			Hashtable<String, Double> costASim, Hashtable<String, double[]> potASim, Hashtable<String, Integer> durASim,
			Hashtable<String, ArrayList<Integer>> postesASimD, ArrayList<String> listaPar, Hashtable<String, Integer> nmodPar,
			Hashtable<String, double[]> potPar, Hashtable<String, Integer> durPar, Hashtable<String, ArrayList<Integer>> postesParD,
			double costArrTGAb, double horasApagadoCV, int nmodCCIni, int nmmodTGIni, ArrayList<String> listaEnEjec, Hashtable<String, Integer> posteIniEE,
			int cantMab, int cantMIni, int cantMSim) {
		super(nombrePart, tipoPart);
		this.listaAIni = listaAIni;
		this.horasMax = horasMax;
		this.nmodAIni = nmodAIni;
		this.costAIni = costAIni;
		this.potAIni = potAIni;
		this.durAIni = durAIni;
		this.postesAIniD = postesAIniD;
		this.listaASim = listaASim;
		this.nmodASim = nmodASim;
		this.costASim = costASim;
		this.potASim = potASim;
		this.durASim = durASim;
		this.postesASimD = postesASimD;
		this.listaPar = listaPar;
		this.nmodPar = nmodPar;
		this.potPar = potPar;
		this.durPar = durPar;
		this.postesParD = postesParD;
		this.costArrTGAb = costArrTGAb;
		this.horasApagadoCV = horasApagadoCV;
		this.nmodCCIni = nmodCCIni;
		this.nmodTGIni = nmodTGIni;
		this.listaEnEjec = listaEnEjec;
		this.posteIniEE = posteIniEE;
		this.cantArrTGAbMax = cantMab;
		this.cantArrIniMax = cantMIni;
		this.cantArrSimMax = cantMSim;
	}
	
	public ArrayList<String> getListaAIni() {
		return listaAIni;
	}
	public void setListaAIni(ArrayList<String> listaAIni) {
		this.listaAIni = listaAIni;
	}
	public Hashtable<String, Integer> getPostesMax() {
		return postesMax;
	}
	public void setPostesMax(Hashtable<String, Integer> postesMax) {
		this.postesMax = postesMax;
	}
	public Hashtable<String, Integer> getNmodAIni() {
		return nmodAIni;
	}
	public void setNmodAIni(Hashtable<String, Integer> nmodAIni) {
		this.nmodAIni = nmodAIni;
	}
	public Hashtable<String, Double> getCostAIni() {
		return costAIni;
	}
	public void setCostAIni(Hashtable<String, Double> costAIni) {
		this.costAIni = costAIni;
	}
	public Hashtable<String, double[]> getPotAIni() {
		return potAIni;
	}
	public void setPotAIni(Hashtable<String, double[]> potAIni) {
		this.potAIni = potAIni;
	}
	public Hashtable<String, Integer> getDurAIni() {
		return durAIni;
	}
	public void setDurAIni(Hashtable<String, Integer> durAIni) {
		this.durAIni = durAIni;
	}
	
	
	
	public Hashtable<String, ArrayList<Integer>> getPostesAIniD() {
		return postesAIniD;
	}

	public void setPostesAIniD(Hashtable<String, ArrayList<Integer>> postesAIniD) {
		this.postesAIniD = postesAIniD;
	}

	public ArrayList<String> getListaASim() {
		return listaASim;
	}
	public void setListaASim(ArrayList<String> listaASim) {
		this.listaASim = listaASim;
	}
	public Hashtable<String, Integer> getNmodASim() {
		return nmodASim;
	}
	public void setNmodASim(Hashtable<String, Integer> nmodASim) {
		this.nmodASim = nmodASim;
	}
	public Hashtable<String, Double> getCostASim() {
		return costASim;
	}
	public void setCostASim(Hashtable<String, Double> costASim) {
		this.costASim = costASim;
	}
	public Hashtable<String, double[]> getPotASim() {
		return potASim;
	}
	public void setPotASim(Hashtable<String, double[]> potASim) {
		this.potASim = potASim;
	}
	public Hashtable<String, Integer> getDurASim() {
		return durASim;
	}
	public void setDurASim(Hashtable<String, Integer> durASim) {
		this.durASim = durASim;
	}

	
	
	public Hashtable<String, ArrayList<Integer>> getPostesASimD() {
		return postesASimD;
	}

	public void setPostesASimD(Hashtable<String, ArrayList<Integer>> postesASimD) {
		this.postesASimD = postesASimD;
	}

	public void setPostesParD(Hashtable<String, ArrayList<Integer>> postesParD) {
		this.postesParD = postesParD;
	}

	public ArrayList<String> getListaPar() {
		return listaPar;
	}
	public void setListaPar(ArrayList<String> listaPar) {
		this.listaPar = listaPar;
	}
	public Hashtable<String, Integer> getNmodPar() {
		return nmodPar;
	}
	public void setNmodPar(Hashtable<String, Integer> nmodPar) {
		this.nmodPar = nmodPar;
	}
	public Hashtable<String, double[]> getPotPar() {
		return potPar;
	}
	public void setPotPar(Hashtable<String, double[]> potPar) {
		this.potPar = potPar;
	}
	public Hashtable<String, Integer> getDurPar() {
		return durPar;
	}
	public void setDurPar(Hashtable<String, Integer> durPar) {
		this.durPar = durPar;
	}

	
	public int getCantTGDispCP() {
		return cantTGDispCP;
	}
	public void setCantTGDispCP(int cantTGDispCP) {
		this.cantTGDispCP = cantTGDispCP;
	}
	public int getCantCVDispCP() {
		return cantCVDispCP;
	}
	public void setCantCVDispCP(int cantCVDispCP) {
		this.cantCVDispCP = cantCVDispCP;
	}
	public int getNmodCCIni() {
		return nmodCCIni;
	}
	public void setNmodCCIni(int nmodCCIni) {
		this.nmodCCIni = nmodCCIni;
	}
	public ArrayList<String> getListaEnEjec() {
		return listaEnEjec;
	}
	public void setListaEnEjec(ArrayList<String> listaEnEjec) {
		this.listaEnEjec = listaEnEjec;
	}
	public Hashtable<String, Integer> getPosteIniEE() {
		return posteIniEE;
	}
	public void setPosteIniEE(Hashtable<String, Integer> posteIniEE) {
		this.posteIniEE = posteIniEE;
	}

	public Hashtable<String, Double> getHorasMax() {
		return horasMax;
	}

	public void setHorasMax(Hashtable<String, Double> horasMax) {
		this.horasMax = horasMax;
	}

	public Hashtable<String, ArrayList<Integer>> getPostesAIni() {
		return postesAIni;
	}

	public void setPostesAIni(Hashtable<String, ArrayList<Integer>> postesAIni) {
		this.postesAIni = postesAIni;
	}

	public Hashtable<String, ArrayList<Integer>> getPostesASim() {
		return postesASim;
	}

	public void setPostesASim(Hashtable<String, ArrayList<Integer>> postesASim) {
		this.postesASim = postesASim;
	}

	public Hashtable<String, ArrayList<Integer>> getPostesPar() {
		return postesPar;
	}

	public void setPostesPar(Hashtable<String, ArrayList<Integer>> postesPar) {
		this.postesPar = postesPar;
	}

	public int getNmodTGIni() {
		return nmodTGIni;
	}

	public void setNmodTGIni(int nmodTGIni) {
		this.nmodTGIni = nmodTGIni;
	}

	public int getCantArrTGAbMax() {
		return cantArrTGAbMax;
	}

	public void setCantArrTGAbMax(int cantArrTGAbMax) {
		this.cantArrTGAbMax = cantArrTGAbMax;
	}

	public int getCantArrIniMax() {
		return cantArrIniMax;
	}

	public void setCantArrIniMax(int cantArrIniMax) {
		this.cantArrIniMax = cantArrIniMax;
	}

	public int getCantArrSimMax() {
		return cantArrSimMax;
	}

	public void setCantArrSimMax(int cantArrSimMax) {
		this.cantArrSimMax = cantArrSimMax;
	}

	public double getCostArrTGAb() {
		return costArrTGAb;
	}

	public void setCostArrTGAb(double costArrTGAb) {
		this.costArrTGAb = costArrTGAb;
	}

	public Hashtable<String, ArrayList<Integer>> getPostesParD() {
		return postesParD;
	}

	public double getHorasApagadoCV() {
		return horasApagadoCV;
	}

	public void setHorasApagadoCV(double horasApagadoCV) {
		this.horasApagadoCV = horasApagadoCV;
	}

	public int getPostesApagadoCV() {
		return postesApagadoCV;
	}

	public void setPostesApagadoCV(int postesApagadoCV) {
		this.postesApagadoCV = postesApagadoCV;
	}


	


	
	

}
