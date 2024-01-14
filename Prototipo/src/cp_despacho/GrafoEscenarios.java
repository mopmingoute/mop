/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GrafoEscenarios is part of MOP.
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosGrafoEscCP;
import cp_datatypesEntradas.DatosMatPTrans;
import javafx.scene.web.WebView;
import parque.Corrida;
import procesosEstocasticos.MatPTrans;
import tiempo.LineaTiempo;
import utilitarios.Constantes;
import utilitarios.EnumeradorLexicografico;


/**
 * SE CONVIENE QUE EN LA ETAPA INICIAL HAY SIEMPRE UN ÚNICO ESCENARIO, EL 0
 * @author ut469262
 *
 */

public class GrafoEscenarios {
	
	private DatosGeneralesCP datGen;
	
	private Corrida corrida;
	
	private boolean escUnico;   // true si en el grafo escenarios hay un único escenario posible para recorrer las etapas.
	
	private int cantEscTotal;  // la cantidad total de escenarios que se pueden generar al recorrer las etapas eligiendo escenarios de etapa
	
	private int[] cantEscEtapas;
	
	private ArrayList<MatPTrans> matTransicion; // las probabilidades de transición entre escenarios en etapas consecutivas, empezando por las probabilidades desde e=0 hacia e=1. Denotamos la probabilidad de pasar del escenario s en la etapa e, al escenario w en la etapa e+1 por prob(e,s,w)
	
		
	/**
	 * Clave: nombreParticipante-nombreVariable-entero (poste o día)
	 * se obtiene con el método this.claveVRez()
	 * Valor: el valor rezagado para el poste entero (poste o día) 
	 * 
	 */
	private Hashtable<String, Double> valoresRezagados;
	
	
	/**
	 * Clave: nombreVA + "_" + etapa + "_" + escenario que se obtiene con el método GrafoEscenarios.claveEnGrafo
	 *  etapa entero de 0 hasta cantEtapas-1
	 *  escenario entero de 0 hasta cantEscEtapa[etapa]-1
	 * Valor: double[] con los valores de la variable aleatoria nombreVA por poste en la etapa y el escenario
	 */
	private Hashtable<String, double[]> valoresVA;
	
	/**
	 * El enumerador en el get(e) genera los escenarios desde la etapa 0 hasta la e, 
	 * es decir vectores de dimensión e+1
	 * Ejemplo en el get(3) se producen vectores de 4 valores por ejemplo 1, 10, 3, 2
	 * es decir el escenario total se forma con el escenario 1 de la etapa 0, el 10 de la
	 * etapa 1, etc.
	 */
	private ArrayList<EnumeradorLexicografico> enumeradores;
	
	/**
	 * El enumerador en el get(e) genera escenarios DE LA ETAPA e EXCLUSIVAMENTE, 
	 * es decir vectores de dimensión 1
	 */
	private ArrayList<EnumeradorLexicografico> enumParciales;

	public GrafoEscenarios(DatosGrafoEscCP dat, DatosGeneralesCP dgen, Corrida corrida) {
		enumeradores = new ArrayList<EnumeradorLexicografico>();
		enumParciales = new ArrayList<EnumeradorLexicografico>();
		valoresRezagados = new Hashtable<String, Double>();
		this.datGen = dgen;
		this.corrida = corrida;
		cantEscEtapas = dat.getCantEscEtapa();
		int cantEtapas = dgen.getCantEtapas();
		int cantEscTotal = 1;
		for(int e=0; e<cantEtapas; e++) {
			int[] ceros = new int[e+1];
			int[] cotasup = new int[e+1];
			for(int i=0; i<=e; i++) {
				ceros[i] = 0;
				cotasup[i] = cantEscEtapas[i]-1;
			}
			int[] a0 = new int[] {0};
			int[] ae = new int[] {cantEscEtapas[e]-1};
			enumeradores.add(new EnumeradorLexicografico(e+1, ceros, cotasup));
			cantEscTotal = cantEscTotal*cantEscEtapas[e];
			enumParciales.add(new EnumeradorLexicografico(1, a0 , ae));
		}
		escUnico = false;
		if(cantEscTotal==1) escUnico = true;
		valoresVA = dat.getValoresVA();
		matTransicion = dat.getMatTransicion();

	}
	
	
	
	/**
	 * Devuelve la etapa a la que pertenece un poste, con las etapas empezando en cero
	 * y si el poste está fuera de rango devuelve -1
	 * @param poste
	 */
	public Integer etapaDePoste(Integer poste) {
		if(poste==null) return null;
		for(int e=0; e<datGen.getCantEtapas(); e++) {
			if(poste>=datGen.getPosIniEtapa()[e] & poste<= datGen.getPosFinEtapa()[e]) return e;
		}
		return null;		
	}
	
	
	/**
	 * Devuelve la etapa asociada al día, que se usa para la expansión de las BaseVar, empezando en cero
	 * Se asigna la etapa del INSTANTE DE INICIO DEL DÍA, excepto para el primer día
	 * cuando la corrida comienza en el medio de ese día. En ese caso se toma ETAPA 0
	 * 
	 * El día cero es el día del instante inicial de la corrida CP
	 * 
	 * @param dia que empieza en cero
	 * @return
	 */
	public int etapaDeDia(int dia) {		
		return etapaDePoste(posRepDeDia(dia));
	}

	/**
	 * Devuelve un número de poste representativo de cada día (que pertenece al día), 
	 * con los postes empezando en cero
	 * @param dia
	 * @return
	 */
	public int posRepDeDia(int dia) {
		LineaTiempo lt = corrida.getLineaTiempo();
		long instIni = datGen.getInstIniCP();
		GregorianCalendar gcInstIni = lt.dameTiempo(instIni);
		GregorianCalendar gcRepDia = (GregorianCalendar)gcInstIni.clone();
		gcRepDia.add(Calendar.HOUR, Constantes.CANT_HORAS_DIA*dia);
		// gcRepDia está ahora en el día		
		long segHastaRepDia = lt.restarFechas(gcRepDia, gcInstIni);
		if(segHastaRepDia<=0) {
			return 0;
		}else {
			return (int)Math.floor(segHastaRepDia/datGen.getDur1Pos());
			
		}		
	}
	
	/**
	 * Devuelve un nombre de variable de control del problema lineal resultante de la expansión
	 * 
	 * @param vecEsc el vector con los números de escenario en las etapas de la 0 hasta la etapa del poste o dia 
	 * 
	 * Por ejemplo bonete_pot_120_0_4_10 quiere decir que el poste es el 120 y la variable corresponde a la sucesión
	 * de escenarios por etapas 0, 4, 10
	 * ATENCIÓN: SI EN EL CONJUNTO DEL GRAFO HAY UN ÚNICO ESCENARIO SE OMITE LA NOTACIÓN 0_0_...._0 QUE CORRESPONDERÍA.
	 */
	public String nombreVCPL(BaseVar bV, int[] vecEsc){
		if(bV.getEntero()<0 || escUnico) return bV.getNomBaseVar();
		StringBuilder nom = new StringBuilder(bV.getNomBaseVar() + "_");
		for(int e=0; e<=bV.getEtapa(); e++) {
			System.out.println(nom);
			nom.append(vecEsc[e]);
			if(e<bV.getEtapa()) nom.append("_");			
		}
		return nom.toString();		
	}

	
	/**
	 * Devuelve un nombre de variable de control del problema lineal a partir de un nombre 
	 * agregándole el sufijo con los escenarios
	 * 
	 * @param nomvar es el nombre de la variable, que se obtiene de ConCP
	 * @param nompar es el nombre del participante, que se obtiene de ConCP
	 * @param entero es el número de poste o día
	 * @param vecEsc el vector con los números de escenario en las etapas de la 0 hasta la etapa del poste o dia 
	 * 
	 * ATENCIÓN: SI EN EL CONJUNTO DEL GRAFO HAY UN ÚNICO ESCENARIO SE OMITE LA NOTACIÓN 0_0_...._0 QUE CORRESPONDERÍA.
	 */
	public String nombreVCPL(String nomvar, String nompar, int entero, int[] vecEsc){
		StringBuilder nom = new StringBuilder(nompar + "_" + nompar + "_" + entero + "_");
		int cantEtapas = vecEsc.length;
		for(int e=0; e<cantEtapas; e++) {
			nom.append(vecEsc[e]);
			if(e<cantEtapas-1) nom.append("_");			
		}
		return nom.toString();		
	}	
	
	
	
	/**
	 * Devuelve el valor de una variable aleatoria en un poste y escenario
	 * @param nombreVA
	 * @param poste dentro DEL HORIZONTE TOTAL DEL CP empezando en 0 
	 * @param esc escenario EN LA ETAPA, no desde el origen 
	 * @return
	 */
	public double valorVA(String nombreVA, int poste, int escenario) {	
		int etapa = etapaDePoste(poste);
		String clave = claveEnGrafo(nombreVA, etapa, escenario);
		double[] aux = valoresVA.get(clave);
		if(aux==null) {
			System.out.println("La variable aleatoria " + nombreVA + " no existe o no tiene valores para poste " + poste + " y escenario " + escenario);
		}
		return aux[poste-datGen.getPosIniEtapa()[etapa]];		
	}
	
	
	public static String claveEnGrafo(String nombreVA, int etapa, int esc) {
		String clave = nombreVA + "_" + etapa + "_" + esc;
		return clave;
		
	}
	
	/**
	 * Devuelve el día del horizonte al que pertenece un poste.
	 * EL PRIMER DÍA PUEDE NO CAER ENTERO EN EL HORIZONTE
	 */
	public int diaDePoste(int p) {
		long iIniCP = datGen.getInstIniCP();
		long iIniPos = iIniCP + p*datGen.getDur1Pos();
		LineaTiempo lt = corrida.getLineaTiempo();
		GregorianCalendar gcIniCP = lt.dameTiempo(iIniCP);
		GregorianCalendar gcIniPos = lt.dameTiempo(iIniPos);
		int diaCP = gcIniCP.get(Calendar.DAY_OF_YEAR);
		int diaPos = gcIniPos.get(Calendar.DAY_OF_YEAR);
		int dif = diaPos - diaCP;
		if(dif>=0) {
			return dif;
		}else {
			int dACP = 365;
			if(lt.bisiesto(gcIniCP.get(Calendar.YEAR))) dACP = 366;
			return dACP - diaCP + diaPos;
		}
	}
	
	
	
	
	/**
	 * Si existe un valor rezagado para la variable nomVar del participante nomPar
	 * con el valor entero<0 devuelve true, de lo contrario false
	 * @param nomVar
	 * @param nomPar
	 * @param rezago
	 * @return
	 */
	public boolean existeValRez(String nomVar, String nomPar, int entero) {	
		boolean res = false;
		String clave = claveVRez(nomVar, nomPar, entero);
		if(valoresRezagados.containsKey(clave)) res = true;
		return res;
	}
	
	/**
	 * Devualve el valor de una variable de control rezagada
	 * @param nomVar nombre de la variable por ejemplo "turbinado"
	 * @param nomPar nombre del participante por ejemplo "bonete"
	 * @param entero número negativo que indica el ordinal del poste respecto al poste 0
	 * o bien diaRezagado respecto al dia inicial de la corrida CP
	 * por ejemplo -2 significa dos postes antes del poste 0 inicial del horizonte	
	 * @return
	 */
	public double dameValRez(String nomVar, String nomPar, int entero) {
		if(!existeValRez(nomVar, nomPar, entero)){
			System.out.println("Se pidió valor rezagado que no existe para " + nomVar + " " + nomPar + " " + entero);
			System.exit(1);
		}
		double valor = valoresRezagados.get(claveVRez(nomVar, nomPar, entero));
		return valor;
	}
	
	public String claveVRez(String nomVar, String nomPar, int entero) {
		String clave = nomVar + "_" + nomPar + "_" + entero;
		return clave;
	}
	
	/**
	 * Carga el valor de una variable de control rezagada
	 * @param nomVar
	 * @param nomPar
	 * @param entero poste o día menor que 0
	 * @param valor
	 */
	public void cargaValRez(String nomVar, String nomPar, int entero, double valor) {
		String cla = claveVRez(nomVar, nomPar, entero);
		if(existeValRez(nomVar, nomPar, entero)) {
			System.out.println("SE ESTÁ SOBREESCRIBIENDO UN VALOR REZAGADO YA CARGADO " + cla);
			System.exit(1);
		}
		valoresRezagados.put(cla, valor);
	}
	
	/**
	 * Devuelve la probabilidad de transición desde la etapaIni a la siguiente, de pasar del
	 * esceneraio escIni en etapaIni a escFin en la siguiente etapa
	 * @param etapaIni
	 * @param escIni
	 * @param escFin
	 * @return
	 */
	public double devuelveProbTrans(int etapaIni, int escIni, int escFin) {
		MatPTrans mat = matTransicion.get(etapaIni);
		return mat.getProbs()[escIni][escFin];		
	}
	

	
	/**
	 * Devuelve la probabilidad del escenario compuesto por los escenarios de las etapas del vector escenarios
	 * que puede tener un número de etapas menor al total.
	 * El primer valor es siempre 0, porque en la primera etapa solo hay un escenario
	 * @return 
	 */
	public double devuelveProbEscenario(int[] escenarios) {
		double result = 1;
		int cantEtapas = escenarios.length -1;
		for(int e=0; e<cantEtapas; e++) {
			result = result*devuelveProbTrans(e, escenarios[e], escenarios[e+1]);
		}
		return result;
	}
	
	
	/**
	 * Devuelve para cada poste la probabilidad de estar en el escenario dado por el
	 * vector escenarios, que tiene dimensión cantEtapas
	 * @param escenarios
	 * @return
	 */
	public double[] devuelveProbsPostes(int[] escenarios) {
		double prob = 1;
		int cantPos = datGen.getCantPostes();
		double[] resProb = new double[cantPos];
		int e = 0;
		for(int p=0; p<cantPos; p++) {
			if(p>datGen.getPosFinEtapa()[e]) {
				e++;
				prob = prob*devuelveProbTrans(e-1, escenarios[e-1], escenarios[e]);
			}
			resProb[p] = prob;
		}
		return resProb;
	}
	
	
	

	/**
	 * levanta un texto. OJOJOJO HABLAR CON RODRIGO SI ES UN XML O UN TEXTO GENERADO POR UN EXCEL
	 */
	public void leeGrafoEsc() {
		
	}


	public DatosGeneralesCP getDatGen() {
		return datGen;
	}



	public void setDatGen(DatosGeneralesCP datGen) {
		this.datGen = datGen;
	}



	public ArrayList<MatPTrans> getMatTransicion() {
		return matTransicion;
	}



	public void setMatTransicion(ArrayList<MatPTrans> matTransicion) {
		this.matTransicion = matTransicion;
	}


	public Hashtable<String, double[]> getValoresVA() {
		return valoresVA;
	}


	public void setValoresVA(Hashtable<String, double[]> valoresVA) {
		this.valoresVA = valoresVA;
	}


	public ArrayList<EnumeradorLexicografico> getEnumeradores() {
		return enumeradores;
	}
	

	public void setEnumeradores(ArrayList<EnumeradorLexicografico> enumeradores) {
		this.enumeradores = enumeradores;
	}



	public ArrayList<EnumeradorLexicografico> getEnumParciales() {
		return enumParciales;
	}



	public void setEnumParciales(ArrayList<EnumeradorLexicografico> enumParciales) {
		this.enumParciales = enumParciales;
	}



	public boolean isEscUnico() {
		return escUnico;
	}



	public void setEscUnico(boolean escUnico) {
		this.escUnico = escUnico;
	}



	public int getCantEscTotal() {
		return cantEscTotal;
	}



	public void setCantEscTotal(int cantEscTotal) {
		this.cantEscTotal = cantEscTotal;
	}



	public int[] getCantEscEtapas() {
		return cantEscEtapas;
	}



	public void setCantEscEtapas(int[] cantEscEtapas) {
		this.cantEscEtapas = cantEscEtapas;
	}



	public Corrida getCorrida() {
		return corrida;
	}



	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}



	public Hashtable<String, Double> getValoresRezagados() {
		return valoresRezagados;
	}



	public void setValoresRezagados(Hashtable<String, Double> valoresRezagados) {
		this.valoresRezagados = valoresRezagados;
	}



	
	
}
