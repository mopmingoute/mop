/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BaseVar is part of MOP.
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
import java.util.Hashtable;

import cp_compdespProgEst.CompDespPE;
import datatypesProblema.DatosVariableControl;
import utilitarios.Constantes;
import utilitarios.EnumeradorLexicografico;

/**
 * A partir de esta base se expanden las variables en los escenarios
 * @author ut469262
 *
 */

public class BaseVar {
	
	
	private String nomVar; // nombre de la variable
	private String nomPar; // nombre del participante que la genera
	private String nomBaseVar;   // NOMBRE DE LA VARIABLE  + "_" + NOMBRE DEL PARTICIPANTE + "_"  + POSTE O DÍA   
	private Integer entero;   // puede ser poste o día
	private boolean esPoste;
	private Integer tipo;			/**BINARIA, ENTERA, CONTINUA*/
	private Integer dominio;			/**POSITIVA, LIBRE, SEMICONTINUA*/
	private Integer etapa;
	/**
	 * Si esPoste=true el propio poste
	 * Si esPoste=false y entero es el día, un poste representativo que pertenece al día
	 */
	private Integer poste;    
	private boolean yaDecidida;  // true si corresponde a una decisión ya tomada
	
	/**
	 * Si es true la expansión crea una variable por cada escenario formado por etapas 
	 * desde la etapa 0 hasata la etapa del poste
	 * Si es false se crea una variable para cada escenario SOLO DE LA ETAPA DEL POSTE
	 */
	private boolean expandeDesde0; 
	
	/**
	 * atributos de las cotas superior e inferior
	 * A LAS VARIABLES BINARIAS SE LE ESTÁ PONIENDO COTAS 0 Y 1
	 */
	private Double cinffija;
	private String vacinf;
	private Double csupfija;
	private String vacsup;
	

	private static GrafoEscenarios ge;
	
	
	/**
	 * Clave: String nomVar + "_" + "nomPar" generado por método estático this.generaNomVarPar
	 * Valor: true si las BaseVar con esos nombres son por poste. False si son por día
	 */
	private static Hashtable<String, Boolean> catalogoVarParPos;
	
	
	static {
		catalogoVarParPos = new Hashtable<String, Boolean>(); 
	}
	
	
	/**
	 * 
	 * @param nomVar nombre de la variable de control del participante, se saca de cp_constantes.
	 * @param nomPar nombre del participante, excepto para el caso de las ofertas de comercio internacional donde es el nombre de la oferta
	 * @param entero poste o día de la BaseVar
	 * @param esPoste true si el entero asociado a la BaseVar es el poste, false si es el día
	 * @param tipo  BINARIA, ENTERA, CONTINUA  tomados de Constantes
	 * @param dominio   POSITIVA, LIBRE, SEMICONTINUA, etc. tomados de Constantes
	 * @param yaDecidida true si es una variable ya decidida en un poste anterior al inicio del horizonte del CP
	 * @param cinffija   cota inferior fija 
	 * @param vacinf    si no es null nombre de la variable aleatoria que MULTIPLICA LA COTA INFERIOR FIJA y si es null no se emplea
	 * @param csupfija   cota superior fija 
	 * @param vacsup    si no es null nombre de la variable aleatoria que MULTIPLICA LA COTA SUPERIOR FIJA y si es null no se emplea
	 * @param expandeDesde0  si es true en la expansion se crea una variable para cada escenario desde el origen y si
	 * es false una variable para cada escenario de la etapa asociada a entero
	 */
	public BaseVar(String nomVar, String nomPar, Integer entero, boolean esPoste, Integer tipo, Integer dominio, boolean yaDecidida, 
			Double cinffija, String vacinf, Double csupfija, String vacsup, boolean expandeDesde0) {
		super();
		this.nomVar = nomVar;
		this.nomPar = nomPar;
		this.nomBaseVar = generaNomBVar(nomVar, nomPar, entero);
		this.entero = entero;
		this.esPoste = esPoste;
		this.tipo = tipo;
		this.dominio = dominio;
		this.yaDecidida = yaDecidida;
		this.cinffija = cinffija;
		this.vacinf = vacinf;
		this.csupfija = csupfija;
		this.vacsup = vacsup;
		if(esPoste) {
			etapa = ge.etapaDePoste(entero);
			poste = entero;
		}else {
			etapa = ge.etapaDeDia(entero);
			poste = ge.posRepDeDia(entero);
		}
		this.expandeDesde0 = expandeDesde0;
		String clave = generaNomVarPar(nomVar, nomPar);
		if(!catalogoVarParPos.contains(clave)) catalogoVarParPos.put(clave, esPoste);
		
		
	}


	public static String generaNomBVar(String nomVar, String nomPar, Integer entero) {
		return CompDespPE.generaNomBVar(nomVar, nomPar, entero);
	}
	
	
	public static String generaNomVar(String nomVar, String nomPar, int entero, int[] vecEsc) {
		StringBuilder nom = new StringBuilder(CompDespPE.generaNomVarPar(nomVar, nomPar) + "_" + entero);
		if(!ge.isEscUnico()) {
			nom.append("_");			
			for(int e=0; e<vecEsc.length; e++) {
				nom.append(vecEsc[e]);
				if(e<vecEsc.length-1) nom.append("_");
			}
		}
		return nom.toString();
	}
	
	
	
	public static String generaNomVar(String nomVarPar, int entero, int[] vecEsc) {
		StringBuilder nom = new StringBuilder(nomVarPar + "_" + entero);
		if(!ge.isEscUnico()) {
			nom.append("_" + generaSufijoEscenario(vecEsc));			
		}
		return nom.toString();
	}
	
	
	public static String generaNomVarPar(String nomVar, String nomPar) {
		return CompDespPE.generaNomVarPar(nomVar, nomPar);		
	}
	

	/**
	 * Genera string de la forma e1_e2_....en con los valores de los escenarios de cada etapa del vector esc
	 * @param esc
	 * @return
	 */
	public static String generaSufijoEscenario(int[] esc) {
		String suf = "";
		for(int i=0; i<esc.length; i++) {
			suf = suf + esc[i];
			if(i<esc.length-1) suf = suf + "_";
		}
		return suf;
	}

	/**
	 * Se crean tantas variables como escenarios posibles 
	 * recorriendo etapas desde el poste 0 hasta el poste.
	 * o bien tantos como escenarios dela etapa, según el valor de expandeDesde0
	 * 
	 * Las variables de control con poste o día < 0 que corresponden a decisiones ya tomadas al inicio
	 * del horizonte, se expanden como una única variable de control, que en su nombre no lleva sufijo de escenario.
	 */	
	public ArrayList<DatosVariableControl> expandeBaseVar(){
		ArrayList<DatosVariableControl> result = new ArrayList<DatosVariableControl>();
		Integer poste = entero;
		if(entero>=0) {
			int etapa = 0;
			if(esPoste) {
				etapa = ge.etapaDePoste(entero);
			}else {
				// el entero indica día
				etapa = ge.etapaDeDia(entero);
				poste = ge.posRepDeDia(entero);
			}
			EnumeradorLexicografico enumerador;
			if(expandeDesde0) {				
				enumerador = ge.getEnumeradores().get(etapa);
			}else {
				enumerador = ge.getEnumParciales().get(etapa);
			}
			enumerador.inicializaEnum();
			int[] vector = enumerador.devuelveVector();
			Double ci = cinffija;
			Double cs = csupfija;		
			while(vector != null) {
				if(vacinf!=null) ci = cinffija*ge.valorVA(vacinf, poste, vector[etapa] );
				if(vacsup!=null) cs = csupfija*ge.valorVA(vacsup, poste, vector[etapa]);
				String nombre = ge.nombreVCPL(this, vector);
				DatosVariableControl dvc = new DatosVariableControl(nombre, tipo, dominio, ci, cs);
				result.add(dvc);
				vector = enumerador.devuelveVector();
			}
		}else {
			String nombre = generaNomBVar(nomVar, nomPar, entero); 
			DatosVariableControl dvc = new DatosVariableControl(nombre, tipo, dominio, null, null);
			result.add(dvc);			
		}
		return result;
	}
	

	/**
	 * Devuelve true si las BaseVar con esos nombres son por poste y false si son por día
	 * @param nomVar
	 * @param nomPar
	 * @return
	 */
	public static boolean esPorPoste(String nomVar, String nomPar) {
		String clave = generaNomVarPar(nomVar, nomPar);
		return catalogoVarParPos.get(clave);		
	}
	
	
	
	public String getNomBaseVar() {
		return nomBaseVar;
	}


	public void setNomBaseVar(String nomBaseVar) {
		this.nomBaseVar = nomBaseVar;
	}


	public String getNomVar() {
		return nomVar;
	}


	public void setNomVar(String nomVar) {
		this.nomVar = nomVar;
	}


	public String getNomPar() {
		return nomPar;
	}


	public void setNomPar(String nomPar) {
		this.nomPar = nomPar;
	}


	public int getEntero() {
		return entero;
	}


	public void setEntero(int entero) {
		this.entero = entero;
	}


	public boolean isEsPoste() {
		return esPoste;
	}


	public void setEsPoste(boolean esPoste) {
		this.esPoste = esPoste;
	}

	

	public int getPoste() {
		return poste;
	}


	public void setPoste(int poste) {
		this.poste = poste;
	}


	public Integer getTipo() {
		return tipo;
	}
	public void setTipo(Integer tipo) {
		this.tipo = tipo;
	}
	public Integer getDominio() {
		return dominio;
	}
	public void setDominio(Integer dominio) {
		this.dominio = dominio;
	}

	

	public boolean isExpandeDesde0() {
		return expandeDesde0;
	}



	public void setExpandeDesde0(boolean expandeDesde0) {
		this.expandeDesde0 = expandeDesde0;
	}






	public double getCinffija() {
		return cinffija;
	}


	public void setCinffija(double cinffija) {
		this.cinffija = cinffija;
	}


	public String getVacinf() {
		return vacinf;
	}


	public void setVacinf(String vacinf) {
		this.vacinf = vacinf;
	}


	public double getCsupfija() {
		return csupfija;
	}


	public void setCsupfija(double csupfija) {
		this.csupfija = csupfija;
	}


	public String getVacsup() {
		return vacsup;
	}


	public void setVacsup(String vacsup) {
		this.vacsup = vacsup;
	}


	public static GrafoEscenarios getGe() {
		return ge;
	}

	public static void setGe(GrafoEscenarios ge) {
		BaseVar.ge = ge;
	}



	public int getEtapa() {
		return etapa;
	}



	public void setEtapa(int etapa) {
		this.etapa = etapa;
	}


	public boolean isYaDecidida() {
		return yaDecidida;
	}


	public void setYaDecidida(boolean yaDecidida) {
		this.yaDecidida = yaDecidida;
	}


	public static Hashtable<String, Boolean> getCatalogoVarParPos() {
		return catalogoVarParPos;
	}


	public static void setCatalogoVarParPos(Hashtable<String, Boolean> catalogoVarParPos) {
		BaseVar.catalogoVarParPos = catalogoVarParPos;
	}	
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(nomBaseVar + "\t");
		sb.append("esPoste = " +esPoste + "\t");
		sb.append(Constantes.TIPOSVC[tipo] + "\t");
		sb.append(Constantes.TIPOSVC[dominio] + "\t");
		sb.append("yaDecidida = " + yaDecidida + "\t");
		sb.append("cinffija = " + cinffija + "\t");
		if(vacinf!=null) sb.append("vacinf = " + vacinf + "\t");
		sb.append("csupfija = " + csupfija + "\t");
		if(vacsup!=null) sb.append("vacsup =" + vacsup + "\t");
		return sb.toString();	
	}

}
