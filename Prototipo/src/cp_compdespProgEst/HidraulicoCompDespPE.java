/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * HidraulicoCompDespPE is part of MOP.
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

import compdespacho.HidraulicoCompDesp;
import cp_datatypesEntradas.DatosCicloCombCP;
import cp_datatypesEntradas.DatosHidroCP;
import cp_datatypesEntradas.DatosImpoExpoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.AproximadorLinealPE;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_despacho.NombreBaseVar;
import datatypes.Pair;
import parque.CicloCombinado;
import parque.GeneradorHidraulico;
import parque.Impacto;
import utilitarios.Constantes;
import utilitarios.Par;
import utilitarios.ParReales;
import utilitarios.Recta;

import java.util.ArrayList;

public class HidraulicoCompDespPE extends GeneradorCompDespPE {
	
	private HidraulicoCompDesp compDesp;
	
	private boolean conLago; // Se toma del xml
	
	private GeneradorHidraulico gh;	
	private ArrayList<GeneradorHidraulico> ghsArriba;  // hidráulico aguas arriba
	private int cantModDisp;

	private String nombreBase;	
	private DatosHidroCP dCP;
	
	private String modoPot;
	private String modoVerMax;
	
	private double valAgua;    // En USD/hm3
	
	private double volIniCPHm3;  // volumen inicial en el horizonte de corto plazo
	
	private double filtradoDia; // volumen filtrado en hm3/día
	private double evaporadoDia; // idem evaporado
	private double evafilPosHm3; // volumen evaporado y filtrado por poste en hm3
	private int horasTransito; // horas de transito entre la central this Y LA CENTRAL AGUAS ABAJO.
	private int postesTransito; // ídem postes de tránsito, implícitos en la cantidad de erogados rezagados leídos
	
	/**
	 * Caudal en m3/s erogado por la cantral en los postes anteriores. 
	 * El valor en i es el erogado i+1 postes antes del poste 0 
	 * ATENCIÓN QUE SE INVIERTE EL ORDEN CRONOLOGICO DE LOS DATOS DE ENTRADA
	 */
	private double[] qeroAntP;  
	
	private ArrayList<Recta> funcionPQ;  // Se emplean para MODO PQ; TODO: se toman los valores para unas cotas prefijadas aguas arriba de la central y de la central aguas abajo	
	private double coefEnerg;   // coeficiente energético en MW/(m3/seg) para el caso MODO  COEF_FIJO
	private double factorCompartir;  // proporción de la energía generada que va al sistema, vale 1 excepto 0.5 para Salto Grande

	private double qvermax;  // vertimiento máximo en m3/s si se usa MODO_VERMAX = FIJO_COTA
	private double qturmax;  // turbinado máximo en m3/s de una máquina
	private double pendQVerMax;  // pendiente del vertimiento máximo en (m3/s)/hm3 si se usa MODO_VERMAX = RECTA 
	
	
	/**
	 * Término independiente y coeficiente de recta de erogado mínimo
	 * La recta da el erogado mínimo en m3/s en función del volumen en hm3 por encima
	 * del xmin del polinomio de vertido.
	 * 
	 */
	private ParReales coefsEroMin;
	
	private ArrayList<Double> fVMi;   // valores del vertido máximo por puntos en m3/s
	private ArrayList<Double> vVMi;   // valores del volumen por puntos en hm3
	private static final double[] deltaCotaVerMaxAL = {1.5, 3.0}; // valores por encima del rango menor de la función de vertimiento máximo usados en la aprox.lineal
	private static final double deltaCotaVerMaxRECTA = 6.0;  // variación de cota en m para calcular dVerMax/dVolumen en la opción de vertimiento máximo RECTA
	
	private double volsup; // volumen por encima del que se empieza a penalizar
	private double volinf; // volumen por debajo del que se empieza a penalizar
	private double penvolsupHm3h; // penalización por excedente de volumen respecto a volsup, en USD/(hm3*hora)
	private double penvolinfHm3h; // penalización por faltante de volumen respecto a volinf, en USD/(hm3*hora)
	
	private boolean impactoQmin;  // true si hay un impacto que involucra a esta central penalizando el faltante de caudal respecto a un mínimo
	private double qinf; // caudal por debajo del que se empieza a penalizar
	private double penQFaltUSDporHm3; // penalización por faltante de caudal en USD/hm3
	
	private String nombreVAAporte;
	
	private static final double deltaCotaPen = 0.1;  // variación de cota en m para calcular dVol/dCota para penalizaciones
	private static final double deltaCotaEro =  3.0;   // variación de cota en m para calcular dVol/dCota para estimar rectas que acotan erogado
	
	/**
	 * Crea una aproximación por puntos para el vertido máximo más exacto
	 * Se aproxima en el volumen menor del rango más bajo (v1), en el volumen mayor del rango más alto (vn)
	 * y en los puntos que al v1 le suman los valores del deltaCotaVerMax
	 */
	private AproximadorLinealPE apLVM;   // aproximadorlinel del vertimiento máximo
	
	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosHidroCP)dpcp;	
	}
	
	
	@Override
	public void completaConstruccion() {
		instIniCP = dGCP.getInstIniCP();
		gh = (GeneradorHidraulico)participante;
		conLago = gh.getCompG().getEvolucionComportamientos().get(Constantes.COMPLAGO).getValor(instIniCP).equalsIgnoreCase(Constantes.HIDROCONLAGO);
		ghsArriba = gh.getGeneradoresArriba();		
		factorCompartir = gh.getFactorCompartir().getValor(instIniCP);	
		qturmax = gh.getqTur1Max().getValor(instIniCP);
		valAgua = dCP.getValAgua();		
		nomPar = gh.getNombre();				
		nombreVAAporte = ConCP.APORTE + "-" + nomPar;		
		cantModDisp = gh.getCantModInst().getValor(instIniCP) - gh.getMantProgramado().getValor(instIniCP);
		modoPot = dCP.getModoPot();
		modoVerMax = dCP.getModoVerMax();			
		filtradoDia = gh.getfFiltracion().dameValor(dCP.getCotaIni())*Constantes.SEGUNDOSXDIA/Constantes.M3XHM3; // en hm3
		evaporadoDia = gh.getfEvaporacion().dameValor(dCP.getCotaIni())*Constantes.SEGUNDOSXDIA/Constantes.M3XHM3;
		evafilPosHm3 = Math.max(0,(filtradoDia+evaporadoDia)*dur1Pos/Constantes.SEGUNDOSXDIA); 
		
		postesTransito = 0;
		if(dCP.getQeroAbaH()!=null) {
			int cantPAnt = dCP.getQeroAbaH().length*dGCP.getCantPosHora();
			qeroAntP = new double[cantPAnt];
			for(int p=0; p<cantPAnt; p++) {
				qeroAntP[p] = dCP.getQeroAbaH()[(cantPAnt-1-p)/dGCP.getCantPosHora()];
			}
			postesTransito = qeroAntP.length;
		}
		volIniCPHm3 = gh.getfVoco().dameValor(dCP.getCotaIni());
		
		// Se asegura que el evaporado y filtrado no vacíen el lago
		evafilPosHm3 = Math.min(evafilPosHm3, volIniCPHm3/dGCP.getCantPostes());
		
		// Calcula coeficiente fijo de potencia a partir de las cotas iniciales y para una fracción fQCoef del caudal turbinado máximo
		if(modoPot.equalsIgnoreCase(ConCP.COEF_FIJO)) {
			double qturM = gh.getqTur1Max().getValor(instIniCP) * gh.getCantModInst().getValor(instIniCP) * ConCP.FQCOEF;
			double cotaM = dCP.getCotaIni();
			double cotaMHidroAbajo;
			if(gh.getGeneradorAbajo()!=null) {
				cotaMHidroAbajo = gh.getGeneradorAbajo().getfCovo().dameValor(gh.getGeneradorAbajo().getVolFijo().getValor(instIniCP));
			}else {
				cotaMHidroAbajo = 0.0;
			}
			HidraulicoCompDesp hcd = (HidraulicoCompDesp)gh.getCompDesp();
			double cotaMAbajo = hcd.dameCotaAguasAbajo(qturM, cotaMHidroAbajo);
			coefEnerg = gh.coefEnergetico(cotaM, cotaMAbajo);
		}
		
		
		/**
		 * Calcula penalizaciones por cota faltante y excedente
		 * dVol[m3]/dCota[m] = superficie del lago[m2] ;   dCota[m]/dVol[m3] = 1/superficie[m2]
		 * pen[MUSD/m3.dia] = penPorMCota[MUSD/m.dia] * (1/superficie[m2])
		 * penPorHm3dia[MUSD/hm3.dia] = penPorMCota[MUSD/m.dia] * (1/superficie[m2])* 1E6[m2/km2]
		 * penPorHm3h[USD/hm3.dia] = penPorMCota[MUSD/m.dia] * (1/superficie[m2])* 1E6[m2/km2]*1E6[USD/MUSD] / HorasPorDia
		 */
		volinf = gh.getVolumenControlMinimo().getValor(instIniCP); // en hm3
		double cotainf = gh.getfCovo().dameValor(volinf);
		double penInfCota = gh.getPenalidadControlMinimo().getValor(instIniCP);  // en MUSD/m.dia		
		double dVdC = (gh.getfVoco().dameValor(cotainf + deltaCotaPen) - volinf)*Constantes.M3XHM3/deltaCotaPen;  // derivada (superficie en m2)
		penvolinfHm3h = penInfCota/(dVdC*Constantes.HORASXDIA)*Constantes.M3XHM3*Constantes.USDXMUSD;
		
		volsup = gh.getVolumenControlMaximo().getValor(instIniCP);
		double cotasup = gh.getfCovo().dameValor(volsup);
		double penSupCota = gh.getPenalidadControlMaximo().getValor(instIniCP);
		dVdC = (volsup - gh.getfVoco().dameValor(cotasup - deltaCotaPen))*Constantes.M3XHM3/deltaCotaPen;  // derivada (superficie en m2)
		penvolsupHm3h = penSupCota/(dVdC*Constantes.HORASXDIA)*Constantes.M3XHM3*Constantes.USDXMUSD;

		/**
		 * Calcula penalización por faltante de caudal erogado
		 */
		impactoQmin = false;
		for(Impacto imp: gh.getImpactosQueLoInvolucran()) {			
			if(imp.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO) {
				penQFaltUSDporHm3 = imp.getCostoUnit().getValor(instIniCP);
				impactoQmin = true;
				qinf = imp.getLimite().getValor(instIniCP);
			}
		}
		
		
		/**
		 * Calcula coeficientes de la recta que acota inferiormente el erogado en función del volumen del lago
		 * Se toma una recta que vale cero para el volsup (volumen por encima de la cotasup que se empieza a penalizar)
		 * y coincide con la curva del erogado mínimo para el volumen cotasup + deltaCotaEroVert 
		 */
		double vol1 = volsup;     // volumen para el que empieza a ser qeromin > 0
		double cotaSup = gh.getfCovo().dameValor(volsup);
		double cotaMasDelta = cotaSup + deltaCotaEro;
		double volMasDelta = gh.getfVoco().dameValor(cotaMasDelta);   // volumen para cotasup + deltaCotaEroVert
		double qMasDelta = gh.getfQEroMin().dameValor(cotaMasDelta);  // qeromin para volMasDelta
		coefsEroMin = new ParReales( -volsup*qMasDelta/(volMasDelta-volsup),
				                     qMasDelta/(volMasDelta-volsup) );
		
		/**
		 * Crea aproximador de la función de vertido máximo más exacta
		 * Para la cota mínima se supone vertimiento cero
		 */
		if(modoVerMax.equalsIgnoreCase(ConCP.AL)) {
			fVMi = new ArrayList<Double>();
			vVMi = new ArrayList<Double>();
			ArrayList<Double> cotaVMi = new ArrayList<Double>();
			cotaVMi.add(gh.getfQVerMax().getXmin());
			for(int i=0; i<deltaCotaVerMaxAL.length; i++) {
				cotaVMi.add(deltaCotaVerMaxAL[i]);
			}
			if(gh.getfQVerMax().getXmax()>deltaCotaVerMaxAL[deltaCotaVerMaxAL.length-1])
					cotaVMi.add(gh.getfQVerMax().getXmax());
			vVMi.add(0.0);
			fVMi.add(0.0);
			for(Double cota: cotaVMi) {
				vVMi.add(gh.getfVoco().dameValor(cota));
				fVMi.add(gh.getfQVerMax().dameValor(cota));
			}
			apLVM = new AproximadorLinealPE(this, nomPar, ConCP.VOLINI, ConCP.QVERMAX, true, vVMi, fVMi);
		}else if(modoVerMax.equalsIgnoreCase(ConCP.FIJO_COTA)){
			double vcotaIni = gh.getfVoco().dameValor(dCP.getCotaIni());
			qvermax = gh.getfQVerMax().dameValor(dCP.getCotaIni());
		}else {
			double cotaVol0 = gh.getfCovo().dameValor(0.0);
			double volVerMax = gh.getfVoco().dameValor(cotaVol0 + deltaCotaVerMaxRECTA);
			pendQVerMax = gh.getfQVerMax().dameValor(volVerMax)/volVerMax;		
		}
	}	

	
	@Override
	public void cargarValoresRezagados() {
		for(int i=postesTransito-1; i>=0; i--) {
			grafo.cargaValRez(ConCP.QERO, gh.getNombre(), -(i+1) , qeroAntP[i]);		
		}
	}
	




	@Override
	public void crearBasesVar() {
		
		for(int p=0; p<cantPos; p++) {			
			long inst = instIniCP + dur1Pos*p;
			
			double qturmax = gh.getqTur1Max().getValor(inst)*cantModDisp;
			 
			double potMax = gh.getPotenciaMaxima().getValor(inst)*cantModDisp*gh.getFactorCompartir().getValor(inst);
			int nmod = gh.getCantModInst().getValor(instIniCP)-gh.getMantProgramado().getValor(inst);

			BaseVar bV = new BaseVar(ConCP.POT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, potMax, null, true);
			cargaBaseVar(bV, ConCP.POT, nomPar, p);	
		
			bV = new BaseVar(ConCP.QERO, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, null, null, true);
			cargaBaseVar(bV, ConCP.QERO, nomPar, p);	
			
			
			bV = new BaseVar(ConCP.QTUR, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, qturmax, null, true);
			cargaBaseVar(bV, ConCP.QTUR, nomPar, p);	
			
			if(modoVerMax.equalsIgnoreCase(ConCP.FIJO_COTA) && conLago) {
				bV = new BaseVar(ConCP.QVER, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, qvermax, null, true);
			}else {
				bV = new BaseVar(ConCP.QVER, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, null, null, true);				
			}		
			cargaBaseVar(bV, ConCP.QVER, nomPar, p);	
			
			if(conLago) {
				bV = new BaseVar(ConCP.QVERMAX, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, null, null, true);
				cargaBaseVar(bV, ConCP.QVERMAX, nomPar, p);	
			}
			
			bV = new BaseVar(ConCP.VOLINI, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					null, null, null, null, true);
			cargaBaseVar(bV, ConCP.VOLINI, nomPar, p);
			
			bV = new BaseVar(ConCP.VOLFIN, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					null, null, null, null, true);
			cargaBaseVar(bV, ConCP.VOLFIN, nomPar, p);	
			
			if(conLago) {
				if(gh.isHayControldeCotasMaximas()) {
					bV = new BaseVar(ConCP.VOLEXCED, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
							cero, null, null, null, true);
					cargaBaseVar(bV, ConCP.VOLEXCED, nomPar, p);
				}
				
				if(gh.isHayControldeCotasMinimas()) {
					bV = new BaseVar(ConCP.VOLFALT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
							cero, null, null, null, true);
					cargaBaseVar(bV, ConCP.VOLFALT, nomPar, p);
				}
			
				if(impactoQmin) {
					bV = new BaseVar(ConCP.QFALT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, null, null, true);
					cargaBaseVar(bV, ConCP.QFALT, nomPar, p);
				}
				
				if(modoVerMax.equalsIgnoreCase(ConCP.AL)) {
					apLVM.construyeBasesVar(p);
				}
			
			}
		}		
	}



	public void cargaVarsEstadoHiperplanos() {
		NombreBaseVar nvb = new NombreBaseVar(ConCP.VOLFIN, nomPar, cantPos-1);
		constHip.getNombresVEParticipantes().put(ConCP.VOLFIN + "_" + nomPar, nvb);		
	}




	@Override
	public void crearBasesRest() {
		crearPotenciaCoefFijo();
		if(conLago) {
			crearBalanceLago();
		}else {
			crearBalanceSinLago();
		}
		defineQErogado();
		crearVolIniFinYPaso0();
		if(conLago) {
			crearEroMin();		
			if(modoVerMax.equalsIgnoreCase(ConCP.AL)) {
				creaVerMaxAL();
			}else if(modoVerMax.equalsIgnoreCase(ConCP.RECTA)) {
				crearVerMaxRECTA();
			}   // En el caso de vertimiento máximo FIJO_COTA se fija cota superior a la variable
			creaVolYCaudPenalizaciones();
		}
	}

	
	
	
	
	/**
	 * Crea la restricción que vincula potencia con caudal turbinado
	 * con coeficiente energético fijo en [MW/(m3/s)]
	 * 
	 * pot[p] - coefEnerg * qtur[p] * factorCompartir = 0
	 * 
	 */
	public void crearPotenciaCoefFijo() {
		for(int p=0; p<cantPos; p++) {  
			BaseRest br = new BaseRest(ConCP.RCOEFENERG, nomPar, p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);			
			br.agSumVar(ConCP.POT, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.QTUR, nomPar, p, -coefEnerg*factorCompartir, null);
			agrega1BR(generaNomBRest(ConCP.RCOEFENERG, nomPar, p), br);				
		}		
	}
	
	
	
	/**
	 * Balance del lago en hm3: volumen final en función del inicial
	 * 
	 * volFin[p] - volIni[p] 
	 * -  suma en generadores aguas arriba[qero.HidraulicaArriba[ p-HidraulicaArriba.postesTransito ]*dur1Pos/M3XHM3]
	 * +  qero[p]*dur1Pos/M3XHM3   =  aporte* dur1Pos/M3XHM3  - (filtradoDia+evaporadoDia)/cantPosDia
	 * 
	 * ATENCIÓN: LA VARIABLE DUAL 
	 * 
	 * 	para p=0,…,cantPos-1
	 */

	public void crearBalanceLago() {
		ArrayList<String> nomVA = new ArrayList<String>();
		nomVA.add(ConCP.APORTE + "-" + nomPar);
		ArrayList<Double> coef = new ArrayList<Double>();
		coef.add(dur1Pos/Constantes.M3XHM3);
		for(int p=0; p<cantPos; p++) {  		 
			BaseRest br = new BaseRest(ConCP.RBALLAGO, nomPar, p, 
					-evafilPosHm3, nomVA, coef, null, p, Constantes.RESTIGUAL);					
			br.agSumVar(ConCP.VOLFIN, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.VOLINI, nomPar, p, -1.0, null);			
			for(GeneradorHidraulico gharr: ghsArriba) {
				HidraulicoCompDespPE hPEarr = (HidraulicoCompDespPE)gharr.getCompDespPE();
				int ptrans = hPEarr.getPostesTransito();
				br.agSumVar(ConCP.QERO, gharr.getNombre(), p - ptrans, -dur1Pos/Constantes.M3XHM3, null);	
			}
			br.agSumVar(ConCP.QERO, nomPar, p, dur1Pos/Constantes.M3XHM3, null);			
			agrega1BR(generaNomBRest(ConCP.RBALLAGO, gh.getNombre(), p), br);	
		}		
	}
	

	/**
	 * Balance si no hay lago
	 * 
	 * - Volumen final igual al inicial
	 * - Erogado entrante igual al saliente
	 * 
	 * volFin[p] - volIni[p] = 0
	 * 
	 * -  suma en generadores aguas arriba[qero.HidraulicaArriba[ p-HidraulicaArriba.postesTransito ]*dur1Pos/M3XHM3]
	 * +  qero[p]*dur1Pos/M3XHM3   =  aporte* dur1Pos/M3XHM3  
	 * 
	 * 	para p=0,…,cantPos-1
	 */
	public void crearBalanceSinLago() {
		
		for(int p=0; p<cantPos; p++) {  		
			BaseRest br = new BaseRest(ConCP.RVOLFIJOSINLAGO, nomPar, p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.VOLFIN, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.VOLINI, nomPar, p, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RVOLFIJOSINLAGO, gh.getNombre(), p), br);	
		}
		
		ArrayList<String> nomVA = new ArrayList<String>();
		nomVA.add(ConCP.APORTE + "-" + nomPar);
		ArrayList<Double> coef = new ArrayList<Double>();
		coef.add(dur1Pos/Constantes.M3XHM3);
		for(int p=0; p<cantPos; p++) {  		 
			BaseRest br = new BaseRest(ConCP.RBALLAGO, nomPar, p, 
					-evafilPosHm3, nomVA, coef, null, p, Constantes.RESTIGUAL);			
			for(GeneradorHidraulico gharr: ghsArriba) {
				HidraulicoCompDespPE hPEarr = (HidraulicoCompDespPE)gharr.getCompDespPE();
				int ptrans = hPEarr.getPostesTransito();
				br.agSumVar(ConCP.QERO, gharr.getNombre(), p - ptrans, -dur1Pos/Constantes.M3XHM3, null);	
			}
			br.agSumVar(ConCP.QERO, nomPar, p, dur1Pos/Constantes.M3XHM3, null);
			
			agrega1BR(generaNomBRest(ConCP.RBALLAGO, gh.getNombre(), p), br);	
		}			
	}
	
	
	
	/**
	 * Define el caudal erogado como suma de turbinado y vertido
	 * 
	 * qero[p] - qtur[p] - qver[p] = 0,  para p=0,..,cantPos
	 * 
	 */
	public void defineQErogado() {
		for(int p=0; p<cantPos; p++) {  	
			BaseRest br = new BaseRest(ConCP.RDEFQERO, nomPar, p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);		
			br.agSumVar(ConCP.QERO, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.QTUR, nomPar, p, -1.0, null);	
			br.agSumVar(ConCP.QVER, nomPar, p, -1.0, null);	
			agrega1BR(generaNomBRest(ConCP.RDEFQERO, gh.getNombre(), p), br);	
		}		
	}
	
	/**
	 * Crea la igualdad de volini[p+1] = volfin[p]
	 * y la condición inicial del lago
	 * 
	 * volIni[0] = volIniCPHm3
	 * 
	 * volIni[p] - volFin[p-1] = 0,  para p=1,...cantPostes
	 * 
	 */
	public void crearVolIniFinYPaso0() {		
		BaseRest br = new BaseRest(ConCP.RVOLINI0, nomPar, 0, 
				volIniCPHm3 , null, null, null, 0, Constantes.RESTIGUAL);	
		br.agSumVar(ConCP.VOLINI, nomPar, 0, 1.0, null);
		agrega1BR(generaNomBRest(ConCP.RVOLINI0, gh.getNombre(), 0), br);	
	
		for(int p=1; p<cantPos; p++) {  		 
			br = new BaseRest(ConCP.RVOLINIFIN, nomPar, p, 
					cero, null, null, null, p, Constantes.RESTIGUAL);			
			br.agSumVar(ConCP.VOLINI, nomPar, p, 1.0, null);
			br.agSumVar(ConCP.VOLFIN, nomPar, p-1, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RVOLINIFIN, gh.getNombre(), p), br);	
		}
		
	}
	
	/**
	 * Erogado mínimo por razones de seguridad de presas
	 * TODO A SUSTITUIR EVENTUALMENTE USANDO QEROMIN COMBINACION LINEAL PARA REPRESENTAR UNA FUNCIÓN POR PUNTOS
	 * 
	 * qero >= coef.a + coef.b * volini
	 * 
	 * qero  - coef.b * volini >= coef.a
	 * 
	 */
	public void crearEroMin() {
		double a = coefsEroMin.getReal1();
		double b = coefsEroMin.getReal2();
	
		for(int p=0; p<cantPos; p++) {  			 
			BaseRest br = new BaseRest(ConCP.REROMIN, nomPar, p, 
					a, null, null, null, p, Constantes.RESTMAYOROIGUAL);	
			br.agSumVar(ConCP.QERO, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.VOLINI, nomPar, p, -b, null);	
			agrega1BR(generaNomBRest(ConCP.REROMIN, nomPar, p), br);	
		}		
	}
	
	

	/**
	 * Vertimiento máximo por limitaciones del vertedero cuando se  
	 * usa la aproximación modoVerMax = RECTA
	 *  qver[p] - qverMax[p] <= 0
	 */
	public void creaVerMaxAL() {		
		for(int p=0; p<cantPos; p++) {
			apLVM.construyeBasesRest(p);
			BaseRest br = new BaseRest(ConCP.RVERMAXAL, nomPar, p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);	
			br.agSumVar(ConCP.QVER, nomPar, p, 1.0, null);
			br.agSumVar(ConCP.QVERMAX, nomPar, p, -1.0, null);
			agrega1BR(generaNomBRest(ConCP.RVERMAXAL, nomPar, p), br);
		}	
	}
	
	
	/**
	 * Vertimiento máximo por limitaciones del vertedero cuando se  
	 * usa la aproximación modoVerMax = RECTA
	 * 
	 * qver[p] <= pendQVerMax*volini[p]
	 * qver[p] - pendQVerMax*volini[p]  <= 0
	 * 
	 */	
	public void crearVerMaxRECTA() {
		for(int p=0; p<cantPos; p++) {  			 
			BaseRest br = new BaseRest(ConCP.RVERMAXRECTA, nomPar, p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);
			br.agSumVar(ConCP.QVER, nomPar, p, 1.0, null);	
			br.agSumVar(ConCP.VOLINI, nomPar, p, -pendQVerMax, null);	
			agrega1BR(generaNomBRest(ConCP.RVERMAXRECTA, nomPar, p), br);	
		}		
	}


	
	/**
	 * Crea las variables de volumen inundado (excedente), volumen faltante y caudal faltante
	 *
	 * volini[p] - volexced[p] <= volsup
	 * 
	 * volini[p] + volfalt[p] >= volinf
	 * 
	 * qero[p] + qfalt[p] >= qinf
 	 * 
	 */
	public void creaVolYCaudPenalizaciones() {
		if(gh.isHayControldeCotasMaximas()) {
			for(int p=0; p<cantPos; p++) {  			 
				BaseRest br = new BaseRest(ConCP.RVOLEXCED, nomPar, p, 
						volsup, null, null, null, p, Constantes.RESTMENOROIGUAL);				
				br.agSumVar(ConCP.VOLINI, nomPar, p, 1, null);	
				br.agSumVar(ConCP.VOLEXCED, nomPar, p, -1, null);
				agrega1BR(generaNomBRest(ConCP.RVOLEXCED, nomPar, p), br);	
			}
		}
		if(gh.isHayControldeCotasMinimas()) {
			for(int p=0; p<cantPos; p++) {  			 
				BaseRest br = new BaseRest(ConCP.RVOLFALT, nomPar, p, 
						volinf, null, null, null, p, Constantes.RESTMAYOROIGUAL);	
				br.agSumVar(ConCP.VOLINI, nomPar, p, 1, null);
				br.agSumVar(ConCP.VOLFALT, nomPar, p, 1, null);	
				agrega1BR(generaNomBRest(ConCP.RVOLFALT, nomPar, p), br);	
			}	
		}
		if(impactoQmin) {
			for(int p=0; p<cantPos; p++) {  			 
				BaseRest br = new BaseRest(ConCP.RQFALT, nomPar, p, 
						qinf, null, null, null, p, Constantes.RESTMAYOROIGUAL);	
				br.agSumVar(ConCP.QERO, nomPar, p, 1, null);
				br.agSumVar(ConCP.QFALT, nomPar, p, 1, null);	
				agrega1BR(generaNomBRest(ConCP.RQFALT, nomPar, p), br);	
			}	
		}
	}
	


	@Override
	public void crearBaseObj() {
		double coef;
		BaseTermino bt;		
		for(int p=0; p<cantPos; p++) { 	
			// Excedente de volumen
			if(conLago) {
				if(gh.isHayControldeCotasMaximas()) {			
					coef = penvolsupHm3h*dur1Pos/Constantes.SEGUNDOSXHORA;
					bt = new BaseTermino(ConCP.VOLEXCED, nomPar, p, coef , null, grafo);
					agrega1BTalObj(bt);	
				}
				// Faltante de volumen
				if(gh.isHayControldeCotasMinimas()) {		
					coef = penvolinfHm3h*dur1Pos/Constantes.SEGUNDOSXHORA;
					bt = new BaseTermino(ConCP.VOLFALT, nomPar, p, coef, null, grafo);
					agrega1BTalObj(bt);	
				}
				// Faltante de caudal
				if(impactoQmin) {
					coef = penQFaltUSDporHm3*dur1Pos/Constantes.M3XHM3;
					bt = new BaseTermino(ConCP.QFALT, nomPar, p, coef, null, grafo);
					agrega1BTalObj(bt);
				}
			}
		}		
		double cos = gh.getCostoVariable().getValor(instIniCP);
		contObjCVarOyMEnergia(cos, ConCP.POT, nomPar);
		if(!usoHip && conLago) {	
			bt = new BaseTermino(ConCP.VOLINI, nomPar, 0, valAgua, null, grafo);
			agrega1BTalObj(bt);			
			bt = new BaseTermino(ConCP.VOLFIN, nomPar, cantPos-1, -valAgua, null, grafo);
			agrega1BTalObj(bt);			
		}
	}


	/**
	 * Devuelve el coeficiente energético marginal [MWh/hm3] del último MWh generado dadas
	 * las condiciones hidráulicas (físicamente, antes del reparto por factor compartir). Es la variación en la generación en la hora si se reduce el
	 * agua turbinada en 1 hm3, respecto a la dada.
	 * 
	 * @param vol  volumen aguas arriba en hm3
	 * @param qero  caudal erogado en m3/s
	 * @param volHAbajo  volumen aguas abajo en hm3
	 * @return
	 */
	public double dameCoefEnergMargMWhHm3(double vol, double qero, double volHAbajo) {
		if(modoPot.equalsIgnoreCase(ConCP.COEF_FIJO)) {
			return coefEnerg*Constantes.M3XHM3/Constantes.SEGUNDOSXHORA;
		}else {
			System.out.println("No se programó devuelveCoefEnergMarginal para funciones PQ");
			System.exit(1);
		}
		return 0;
	}
	
	
	public HidraulicoCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(HidraulicoCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public GeneradorHidraulico getGh() {
		return gh;
	}

	public void setGh(GeneradorHidraulico gh) {
		this.gh = gh;
	}


	public String getNombreBase() {
		return nombreBase;
	}


	public void setNombreBase(String nombreBase) {
		this.nombreBase = nombreBase;
	}


	public DatosHidroCP getdCP() {
		return dCP;
	}


	public void setdCP(DatosHidroCP dCP) {
		this.dCP = dCP;
	}





	public double getVolIniCPHm3() {
		return volIniCPHm3;
	}



	public void setVolIniCPHm3(double volIniCPHm3) {
		this.volIniCPHm3 = volIniCPHm3;
	}



	public static double getDeltacotaPen() {
		return deltaCotaPen;
	}



	public double getFiltradoDia() {
		return filtradoDia;
	}


	public void setFiltradoDia(double filtradoDia) {
		this.filtradoDia = filtradoDia;
	}


	public double getEvaporadoDia() {
		return evaporadoDia;
	}


	public void setEvaporadoDia(double evaporadoDia) {
		this.evaporadoDia = evaporadoDia;
	}


	public int getHorasTransito() {
		return horasTransito;
	}


	public void setHorasTransito(int horasTransito) {
		this.horasTransito = horasTransito;
	}


	public int getPostesTransito() {
		return postesTransito;
	}


	public void setPostesTransito(int postesTransito) {
		this.postesTransito = postesTransito;
	}



	public ArrayList<GeneradorHidraulico> getGhsArriba() {
		return ghsArriba;
	}



	public void setGhsArriba(ArrayList<GeneradorHidraulico> ghsArriba) {
		this.ghsArriba = ghsArriba;
	}





	public int getCantModDisp() {
		return cantModDisp;
	}


	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}


	public double getEvafilPosHm3() {
		return evafilPosHm3;
	}


	public void setEvafilPosHm3(double evafilPosHm3) {
		this.evafilPosHm3 = evafilPosHm3;
	}


	public Double getFactorCompartir() {
		return factorCompartir;
	}


	public void setFactorCompartir(Double factorCompartir) {
		this.factorCompartir = factorCompartir;
	}


	public boolean isImpactoQmin() {
		return impactoQmin;
	}


	public void setImpactoQmin(boolean impactoQmin) {
		this.impactoQmin = impactoQmin;
	}


	public double[] getQeroAntP() {
		return qeroAntP;
	}



	public void setQeroAntP(double[] qeroAntP) {
		this.qeroAntP = qeroAntP;
	}



	public ArrayList<Recta> getFuncionPQ() {
		return funcionPQ;
	}



	public void setFuncionPQ(ArrayList<Recta> funcionPQ) {
		this.funcionPQ = funcionPQ;
	}



	public Double getCoefEnerg() {
		return coefEnerg;
	}



	public void setCoefEnerg(Double coefEnerg) {
		this.coefEnerg = coefEnerg;
	}



	public String getModoPot() {
		return modoPot;
	}


	public void setModoPot(String modoPot) {
		this.modoPot = modoPot;
	}


	public String getModoVerMax() {
		return modoVerMax;
	}


	public void setModoVerMax(String modoVerMax) {
		this.modoVerMax = modoVerMax;
	}



	public Double getQvermax() {
		return qvermax;
	}


	public void setQvermax(Double qvermax) {
		this.qvermax = qvermax;
	}


	public Double getPendQVerMax() {
		return pendQVerMax;
	}


	public void setPendQVerMax(Double pendQVerMax) {
		this.pendQVerMax = pendQVerMax;
	}


	public static double[] getDeltacotavermaxal() {
		return deltaCotaVerMaxAL;
	}


	public static double getDeltacotavermaxrecta() {
		return deltaCotaVerMaxRECTA;
	}


	public static double getDeltacotaero() {
		return deltaCotaEro;
	}


	public ArrayList<Double> getfVMi() {
		return fVMi;
	}


	public void setfVMi(ArrayList<Double> fVMi) {
		this.fVMi = fVMi;
	}


	public ArrayList<Double> getvVMi() {
		return vVMi;
	}


	public void setvVMi(ArrayList<Double> vVMi) {
		this.vVMi = vVMi;
	}


	public AproximadorLinealPE getApLVM() {
		return apLVM;
	}


	public void setApLVM(AproximadorLinealPE apLVM) {
		this.apLVM = apLVM;
	}




	public ParReales getCoefsEroMin() {
		return coefsEroMin;
	}



	public void setCoefsEroMin(ParReales coefsEroMin) {
		this.coefsEroMin = coefsEroMin;
	}



	public double getVolsup() {
		return volsup;
	}



	public void setVolsup(double volsup) {
		this.volsup = volsup;
	}



	public double getVolinf() {
		return volinf;
	}



	public void setVolinf(double volinf) {
		this.volinf = volinf;
	}




	public double getPenvolsupHm3h() {
		return penvolsupHm3h;
	}



	public void setPenvolsupHm3h(double penvolsupHm3h) {
		this.penvolsupHm3h = penvolsupHm3h;
	}



	public double getPenvolinfHm3h() {
		return penvolinfHm3h;
	}



	public void setPenvolinfHm3h(double penvolinfHm3h) {
		this.penvolinfHm3h = penvolinfHm3h;
	}



	public static double getDeltacotapen() {
		return deltaCotaPen;
	}



	public double getPenQFaltUSDporHm3() {
		return penQFaltUSDporHm3;
	}


	public void setPenQFaltUSDporHm3(double penQFaltUSDporHm3) {
		this.penQFaltUSDporHm3 = penQFaltUSDporHm3;
	}


	public double getQinf() {
		return qinf;
	}



	public void setQinf(double qinf) {
		this.qinf = qinf;
	}


	public boolean isConLago() {
		return conLago;
	}


	public void setConLago(boolean conLago) {
		this.conLago = conLago;
	}


	public double getQturmax() {
		return qturmax;
	}


	public void setQturmax(double qturmax) {
		this.qturmax = qturmax;
	}


	public void setCoefEnerg(double coefEnerg) {
		this.coefEnerg = coefEnerg;
	}


	public void setFactorCompartir(double factorCompartir) {
		this.factorCompartir = factorCompartir;
	}


	public void setQvermax(double qvermax) {
		this.qvermax = qvermax;
	}


	public void setPendQVerMax(double pendQVerMax) {
		this.pendQVerMax = pendQVerMax;
	}


	public double getValAgua() {
		return valAgua;
	}


	public void setValAgua(double valAgua) {
		this.valAgua = valAgua;
	}


	public String getNombreVAAporte() {
		return nombreVAAporte;
	}


	public void setNombreVAAporte(String nombreVAAporte) {
		this.nombreVAAporte = nombreVAAporte;
	}






	
	
}
