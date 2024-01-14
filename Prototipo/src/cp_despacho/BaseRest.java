/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BaseRest is part of MOP.
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

package cp_despacho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import cp_compdespProgEst.CompDespPE;
import datatypesProblema.DatosRestriccion;
import utilitarios.Constantes;
import utilitarios.EnumeradorLexicografico;

/**
 * 
 * Permite generar múltiples restricciones en la programación estocástica, recorriendo los escenarios, 
 * a partir de una información de base
 */
public class BaseRest {
	
	private static GrafoEscenarios ge;
	
	/**
	 * String que encabezará todas las restricciones que se creen a partir de esta BaseRest 
	 * por ejemplo balance_bonete_10  (10 es el sufijo de poste)
	 * Si hay una única restricción para todos los postes se carga "T" en lugar del poste
	 * Para pasar a los nombres de restricciones puede ser necesario agregar los escenarios.
	 */
	private String nomBaseRes;  
	
	/**
	 * Los términos invariables en los escenarios, porque la variable es la misma en todos los escenarios
	 * y serán los mismos en todas las restricciones generadas por la expansión:
	 * clave: nombre de la variable (incluso eventualmente el poste)
	 * valor: coeficiente
	 */
	private Hashtable<String, Double>  terminosFijos;
	
	/**
	 * Los términos que dependen del escenario desde la etapa 0 a la del poste del término 
	 * y en la expansión serán distintos para cada restricción
	 * clave: nomBaseVar obtenido con BaseVar.generaNomBVar()
	 * valor: el BaseTermino con la BaseVar de ese nombre
	 */
	private Hashtable<String,BaseTermino> terminosBase; 
	
	/**
	 * Lista de nomBaseVar de los BaseVar contenidos en terminosBase
	 */
	private ArrayList<String> nomsBaseVar;
	
	private boolean idNula;  // si hay algún términoBase o términoBase de coeficiente no nulo se setea en false
	
	private BaseSM segM;
	
	private int tipo;    //tipo de operador Igualdad, menoroigual, mayoroigual
	
	
	/**
	 * @param nomRestDeConCP una constante que define nombres de restricciones tomada del paquete ConCP
	 * @param nomPar nombre del participante o null si no corresponde
	 * @param ent poste o día o null si no corresponde. 
	 * ATENCION: LAS RESTRICCIONES QUE GENERAN VARIABLES QUE ACUMULAN SUMANDOS DEBEN TENER EL POSTE O DÍA DEL ÚLTIMO SUMANDO
	 * @param fijoSM valor fijo del segundo miembro si corresponde 
	 * @param nomsVASMSum lista de nombres de las variables aleatorias que se adicionan en combinación lineal al segundo miembro si corresponde o null en otro caso
	 * @param coefsVASMSum coeficientes respectivos de la combinación lineal
	 * @param nomVASMProd nombre de la VA que multiplica los sumandos anteriores en el segundo miembro si corresponde o null en otro caso
	 * @param posteSM el poste para dar valor a la VA nomVASM si es != null y el SM es aleatorio
	 * @param tipo (=, <, <=, etc.) según utilitarios.Constantes
	 * 
	 */
	public BaseRest(String nomRestDeConCP, String nomPar, Integer ent, 
			Double fijoSM, ArrayList<String> nomsVASMSum, ArrayList<Double> coefsVASMProd, String nomVASMProd, Integer posteSM, Integer tipo) {
		this.nomBaseRes = generaNomBRest(nomRestDeConCP, nomPar, ent);
		this.tipo = tipo;
		segM = new BaseSM(fijoSM, nomsVASMSum, coefsVASMProd, nomVASMProd, posteSM);
		terminosFijos = new Hashtable<String, Double>();
		terminosBase = new Hashtable<String, BaseTermino>();
		nomsBaseVar = new ArrayList<String>();
		idNula = true;
	}
	


	/**
	 * Genera un nombre de restricción tomando un nombre que debe ser extraído del paquete ConCP,
	 * un nombre de participante y un entero día o poste.
	 * Si el String nomPar es null no lo agrega
	 * Si el Integer es null no agrega un entero
	 * @param constDeConCP
	 * @param nomPar
	 * @param entero
	 * @return
	 */
	public static String generaNomBRest(String constDeConCP, String nomPar, Integer entero) {
		return CompDespPE.generaNomBRest(constDeConCP, nomPar, entero);
		
	}
	
	/**
	* Produce las restricciones creando una para cada elemento del producto cartesiano de todos los escenarios posibles 
	* en las etapas incluidas en los términos base de la RestBaseExp.
	* Si por ejemplo los términos base están en las etapas 0, 1, 4 se producirá una restricción 
	* para cada escenario del producto cartesiano de las etapas 0, 1, 2, 3, 4.
	* 
	* Un BaseTermino que tiene nombreBase pot_TGexp, en el escenario de etapas 0, 4, 3, 0, 1 
	* generará una variable con el nombre: pot_TGexp _0_4_3_0_1
	* 
	* Si coef[] tiene dimension 1 se repite el valor que contiene para todos los escenarios de la expansión
	*/
	public ArrayList<DatosRestriccion> expandeBaseRest(GrafoEscenarios ge){
		ArrayList<DatosRestriccion> result = new ArrayList<DatosRestriccion>();
		int etapaMax = 0;
		for(BaseTermino bt: terminosBase.values()) {
			if(bt.dameEtapa()>etapaMax) etapaMax = bt.dameEtapa();
		}
		EnumeradorLexicografico elex = ge.getEnumeradores().get(etapaMax);
		elex.inicializaEnum();
		int[] vector = elex.devuelveVector();
		while(vector!=null) {    
			// vector contiene el número de escenario en cada etapa
			DatosRestriccion dres = new DatosRestriccion();
			dres.setNombre(creaNomRestEvolEsc(nomBaseRes, vector));
			for(BaseTermino bt: terminosBase.values()) {
				BaseVar bV = bt.getbV();
				String nombreVar = ge.nombreVCPL(bV, vector);			
				int poste = bV.getPoste();
				double coef = bt.getCoef();
				String nomVACoef = bt.getNomVAcoef();
				if(nomVACoef!=null) {
					int etapa = bV.getEtapa();
					double valVA = ge.valorVA(nomVACoef, poste, vector[etapa]);
					coef = coef*valVA;
				}
				dres.getTerminos().put(nombreVar, coef);
			}
			dres.setTipo(tipo);
			int posteSM = segM.getPosteSM();
			int etapaSM = ge.etapaDePoste(posteSM);
			dres.setSegundoMiembro(segM.valorSM(vector[etapaSM]));

			result.add(dres);

			vector = elex.devuelveVector();
		}
		return result;		
	}


	/**
	 * Crea a partir del nombre base, un nombre de restricción QUE INCLUYE LA EVOLUCIÓN DE LOS ESCENARIOS
	 * @param nombreBase  de la BaseRest que se está expandiendo
	 * @param vectorEsc  vector con escenarios desde etapa 0 hasta la última etapa E que
	 * abarca la BaseRest
	 * @return el nombre de la restricción con el sufijo  _e0_e1_...eE
	 */
	public static String creaNomRestEvolEsc(String nombreBase, int[] vectorEsc) {
		StringBuilder nom = new StringBuilder(nombreBase +  "_");
		for(int e=0; e<vectorEsc.length; e++) {
			nom.append(vectorEsc[e]);
			if(e!=vectorEsc.length-1) nom.append("_");
		}
		return nom.toString();
	}



	/**
	* Cuando p>0 agrega a la BaseRest this un BaseTermino con la variable
	* nombreParticipante-nombreVar y con el coeficiente coef para todos los
	* escenarios, en el poste p
	* Cuando p<0 agrega a todos los valores del double[] segundo miembro de la BaseRest, el producto de 
	* coef[escenario] por el valor de la variable rezagada (-p) pasos CUYO VALOR SE CONOCE COMO DATO
	* LOS SUMANDOS DE UNA RESTRICCIÓN DEBEN NECESARIAMENTE AGREGARSE POR ESTA FUNCIÓN
	* ===============================================================================
	* 
	* @param nombreVar  nombre de la variable, tomado como constante de clase ConCP, ejemplo
	* @param nomPar nombre del paraticipante y en el caso de ofertas de impoexpo nombre de la oferta
	* @param entero poste o día que indiza la variable
	* @param coef  array con los coeficientes de la variable en la restricción, normalmente es un
	* array con un solo elemento, en previsión de que pueda programarse una expansión posterior.
	*
	*/
	public void agSumVar(String nomVar, String nomPar, Integer entero, double coef, String nomVAcoef) {
		if(Math.abs(coef)>ConCP.MINIMOCOEFNONULO) {
			idNula = false;

			if(ge.existeValRez(nomVar, nomPar, entero)) {			
				double valRez = ge.dameValRez(nomVar, nomPar, entero);				
				segM.setFijo(segM.getFijo()-valRez*coef); 
			}else {	
				BaseVar bv = CompDespPE.dameBaseVar(nomVar, nomPar, entero);
				BaseTermino bt = new BaseTermino(nomVar, nomPar, entero, coef, nomVAcoef, ge);
				terminosBase.put(bv.getNomBaseVar(), bt);
				nomsBaseVar.add(bv.getNomBaseVar());
			}
		}
	}

	/**
	 * Agrega a la restricción this los BaseTerm de otra BaseTerm br
	 * @param br
	 */
	public void contribuir(BaseRest br) {
	
		for(String sbv: br.getTerminosFijos().keySet()) {
			Double coef1 = terminosFijos.get(sbv);
			if(coef1==null) {
				terminosFijos.put(sbv, coef1);
			}else {
				terminosFijos.remove(sbv);
				Double coef2 = coef1 + br.getTerminosFijos().get(sbv);
				terminosFijos.put(sbv, coef2);
			}
		}
		for(BaseTermino bt: br.getTerminosBase().values()) {
			if(bt.getNomVAcoef()!=null) {
				System.out.println("Se intentó contribuir a una restricción con otra de coeficiente aleatorio, variable: " + br.toString());
				System.exit(1);
			}
			BaseVar bV = bt.getbV();
			String nbV = bV.getNomBaseVar();
			double coef2 =  bt.getCoef();
			if(nomsBaseVar.contains(nbV)) {
				double coef1 = terminosBase.get(nbV).getCoef();
				terminosBase.get(nbV).setCoef(coef1+coef2);    
			}else {
				terminosBase.get(nbV).setCoef(coef2);
			}
			
		}
		
		
		
		
//		while (itdV.hasNext()) {
//			String currKey = itdV.next();
//			double currVal = terminos.getOrDefault(currKey, 0.0) + dr.terminos.get(currKey);
//			terminos.remove(currKey);
//    		if (Math.abs(currVal) > Constantes.EPSILONCOEF ) { 
//    			terminos.put(currKey, currVal);
//			}		
//			
//		}
	}
	

	public static GrafoEscenarios getGe() {
		return ge;
	}


	public static void setGe(GrafoEscenarios grafo) {
		ge = grafo;
	}


	public Hashtable<String, Double> getTerminosFijos() {
		return terminosFijos;
	}


	public void setTerminosFijos(Hashtable<String, Double> terminosFijos) {
		this.terminosFijos = terminosFijos;
	}





	
	
	public Hashtable<String, BaseTermino> getTerminosBase() {
		return terminosBase;
	}



	public void setTerminosBase(Hashtable<String, BaseTermino> terminosBase) {
		this.terminosBase = terminosBase;
	}



	public ArrayList<String> getNomsBaseVar() {
		return nomsBaseVar;
	}



	public void setNomsBaseVar(ArrayList<String> nomsBaseVar) {
		this.nomsBaseVar = nomsBaseVar;
	}



	public BaseSM getSegM() {
		return segM;
	}



	public void setSegM(BaseSM segM) {
		this.segM = segM;
	}






	public int getTipo() {
		return tipo;
	}



	public void setTipo(int tipo) {
		this.tipo = tipo;
	}



	public String getNomBaseRes() {
		return nomBaseRes;
	}



	public void setNomBaseRes(String nomBaseRes) {
		this.nomBaseRes = nomBaseRes;
	}



	public boolean isIdNula() {
		return idNula;
	}



	public void setIdNula(boolean idNula) {
		this.idNula = idNula;
	}



	public String toString() {
		StringBuilder sb = new StringBuilder(nomBaseRes + ":\t");
		ArrayList<String> tf = new ArrayList<String>();
		tf.addAll(terminosFijos.keySet());
		Collections.sort(tf);
		double coef = 0;
		for(String s: tf) {
			coef = terminosFijos.get(s);
			if(coef!=0) {
				sb.append(coef + "." + s + "\t");
			}		
		}
		ArrayList<String> nsBV = new ArrayList<String>();
		nsBV.addAll(terminosBase.keySet());
		Collections.sort(nsBV);
		for(String n1BV: nsBV) {
			BaseTermino bt = terminosBase.get(n1BV);
			if(bt.getCoef()!=0)	sb.append(bt.toString() + "\t");
		}
		if(tipo==Constantes.RESTIGUAL) {
			sb.append("=");
		}else if(tipo==Constantes.RESTMAYOROIGUAL) {
			sb.append(">=");
		}else {
			sb.append("<=");
		}
		sb.append("\t");
		sb.append(segM.getFijo());
		if(segM.getNomsVASMSum()!=null) {
			sb.append("[");
			int iva=0;
			for(String s: segM.getNomsVASMSum()) {
				sb.append("+");
				sb.append(segM.getCoefsVASMSum().get(iva));
				sb.append(s);
				iva++;
			}
			sb.append("]");
		}
		if(segM.getNomVASMProd()!=null) {
			sb.append("*");
			sb.append(segM.getNomVASMProd());
			sb.append("[p=" + segM.getPosteSM() + ",esc]");
			
		}
		return sb.toString();
	}

	
}



