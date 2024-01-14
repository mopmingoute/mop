package cp_salidas;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_despacho.GrafoEscenarios;
import utilitarios.EnumeradorLexicografico;
import utilitarios.ParReales;
import utilitarios.UtilArrays;

public class ResultadosDePE {
	
	
	/**
	 * Clave nombre variable + "_" +  nombre participante  + "_" + identificador de escenario e1_e2_...._eE
	 * obtenible con el método claveResultadosDePE
	 * el identificador de escenario debe abarcar todo el horizonte, es decir tener cantEtapas valores
	 * 
	 * Valor: Los resultados del escenario, para cada poste o día según la variable.
	 */
	private static Hashtable<String, ResultadoEsc1V> resultados;
	
	/**
	 * Tipos de resultado
	 * Clave nombre variable + "_" +  nombre participante, obtenible con el método BaseVar.generaNomVarPar
	 * Valor: el participante, variable o rango asociado a la pareja (nombre de variable, nombre del participante)
	 */
	private static Hashtable<String, String> parDeVarPar;  // nombre del participante
	private static Hashtable<String, String> varDeVarPar; // nombre de la variable
	private static Hashtable<String, String> rangoDeVarPar; // POSTE, DIA O UNICO
	
	/**
	 * Clave: nombre del participante
	 * Valor: lista de nombres de variable asociadas al participante
	 */
	private static Hashtable<String, ArrayList<String>> variablesDePar;  
	
	
	private static GrafoEscenarios ge;

	
	public ResultadosDePE(GrafoEscenarios ge2) {
		resultados = new Hashtable<String, ResultadoEsc1V>();
		parDeVarPar = new Hashtable<String, String>();
		varDeVarPar = new Hashtable<String, String>();
		rangoDeVarPar = new Hashtable<String, String>();
		variablesDePar = new Hashtable<String, ArrayList<String>>();  
		ge = ge2;
		
	}
	
	public static String claveResultadosDePE(String nomvar, String nompar, int[] esc) {
		String result = nomvar + "_" + nompar + "_";
		for(int i=0; i<esc.length; i++) {
			result = result + esc[i];
			if(i<esc.length-1) result = result + "_";
		}
		return result;
	}
	
	
	
	
	/**
	 * Carga los valores de una variable de un participante en un escenario, en la estructura ResultadosDePE resultadosPE
	 * @param nomvar
	 * @param nompar
	 * @param esc
	 * @param rango  puede ser "DIA" si hay un valor por día del escenario, "POSTE" si hay un valor por poste 
	 * o "UNICO" si hay un único valor para el escenario, según constantes en ConCP
	 * @param valores
	 * @return
	 */
	public static void cargaRes1V1Esc(String nomvar, String nompar, int[] esc, String rango, Double[] valores) {
		boolean esposte = false;
		boolean unico = false;
		if(rango.equalsIgnoreCase(ConCP.POSTE)) {
			esposte = true;
		}else if(rango.equalsIgnoreCase(ConCP.UNICO)){
			unico = true;
		}
		if(esposte && valores.length!=ge.getDatGen().getCantPostes()) {
			System.out.println("Error en cantidad de datos a cargar de " + nomvar + "_" + nompar);
			System.exit(1);
		}
		if((!esposte & !unico) && valores.length!=ge.getDatGen().getCantDias()) {
			System.out.println("Error en cantidad de datos a cargar de " + nomvar + "_" + nompar);
			System.exit(1);
		}
		if(unico && valores.length!= 1) {
			System.out.println("Error en cantidad de datos a cargar de " + nomvar + "_" + nompar);
			System.exit(1);			
		}
		ResultadoEsc1V res = new ResultadoEsc1V(nomvar, nompar, esc, esposte, ge, valores);
		String claAux = BaseVar.generaNomVarPar(nomvar, nompar);
		if(BaseVar.getCatalogoVarParPos().get(claAux) == null) BaseVar.getCatalogoVarParPos().put(claAux, esposte);
		String clave = claveResultadosDePE(nomvar, nompar, esc);
		if(!resultados.contains(clave)) {
			resultados.put(clave, res);
		}else {
			System.out.println("Error en cargaRes1V1Esc, ya existía la clave");
			System.exit(1);
		}
		String vp = BaseVar.generaNomVarPar(nomvar, nompar);
		if(parDeVarPar.get(vp) == null) parDeVarPar.put(vp, nompar);
		if(varDeVarPar.get(vp) == null) varDeVarPar.put(vp, nomvar);
		if(rangoDeVarPar.get(vp) == null) rangoDeVarPar.put(vp, rango);
		if(variablesDePar.get(nompar)== null) variablesDePar.put(nompar, new ArrayList<String>());
		if(!variablesDePar.get(nompar).contains(nomvar)) variablesDePar.get(nompar).add(nomvar);
	
	}
	
	
	/**
	 * Devuelve los valores de una variable de un participante en un escenario o null
	 * @param nomvar
	 * @param nompar
	 * @return
	 */
	public static Double[] devuelveRes1V1Esc(String nomvar, String nompar, int[] esc) {
		String clave = claveResultadosDePE(nomvar, nompar, esc);
		Double[] resD = resultados.get(clave).getValores();
		if(resD == null) {
			return null;
		}else {
			Double[] res = new Double[resD.length];
			for(int i=0; i<resD.length; i++) {
				if(resD[i]!=null) res[i] = resD[i];
			}
			return res;
		}
	}
	
	

	/**
	 * Devuelve un vector con los valores esperados en los escenarios, de una variable de un participante
	 * en todos los postes o días.
	 * @param nomvar
	 * @param nompar
	 * @return
	 */
	public static double[] devuelveValorEsperado(String nomvar, String nompar) {
		System.out.println(nomvar + "-" + nompar);
		if(nomvar.equalsIgnoreCase(ConCP.VALAGUA) && nompar.equalsIgnoreCase("bonete")) {
			int pp = 0;
		}
		boolean poste = true;
		String cl = BaseVar.generaNomVarPar(nomvar, nompar);
		int cant = ge.getDatGen().getCantPostes();
		if (!rangoDeVarPar.get(cl).equalsIgnoreCase(ConCP.POSTE)) {
			poste = false;
			cant = ge.getDatGen().getCantDias();
		}
		int cantEtapas = ge.getDatGen().getCantEtapas();
		EnumeradorLexicografico enEsc = ge.getEnumeradores().get(cantEtapas-1);
		enEsc.inicializaEnum();
		int[] esc = enEsc.devuelveVector();
		double[] valEsp = new double[cant];
		while(esc != null) {
			double prob = ge.devuelveProbEscenario(esc);
			Double[] res1esc = devuelveRes1V1Esc(nomvar, nompar, esc);
			for(int i=0; i<cant; i++) {
				if(res1esc[i]!=null) {
					valEsp[i] = valEsp[i] + res1esc[i]*prob;
				}
			}
			esc = enEsc.devuelveVector();
		}
		return valEsp;		
	}
	
	
	/**
	 * Devuelve una matriz r con los valores esperados en los escenarios, de una variable de un participante
	 * en todos los postes o días.
	 * @param nomvar
	 * @param nompar
	 * @return double[][] result : primer índice percentil, segundo índice poste o día.
	 */
	public static double[][] devuelvePercentiles(String nomvar, String nompar, ArrayList<Double> percentiles) {
		boolean poste = true;
		int npercent = percentiles.size();
		int cantP = ge.getDatGen().getCantPostes();
		String cl = BaseVar.generaNomVarPar(nomvar, nompar);
		if (!rangoDeVarPar.get(cl).equalsIgnoreCase(ConCP.POSTE)) {
			poste = false;
			cantP = ge.getDatGen().getCantDias();
		}
		int cantEtapas = ge.getDatGen().getCantEtapas();
		EnumeradorLexicografico enEsc = ge.getEnumeradores().get(cantEtapas-1);
		enEsc.inicializaEnum();
		int cantesc = enEsc.getCantTotalVectores();
		// ATENCIÓN, EN valores:  primer índice poste o día, segundo día escenario
		ArrayList<ArrayList<Double>> valores = new ArrayList<ArrayList<Double>>(); 
		for(int ip = 0; ip<cantP; ip++) {
			valores.add(new ArrayList<Double>());
		}
		int[] esc = enEsc.devuelveVector();
		ArrayList<Double> probEsc = new ArrayList<Double>();		
		while(esc != null) {
			probEsc.add(ge.devuelveProbEscenario(esc));
			Double[] res1esc = devuelveRes1V1Esc(nomvar, nompar, esc);
			for(int ip=0; ip<cantP; ip++) {
				valores.get(ip).add(res1esc[ip]);
			}
			esc = enEsc.devuelveVector();
		}
		
		double[][] result = new double[npercent][cantP];
		for(int ip=0; ip<cantP; ip++) {
			boolean sigue = true;
			for(int iesc = 0; iesc<cantesc; iesc++) {
				if(valores.get(ip).get(iesc)==null) sigue = false;
			}
			if(sigue) {
				ArrayList<ParReales> listaPar = new ArrayList<ParReales>();
				for(int iesc = 0; iesc<cantesc; iesc++) {
					ParReales pr = new ParReales(valores.get(ip).get(iesc), probEsc.get(iesc));
					listaPar.add(pr);
				}
				ParReales.ordenaListaPorReal1(listaPar);
				int ipercent = 0;
				for(Double p1: percentiles) {
					double sumaprob = 0.0;
					int iesc = 0;
					while(sumaprob<p1) {
						sumaprob += listaPar.get(iesc).getReal2();
						iesc++;
					}
					result[ipercent][ip] = listaPar.get(iesc-1).getReal1();
					ipercent++;
				}
			}
		}
		return result;		
	}

	public static Hashtable<String, ResultadoEsc1V> getResultados() {
		return resultados;
	}

	public static void setResultados(Hashtable<String, ResultadoEsc1V> resultados) {
		ResultadosDePE.resultados = resultados;
	}

	public static GrafoEscenarios getGe() {
		return ge;
	}

	public static void setGe(GrafoEscenarios ge) {
		ResultadosDePE.ge = ge;
	}


	
	
	public static Hashtable<String, String> getParDeVarPar() {
		return parDeVarPar;
	}

	public static void setParDeVarPar(Hashtable<String, String> parDeVarPar) {
		ResultadosDePE.parDeVarPar = parDeVarPar;
	}

	public static Hashtable<String, String> getVarDeVarPar() {
		return varDeVarPar;
	}

	public static void setVarDeVarPar(Hashtable<String, String> varDeVarPar) {
		ResultadosDePE.varDeVarPar = varDeVarPar;
	}

	public static Hashtable<String, String> getRangoDeVarPar() {
		return rangoDeVarPar;
	}

	public static void setRangoDeVarPar(Hashtable<String, String> rangoDeVarPar) {
		ResultadosDePE.rangoDeVarPar = rangoDeVarPar;
	}

	public static Hashtable<String, ArrayList<String>> getVariablesDePar() {
		return variablesDePar;
	}

	public static void setVariablesDePar(Hashtable<String, ArrayList<String>> variablesDePar) {
		ResultadosDePE.variablesDePar = variablesDePar;
	}


	

	
	

	
	
	
	

}
