package cp_buscadorHiperplanos;

import java.util.ArrayList;
import java.util.Hashtable;

import cp_datatypesEntradas.DatosGeneralesCP;
import datatypesResOptim.DatosHiperplano;
import datatypesResOptim.DatosTablaHiperplanos;
import datatypesResOptim.DatosTablaVByValRec;
import estado.Discretizacion;
import estado.VariableEstadoPar;
import futuro.ClaveDiscreta;
import futuro.Hiperplano;
import futuro.InformacionValorPunto;
import futuro.TablaHiperplanosMemoria;
import futuro.TablaVByValRecursosMemoria;
import optimizacion.ResOptim;
import optimizacion.ResOptimHiperplanos;
import optimizacion.ResOptimIncrementos;
import parque.Corrida;
import utilitarios.Constantes;
import utilitarios.EnumeradorLexicografico;
import utilitarios.LeerDatosArchivo;

public class LectorTablaResOptim {
	
	private Corrida corrida;
	private TablaVByValRecursosMemoria tablaVBInc;
	private TablaHiperplanosMemoria tablaVBH;
	private String tipoVBellman;
	private boolean usaHiperplanos;
	private DatosGeneralesCP dGCP;
	private String ruta;
	
	// Se usará como enumerador de los estados de las VE de los hiperplanos
	private EnumeradorLexicografico enumVEHip;
	/**
	 *  VARIABLES PARA LA SOLUCIÓN PROVISORIA 
	 */
	
	// VARIABLES QUE DESCRIBEN EL ARCHIVO DE LECTURA PARA UN PASO

	// directorio de salidas OPT de la corrida que produce los VB y de los recursos
	private String dirProvisorio = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\CORRIDAS DIARIAS MOP\\Potencia_Firme_V\\2023-10-31-12-38-25-OPT";
	
	private String nombreVBellman = "valorVB";
	private String[] nombresVEHiperplanos = {"volumen_bonete", "volumen_palmar", "volumen_Salto"};
	private String[] nombresHidAportantesVEH = {"bonete", "palmar", "salto"};
	private Hashtable<String, VariableEstadoPar> varsEstadoH;
	private int cantVEHip = 3;
	private int[] ordinalesVEHiperplanos = {1, 2, 3};	

	private int cantVETotales = 4;
	private int[] cantEstadosTodasVE = {5, 10, 5, 5};
	private int paso = 15;
	
	/**
	 * Tabla que contiene todos los hiperplanos para los valores elegidos de
	 * las variables prefijadas
	 * clave: la ClaveDiscreta total generada con los ordinales en la discretización 
	 * de las VE de los hiperplanos
	 */
	private Hashtable<ClaveDiscreta, Hiperplano> hiperplanos;
	
	/**
	 * CRITERIO PARA SELECCIONAR HIPERPLANOS
	 * Se retiene 1 de cada factoresRaleo valores de discretización, empezando del ordinal 0
	 * Hay un factor para cada VE de los hiperplanos 
	 */
	private static final int[] factoresRaleo = {5, 3, 3};
	
	/**
	 * FIN VARIABLES PARA LA SOLUCIÓN PROVISORIA
	 */
	
	public LectorTablaResOptim(Corrida corrida, String ruta, String tipoVBellman, DatosGeneralesCP dGCP) {
		this.corrida = corrida;
		this.ruta = ruta;
		this.tipoVBellman = tipoVBellman;
		this.dGCP = dGCP;
		this.usaHiperplanos = false;
		if(tipoVBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) usaHiperplanos = true;
		varsEstadoH = new Hashtable<String, VariableEstadoPar>();
		for(String nh: corrida.getHidraulicos().keySet()) {
			varsEstadoH.put(nh, corrida.getHidraulicos().get(nh).getVolumen());
		}
	}
	
	
	
	
	
	
	public void traeResOptim(String ruta, String tipoVBellman) {
		String nombreArch;
		if (tipoVBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)){
			nombreArch = ruta + "/TablaVByValRecursos";
			Object tvb = utilitarios.ManejaObjetosEnDisco.traerDeDisco(nombreArch);
			tablaVBInc = new TablaVByValRecursosMemoria((DatosTablaVByValRec) tvb);
		} else if (tipoVBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			nombreArch = ruta + "/TablaHiperplanos";
			Object th = utilitarios.ManejaObjetosEnDisco.traerDeDisco(nombreArch);
			tablaVBH = new TablaHiperplanosMemoria((DatosTablaHiperplanos) th);
		}
	}
	

		
	
	
	
	/**
	 * LECTOR PROVISORIO DE HIPERPLANOS
	 * @param ordinalesVEPrefijadas
	 * @param valoresVEPrefijadas
	 * @param nombresVEhiperplanos
	 * @param ordinalesVEhiperplanos
	 * @param paso
	 * @param archValores
	 * @return result tabla con
	 * clave: ClaveDiscreta de los enteros de las VE de los hiperplanos
	 * valor: el hiperplano con esa clave asociada y las VE prefijadas.
	 */
	public Hashtable<ClaveDiscreta, Hiperplano> devuelveHiperplanosVEDEPaso(){
		Hashtable<ClaveDiscreta, Hiperplano> result = new Hashtable<ClaveDiscreta, Hiperplano>();
		Hashtable<String, Hashtable<ClaveDiscreta, Double>> tablasValoresRecursos = new Hashtable<String, Hashtable<ClaveDiscreta, Double>>();
		// lee valores de Bellman para todos los estados de las variables de los hiperplanos
		// dejando fijos los valores de las VE prefijadas
		String nomArch = dirProvisorio + "/ValoresDeBellmanFinT.xlt";
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(nomArch);
		Hashtable<ClaveDiscreta, Double> tablaVB = devuelveValores(texto, paso);
		
		// lee valores de los recursos para todos los estados de las variables de los hiperplanos
		// dejando fijos los valores de las VE prefijadas
		for(String nomVEH: nombresVEHiperplanos) {
			nomArch = dirProvisorio + "ValRecursosVE-" + nomVEH + ".xlt";
			texto = LeerDatosArchivo.getDatos(nomArch);
			tablasValoresRecursos.put(nomVEH, devuelveValores(texto, paso));		
		}	
		int[] cotasInf = new int[cantVEHip];
		int[] cotasSup = new int[cantVEHip];
		for(int i=0; i<cantVEHip; i++) {
			cotasSup[i] = cantEstadosTodasVE[ordinalesVEHiperplanos[i]] - 1;
		}
		enumVEHip = new EnumeradorLexicografico(cantVEHip, cotasInf, cotasSup);
		enumVEHip.inicializaEnum();
		int[] vectorTotal = new int[cantVETotales];
		for(int ivp=0; ivp<dGCP.getCantVEPrefijadas(); ivp++){
			vectorTotal[dGCP.getOrdinalVEPrefijadas()[ivp]] = dGCP.getValorVEPrefijadas()[ivp].intValue();
		}
		int[] vecVEH = enumVEHip.devuelveVector();
		int numIdHip = 0;
		while(vecVEH!=null) {
			for(int j=0; j<cantVEHip; j++) {
				vectorTotal[ordinalesVEHiperplanos[j]] = vecVEH[j];
			}
			ClaveDiscreta clave = new ClaveDiscreta(vectorTotal);
			double vBellman = tablaVB.get(clave);
			double[] coefs = new double[cantVEHip];
			double[] punto = new double[cantVEHip];
			int ip=0; // recorre los participantes hidráulicos que aportan VE de los hiperplanos
			long instanteFinCP = dGCP.getInstIniCP() + dGCP.getCantDias()*Constantes.SEGUNDOSXDIA;
			for(String nvh: nombresVEHiperplanos) {
				String nomPart = nombresHidAportantesVEH[ip];
				Discretizacion dis = varsEstadoH.get(nomPart).getEvolDiscretizacion().getValor(instanteFinCP);
				punto[ip] = dis.devuelveValorOrdinal(vectorTotal[ordinalesVEHiperplanos[ip]]);
				coefs[ip] = tablasValoresRecursos.get(nvh).get(clave);
				ip++;
			}
			DatosHiperplano dh = new DatosHiperplano(numIdHip, paso, 0, punto, vBellman, coefs);
			Hiperplano h = new Hiperplano(dh);
			numIdHip++;
			ClaveDiscreta claveH = new ClaveDiscreta(vecVEH);
			result.put(claveH, h);
			vecVEH = enumVEHip.devuelveVector();
		}		
		return result;	
	}
	
	
	
	
	
	/**
	 * Devuelve los valores de Bellman o de un recurso para un paso dado, contenidos en el texto
	 * leído de un archivo de salida de la optimización ValoresDeBellmanFinT.xlt o 
	 * ValRecursosVE-.....xlt
	 * @param texto
	 * @param paso
	 * @return
	 */
	public Hashtable<ClaveDiscreta, Double> devuelveValores(ArrayList<ArrayList<String>> texto, int paso){
		Hashtable<ClaveDiscreta, Double> result = new Hashtable<ClaveDiscreta, Double>();
		int fila = 1 + cantVETotales;
		while (Integer.parseInt(texto.get(fila).get(0)) > paso){
			fila++;
		}
		int[] cotasInf = new int[cantVETotales];
		int[] cotasSup = new int[cantVETotales];
		for(int i=0; i<cantVETotales; i++) {
			cotasSup[i] = cantEstadosTodasVE[i]-1;
		}
		EnumeradorLexicografico enumEst = new EnumeradorLexicografico(cantVETotales, cotasInf, cotasSup);
		enumEst.inicializaEnum();
		int[] vec = enumEst.devuelveVector();
		int j=2;
		while(vec != null) {
			ClaveDiscreta clave = new ClaveDiscreta(vec);
			result.put(clave, Double.parseDouble(texto.get(fila).get(j)));
			j++;
			vec = enumEst.devuelveVector();
		}
		return result;
	}
	
	/**
	 * Devuelve en el atributo hiperplanos los hiperplanos de una subgrilla de estados formada por los extremos 
	 * de la discretización de las VE de los hiperplanos y una fracción fraccionDiscretizaciones
	 * de los pasos de discretización que no son los extremos
	 *  
	 * @param hiperTotales todos los hiperplanos dadas las VE prefijadas
	 * clave: la ClaveDiscreta tomando las VE de los hiperplanos
	 * valor: el hiperplano asociado a la clave.
	 */
	public void devuelveHiperplanosRaleados(Hashtable<ClaveDiscreta, Hiperplano> hiperTotales) {
		enumVEHip.inicializaEnum();
		int[] vecVEH = enumVEHip.devuelveVector();
		while(vecVEH!=null) {
			boolean retiene = true;
			for(int i=0; i<cantVEHip; i++) {
				if(vecVEH[i]!=0 && vecVEH[i]!=enumVEHip.getCotasSuperiores()[i]
						&&  vecVEH[i]%factoresRaleo[i] != 0) retiene = false;
			}		
			if(retiene) {
				ClaveDiscreta claveH = new ClaveDiscreta(vecVEH);
				Hiperplano h = hiperTotales.get(claveH);
				hiperplanos.put(claveH, h);
			}
			vecVEH = enumVEHip.devuelveVector();
		}	
	}	
}

	
	
	
	
	
	
	
