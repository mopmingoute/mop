/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BaseObj is part of MOP.
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
import java.util.Hashtable;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;

public class BaseObj {
	
	
	/**
	 * CODIGO DE NOMBRE DE VARIABLES EN EL PROBLEMA LINEAL
	 * nombre_poste_e1-e2-e3-...-eE
	 * 
	 * donde e1,...eE son los números de escenario de las etapas 1,...,E
	 */
	
	
	/**
	 * Los términos que aparecen probabilidad 1 en el objetivo
	 * clave: nombre de la variable INCLUSO EL POSTE si corresponde
	 * valor: el coeficiente de la variable
	 */
	private Hashtable<String, Double> terminosFijos;
	
	/**
	 * Los términos del objetivo que expandirán creando un sumando para cada escenario
	 * ponderado con la probabilidad del escenario desde el inicio hasta el poste de la variable
	 * contenido en el término
	 */
	private ArrayList<BaseTermino> terminosBase;   
	private double terminoIndependiente;   // para para los cantEsc[etapa] escenarios de la etapa el valor del termino independiente.
	
	private static GrafoEscenarios ge;
	
	private static int cantPos; 
	
	
	private static ArrayList<BaseTermino> basesTerCostoEsc; 
	private static ArrayList<BaseVar> basesVarCostoEsc;
	private static ArrayList<DatosVariableControl> varsCostoEsc;
	private static Hashtable<String, DatosRestriccion> restsCostoEsc;
	
	
	public BaseObj() {
		terminosBase = new ArrayList<BaseTermino>();
		terminosFijos = new Hashtable<String, Double>();
		varsCostoEsc = new ArrayList<DatosVariableControl>();
		restsCostoEsc = new Hashtable<String, DatosRestriccion>();
	}
	
	static {
		basesTerCostoEsc = new ArrayList<BaseTermino>();
		basesVarCostoEsc = new ArrayList<BaseVar>();
	}

   /**
	* Expande cada terBase produciendo sumandos para el DatosObjetivo. Cada terBase produce tantos sumandos como el producto cartesiano de todos los 
	* escenarios posibles en las etapas de la 0 hasta la etapa del poste del terBase. Cada sumando está multiplcado por la probabilidad 
	* del escenario total hasta la etapa del poste (producto de las probabilidades condicionales de las etapas)
	* 
	* Si por ejemplo están involucradas las etapas 0, 1, 2 una variable con nombreBase pot_TGexp_70
	* en los escenarios 2, 4, 3 respectivos para cada etapa tendrá el nombre: pot_TGexp_70_2_4_3 y 
	* se le aplicará el coeficiente del escenario 3 de la etapa 2, tomado del atributo coeficientes 
	* del termino que da lugar a la variable, multiplicado por la probabilidad:
	*       probIni[2]*prob(0,2,4)*prob(1,4,3)
	*/
	public DatosObjetivo expandeBaseObj(){
		DatosObjetivo dobj = new DatosObjetivo();
		for(String s: terminosFijos.keySet()) dobj.getTerminos().put(s, terminosFijos.get(s));
		dobj.setTerminoIndependiente(terminoIndependiente);
		int cantEtapas = 0;
		for(BaseTermino bt: terminosBase) {
			if(bt.dameEtapa()>cantEtapas) cantEtapas = bt.dameEtapa();
		}
		EnumeradorLexicografico elex = ge.getEnumeradores().get(cantEtapas);
		elex.inicializaEnum();
		int[] vector = elex.devuelveVector();
		while(vector!=null) {
			double prob = ge.devuelveProbEscenario(vector);		
			if(ge.isEscUnico())prob = 1;
			for(BaseTermino bt: terminosBase) {
				String nomVar = ge.nombreVCPL(bt.getbV(), vector);
				String nomVA = bt.getNomVAcoef();
				int etapa = bt.getbV().getEtapa();
				double val = 1.0;
				if(nomVA!= null) val = ge.valorVA(nomVA, bt.getbV().getEntero(), vector[etapa]);
				dobj.getTerminos().put(nomVar, bt.getCoef()*val*prob);
			}
			vector = elex.devuelveVector();
		}
		return dobj;			
	}
	
	
	public static ArrayList<DatosVariableControl> creaVarsCostoPorEscenario(){
		ArrayList<DatosVariableControl> result = new ArrayList<DatosVariableControl>();
		int cantEtapas = 0;
		cantPos = ge.getDatGen().getCantPostes();
		cantEtapas = ge.getDatGen().getCantEtapas();
		EnumeradorLexicografico elex = ge.getEnumeradores().get(cantEtapas-1);
		elex.inicializaEnum();
		int[] vector = elex.devuelveVector();
		while(vector!=null) {	
			String nomCostoEsc = BaseVar.generaNomVar(ConCP.COSTOESC, ConCP.DESPACHO, cantPos-1, vector);
			result.add(new DatosVariableControl(nomCostoEsc, Constantes.VCCONTINUA, Constantes.VCLIBRE, null, null));     			
			vector = elex.devuelveVector();
		}
		varsCostoEsc = result;
		return result;			
	}
	
	
	
	public static Hashtable<String, DatosRestriccion> creaRestCostoPorEscenario(){
		Hashtable<String, DatosRestriccion> result = new Hashtable<String, DatosRestriccion>();
		int cantEtapas = ge.getDatGen().getCantEtapas();
		EnumeradorLexicografico elex = ge.getEnumeradores().get(cantEtapas-1);
		elex.inicializaEnum();
		int[] vector = elex.devuelveVector();
		while(vector!=null) {
			DatosRestriccion dr = new DatosRestriccion();
			dr.setNombre(BaseRest.creaNomRestEvolEsc(BaseRest.generaNomBRest(ConCP.RCOSTOESC,ConCP.DESPACHO, ge.getDatGen().getCantPostes()-1), vector));
			dr.agregarTermino(BaseVar.generaNomVar(ConCP.COSTOESC, ConCP.DESPACHO, cantPos-1, vector), 1.0);
//			for(String s: terminosFijos.keySet()) {
//				dr.agregarTermino(s, -terminosFijos.get(s));
//			}
			for(BaseTermino bt: basesTerCostoEsc) {
				String nomVar = ge.nombreVCPL(bt.getbV(), vector);
				String nomVA = bt.getNomVAcoef();
				int etapa = bt.getbV().getEtapa();
				double val = -1.0;
				if(nomVA!= null) val = ge.valorVA(nomVA, bt.getbV().getEntero(), vector[etapa]);
				dr.agregarTermino(nomVar, bt.getCoef()*val);
			}
			result.put(dr.getNombre(), dr);
			vector = elex.devuelveVector();
		}
		restsCostoEsc = result;
		return result;							
	}


	public ArrayList<BaseTermino> getTerminosBase() {
		return terminosBase;
	}
	
	
	
	public void setTerminosBase(ArrayList<BaseTermino> terminosBase) {
		this.terminosBase = terminosBase;
	}
	
	
	
	public Hashtable<String, Double> getTerminosFijos() {
		return terminosFijos;
	}
	
	
	
	public void setTerminosFijos(Hashtable<String, Double> terminosFijos) {
		this.terminosFijos = terminosFijos;
	}
	
	
	
	public double getTerminoIndependiente() {
		return terminoIndependiente;
	}
	
	
	
	public void setTerminoIndependiente(double terminoIndependiente) {
		this.terminoIndependiente = terminoIndependiente;
	}
	
	
	
	public static GrafoEscenarios getGe() {
		return ge;
	}
	
	
	
	public static void setGe(GrafoEscenarios ge) {
		BaseObj.ge = ge;
	}
	
	
	


	public static ArrayList<BaseTermino> getBasesTerCostoEsc() {
		return basesTerCostoEsc;
	}

	public static void setBasesTerCostoEsc(ArrayList<BaseTermino> basesTerCostoEsc) {
		BaseObj.basesTerCostoEsc = basesTerCostoEsc;
	}

	public static ArrayList<BaseVar> getBasesVarCostoEsc() {
		return basesVarCostoEsc;
	}

	public static void setBasesVarCostoEsc(ArrayList<BaseVar> basesVarCostoEsc) {
		BaseObj.basesVarCostoEsc = basesVarCostoEsc;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		ArrayList<String> terFs = new ArrayList<String>();
		terFs.addAll(terminosFijos.keySet());
		Collections.sort(terFs);
		boolean sinFijo = true;
		for(String vs: terFs) {
			sinFijo = false;
			if(terminosFijos.get(vs)<0) {
				sb.append("-");
			}else {
				sb.append("+");
			}
			sb.append(terminosFijos.get(vs) + vs + " ");
		}
		
//		if(terminosBase.size()>0 && sinFijo==false)sb.append("\n");
		Collections.sort(terminosBase);
		int il=1;
		for(BaseTermino bt: terminosBase) {
			if(bt.getCoef()!=0) {
				sb.append(bt.toString() + " ");	
	//			if(il%10==0 && il>1) sb.append("\n");
				il++;
			}
		}
	
		return sb.toString();
		
	}


	public static int getCantPos() {
		return cantPos;
	}


	public static void setCantPos(int cantPos) {
		BaseObj.cantPos = cantPos;
	}


	public static ArrayList<DatosVariableControl> getVarsCostoEsc() {
		return varsCostoEsc;
	}


	public static void setVarsCostoEsc(ArrayList<DatosVariableControl> varsCostoEsc) {
		BaseObj.varsCostoEsc = varsCostoEsc;
	}


	public static Hashtable<String, DatosRestriccion> getRestsCostoEsc() {
		return restsCostoEsc;
	}


	public static void setRestsCostoEsc(Hashtable<String, DatosRestriccion> restsCostoEsc) {
		BaseObj.restsCostoEsc = restsCostoEsc;
	}

	

}
