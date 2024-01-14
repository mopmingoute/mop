/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TermicoCompDespPE is part of MOP.
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

import java.util.Hashtable;

import compdespacho.CicloCombCompDesp;
import compdespacho.TermicoCompDesp;
import cp_datatypesEntradas.DatosCicloCombCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_datatypesEntradas.DatosTermicoCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import parque.CicloCombinado;
import parque.GeneradorTermico;
import tiempo.Evolucion;
import utilitarios.Constantes;

public class TermicoCompDespPE extends GeneradorCompDespPE{
	
	private TermicoCompDesp compDesp;
	private GeneradorTermico gt;
	
	private DatosTermicoCP dCP;
	
	private int[] cantModDisp;
	
	private double potMax;  // potencia máxima de un módulo
	private double potMin;  // potencia mínima de un módulo
	private Hashtable<String, Evolucion<Double>> rendsPotMin;  // rendimiento en el mínimo técnico, clave nombre de combustible
	private Hashtable<String, Evolucion<Double>> rendsPotMax;  // rendimiento en el máximo
	private double rendMin;
	private double rendMax;
	private long instIniCP;
	private int cantModIni;  // cantidad de módulos operando al fin del paso anterior al inicio del horizonte
	private double costoArr1Mod; // costo de arranque de un módulo
	
	double potTerProp; 
	double potTerMinTec; 
	
	public TermicoCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(TermicoCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public GeneradorTermico getGt() {
		return gt;
	}

	public void setGt(GeneradorTermico gt) {
		this.gt = gt;
	}

	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosTermicoCP)dpcp;	
	
	}
	

	@Override
	public void completaConstruccion() {
		instIniCP = dGCP.getInstIniCP();
		long inst = instIniCP;	
		gt = (GeneradorTermico)participante;
		potMin = gt.getMinimoTecnico().getValor(inst);
		potMax = gt.getPotenciaMaxima().getValor(inst);
		// TODO  FALTA RESOLVER EL PROBLEMA DE COMBUSTIBLES MÚLTIPLES
		String nomComb1 = gt.getListaCombustibles().get(0);
		rendMin = gt.getRendsPotMin().get(nomComb1).getValor(instIniCP);
		rendMax = gt.getRendsPotMax().get(nomComb1).getValor(instIniCP);
		cantModDisp = new int[cantPos];		
		for(int p=0; p<cantPos; p++) {
			cantModDisp[p] = gt.getCantModInst().getValor(inst) - gt.getMantProgramado().getValor(inst);
		}
		
		potTerProp = ((TermicoCompDesp)gt.getCompDesp()).calcPotEspTerProp(potMin, potMax, rendMin, rendMax);
		potTerMinTec = ((TermicoCompDesp)gt.getCompDesp()).calcPotTerMinTec(potMin, rendMin);
		cantModIni = dCP.getCantModIni();
		costoArr1Mod = dCP.getCostoArr1Mod();
		
	}
	

	
	
	@Override
	public void cargarValoresRezagados() {
		grafo.cargaValRez(ConCP.NMOD, nomPar, -1, cantModIni);
	}	

	@Override
	public void crearBasesVar() {
		creaCantMod();
		creaVarArranques();
		creaPotencia();
		creaEnerTPC();
	}
	
	
	/**
	 * Crea cantidad de módulos operando 
	 */
	public void creaCantMod() {
		BaseVar bV;
		for(int p=0; p<cantPos; p++) {
			int cota = cantModDisp[p];
			bV = new BaseVar(ConCP.NMOD, nomPar, p, true, Constantes.VCENTERA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)cota, null, true);
			cargaBaseVar(bV, ConCP.NMOD, nomPar, p);		
		}			
	}
	
	
	/**
	 * Crea variables de cantidad de arranques por poste y total 
	 */
	public void creaVarArranques() {
		BaseVar bV;
		for(int p=0; p<cantPos; p++) {
			bV = new BaseVar(ConCP.ARR, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)cantModDisp[p], null, true);
			cargaBaseVar(bV, ConCP.ARR, nomPar, p);			
		}
		bV = new BaseVar(ConCP.CANTARRTOT, nomPar, cantPos-1, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
				cero, null, null, null, true);
		cargaBaseVar(bV, ConCP.CANTARRTOT, nomPar, cantPos-1);				
	}
	
	
	/**
	 * Crea potencia de la central
	 */
	public void creaPotencia() {
		BaseVar bV;
		for(int p=0; p<cantPos; p++) {
			bV = new BaseVar(ConCP.POT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, null, null, true);
			cargaBaseVar(bV, ConCP.POT, nomPar, p);		
		}					
	}
	
	
	/**
	 * Crea enerTPC
	 */
	public void creaEnerTPC(){
		BaseVar bV;
		for(int p=0; p<cantPos; p++) {
			for(String c: gt.getListaCombustibles()){
				String nomVar = ConCP.ENERTPC + "_" + c;
				bV = new BaseVar(nomVar, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, null, null, true);
				cargaBaseVar(bV, nomVar, nomPar, p);	
			}
		}
	}
	
	
	
	
	
	
	@Override
	public void crearBasesRest() {
		creaPotMinMax();
		creaEnerTermica();
		creaArranquesPos();
		creaArranquesTot();
	}
	
	
	
	
	/**
	 * Crea restricciones de máximo y mínimo técnico de la potencia
	 *
	 * Potencia máxima y mínima 
	 *
	 * pot[p] - nmod[p]*potMax <= 0
	 * pot[p] - nmod[p]*potMin >= 0
	 * para todo p=0,…, cantPos-1
	 */
	public void creaPotMinMax() {
		long inst = dGCP.getInstIniCP();
		for(int p=0; p<cantPos; p++) {			
			double potMax = gt.getPotenciaMaxima().getValor(inst);
			BaseRest br = new BaseRest(ConCP.RPOTMAX, gt.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.POT, nomPar, p, 1, null);
			br.agSumVar(ConCP.NMOD, nomPar, p, -potMax, null);
			agrega1BR(generaNomBRest(ConCP.RPOTMAX, gt.getNombre(), p), br);
			
			
			double potMin = gt.getMinimoTecnico().getValor(inst);
			br = new BaseRest(ConCP.RPOTMIN, gt.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMAYOROIGUAL);
			br.agSumVar(ConCP.POT, nomPar, p, 1, null);
			br.agSumVar(ConCP.NMOD, nomPar, p, -potMin, null);			
			agrega1BR(generaNomBRest(ConCP.RPOTMIN, gt.getNombre(), p), br);	
		}
	}
	
	
	
	/**
	 * Restricciones de energía térmica y uso de combustibles
	 *
	 * suma en c (fc * enerTp,c) 
	 * - potTerProp *potTG[p]*durposp/segxhora  
	 * - (potTerMinTec - potTerProp*potMin)*nMod[p]*durposp/segxhora
	 * 		= 0
	 * para todo p=0,…, cantPos-1
	 * 
	 */
	public void creaEnerTermica() {		
		for(int p=0; p<cantPos; p++) {					
			BaseRest br = new BaseRest(ConCP.RENERTERMICAS, gt.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			// TODO  ATENCIÓN COEFICIENTES fc DE LOS COMBUSTIBLES
			for(String c: gt.getListaCombustibles()) {
				br.agSumVar(ConCP.ENERTPC + "_" + c, nomPar, p, 1.0, null);
			}	
			br.agSumVar(ConCP.POT, nomPar, p, -potTerProp*dur1Pos/Constantes.SEGUNDOSXHORA, null);	
			br.agSumVar(ConCP.NMOD, nomPar, p, - (potTerMinTec - potTerProp*potMin)*dur1Pos/Constantes.SEGUNDOSXHORA, null);		
			agrega1BR(generaNomBRest(ConCP.RENERTERMICAS, nomPar, p), br);	
		}		
	}
	
	

	/**
	 * Definición de arranques por poste
	 * nmod[0] -  a(0)   <= nmodIni   Se cargó nmod[-1] como valor rezagado
	 * 
	 * nmod[p] - nmod[p-1] - a(p) <= 0
	 * para todo p = 1,.., cantPos-1 postes del horizonte de estudio
	 * Como hay un costo de arranque no nulo, sin no aumenta o si disminuye la cantidad de módulos de p-1 a p la variables va a su cota inferior nula.
	 * cantArr = suma en p=0,…,cantPos-1 ( a(p) )
	 */
	public void creaArranquesPos(){
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RCANTARRPOS, gt.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.NMOD, nomPar, p, 1.0, null);
			br.agSumVar(ConCP.NMOD, nomPar, p-1, -1.0, null);
			br.agSumVar(ConCP.ARR, nomPar, p, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RCANTARRPOS, gt.getNombre(), p), br);	
		}
	}
	
	
	/**
	 * Definición de arranques totales en el horizonte
	 *
	 *  cantArrTot - suma en p=0,..cantPos (arr[p]) = 0
	 *  
	 */	
	public void creaArranquesTot() {		
		BaseRest br = new BaseRest(ConCP.RCANTARRTOT, gt.getNombre(), cantPos-1, 
				cero, null, null, null,cantPos-1, Constantes.RESTIGUAL);
		br.agSumVar(ConCP.CANTARRTOT, nomPar, cantPos-1, 1.0, null);
		for(int p=0; p<cantPos; p++) {			
			br.agSumVar(ConCP.ARR, nomPar, p, -1.0, null);
		}
		agrega1BR(generaNomBRest(ConCP.RCANTARRTOT, gt.getNombre(), null), br);	
	}	
	
	
	

	@Override
	/**
	 * Se computa sólo el costo de arranques y de OyM no combustible sin considerar
	 * el costo de combustible, que se toma en el ContratoCombCanioDespPE
	 * 
	 */
	public void crearBaseObj() {

		BaseTermino bt = new BaseTermino(ConCP.CANTARRTOT, nomPar, cantPos-1, dCP.getCostoArr1Mod(), null, grafo);
		agrega1BTalObj(bt);		
		
		double cos = gt.getCostoVariable().getValor(instIniCP);
		contObjCVarOyMEnergia(cos, ConCP.POT, nomPar);
	}

	public DatosTermicoCP getdCP() {
		return dCP;
	}

	public void setdCP(DatosTermicoCP dCP) {
		this.dCP = dCP;
	}

	public int[] getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int[] cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public double getPotMax() {
		return potMax;
	}

	public void setPotMax(double potMax) {
		this.potMax = potMax;
	}

	public double getPotMin() {
		return potMin;
	}

	public void setPotMin(double potMin) {
		this.potMin = potMin;
	}



	public Hashtable<String, Evolucion<Double>> getRendsPotMin() {
		return rendsPotMin;
	}

	public void setRendsPotMin(Hashtable<String, Evolucion<Double>> rendsPotMin) {
		this.rendsPotMin = rendsPotMin;
	}

	public Hashtable<String, Evolucion<Double>> getRendsPotMax() {
		return rendsPotMax;
	}

	public void setRendsPotMax(Hashtable<String, Evolucion<Double>> rendsPotMax) {
		this.rendsPotMax = rendsPotMax;
	}



	public int getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(int cantModIni) {
		this.cantModIni = cantModIni;
	}

	public double getCostoArr1Mod() {
		return costoArr1Mod;
	}

	public void setCostoArr1Mod(double costoArr1Mod) {
		this.costoArr1Mod = costoArr1Mod;
	}

	public double getPotTerProp() {
		return potTerProp;
	}

	public void setPotTerProp(double potTerProp) {
		this.potTerProp = potTerProp;
	}

	public double getPotTerMinTec() {
		return potTerMinTec;
	}

	public void setPotTerMinTec(double potTerMinTec) {
		this.potTerMinTec = potTerMinTec;
	}




	
	

}
