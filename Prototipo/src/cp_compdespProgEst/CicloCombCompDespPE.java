/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CicloCombCompDespPE is part of MOP.
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
import java.util.Hashtable;

import compdespacho.CicloCombCompDesp;
import compdespacho.EolicoCompDesp;
import cp_datatypesEntradas.DatosCicloCombCP;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosImpoExpoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import parque.CicloCombinado;
import parque.GeneradorEolico;
import utilitarios.Constantes;

public class CicloCombCompDespPE extends GeneradorCompDespPE{
	
	/**
	 * TODO:
	 * DATO TRANSITORIO: VALOR POR EL QUE SE MUTIPLICA EL COSTO DE MINIMO TÉCNICO PARA SACAR EL COSTO
	 * DE ARRANQUE Y PARADA; ESTE DATO ES ARBITRARIO
	 */
	private static final double multConsumoArrYPar = 2; 
	
	private CicloCombCompDesp compDesp;
	private CicloCombinado cc;
	
	private DatosCicloCombCP dCP;
	
	private int[] cantTGDisp; // cantidad de TGs disponibles por poste del horizonte
	private int[] cantCVDisp; // cantidad de ciclos de vapor disponibles por poste del horizonte
	
	private double potMax1TG;
	private double potMin1TG;
	private double potMax1CC;
	private double potMin1CC;
	private double potMax1CV;
	private double rendMinTG;
	private double rendMaxTG;
	private double rendMinCC;
	private double rendMaxCC;
	private long instIniCP;
	private int nModTGm1;  // cantidad de módulos de TG que operaron abiertos en p=-1
	private int nModCCm1;  // cantidad de módulos de TG que operaron combinados en p=-1 :  nModCC(-1) 
	
	double potTerPropTG; 
	double potTerMinTec1TG; 
	double potTerPropCC;
	double potTerMinTec1CC; 
	double potTerPropArr;  // 	TODO ATENCIÓN SE TOMA AHORA PROVISORIO COMO IGUAL AL COSTO MEDIO EN MININMO TÉCNICO
	double potTerPropPar;  // 	TODO ATENCIÓN SE TOMA AHORA PROVISORIO COMO IGUAL AL COSTO MEDIO EN MININMO TÉCNICO
	
	@Override
	public void completaConstruccion() {
		instIniCP = dGCP.getInstIniCP();
		long inst = instIniCP;
		cc = (CicloCombinado)participante;
		cantTGDisp = new int[cantPos];
		cantCVDisp = new int[cantPos];
		for(int p=0; p<cantPos; p++) {
			cantTGDisp[p] = cc.getTGs().getCantModInst().getValor(inst) - cc.getTGs().getMantProgramado().getValor(inst);
			cantCVDisp[p] = cc.getCCs().getCantModInst().getValor(inst) - cc.getCCs().getMantProgramado().getValor(inst);			
		}

		nModTGm1 = dCP.getNmodTGIni();
		nModCCm1 = dCP.getNmodCCIni();
		potMax1TG = cc.getTGs().getPotenciaMaxima().getValor(instIniCP);
		potMin1TG = cc.getTGs().getMinimoTecnico().getValor(instIniCP);
		potMax1CC = cc.getCCs().getPotenciaMaxima().getValor(instIniCP);
		potMin1CC = cc.getCCs().getMinimoTecnico().getValor(instIniCP);
		
		potMax1CV = cc.getPotMax1CV().getValor(instIniCP);
		// TODO  FALTA RESOLVER EL PROBLEMA DE COMBUSTIBLES MÚLTIPLES
		String nomComb1 = cc.getListaCombustibles().get(0);
		rendMinTG = cc.getTGs().getRendsPotMin().get(nomComb1).getValor(instIniCP);
		rendMaxTG = cc.getTGs().getRendsPotMax().get(nomComb1).getValor(instIniCP);
		rendMinCC = cc.getCCs().getRendsPotMin().get(nomComb1).getValor(instIniCP);
		rendMaxCC = cc.getCCs().getRendsPotMax().get(nomComb1).getValor(instIniCP);
		
		potTerPropTG = ((CicloCombCompDesp)cc.getCompDesp()).calcPotEspTerProp(potMin1TG, potMax1TG, rendMinTG, rendMaxTG);
		potTerMinTec1TG = ((CicloCombCompDesp)cc.getCompDesp()).calcPotTerMinTec(potMin1TG, rendMinTG);
		potTerPropCC = ((CicloCombCompDesp)cc.getCompDesp()).calcPotEspTerProp(potMin1CC, potMax1CC, rendMinCC, rendMaxCC);
		potTerMinTec1CC = ((CicloCombCompDesp)cc.getCompDesp()).calcPotTerMinTec(potMin1CC, rendMinCC);
		potTerPropArr = potTerMinTec1CC*multConsumoArrYPar/potMin1CC;
		potTerPropPar = potTerMinTec1CC*multConsumoArrYPar/potMin1CC;
		double horas;
		dCP.setPostesMax(new Hashtable<String, Integer>());
		for(String a: dCP.getListaAIni()) {
			if(dCP.getHorasMax().containsKey(a)) {
				horas = dCP.getHorasMax().get(a);
				int postes = (int)Math.round(horas*dGCP.getCantPosHora());
				dCP.getPostesMax().put(a, postes);
			}
		}
		
		horas = dCP.getHorasApagadoCV();
		dCP.setPostesApagadoCV((int)Math.round(horas*dGCP.getCantPosHora()));
		
		// Crea postesAIni, con listas de postes del horizonte en los que puede haber arranques iniciales
		Hashtable<String, ArrayList<Integer>> aux1 = new Hashtable<String, ArrayList<Integer>>();
		dCP.setPostesAIni(aux1);
		for(String a: dCP.getListaAIni()) {
			ArrayList<Integer> aux2 = new ArrayList<Integer>();
			dCP.getPostesAIni().put(a, aux2);
			// Se cargan los postes de arranques iniciales ya decididos, que son menores a cero
			for(String e: dCP.getListaEnEjec()){
				if(dCP.getListaAIni().contains(e)) {
					aux2.add(dCP.getPosteIniEE().get(e));
				}
			}
		}
		for(int p=0; p<cantPos; p++) {
			int pDelDia = p%dGCP.getCantPosDia(); // ordinal del poste en el día
			for(String a: dCP.getListaAIni()) {
				if(dCP.getPostesAIniD().get(a).contains(pDelDia)) 
					dCP.getPostesAIni().get(a).add(p);
			}			
		}

		// Crea postesASim, con listas de postes del horizonte en los que puede haber arranques simples
		aux1 = new Hashtable<String, ArrayList<Integer>>();
		dCP.setPostesASim(aux1);
		for(String s: dCP.getListaASim()) {
			ArrayList<Integer> aux2 = new ArrayList<Integer>();
			dCP.getPostesASim().put(s, aux2);
			// Se cargan los postes de arranques simples ya decididos, que son menores a cero
			for(String e: dCP.getListaEnEjec()){
				if(dCP.getListaASim().contains(e)) {
					aux2.add(dCP.getPosteIniEE().get(e));
				}
			}
		}
		for(int p=0; p<cantPos; p++) {
			int pDelDia = p%dGCP.getCantPosDia(); // ordinal del poste en el día
			for(String s: dCP.getListaASim()) {
				if(dCP.getPostesASimD().get(s).contains(pDelDia)) 
					dCP.getPostesASim().get(s).add(p);
			}			
		}
		
		// Crea postesPar, con listas de postes del horizonte en los que puede haber paradas
		aux1 = new Hashtable<String, ArrayList<Integer>>();
		dCP.setPostesPar(aux1);
		for(String pr: dCP.getListaPar()) {
			ArrayList<Integer> aux2 = new ArrayList<Integer>();
			dCP.getPostesPar().put(pr, aux2);
			// Se cargan los postes de paradas ya decididos, que son menores a cero
			for(String e: dCP.getListaEnEjec()){
				if(dCP.getListaPar().contains(e)) {
					aux2.add(dCP.getPosteIniEE().get(e));
				}
			}
		}
		for(int p=0; p<cantPos; p++) {
			int pDelDia = p%dGCP.getCantPosDia(); // ordinal del poste en el día
			for(String pr: dCP.getListaPar()) {
				if(dCP.getPostesParD().get(pr).contains(pDelDia)) 
					dCP.getPostesPar().get(pr).add(p);
			}			
		}
		
	}	
	
	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosCicloCombCP)dpcp;	
	
	}
	
	
	@Override
	public void cargarValoresRezagados() {
		grafo.cargaValRez(ConCP.NMODCC, nomPar, -1, nModCCm1);
		grafo.cargaValRez(ConCP.NMODTG, nomPar, -1, nModTGm1);
	}

	@Override
	public void crearBasesVar() {
		BaseVar bV;
		String nomVar;
		// Variables binarias de postes de arranques iniciales
		for(String a: dCP.getListaAIni()) {
			nomVar = ConCP.XAINI + "_" + a;
			for(Integer p: dCP.getPostesAIni().get(a)) {
				bV = new BaseVar(nomVar, nomPar, p, true, Constantes.VCENTERA, Constantes.VCPOSITIVA, false, 
						null, null, 1.0, null, true);
				cargaBaseVar(bV, nomVar, nomPar, p);
			}
		}	
		
		
		// Variables binarias de prohibición de arranques iniciales
		for(String a: dCP.getListaAIni()) {
			if(dCP.getHorasMax().containsKey(a)) {
				nomVar = ConCP.ZINI + "_" + a;
				for(Integer p: dCP.getPostesAIni().get(a)) {
					bV = new BaseVar(nomVar, nomPar, p, true, Constantes.VCBINARIA, Constantes.VCPOSITIVA, false, 
							null, null, null, null, true);
					cargaBaseVar(bV, nomVar, nomPar, p);
				}
			}
		}	
		
		// Variables binarias de postes de arranques simples
		for(String s: dCP.getListaASim()) {
			nomVar = ConCP.XASIM + "_" + s;
			for(Integer p: dCP.getPostesASim().get(s)) {
				bV = new BaseVar(nomVar, nomPar, p, true, Constantes.VCBINARIA, Constantes.VCPOSITIVA, false, 
						null, null, null, null, true);
				cargaBaseVar(bV, nomVar, nomPar, p);
			}
		}
		// Variables binarias de postes de paradas
		for(String pr: dCP.getListaPar()) {
			nomVar = ConCP.YPAR + "_" + pr;
			for(Integer p: dCP.getPostesPar().get(pr)) {
				bV = new BaseVar(nomVar, nomPar, p, true, Constantes.VCBINARIA, Constantes.VCPOSITIVA, false, 
						null, null, null, null, true);
				cargaBaseVar(bV, nomVar, nomPar, p);
			}
		}
		
		// Variables enteras de cant de módulos combinados en el paso nModCC y al inicio del paso nModIni
		// nmodCCIni[p]<= min(cantModTGDisp , cantModCVdisp* relacionTGCCInst)
		// nmodCC[p]<= min(cantModTGDisp , cantModCVdisp* relacionTGCCInst)
		//		relacionTGCCInst = entero por exceso (cantTGinstaladas/cantCVinstalados) de diseño, atributo del ciclo combinado
		
		for(int p=0; p<cantPos; p++) {
			int cotaSup = Math.min(cantTGDisp[p], cantCVDisp[p]*(int)cc.getRelacionTGCCInst());
			bV = new BaseVar(ConCP.NMODCC, nomPar, p, true, Constantes.VCENTERA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)cotaSup, null, true);
			cargaBaseVar(bV, ConCP.NMODCC, nomPar, p);			

			bV = new BaseVar(ConCP.NMODCCINI, nomPar, p, true, Constantes.VCENTERA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)cotaSup, null, true);
			cargaBaseVar(bV, ConCP.NMODCCINI, nomPar, p);			
		}	
		
		// Variables enteras de cantidad de módulos de TG en ciclo abierto en funcionamiento por poste
		for(int p=0; p<cantPos; p++) {
			bV = new BaseVar(ConCP.NMODTG, nomPar, p, true, Constantes.VCENTERA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)cantTGDisp[p], null, true);
			cargaBaseVar(bV, ConCP.NMODTG, nomPar, p);			
		}
		
		// Variables enteras de cantidad de TGs en ciclo abierto arrancadas por poste
		for(int p=0; p<cantPos; p++) {
			bV = new BaseVar(ConCP.ARRTGAB, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)cantTGDisp[p], null, true);
			cargaBaseVar(bV, ConCP.ARRTGAB, nomPar, p);			
		}
		
		// Cantidad total de arranques de TGs en ciclo abierto en el período
		// Se atribuye el último poste porque toma un valor para cada escenario desde 0
		bV = new BaseVar(ConCP.CANTARRTGAB, nomPar, cantPos-1, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
				cero, null, (double)dCP.getCantArrTGAbMax(), null, true);
		cargaBaseVar(bV, ConCP.CANTARRTGAB, nomPar, cantPos-1);	
		
		// Cantidad total de arranques iniciales de cada tipo del CV en el período
		for(String a: dCP.getListaAIni()) {
			bV = new BaseVar(ConCP.CANTARRINI + "_" + a, nomPar, cantPos-1, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)dCP.getCantArrIniMax(), null, true);
			cargaBaseVar(bV, ConCP.CANTARRINI + "_" + a, nomPar, cantPos-1);	
		}
		
		// Cantidad total de arranques simples de TGs en el período
		for(String s: dCP.getListaASim()) {
			bV = new BaseVar(ConCP.CANTARRSIM + "_" + s, nomPar, cantPos-1, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, (double)dCP.getCantArrSimMax(), null, true);
			cargaBaseVar(bV, ConCP.CANTARRSIM + "_" + s, nomPar, cantPos-1);	
		}
		
		// Variables de potencia de cada régimen de operación por poste y de la potencia total del ciclo (POT)	
		long inst = dGCP.getInstIniCP();
		int durPos = dGCP.getDur1Pos();
		
		for(int p=0; p<cantPos; p++) {
			double potMaxCCCV = cc.getTGs().getPotenciaMaxima().getValor(inst) * cantTGDisp[p]  + potMax1CV*cantCVDisp[p];
			bV = new BaseVar(ConCP.POTCC, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, potMaxCCCV, null, true);
			cargaBaseVar(bV, ConCP.POTCC, nomPar, p);		
			bV = new BaseVar(ConCP.POTAINI, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, potMaxCCCV, null, true);
			cargaBaseVar(bV, ConCP.POTAINI, nomPar, p);	
			if(!dCP.getListaASim().isEmpty()) {
				bV = new BaseVar(ConCP.POTASIM, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, potMaxCCCV, null, true);
				cargaBaseVar(bV, ConCP.POTASIM, nomPar, p);	
			}
			if(!dCP.getListaPar().isEmpty()) {
				bV = new BaseVar(ConCP.POTPAR, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, potMaxCCCV, null, true);
				cargaBaseVar(bV, ConCP.POTPAR, nomPar, p);
			}
			bV = new BaseVar(ConCP.POTTGAB, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, potMaxCCCV, null, true);
			cargaBaseVar(bV, ConCP.POTTGAB, nomPar, p);	
			bV = new BaseVar(ConCP.POT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, potMaxCCCV, null, true);
			cargaBaseVar(bV, ConCP.POT, nomPar, p);	
			inst += durPos;
		}	
		
		// Variables de energía térmica por poste y por combustible
		for(int p=0; p<cantPos; p++) {
			for(String c: cc.getListaCombustibles()){
				nomVar = ConCP.ENERTPC + "_" + c;
				bV = new BaseVar(nomVar, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, Double.MAX_VALUE, null, true);
				cargaBaseVar(bV, nomVar, nomPar, p);	
			}
		}
	}
	
	
	@Override
	public void crearBasesRest() {
		creaNModCCIni();      				// definen nMODCCINI
		creaNModCC();      				// definen nMODCC
		creaIncompArrYPar();   			// incompatibilidad entre arranques y entre arranques y paradas
		creaIncompArrIniYCCRegimen();  	// incompatibilidad entre arranques iniciales y estado de régimen de CC
		if(!dCP.getListaPar().isEmpty())
			creaRequisitosParada();			// limita cuándo puede haber paradas de TGs combinadas
		if(!dCP.getListaASim().isEmpty()) 
			creaRequisitosArrSimple();		// limita cuándo puede haber arranques simples
		creaTotalModulosDespachados();	// limita la cantidad total de módulos de TG despachados a los disponibles
		creaArrTGAbierto();				// define las variables de cants. de arranques por poste de TGs en ciclo abierto
		creaCantArrTotTGAb();				// define la variable cant de arranques total de TGs en ciclo abierto
		creaCantArrIni();				// define la variable cant de arranques iniciales total para cada tipo de arranque inicial
		if(!dCP.getListaASim().isEmpty()) 
			creaCantArrSim();				// define la variable cant de arranques simples total para cada tipo de arranque simple
		creaPotMaxMinCC();				// restricciones de potencia máxima y mínima de las turbinas en ciclo combinado
		creaPotArrIni();				// potencia de TGs en arranques iniciales para combinarse
		if(!dCP.getListaASim().isEmpty()) 
			creaPotArrSim();				// potencia de TGs en arranques simples para unirse a las ya combinadas
		if(!dCP.getListaPar().isEmpty())
			creaPotParada();			
		// potencia de TGs que estaban combinadas y están parando
		creaPotTotalDelCC();			// define la potencia total como suma de pots en ciclo abierto, combinado, en arranques y paradas
		creaPotMinMaxTGAb();			// restricciones de potencia máxima y mínima de TGs despachadas en ciclo abierto 
//		creaPotMaxCCCV();				// restricción de potencia máxima de ciclo combinado total por la disponibilidad de TGs y CVs 
		creaEnerTermica();				// definición de energías térmicas por poste y combustible
		creaRestTipoArranque();			// restricciones de tipo de arranque según tiempo previo de apagado
		
//		creaArrYParYaDecididos();		// restricciones que cargan con 1 las variables binarias con postes <0
	}
		
	/**
	 * El número nmodCCIni[p] de módulos de TG combinados al inicio de 
	 * cada poste p resulta de los módulos combinados en p-1
	 * y de los arranques decididos en postes anteriores y que en p-1 
	 * tienen su último paso de rampa.
	 * nmodCCIni[p] - nmodCC[p-1]  
	 *   -  suma en a  [si (p-duraini[a] pertenece a postesAini^a )  (xini^a[p-duraini[a]]) * nmodaini^a ) ]
	 *   -  suma en s [si (p-durasim[s] pertenece a postesAsim^s )( xsim^s[p-durasim[s]) *nmodasim^s ) ]   = 0
	 * para todo p=0,..,cantPos-1
	 * La restricción de no negatividad de nmodCC[d] impide que haya paradas de TGs combinadas que no arrancaron nunca.
	 *
	 */
	public void creaNModCCIni() {
		for(int p=0; p<cantPos; p++) {  
			 
			BaseRest br = new BaseRest(ConCP.RNMODCCINI, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);			
			br.agSumVar(ConCP.NMODCCINI, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.NMODCC, nomPar, p-1, -1.0, null);
			
			for(String a: dCP.getListaAIni()) {
				int dur = dCP.getDurAIni().get(a);
				int nmod = dCP.getNmodAIni().get(a);
				for(Integer q: postesEnLista(dCP.getPostesAIni().get(a), p-dur, p-dur)) {
					br.agSumVar(ConCP.XAINI + "_" + a, nomPar, q, -nmod, null);
				}
			}		
			for(String s: dCP.getListaASim()) {
				int dur = dCP.getDurASim().get(s);
				int nmod = dCP.getNmodASim().get(s);
				String nom = ConCP.XASIM + "_" + s;
				for(Integer q: postesEnLista(dCP.getPostesASim().get(s), p-dur, p-dur)) {
					br.agSumVar(nom, nomPar, q, -nmod, null);
				}
			}			
			agrega1BR(generaNomBRest(ConCP.RNMODCC, cc.getNombre(), p), br);	
		}
	}
	
	
	
	
	/**	
	 * El número nmodCC[p] de módulos de TG combinados en el poste p resulta de los módulos combinados al inicio de p y de las paradas decididas en p
	 * nmodCC[p]  = nmodCCIni[p]
	 *    - suma en pr [ (si (p ∈postesPar^pr )(ypar[p]^pr *nmodpar^pr)]
	 *
	 * nmodCC[p]  - nmodCCIni[p]
	 *    + suma en pr [ (si (p ∈postesPar^pr )(ypar[p]^pr *nmodpar^pr)]  = 0
	 */
	public void creaNModCC() {
		
		for(int p=0; p<cantPos; p++) {  
			 
			BaseRest br = new BaseRest(ConCP.RNMODCC, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);			
			br.agSumVar(ConCP.NMODCC, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.NMODCCINI, nomPar, p, -1.0, null);
			
			for(String pr: dCP.getListaPar()) {
				int dur = dCP.getDurPar().get(pr);
				int nmod = dCP.getNmodPar().get(pr);
				for(Integer q: postesEnLista(dCP.getPostesPar().get(pr), p, p)) {
					br.agSumVar(ConCP.YPAR + "_" + pr, nomPar, q, nmod, null);
				}
			}				
			agrega1BR(generaNomBRest(ConCP.RNMODCCINI, cc.getNombre(), p), br);	
		}
	}
		

	
	
	/** En un poste p no pueden pasar a la vez dos de las siguientes situaciones:
	 *	  hay TGs en un arranque inicial cualquiera
	 *	  hay TGs en un arranque simple cualquiera
	 *	  hay TGs en una parada cualquiera
	 *	En un poste p no pueden estar ocurriendo dos arranques iniciales diferentes
	 *	En un poste p no pueden estar ocurriendo dos arranques simples diferentes
	 *	En un poste p no pueden estar ocurriendo dos paradas diferentes
	 *
     *	suma en a  (suma en (q: postesini,p-duraini[a]+1,…,p )(xini[p]^a) ) 
	 *	+ suma en s(suma en (q: postessim , p-durasim[s]+1,…,p ) (xsim[p]^s) )
	 *	- suma en pr ((suma en (q: postespar,p-durpar[pr]+1,…p )(ypar[p]^pr) ) 
	 *			<= 1
	 */
	public void creaIncompArrYPar() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RINCOMPARRYPAR, cc.getNombre(), p, 
					uno, null, null, null, p, Constantes.RESTMENOROIGUAL);		
			for(String a: dCP.getListaAIni()) {
				int dur = dCP.getDurAIni().get(a);
				String nom = ConCP.XAINI + "_" + a;
				for(Integer q: postesEnLista(dCP.getPostesAIni().get(a), p-dur+1, p)) {
					br.agSumVar(nom, nomPar, q, 1.0, null);
				}
				
			}
			
			for(String s: dCP.getListaASim()) {
				int dur = dCP.getDurASim().get(s);
				String nom = ConCP.XASIM + "_" + s;
				for(Integer q: postesEnLista(dCP.getPostesASim().get(s), p-dur+1, p)) {
					br.agSumVar(nom, nomPar, q, 1.0, null);
				}
			}
			
			for(String pr: dCP.getListaPar()) {
				int dur = dCP.getDurPar().get(pr);
				String nom = ConCP.YPAR + "_" + pr;
				for(Integer q: postesEnLista(dCP.getPostesPar().get(pr), p-dur+1, p)) {
					br.agSumVar(nom, nomPar, q, 1.0, null);
				}
			}
			agrega1BR(generaNomBRest(ConCP.RINCOMPARRYPAR, cc.getNombre(), p), br);	
		}
	}
	
	
	/** 
	 * No puede haber un arranque inicial en curso en un poste, 
	 * si la cantidad de módulos de ciclo combinado en regimen ese poste es no nula.
	 * suma en a  (suma en (q:postesini^a,p-duraini[a]+1,..,p) (xini[q]^a) ) 
	 *          ≤  (cantTGDisp  - nmodCC[p]) / cantTGDisp  
	 * equivalente a         
	 * suma en a  (suma en (q:postesini^a,p-duraini[a]+1,..,p) (xini[q]^a * cantTGDisp) ) 
	 *       +  nmodCC[p]     ≤  cantTGDisp 
	 *           
	 * para todo p
	 */
	public void creaIncompArrIniYCCRegimen() {
		long inst = dGCP.getInstIniCP();
		int durPos = dGCP.getDur1Pos();
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RINCOMPINIREG, cc.getNombre(), p, 
					(double)cantTGDisp[p], null, null, null, p, Constantes.RESTMENOROIGUAL);	
			for(String a: dCP.getListaAIni()) {
				int dur = dCP.getDurAIni().get(a);
				String nom = ConCP.XAINI + "_" + a;
				for(Integer q: postesEnLista(dCP.getPostesAIni().get(a), p-dur+1, p)) {
					br.agSumVar(nom, nomPar, q, cantTGDisp[p], null);
				}				
			}
			br.agSumVar(ConCP.NMODCC, nomPar, p, 1.0, null);
			agrega1BR(generaNomBRest(ConCP.RINCOMPINIREG, cc.getNombre(), p), br);			
		}				
	}
	
	
	/**No puede iniciarse una parada en un poste p si no hay módulos combinados 
	 * suficientes al inicio del poste, porque estaban en régimen en el paso anterior p-1 
	 * o terminaron de arrancar en p.
	 * 
	 *  nmodCCIni[p] 
	 *  - suma en pr [(si p pertenece a postesPar^pr )(ypar[p]^pr * nmodpar^pr) ]  ≥ 0
	 * 	
	 * para todo poste p
	 * La restricción de no negatividad de nmodCC[d] impide que haya paradas de TGs combinadas que no arrancaron nunca.
	 */
	public void creaRequisitosParada() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RREQPARADA, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMAYOROIGUAL);
			br.agSumVar(ConCP.NMODCCINI, nomPar, p, 1.0, null);
			for(String pr: dCP.getListaPar()) {
				for(Integer q: postesEnLista(dCP.getPostesPar().get(pr), p, p)) {
					br.agSumVar(ConCP.YPAR + "_" + pr, nomPar, q, -dCP.getNmodPar().get(pr), null);
				}		
			}
			agrega1BR(generaNomBRest(ConCP.RREQPARADA, cc.getNombre(), p), br);		
		}	
		
	}
	
	
	/**
	 * No puede comenzar ningún arranque simple en un poste si no hay al menos 
	 * un módulo ya combinados al inicio del poste
	 * 
	 * suma en s (xsim[p]^s * nmodsim^s) - nmodCCIni[p]  <= 0
	 * para todo poste p
	 * Como no puede haber más un arranque simple por una restricción anterior,
	 * si nmoddCC>1 no hay problema.
	 */
	public void creaRequisitosArrSimple() {

		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RREQARRSIM, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);			
			for(String s: dCP.getListaASim()) {
				for(Integer q: postesEnLista(dCP.getPostesASim().get(s), p, p)) {
					br.agSumVar(ConCP.XASIM + "_" + s, nomPar, q, dCP.getNmodASim().get(s), null);
				}		
			}
			br.agSumVar(ConCP.NMODCCINI, nomPar, p, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RREQARRSIM, cc.getNombre(), p), br);		
		}	
		
	}
	
	
	
	/**
	 * Total de módulos de TG despachados combinados en régimen, 
	 * en arranques, en paradas y en ciclo simple en un poste 
	 * no excede los módulos de TG disponibles
	 *
	 * nmodCC[p] 
	 * + suma en a∈listaAini (suma en q:postesIni,p-duraini[a]+1,..,p )(xini[q]^a nmodAini^a))
	 * + suma en s ∈listaASim (suma en q:postessim,p-durasim[s]+1,.,p )(xsim[q]^s nmodAsim^s )
	 * + suma en pr ∈listaPar (suma en q:postespar,p-durpar[pr]+1,…,p )(ypar[q]^pr nmodPar^pr )
	 * + nmodTG[p]  <= cantModTGDisp[p]
	 * para todo poste p
	 */
	public void creaTotalModulosDespachados() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RTOTMODDESP, cc.getNombre(), p, 
					(double)cantTGDisp[p], null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.NMODCC, nomPar, p, 1.0, null);
			int dur;
			int nmod;
			for(String a: dCP.getListaAIni()) {
				dur = dCP.getDurAIni().get(a);
				nmod = dCP.getNmodAIni().get(a);
				for(Integer q: postesEnLista(dCP.getPostesAIni().get(a), p-dur+1, p)) {
					br.agSumVar(ConCP.XAINI + "_" + a, nomPar, q, nmod, null);
				}		
			}
			for(String s: dCP.getListaASim()) {
				dur = dCP.getDurASim().get(s);
				nmod = dCP.getNmodASim().get(s);
				for(Integer q: postesEnLista(dCP.getPostesASim().get(s), p-dur+1, p)) {
					br.agSumVar(ConCP.XASIM + "_" + s, nomPar, q, nmod, null);
				}		
			}
			for(String pr: dCP.getListaPar()) {
				dur = dCP.getDurPar().get(pr);
				nmod = dCP.getNmodPar().get(pr);
				for(Integer q: postesEnLista(dCP.getPostesPar().get(pr), p-dur+1, p)) {
					br.agSumVar(ConCP.YPAR + "_" + pr, nomPar, q, nmod, null);
				}		
			}
			br.agSumVar(ConCP.NMODTG, nomPar, p, 1.0, null);
			agrega1BR(generaNomBRest(ConCP.RTOTMODDESP, cc.getNombre(), p), br);		
		}	
	}
	
	
	
	/**
	 * Cantidad de arranques de TGs en ciclo abierto POR POSTE
	 * nmodTG[0] -  a(0)   <= nmodTGini   Se cargó nmodTG[-1] como valor rezagado
	 * 
	 * nmodTG[p] - nmodTG[p-1] - a(p) <= 0
	 * para todo p = 1,.., cantPos-1 postes del horizonte de estudio
	 * Como hay un costo de arranque no nulo, sin no aumenta o si disminuye la cantidad de módulos de p-1 a p la variables va a su cota inferior nula.
	 * cantArrTG = suma en p=0,…,cantPos-1 ( a(p) )
	 */
	public void creaArrTGAbierto() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RCANTARRTGABPOS, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.NMODTG, nomPar, p, 1.0, null);
			br.agSumVar(ConCP.NMODTG, nomPar, p-1, -1.0, null);
			br.agSumVar(ConCP.ARRTGAB, nomPar, p, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RCANTARRTGABPOS, cc.getNombre(), p), br);	
		}
	}
	
	
	
	/**
	 * Define cantArrTGAb
	 *  cantArrTGAb - suma en p=0,..cantPos (arrTGAb[p]) = 0
	 *  
	 */	
	public void creaCantArrTotTGAb() {		
		BaseRest br = new BaseRest(ConCP.RCANTARRTGABTOT, cc.getNombre(), cantPos-1, 
				cero, null, null, null,cantPos-1, Constantes.RESTIGUAL);
		br.agSumVar(ConCP.CANTARRTGAB, nomPar, cantPos-1, 1.0, null);
		for(int p=0; p<cantPos; p++) {			
			br.agSumVar(ConCP.ARRTGAB, nomPar, p, -1.0, null);
		}
		agrega1BR(generaNomBRest(ConCP.RCANTARRTGABTOT, cc.getNombre(), null), br);	
	}
	
	/**
	 * Define cantArrIni
	 *  cantArrIni^a - suma en (p:postesini^a,0+1,.,canPos-1) (xAini[p]^a) = 0
	 *  
	 *  para todo a, tipo de arranque inicial
	 */
	public void creaCantArrIni() {
		for(String a: dCP.getListaAIni()) {
			BaseRest br = new BaseRest(ConCP.RCANTARRINI + "_" + a, cc.getNombre(), cantPos-1, 
					cero, null, null, null, cantPos-1, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.CANTARRINI + "_" + a, nomPar, cantPos-1, 1.0, null);			
			for(Integer q: postesEnLista(dCP.getPostesAIni().get(a), 0, cantPos-1)) {			
				br.agSumVar(ConCP.XAINI + "_" + a, nomPar, q, -1.0, null);
			}
			agrega1BR(generaNomBRest(ConCP.RCANTARRINI + "_" + a, cc.getNombre(), cantPos-1), br);	
		}		
	}
	
	/**
	 * Define cantArrSim
	 *  cantArrIni^s - suma en (p:postesini^s,0+1,.,canPos-1) (xASim[p]^s) = 0
	 *  
	 *  para todo s, tipo de arranque simple
	 */	
	public void creaCantArrSim() {
		for(String s: dCP.getListaASim()) {
			BaseRest br = new BaseRest(ConCP.RCANTARRSIM + "_" + s, cc.getNombre(), cantPos-1, 
					cero, null, null, null, cantPos-1, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.CANTARRSIM + "_" + s, nomPar, cantPos-1, 1.0, null);
			for(Integer q: postesEnLista(dCP.getPostesASim().get(s), 0, cantPos-1)) {			
				br.agSumVar(ConCP.XASIM+ "_" + s, nomPar, q, -1.0, null);
			}
			agrega1BR(generaNomBRest(ConCP.RCANTARRSIM + "_" + s, cc.getNombre(), cantPos-1), br);	
		}	
	}
	

	/**
	 * Potencia de turbinas de ciclo combinado de turbinas en régimen
	 *
	 * potCC[p] - nmodCC[p] * Pmax1CC <= 0
	 * potCC[p] - nmodCC[p] * Pmin1CC >= 0
	 * para todo p=0,…, cantPos-1
	 */
	public void creaPotMaxMinCC() {
		long inst = dGCP.getInstIniCP();
		int durPos = dGCP.getDur1Pos();
		for(int p=0; p<cantPos; p++) {
			double potMaxCC = cc.getCCs().getPotenciaMaxima().getValor(inst);

			BaseRest br = new BaseRest(ConCP.RPOTMAXCC, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.POTCC, nomPar, p, 1, null);
			br.agSumVar(ConCP.NMODCC, nomPar, p, -potMaxCC, null);
			agrega1BR(generaNomBRest(ConCP.RPOTMAXCC, cc.getNombre(), p), br);	
			
			double potMinCC = cc.getCCs().getMinimoTecnico().getValor(inst);
			br = new BaseRest(ConCP.RPOTMINCC, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMAYOROIGUAL);
			br.agSumVar(ConCP.POTCC, nomPar, p, 1, null);
			br.agSumVar(ConCP.NMODCC, nomPar, p, -potMinCC, null);			
			agrega1BR(generaNomBRest(ConCP.RPOTMINCC, cc.getNombre(), p), br);	
			
			inst += durPos;
		}

	}
	
	/**
	 * Potencia de turbinas en arranques iniciales de CC
	 * 
	 * potAini[p] -
	 *      suma en a [suma en (q:postesini,p-duraini[a]+1,…,p )(xini[q]^a*(potaini^a (p-q))] = 0
	 *      para todo p=0,…, cantPos-1
	 * 
	 * @return
	 */
	public void creaPotArrIni() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RPOTARRINI, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.POTAINI, nomPar, p, 1.0, null);
			int dur;
			for(String a: dCP.getListaAIni()) {
				dur = dCP.getDurAIni().get(a);
				for(Integer q: postesEnLista(dCP.getPostesAIni().get(a), p-dur+1, p)) {
					br.agSumVar(ConCP.XAINI + "_" + a, nomPar, q, -dCP.getPotAIni().get(a)[p-q], null);
				}		
			}	
			agrega1BR(generaNomBRest(ConCP.RPOTARRINI, cc.getNombre(), p), br);	
		}
	}
	
	
	/**
	 * Potencia de turbinas en arranques simples de CC
	 * 
	 * 
	 * potAsim[p]
	 *   - suma en s(suma en (q:postessim,p-durasim[a]+1,.,p )((xsim[q]^s * potsim^pr(p-q)) = 0
	 *   
	 *  para todo p=0,…, cantPos-1
	 *  
	 * @return
	 */
	public void creaPotArrSim() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RPOTARRSIM, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.POTASIM, nomPar, p, 1.0, null);
			int dur;
			for(String s: dCP.getListaASim()) {
				dur = dCP.getDurASim().get(s);
				for(Integer q: postesEnLista(dCP.getPostesASim().get(s), p-dur+1, p)) {
					br.agSumVar(ConCP.XASIM + "_" + s, nomPar, q, -dCP.getPotASim().get(s)[p-q], null);
				}		
			}
			agrega1BR(generaNomBRest(ConCP.RPOTARRSIM, cc.getNombre(), p), br);	
		}
	}
	
	/**
	 * Potencia de turbinas en parada de CC
	 * 
	 * potPar[p] 
	 *     - suma en pr[suma en (q:postespar,p-durapar[pr]+1,…,p )(ypar[q]^pr * potpar^pr(p-q)] = 0
	 *     
	 * para todo p=0,…, cantPos-1

	 * @return
	 */
	public void creaPotParada() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RPOTPAR, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.POTPAR, nomPar, p, 1.0, null);
			int dur;
			for(String pr: dCP.getListaPar()) {
				dur = dCP.getDurPar().get(pr);
				for(Integer q: postesEnLista(dCP.getPostesPar().get(pr), p-dur+1, p)) {
					br.agSumVar(ConCP.YPAR + "_" + pr, nomPar, q, -dCP.getPotPar().get(pr)[p-q], null);
				}		
			}
			agrega1BR(generaNomBRest(ConCP.RPOTPAR, cc.getNombre(), p), br);	
		}
	}
	
	
	
	/**
	 * Potencia total del ciclo combinado
	 * pot[p] - potCC[p] - potAini[p] - potAsim[p]  - potPar[p] - potTG[p] = 0
	 * para p=0,.., cantPos-1
	 */ 
	public void creaPotTotalDelCC(){
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RPOTTOTAL, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.POT, nomPar, p, 1.0, null);
			br.agSumVar(ConCP.POTCC, nomPar, p, -1.0, null);
			br.agSumVar(ConCP.POTAINI, nomPar, p, -1.0, null);
			if(!dCP.getListaASim().isEmpty()) br.agSumVar(ConCP.POTASIM, nomPar, p, -1.0, null);
			if(!dCP.getListaPar().isEmpty()) br.agSumVar(ConCP.POTPAR, nomPar, p, -1.0, null);
			br.agSumVar(ConCP.POTTGAB, nomPar, p, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RPOTTOTAL, cc.getNombre(), p), br);	
		}
		
		
		
	}
	
	/**
	 * Potencia máxima y mínima de turbinas en ciclo abierto
	 *
	 * potTG[p] - nmodTG[p]*Pmax1TG <= 0
	 * potTG[p] - nmodTG[p]*Pmin1TG >= 0
	 * para todo p=0,…, cantPos-1
	 */
	public void creaPotMinMaxTGAb() {
		long inst = dGCP.getInstIniCP();
		int durPos = dGCP.getDur1Pos();
		for(int p=0; p<cantPos; p++) {
			
			double potMaxTG = cc.getTGs().getPotenciaMaxima().getValor(inst);
			BaseRest br = new BaseRest(ConCP.RPOTMAXTGAB, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.POTTGAB, nomPar, p, 1, null);
			br.agSumVar(ConCP.NMODTG, nomPar, p, -potMaxTG, null);
			agrega1BR(generaNomBRest(ConCP.RPOTMAXTGAB, cc.getNombre(), p), br);
			
			
			double potMinTG = cc.getTGs().getMinimoTecnico().getValor(inst);
			br = new BaseRest(ConCP.RPOTMINTGAB, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMAYOROIGUAL);
			br.agSumVar(ConCP.POTTGAB, nomPar, p, 1, null);
			br.agSumVar(ConCP.NMODTG, nomPar, p, -potMinTG, null);			
			agrega1BR(generaNomBRest(ConCP.RPOTMINTGAB, cc.getNombre(), p), br);	
			
			inst += durPos;
		}
	}
	

	
	
	
	
	
	
	/**
	 * Restricciones de energía térmica y uso de combustibles
	 *
	 * suma en c (fc * enerTp,c) 
	 * - potTerPropTG *durposp *potTG[p]  
	 * - (potTerMintec1TG - potTerPropTG*PMin1TG)*durposp*nModTG[p]
	 * - potTerPropCC *durposp *potCC[p] 
	 * - (potTerMintec1CC - potTerPropCC*PMin1CC)*durposp*nModCC[p] 
	 * - potTerPropArr * potAini[p]
	 * - potTerPropArr * potASim[p] 
	 * - potTerPropPar* potPar[p]  = 0
	 * ATENCIÓN CON LA ENERGÍA TÉRMICA DE ARRANQUES Y PARADAS
	 * 
	 * para todo p=0,…, cantPos-1
	 * 
	 */
	public void creaEnerTermica() {		
		for(int p=0; p<cantPos; p++) {					
			BaseRest br = new BaseRest(ConCP.RENERTERMICAS, cc.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			// TODO  ATENCIÓN COEFICIENTES fc DE LOS COMBUSTIBLES
			for(String c: cc.getListaCombustibles()) {
				br.agSumVar(ConCP.ENERTPC + "_" + c, nomPar, p, 1.0, null);
			}	
			br.agSumVar(ConCP.POTTGAB, nomPar, p, -potTerPropTG*dur1Pos/Constantes.SEGUNDOSXHORA, null);	
			br.agSumVar(ConCP.NMODTG, nomPar, p, - (potTerMinTec1TG - potTerPropTG*potMin1TG)*dur1Pos/Constantes.SEGUNDOSXHORA, null);		
			br.agSumVar(ConCP.POTCC, nomPar, p, -potTerPropCC*dur1Pos/Constantes.SEGUNDOSXHORA, null);	
			br.agSumVar(ConCP.NMODCC, nomPar, p, - (potTerMinTec1CC - potTerPropCC*potMin1CC)*dur1Pos/Constantes.SEGUNDOSXHORA, null);						
			br.agSumVar(ConCP.POTAINI, nomPar, p, -potTerPropArr*dur1Pos/Constantes.SEGUNDOSXHORA, null);	
			if(!dCP.getListaASim().isEmpty())  br.agSumVar(ConCP.POTASIM, nomPar, p, -potTerPropArr*dur1Pos/Constantes.SEGUNDOSXHORA, null);	
			if(!dCP.getListaPar().isEmpty()) br.agSumVar(ConCP.POTPAR, nomPar, p, -potTerPropPar*dur1Pos/Constantes.SEGUNDOSXHORA, null);	
			agrega1BR(generaNomBRest(ConCP.RENERTERMICAS, nomPar, p), br);	
		}		
	}
	
	
	
	
	/**
	 * Para todo p de postesIni(a), para todo arranque inicial con límite de horas máximas
	 * Se cumplen las restricciones:
	 * 
	 * zini^a[p] >=   1 – suma en (q=p-NP^a,…,p-1) ( nmodCC[p]) )
	 * xini^a[p] +  zini^a[p]  <= 1		para todo poste p
	 * 
	 * lo que es equivalente a
	 * 
	 * zini^a[p] + suma en (q=p-NP^a,..,p-1) ( nmodCC[p]) ) + aRestar >=   1  
	 * (aRestar es 1 si p< npa + 1 - apaAntCV, donde apaAntCV es la cantidad de postes apagado del CV anterior al inicio
	 *  y 0 en otro caso) 
	 * xini^a[p] +  zini^a[p]  <= 1		para todo poste p de postesIni[a]
	 * 
	 * Como no permitir un tipo de arranque tiende a aumentar el costo total, 
	 * si el arranque resulta necesario la zinia[p] va a tener a tomar su menor valor posible. 
	 * Si el ciclo de vapor estuvo prendido en alguno de los NPa postes anteriores
	 * ese menor valor posible es cero, porque suma en (q=p-NPa,…,p-1) ( nmodCC[p])  
	 * toma un valor menor o igual que cero.
	 */
	public void creaRestTipoArranque() {		
		for(String a: dCP.getListaAIni()) {
			int npa;
			if(dCP.getPostesMax().get(a)==null) {
				npa = Integer.MAX_VALUE;
			}else {
				npa = dCP.getPostesMax().get(a); // máxima cant. de postes apagado que permiten arranque a			
			}
			int apaAntCV = dCP.getPostesApagadoCV(); // cantidad de postes que llevava apagado el iniciar el horizonte CP
			int plimAnt = npa + 1 - apaAntCV; // si p < plimAnt puede hacer el arranque a debibo al funcionamiento anterior al inicio del horizonte CP
			if(dCP.getHorasMax().containsKey(a)) {
				for(Integer p: dCP.getPostesAIni().get(a)) {
					int aRestar = 0;
					if(p<plimAnt) aRestar = 1;
					BaseRest br = new BaseRest(ConCP.RZIMPIDEARRINI + "_" + a, cc.getNombre(), p, 
						uno-aRestar, null, null, null, p, Constantes.RESTMAYOROIGUAL);
					br.agSumVar(ConCP.ZINI + "_" + a, nomPar, p, 1.0, null);									
					for(int q=Math.max(0,p-npa); q<=p-1; q++) {					
						br.agSumVar(ConCP.NMODCC, nomPar, q, 1.0, null);		
					}
					agrega1BR(generaNomBRest(ConCP.RZIMPIDEARRINI + "_" + a, cc.getNombre(), p), br);
								
					br = new BaseRest(ConCP.RUSAZ + "_" + a, cc.getNombre(), p, 
							uno, null, null,null, p, Constantes.RESTMENOROIGUAL);
					br.agSumVar(ConCP.XAINI + "_" + a, nomPar, p, 1.0, null);	
					br.agSumVar(ConCP.ZINI + "_" + a, nomPar, p, 1.0, null);	
					agrega1BR(generaNomBRest(ConCP.RUSAZ + "_" + a, cc.getNombre(), p), br);
				}
			}
		}
	}
	
	
	
	@Override
	/**
	 * Se computa sólo el costo de arranques iniciales y simples, sin considerar
	 * el costo de combustible, que se considera en el ContratoCombCanioDespPE
	 * 
	 * suma en a[suma en (p:postesini^a,0+1,.,canPos-1 )(xini[p]^a* cosini^a)]
	 * suma en s[suma en (p:postessim^s,0+1,.,canPos-1 )(xsim[p]^s* cossim^s)]
	 * + cantArrT * cosArrTG
	 * 
	 */
	public void crearBaseObj() {
		for(String a: dCP.getListaAIni()) {
			BaseTermino bt = new BaseTermino(ConCP.CANTARRINI + "_" + a, nomPar, cantPos-1, dCP.getCostAIni().get(a), null, grafo);
			agrega1BTalObj(bt);		
		}
		for(String s: dCP.getListaASim()) {
			BaseTermino bt = new BaseTermino(ConCP.CANTARRSIM + "_" + s, nomPar, cantPos-1, dCP.getCostASim().get(s), null, grafo);
			agrega1BTalObj(bt);		
		}
		double cosCC = cc.getCCs().getCostoVariable().getValor(instIniCP);
		contObjCVarOyMEnergia(cosCC, ConCP.POTCC, nomPar);
		double cosTG = cc.getTGs().getCostoVariable().getValor(instIniCP);
		contObjCVarOyMEnergia(cosTG, ConCP.POTTGAB, nomPar);
	}


	
	public CicloCombCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(CicloCombCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public CicloCombinado getCc() {
		return cc;
	}
	public void setCc(CicloCombinado cc) {
		this.cc = cc;
	}

	public DatosCicloCombCP getdCP() {
		return dCP;
	}

	public void setdCP(DatosCicloCombCP dCP) {
		this.dCP = dCP;
	}

	public int[] getCantTGDisp() {
		return cantTGDisp;
	}

	public void setCantTGDisp(int[] cantTGDisp) {
		this.cantTGDisp = cantTGDisp;
	}

	public int[] getCantCVDisp() {
		return cantCVDisp;
	}

	public void setCantCVDisp(int[] cantCVDisp) {
		this.cantCVDisp = cantCVDisp;
	}

	public double getPotMax1TG() {
		return potMax1TG;
	}

	public void setPotMax1TG(double potMax1TG) {
		this.potMax1TG = potMax1TG;
	}

	public double getPotMin1TG() {
		return potMin1TG;
	}

	public void setPotMin1TG(double potMin1TG) {
		this.potMin1TG = potMin1TG;
	}

	public double getPotMax1CC() {
		return potMax1CC;
	}

	public void setPotMax1CC(double potMax1CC) {
		this.potMax1CC = potMax1CC;
	}

	public double getPotMin1CC() {
		return potMin1CC;
	}

	public void setPotMin1CC(double potMin1CC) {
		this.potMin1CC = potMin1CC;
	}

	public double getPotMax1CV() {
		return potMax1CV;
	}

	public void setPotMax1CV(double potMax1CV) {
		this.potMax1CV = potMax1CV;
	}

	public double getRendMinTG() {
		return rendMinTG;
	}

	public void setRendMinTG(double rendMinTG) {
		this.rendMinTG = rendMinTG;
	}

	public double getRendMaxTG() {
		return rendMaxTG;
	}

	public void setRendMaxTG(double rendMaxTG) {
		this.rendMaxTG = rendMaxTG;
	}

	public double getRendMinCC() {
		return rendMinCC;
	}

	public void setRendMinCC(double rendMinCC) {
		this.rendMinCC = rendMinCC;
	}

	public double getRendMaxCC() {
		return rendMaxCC;
	}

	public void setRendMaxCC(double rendMaxCC) {
		this.rendMaxCC = rendMaxCC;
	}



	public int getnModTGm1() {
		return nModTGm1;
	}

	public void setnModTGm1(int nModTGm1) {
		this.nModTGm1 = nModTGm1;
	}

	public int getnModCCm1() {
		return nModCCm1;
	}

	public void setnModCCm1(int nModCCm1) {
		this.nModCCm1 = nModCCm1;
	}

	public double getPotTerPropTG() {
		return potTerPropTG;
	}

	public void setPotTerPropTG(double potTerPropTG) {
		this.potTerPropTG = potTerPropTG;
	}

	public double getPotTerMinTec1TG() {
		return potTerMinTec1TG;
	}

	public void setPotTerMinTec1TG(double potTerMinTec1TG) {
		this.potTerMinTec1TG = potTerMinTec1TG;
	}

	public double getPotTerPropCC() {
		return potTerPropCC;
	}

	public void setPotTerPropCC(double potTerPropCC) {
		this.potTerPropCC = potTerPropCC;
	}

	public double getPotTerMinTec1CC() {
		return potTerMinTec1CC;
	}

	public void setPotTerMinTec1CC(double potTerMinTec1CC) {
		this.potTerMinTec1CC = potTerMinTec1CC;
	}

	public double getPotTerPropArr() {
		return potTerPropArr;
	}

	public void setPotTerPropArr(double potTerPropArr) {
		this.potTerPropArr = potTerPropArr;
	}

	public double getPotTerPropPar() {
		return potTerPropPar;
	}

	public void setPotTerPropPar(double potTerPropPar) {
		this.potTerPropPar = potTerPropPar;
	}




	

}
