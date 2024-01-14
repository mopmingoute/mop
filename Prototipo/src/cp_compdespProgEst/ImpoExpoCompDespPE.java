/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpoExpoCompDespPE is part of MOP.
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

import compdespacho.ImpoExpoCompDesp;
import compdespacho.TermicoCompDesp;
import cp_datatypesEntradas.DatosImpoExpoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_despacho.GrafoEscenarios;
import parque.GeneradorTermico;
import parque.ImpoExpo;
import utilitarios.Constantes;

public class ImpoExpoCompDespPE extends CompDespPE{
	
	private ArrayList<String> listaOf;
	
	private ImpoExpoCompDesp compDesp;
	
	/**
	 * DEBE SER UNA IMPOEXPO DE LA CORRIDA MOP LARGO PLAZO 
	 * CON UN SOLO BLOQUE DEL QUE SE TOMA LA POTENCIA MÁXIMA 
	 */
	private ImpoExpo ie;   
	
	private String nomPar; // nombre del participante
	
	private DatosImpoExpoCP dCP;
	
	private boolean hayMulta;  // Para los ie con potencia media o en la base, si es true elimina la restricción dura y usa una multa
	
	/**
	 * Primera clave: nombre de la oferta
	 * Valor: tabla con los vectores de días de ejecución
	 * 
	 * Segunda clave: día de inicio de la ejecución (con los días empezando en cero)
	 * Valor: un int[] con tantos valores como días del horizonte donde hay 1 en los días de ejecución
	 * y 0 en los restantes.
	 */	
	private Hashtable<String,Hashtable<Integer, int[]>> dias;  // int[] para cada día posible de inicio, tiene 1 en los días de ejecución de la oferta
		
	@Override
	public void completaConstruccion() {	
		ie = (ImpoExpo) this.getParticipante();
		if(!ie.getTipoImpoExpo().equalsIgnoreCase(Constantes.IEEVOL)) {
			System.out.println("La impoexpo " + ie.getNombre() + " no es de tipo IEEVOL");
			System.exit(0);
		}
		nomPar = ie.getNombre();
		listaOf = dCP.getListaOf();	
		Hashtable<String, Integer> aP = new Hashtable<String, Integer>();
		
		dias = new Hashtable<String, Hashtable<Integer, int[]>>();			
		for(String s: listaOf) {
			if(!dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {
				aP.put(s,dCP.getAnticipH().get(s)*this.dGCP.getCantPosHora());
				// Construye los vectores binarios con los días de ejecución de las ofertas para cada día de inicio
				if(dCP.getPrimD().get(s)>dGCP.getCantDias()) {
					System.out.println("La oferta " + s + " comienza después del horizonte del problema");
					System.exit(1);
				}
				if(dCP.getUltD().get(s)>=dGCP.getCantDias()) {
					dCP.getUltD().remove(s); 
					dCP.getUltD().put(s, dGCP.getCantDias()-1);
					System.out.println("En la oferta " + s + "se sustituyó el último día de entrada posible de la oferta por el último día del horizonte");
				}
				Hashtable<Integer, int[]> aux1 = new Hashtable<Integer, int[]>();
				for(int d=dCP.getPrimD().get(s); d<= dCP.getUltD().get(s); d++) {
					int[] aux2 = new int[cantDias]; // se inicializa en cero
					// si la oferta se inició para d<0 se carga 1 a partir de d2>=0
					for(int d2=Math.max(d, 0); d2<= Math.min(cantDias-1, d+dCP.getDurD().get(s)-1); d2++) {
						aux2[d2] = 1;
					}
					aux1.put(d, aux2);
				}
				dias.put(s, aux1);
			}
			dCP.setAnticipP(aP);   // TODO ATENCION, HAY QUE TOMAR EL CUENTA LA ANTICIPACIÓN LO QUE NO SE HIZO TODAVÍA
			
			if(dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OBASE) || 
					dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OPOT_MEDIA)) {
				if(dCP.getMulta().get(s)>0.0) hayMulta = true;
			}
		}	
	}
	
	
	
	@Override
	/**
	 * LAS VARIABLES DE INICIO DE OFERTAS YA DECIDIDAS NO SE CONSIDERAN 
	 * VALORES REZAGADOS DE VARIABLES DE CONTROL, SU VALOR SE CARGA COMO RESTRICCION.
	 * PARA ESO SE HA DEBIDO CARGAR EL DÍA DEL PASADO EN QUE ENTRARON COMO PRIMER Y 
	 * ULTIMO DIA DE ENTRADA POSIBLE
	 */
	public void cargarValoresRezagados() {
		// DELIBERADAMENTE EN BLANCO
	}
	

	@Override
	public void crearBasesVar() { // el sufijo de la oferta que vaya a la variable y no al nomPar
		for(String s: dCP.getListaOf()) {
			if(!dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {
				// potencia por poste de la oferta
				for(int p=0; p<cantPos; p++) {											
					double pot = dCP.getPotMax().get(s);
					BaseVar bV = new BaseVar(ConCP.POT, s, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
							cero, null, pot, null, true);
					cargaBaseVar(bV, ConCP.POT, s, p);		
				}			
				// dia de inicio de la oferta	
				for(int d=dCP.getPrimD().get(s); d<=dCP.getUltD().get(s); d++) {
					// pDec es el poste en que hay que tomar la decisión para que la oferta entre el día d								
					BaseVar bV = new BaseVar(ConCP.XINID, s, d, false, Constantes.VCBINARIA, Constantes.VCPOSITIVA, dCP.getYaConv().get(s),
							null, null, null, null, true);										
					cargaBaseVar(bV, ConCP.XINID, s, d);				
				}
			}else {
				// potencia por poste de la oferta con máximo aleatorio
				String nomVAPot = dCP.getNomVAPot().get(s);
				for(int p=0; p<cantPos; p++) {	
					BaseVar bV = new BaseVar(ConCP.POT, s, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false,
							cero, null, uno, nomVAPot, true); 					
					cargaBaseVar(bV, ConCP.POT, s, p);		
				}				
			}
			
			if(!dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)  || !dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OLIBRE) ) {
				BaseVar bV = new BaseVar(ConCP.ENERFALT, s, cantPos-1, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false,
						null, null, null, null, true); 
				cargaBaseVar(bV, ConCP.ENERFALT, s, cantPos-1);		
			}
		}
		
		
		
		
		// potencia total neta por la interconexion por poste, positiva si entra al sistema
		double potMax = ie.getPotEvol().get(0).getValor(dGCP.getInstIniCP()).get(0);
		for(int p=0; p<cantPos; p++) {	
			BaseVar bV = new BaseVar(ConCP.POTIETOTENT, ie.getNombre(), p, true, Constantes.VCCONTINUA, Constantes.VCLIBRE, false,
					-potMax, null, potMax, null, true); 					
			cargaBaseVar(bV, ConCP.POTIETOTENT, ie.getNombre(), p);		
		}
		// binarias que indican funcionamiento de la interconexión por poste
		if(ie.isMinTec()) {
			for(int p=0; p<cantPos; p++) {	
				BaseVar bV = new BaseVar(ConCP.XIMPOEXPENT, ie.getNombre(), p, true, Constantes.VCBINARIA, Constantes.VCPOSITIVA, false, 
						null, null, null, null, true);				
				cargaBaseVar(bV, ConCP.XIMPOEXPENT, ie.getNombre(), p);	
			}
			for(int p=0; p<cantPos; p++) {	
				BaseVar bV = new BaseVar(ConCP.XIMPOEXPSAL, ie.getNombre(), p, true, Constantes.VCBINARIA, Constantes.VCPOSITIVA, false, 
						null, null, null, null, true);				
				cargaBaseVar(bV, ConCP.XIMPOEXPSAL, ie.getNombre(), p);	
			}
		}
	}


	
	

//	@Override
//	public void crearBasesRest() {
//		for(String s: listaOf) {
//			if(!dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {	
//				if(!dCP.getYaConv().get(s)) {
//					creaSoloEntraUnDia(s);
//				}else {
//					creaEntradaYaConv(s);
//				}
//				if(dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OBASE)) {
//					creaPotMaxOfBaseDura(s);
//				}else if(dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OPOT_MEDIA)) {
//					creaPotMediaDura(s);
//					creaPotMaxPoste(s);
//				}else if(dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OLIBRE)) {
//					creaPotMaxPoste(s);
//				}
//			}else {
//				// el tipo de la oferta es Aleat
//				// alcanza con la restricción de caja de cota superior de la potencia por poste
//			
//			}
//			
//		}
//		// Definición de la potencia total entrante al sistema por la interconexión
//		defPotTotalEntrante();
//		if(ie.isMinTec()) {
//			creaPotMinImpoExpo();
//			creaPotMaxImpoExpoConMintec();
//		}else {
//			creaPotMaxImpoExpoSinMintec();
//		}
//	}
	
	
	@Override
	public void crearBasesRest() {
		for(String s: listaOf) {
			if(!dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {	
				if(!dCP.getYaConv().get(s)) {
					creaSoloEntraUnDia(s);
				}else {
					creaEntradaYaConv(s);
				}
				if(dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OBASE) ||
						dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OPOT_MEDIA)) {
					creaPotMaxPoste(s);
					if(hayMulta == false) {
						creaPotMediaDura(s);
					}else {
						creaFaltanteEnergPotMedia(s);
					}
					
				}else if(dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OLIBRE)) {
					creaPotMaxPoste(s);
				}
			}else {
				// el tipo de la oferta es Aleat
				// alcanza con la restricción de caja de cota superior de la potencia por poste
			
			}
			
		}
		// Definición de la potencia total entrante al sistema por la interconexión
		defPotTotalEntrante();
		if(ie.isMinTec()) {
			creaPotMinImpoExpo();
			creaPotMaxImpoExpoConMintec();
		}else {
			creaPotMaxImpoExpoSinMintec();
		}
	}

	/**
	 * Asegura que entre los múltiples días posibles de entrada se elija solo uno
	 * @param s nombre de la oferta
	 * 
	 * suma en d= diaIni,...,diaFin (xiniD[d])  <= 1
	 */
	public void creaSoloEntraUnDia(String nomOf) {
		BaseRest br = new BaseRest(ConCP.RDIAENT, nomOf, null,
				uno+0.1, null, null, null, 0, Constantes.RESTMENOROIGUAL);  // se carga posteSM = 0 por compatibilidad pero no se emplea
		for(int d=dCP.getPrimD().get(nomOf); d<= dCP.getUltD().get(nomOf);d++) {
			br.agSumVar(ConCP.XINID, nomOf, d, 1.0, null);
		}
		agrega1BR(generaNomBRest(ConCP.RDIAENT, nomOf, null), br);
	}
	
	/**
	 * Asigna la potencia máxima a todas las variables de potencia según el día
	 * de entrada de la oferta. 
	 * SE INVOCA SOLO SI LA OFERTA ES DE TIPO BASE Y hayMulta = False
	 * @param nomOf nombre de la oferta
	 * 
	 * pot(p) - suma en d= diaIni,...,diaFin (dias[d,D(p)]*potMax* xini[d]) = 0
	 *	d recorre los días posibles de inicio de vigencia de la oferta
	 *  D(p) día al que pertenece el poste p, es diferente de d.
	 *  dias[d,j] cuando la oferta inicia su ejecución en el dia d, tiene 1 si en el día j  del horizonte se está ejecutando la oferta.
	 * para todo p del horizonte
	 * 
	 */
	public void creaPotMaxOfBaseDura(String nomOf) {		
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RPOTBASE, nomOf, p, 
					cero , null, null, null, p, Constantes.RESTIGUAL);
			br.agSumVar(ConCP.POT, nomOf, p, 1.0, null);
			int primD = dCP.getPrimD().get(nomOf);
			int ultD = dCP.getUltD().get(nomOf);			
			// d recorre los días posibles de entrada de la oferta
			// d2 es el día del horizonte al que pertenece el poste p
			int d2 = grafo.diaDePoste(p);
			for(int d= primD; d<= ultD; d++) {  
				int[] auxd = dias.get(nomOf).get(d);
				double pmax = dCP.getPotMax().get(nomOf);
				br.agSumVar(ConCP.XINID, nomOf, d, -pmax*auxd[d2], null);
			}
			agrega1BR(generaNomBRest(ConCP.RPOTBASE, nomOf, p), br);
		}
	}
	
	
	/**
	 * Crea BaseRest energia faltante para llegar a la potencia media comprometida
	 * Se usa en ofertas tipo BASE y POTMEDIA cuando hayMulta = true
	 * 
	 * Suma en p=1,...,cantPos ( pot(p)*dur1poste/SEGXHORA )  +  energía faltante para la requerida (MWh)  >= energía requerida (MWh)
	 * 
	 */
	public void creaFaltanteEnergPotMedia(String nomOf) {
		boolean ya = dCP.getYaConv().get(nomOf); // true si el contrato ya está convocado al inicio del período
		int durD = dCP.getDurD().get(nomOf);
		double potMedMW = dCP.getPotMed().get(nomOf);
		double enerYaMWh = 0.0;
		if(ya) enerYaMWh = dCP.getEnerYaGWh().get(nomOf)*Constantes.MWHXGWH;
		double enerRequeridaMWh = potMedMW*durD*Constantes.HORASXDIA-enerYaMWh;
		BaseRest br = new BaseRest(ConCP.RPOTMED, nomOf, cantPos-1, 
				enerRequeridaMWh, null, null, null, 0, Constantes.RESTMAYOROIGUAL);  // Se carga posteSM=0 por compatibilidad, no se  usa
		br.agSumVar(ConCP.ENERFALT, nomOf, cantPos-1, 1, null);
		double dd = (double)dGCP.getDur1Pos()/(double)Constantes.SEGUNDOSXHORA;
		for(int p=0; p<cantPos; p++) {			 
			br.agSumVar(ConCP.POT, nomOf, p, dd, null);
		}
		agrega1BR(generaNomBRest(ConCP.RPOTMED, nomOf, null), br);
	}
	

	/**
	 * Asigna la potencia máxima a todas las baseVar de potencia según el día
	 * de entrada de la oferta. 
	 * SE INVOCA CUANDO LA OFERTA NO ES DE TIPO BASE
	 * @param nomOf nombre de la oferta
	 * 
	 * pot(p) - suma en d= diaIni,...,diaFin (dias[d,D(p)]*potMax* xini[d]) <= 0
	 *	d recorre los días posibles de inicio de vigencia de la oferta
	 *  D(p) día al que pertenece el poste p, es diferente de d.
	 *  dias[d,j] cuando la oferta inicia su ejecución en el dia d, tiene 1 si en el día j  del horizonte se está ejecutando la oferta.
	 * para todo p del horizonte
	 * 
	 */
	public void creaPotMaxPoste(String nomOf) {	
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RPOTMAX, nomOf, p,
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL); 
			br.agSumVar(ConCP.POT, nomOf, p, 1.0, null);
			int primD = dCP.getPrimD().get(nomOf);
			int ultD = dCP.getUltD().get(nomOf);			
			// d recorre los días posibles de entrada de la oferta
			// d2 es el día del horizonte al que pertenece el poste p
 			int d2 = grafo.diaDePoste(p);
			for(int d= primD; d<= ultD; d++) {  
				int[] auxd = dias.get(nomOf).get(d);
				double pmax = dCP.getPotMax().get(nomOf);
				br.agSumVar(ConCP.XINID, nomOf, d, -pmax*auxd[d2], null);
			}
			agrega1BR(generaNomBRest(ConCP.RPOTMAX, nomOf, p),br);
		}	
	}
	
	
	/**
	 * Crea las restricciones base de potencia media para una oferta tipo POT_MEDIA cuando hayMulta = FALSE
	 * 
	 * @param nombreOf nombre de la oferta
	 * 
	 * Suma en p=1,...,cantPos ( pot(p)*dur1poste/SEGXHORA )  >=  energia faltante para la media (MWh)
	 */
	public void creaPotMediaDura(String nomOf) {
		boolean ya = dCP.getYaConv().get(nomOf); // true si el contrato ya está convocado al inicio del período
		int durD = dCP.getDurD().get(nomOf);
		double potMedMW = dCP.getPotMed().get(nomOf);
		double enerYaMWh = 0.0;
		if(ya) enerYaMWh = dCP.getEnerYaGWh().get(nomOf)*Constantes.MWHXGWH;
		double enerFaltaMWh = potMedMW*durD*Constantes.HORASXDIA-enerYaMWh;
		BaseRest br = new BaseRest(ConCP.RPOTMED, nomOf, null, 
				enerFaltaMWh, null, null, null, 0, Constantes.RESTMAYOROIGUAL);  // Se carga posteSM=0 por compatibilidad, no se  usa
		double dd = (double)dGCP.getDur1Pos()/(double)Constantes.SEGUNDOSXHORA;
		for(int p=0; p<cantPos; p++) {			 
			br.agSumVar(ConCP.POT, nomOf, p, dd, null);
		}
		agrega1BR(generaNomBRest(ConCP.RPOTMED, nomOf, null), br);
	}
	
	
	/**
	 * Define la variable de potencia total entrante por la interconexión
	 * 
	 * pot tot entrante - suma de potencias entrantes (con signo) de ofertas = 0
	 * signo menos si la oferta es de importación
	 * signo más si la oferta es de exportación
	 */
	public void defPotTotalEntrante() {
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RDEFPOTIETOT, ie.getNombre(), p,
				cero, null, null, null, p, Constantes.RESTIGUAL); 
			br.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, 1.0, null);			
			for(String s: listaOf) {
				Double sig = -1.0;
				if(dCP.getOperacion().get(s).equalsIgnoreCase(Constantes.VENTA)) sig = 1.0;
				br.agSumVar(ConCP.POT, s, p, sig, null);							
			}		
			agrega1BR(generaNomBRest(ConCP.RDEFPOTIETOT, ie.getNombre(), p), br);
		}	
	}
	
	
	/**
	 * Crea las restricciones base que limitan al máximo de la interconexión la 
	 * suma de potencias de ofertas a la potencia
	 * del participante ImpoExpo, tanto en sentido entrante como saliente al sistema
	 * 
	 * potencia entrante neta en la interconexion 
	 *       - (xIEent de la impoexpo)* (pot maxima de la interconexión en p) <=  0
	 *       
	 * - potencia entrante neta en la interconexion  
	 *       + (xIEsal de la impoexpo)* (pot maxima de la interconexión en p) >=  0      
	 *       
	 * para todo poste p
	 */
	public void creaPotMaxImpoExpoConMintec() {
		long instCorr = dGCP.getInstIniCP();
		for(int p=0; p<cantPos; p++) {
			double pmax = ie.getPotEvol().get(0).getValor(instCorr).get(0);			 
			BaseRest brEnt = new BaseRest(ConCP.RPOTMAXIE_ENT, ie.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMENOROIGUAL);			
			brEnt.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, 1.0, null);										
			brEnt.agSumVar(ConCP.XIMPOEXPENT, ie.getNombre(), p, - pmax, null); 		
			agrega1BR(generaNomBRest(ConCP.RPOTMAXIE_ENT,ie.getNombre(),p), brEnt);
			 				
			BaseRest brSal = new BaseRest(ConCP.RPOTMAXIE_SAL, ie.getNombre(), p, 
					cero, null, null, null, p, Constantes.RESTMAYOROIGUAL);
			brSal.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, -1.0, null);		
			brSal.agSumVar(ConCP.XIMPOEXPSAL, ie.getNombre(), p, pmax, null); 
			agrega1BR(generaNomBRest(ConCP.RPOTMAXIE_SAL,ie.getNombre(),p), brSal);
			instCorr += dGCP.getDur1Pos();
			
		}
	}
	
	
	
	/**
	 * Crea las restricciones base que limitan al máximo de la interconexión la 
	 * suma de potencias de ofertas, tanto en sentido entrante como saliente del país
	 * del participante ImpoExpo
	 * 
	 * potencia entrante en la interconexión 
	 *         <= pot maxima de la interconexión en p
	 *        
	 * potencia entrante en la interconexión 
	 *         >= - pot máxima en la interconexión en p      
	 *        
	 * para todo poste p
	 */
	public void creaPotMaxImpoExpoSinMintec() {
		long instCorr = dGCP.getInstIniCP();
		double sig;
		for(int p=0; p<cantPos; p++) {
			double pmax = ie.getPotEvol().get(0).getValor(instCorr).get(0);			 
			BaseRest brEnt = new BaseRest(ConCP.RPOTMAXIE_ENT, ie.getNombre(), p, 
					pmax, null, null, null, p, Constantes.RESTMENOROIGUAL);			
			brEnt.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, 1.0, null);							
			agrega1BR(generaNomBRest(ConCP.RPOTMAXIE_ENT,ie.getNombre(),p), brEnt);
				 
			BaseRest brSal = new BaseRest(ConCP.RPOTMAXIE_SAL, ie.getNombre(), p, 
					-pmax, null, null, null, p, Constantes.RESTMAYOROIGUAL);			
			brSal.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, 1.0, null);		
			instCorr += dGCP.getDur1Pos();
			agrega1BR(generaNomBRest(ConCP.RPOTMAXIE_SAL,ie.getNombre(),p), brSal);
		}
	}
	
	
	
	
	
	
	/**
	 * Crea las restricciones base que limitan al mínimo técnico de la interconexión 
	 * la suma de potencias de ofertas a la potencia
	 * del participante ImpoExpo
	 * 
	 * potencia entrante en p 
	 *       - (xIEEnt de la impoexpo)* (pot mínima de la interconexión en p) >=  0
	 *       
	 * potencia entrante en p 
	 *       + (xIESal de la impoexpo)* (pot mínima de la interconexión en p) <=  0  
	 *       
	 * para todo poste p
	 */
	public void creaPotMinImpoExpo() {
		long instCorr = dGCP.getInstIniCP();
		for(int p=0; p<cantPos; p++) {
			double pmin = ie.getMinTec().getValor(instCorr);			 
			BaseRest brEnt = new BaseRest(ConCP.RPOTMINIE_ENT, ie.getNombre(), p,
					cero, null, null, null, p ,Constantes.RESTMAYOROIGUAL);			
			brEnt.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, 1.0, null);										
			brEnt.agSumVar(ConCP.XIMPOEXPENT, ie.getNombre(), p, - pmin, null);			
			agrega1BR(generaNomBRest(ConCP.RPOTMINIE_ENT,ie.getNombre(),p), brEnt);
			
			BaseRest brSal = new BaseRest(ConCP.RPOTMINIE_SAL, ie.getNombre(), p,
					cero, null, null, null, p ,Constantes.RESTMENOROIGUAL);			
			brSal.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, -1.0, null);										
			brSal.agSumVar(ConCP.XIMPOEXPSAL, ie.getNombre(), p, pmin, null);			
			agrega1BR(generaNomBRest(ConCP.RPOTMINIE_SAL,ie.getNombre(),p), brSal);			
			
			instCorr += dGCP.getDur1Pos();
		}
	}

	/**
	 * Para las entradas de ofertas ya convocadas se crea una BaseRest que impone el día de entrada 
	 * con nombre de variable sin sufijo de escenarios
	 * haciendo 1 el xiniD asociado.
	 * SOLO SE INVOCA PARA OFERTAS YA CONVOCADAS. 
	 * 
	 *  xinid de la oferta - 1 = 0
	 *  para el día de convocatoria que es < 0
	 */
	public void creaEntradaYaConv(String nombreOf) {
		for(String s: listaOf) {
			if(dCP.getYaConv().get(s)) {
				int d = dCP.getPrimD().get(s);
				BaseRest br = new BaseRest(ConCP.RDIAOFYACONV, s, d,
						uno, null, null, null, 0, Constantes.RESTIGUAL);
				br.agSumVar(ConCP.XINID, s, d, 1.0, null);
				agrega1BR(ConCP.RDIAOFYACONV, br);
			}			
		}				
	}
	
//	/**
//	 * Para las ofertas de tipo ALEAT, crea la 
//	 * @param nomOf
//	 */
//	public void creaPotMaxPosteAleat(String nomOf) {		
//		for(int p=0; p<cantPos; p++) {
//			BaseRest br = new BaseRest(ConCP.RPOTMAXPOSTE, nomOf, p, null, dCP.getNomVAPot().get(nomOf), null ,Constantes.RESTMENOROIGUAL); 
//			br.agSumVar(ConCP.POT, nomOf, p, 1.0);
//			int primD = dCP.getPrimD().get(nomOf);
//			int ultD = dCP.getUltD().get(nomOf);			
//			// d recorre los días posibles de entrada de la oferta
//			// d2 es el día del horizonte al que pertenece el poste p
// 			int d2 = grafo.diaDePoste(p);
//			for(int d= primD; d<= ultD; d++) {  
//				int[] auxd = dias.get(nomOf).get(d);
//				double pmax = dCP.getPotMax().get(nomOf);
//				br.agSumVar(ConCP.XINID, nomOf, d, -pmax*auxd[d2]);
//			}
//			agrega1BR(generaNomBRest(ConCP.RPOTMAXPOSTE, nomOf, p),br);
//		}			
//	}
	
	@Override
	public void crearBaseObj() {	
		
		for(String s: listaOf) {
			if(!dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {
				double pre = dCP.getPrecio().get(s);
				if(dCP.getOperacion().get(s).equalsIgnoreCase(Constantes.VENTA)) pre = - pre;
				double coef = (double)dur1Pos*pre/(double)Constantes.SEGUNDOSXHORA;
				for(int p=0; p<cantPos; p++) {				
					BaseTermino bt = new BaseTermino(ConCP.POT, s, p, coef, null, grafo);
					agrega1BTalObj(bt);
				}
			}else {
				// la oferta es de tipo ALEAT
				double aux = uno;
				if(dCP.getOperacion().get(s).equalsIgnoreCase(Constantes.VENTA)) aux = - uno;
				for(int p=0; p<cantPos; p++) {
					String nomVAcoef = dCP.getNomVAPre().get(s);
					BaseTermino bt = new BaseTermino(ConCP.POT, s, p, 
							aux*(double)dur1Pos/(double)Constantes.SEGUNDOSXHORA, nomVAcoef, grafo);
					agrega1BTalObj(bt);
				}				
			}
			if( ( dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OBASE)  || dCP.getTipoOf().get(s).equalsIgnoreCase(ConCP.OPOT_MEDIA)) 
					&& dCP.getHayMulta().get(s) == true) {
				double coef = dCP.getMulta().get(s);
				BaseTermino bt = new BaseTermino(ConCP.ENERFALT, s, cantPos-1, coef, null, grafo);
				agrega1BTalObj(bt);
			}
		}		
	}




	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosImpoExpoCP)dpcp;
		ie = (ImpoExpo)this.participante;
	}




	public ImpoExpoCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(ImpoExpoCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public ImpoExpo getIe() {
		return ie;
	}

	public void setIe(ImpoExpo ie) {
		this.ie = ie;
	}



	public ArrayList<String> getListaOf() {
		return listaOf;
	}



	public void setListaOf(ArrayList<String> listaOf) {
		this.listaOf = listaOf;
	}



	public String getNomPar() {
		return nomPar;
	}



	public void setNomPar(String nomPar) {
		this.nomPar = nomPar;
	}



	public DatosImpoExpoCP getdCP() {
		return dCP;
	}



	public void setdCP(DatosImpoExpoCP dCP) {
		this.dCP = dCP;
	}



	public Hashtable<String, Hashtable<Integer, int[]>> getDias() {
		return dias;
	}



	public void setDias(Hashtable<String, Hashtable<Integer, int[]>> dias) {
		this.dias = dias;
	}



	public boolean isHayMulta() {
		return hayMulta;
	}



	public void setHayMulta(boolean hayMulta) {
		this.hayMulta = hayMulta;
	}
	
	
	

}
