package cp_construccionGrafoEsc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import cp_datatypesEntradas.DatosGeneralesCP;
import cp_despacho.ConCP;
import cp_despacho.GrafoEscenarios;
import utilClusters.Observable;
import utilitarios.Par;
import utilitarios.UtilArrays;

public class ObsEnsamble implements Observable{
	
	private int cantPos;
	
	/**
	 * Según el valor de este atributo la distancia entre observaciones se 
	 * calcula de manera diferente.
	 * Puede tomar lso valores DISTORIG o DISTX
	 */
	private static String tipoDeDistancia;
	
	/**
	 * La distancia euclídea en seriesT ponderando las series
	 */
	private static final String DISTRANS = "DISTRANS"; 
	
	// pesos de cada serie transformada en la distancia DISTRANS
	private static double[] pesosTrans; 
	
	/**
	 * La distancia en el vector X definida en el método this.distX
	 */
	private static final String DISTX = "DISTX";  
	
	private int numEsc;  // número de escenario en el ensamble
	
	/**
	* clave: nombre de la serie original
	* valor: tabla que para cada serie original da los valores por poste 
	*/
	private Hashtable<String, double[]> seriesO;

	/**
	* clave: nombre de la serie transformada
	* valor: tabla que para cada serie transformada da los valores por poste 
	*/
	private Hashtable<String, double[]> seriesT;
	
	/**
	 * vector X con el que se calculan las distancias entre observaciones 
	 */
	private double[] serieX;
	
	private static DatosGeneralesCP datGen;
	private static int cantSeriesO;
	private static int cantSeriesT;
	
	private static String[] nombresSeriesO;  // nombres de las series originales de datos
	private static String[] nombresSeriesT; // idem de series transformadas
	
	/**
	 * Matriz de distancias del conjunto de las ObsEnsamble
	 * clave: par de enteros con los números de observaciones ATENCIÓN LA MATRIZ ES SIMÉTRICA
	 * valor: la distancia dist1, dist2 y dist3 en los vectores X, entre las observaciones del par
	 */
	private static Hashtable<Par, Double> distancias1; 
	private static Hashtable<Par, Double> distancias2; 
	private static Hashtable<Par, Double> distancias3; 
	
	// promedios en la población de las distancias 1, 2 y 3 en el vector X
	private static double dist1Med;
	private static double dist2Med;
	private static double dist3Med;
	
	/**
	 * Cantidad de posiciones adicionales en serieX respecto a la cantidad de postes
	 *  de las observaciones originales y transformadas
	 */
	private static final int POS_ADIC_EN_X = 3; 	
	/**
	* clave: nombre de la serie transformada
	* valor: coeficiente de la serie original, en el orden determinado por nombresSeriesO
	*/
	private static Hashtable<String, double[]>  alfasTrans;
	
	private static GrafoEscenarios ge;
	
	
	
	
	/**
	 * Carga las variables estáticas de la clase
	 */
	public static void cargaVarEstaticas(DatosGeneralesCP datGen, int cantSeriesO, int cantSeriesT, String[] nombresSeriesO, String[] nombresSeriesT,
			Hashtable<String, double[]>  alfasTrans, GrafoEscenarios ge) {
		ObsEnsamble.datGen = datGen;
		ObsEnsamble.cantSeriesO = cantSeriesO;
		ObsEnsamble.cantSeriesT = cantSeriesT;
		ObsEnsamble.nombresSeriesO = nombresSeriesO;
		ObsEnsamble.nombresSeriesT = nombresSeriesT;
		ObsEnsamble.alfasTrans = alfasTrans;
		ObsEnsamble.ge = ge;
//		ObsEnsamble.pesosOrig = pesosOrig;
	}
	
	/**
	 * Crea una ObsEnsamble con el numero de escenario numEsc
	 * El constructor crea las estructuras del objeto y las carga todas con ceros
	 */
	public ObsEnsamble(int numEsc, int cantPosA) {
		this.cantPos = cantPosA;
		double[] aux;
		Hashtable<String, double[]> auxO = new Hashtable<String, double[]>();
		for(String s: nombresSeriesO) {
			aux = new double[cantPos];
			auxO.put(s, aux);
		}		
		Hashtable<String, double[]> auxT = new Hashtable<String, double[]>();
		for(String s: nombresSeriesT) {
			aux = new double[cantPos];	
			auxT.put(s, aux);
		}	
		double[] auxX = new double[cantPos]; 
		this.numEsc = numEsc;
		this.seriesO = auxO;
		this.seriesT = auxT;
		this.serieX = auxX;		
		
	}
	
	
	
	/**
	 * Crea una ObsEnsamble con el numero de escenario numEsc
	 * @param numEsc
	 * @param seriesO
	 * @param seriesT
	 * @param serieX
	 */
	public ObsEnsamble(int numEsc, int cantPosA, Hashtable<String, double[]> seriesO, Hashtable<String, double[]> seriesT, double[] serieX) {
		super();
		cantPos = cantPosA;
		this.numEsc = numEsc;
		this.seriesO = seriesO;
		this.seriesT = seriesT;
		this.serieX = serieX;
	}




	/**
	 * Construye una nueva ObsEnsamble a partir de this, reducida a los postes entre posteIni y posteFin inclusive
	 * Solo se cargan seriesO y seriesT con los valores de this reducidos a la etapa
	 * serieX se carga con ceros
	 * 
	 * @param posteIni
	 * @param posteFin
	 * @return
	 */
	public ObsEnsamble reduceAEtapa(int posteIni, int posteFin) {
		int cantPosE = posteFin - posteIni + 1;
		double[] aux;
		Hashtable<String, double[]> auxO = new Hashtable<String, double[]>();
		for(String s: nombresSeriesO) {
			aux = new double[cantPosE];
			for(int p=posteIni; p<=posteFin; p++) {
				aux[p-posteIni] = seriesO.get(s)[p];					
				auxO.put(s, aux);
			}
		}		
		Hashtable<String, double[]> auxT = new Hashtable<String, double[]>();
		for(String s: nombresSeriesT) {
			aux = new double[cantPosE];
			for(int p=posteIni; p<=posteFin; p++) {
				aux[p-posteIni] = seriesT.get(s)[p];					
				auxT.put(s, aux);
			}
			auxT.put(s, aux);
		}				
		double[] auxX = new double[cantPosE + POS_ADIC_EN_X];
		ObsEnsamble obs = new ObsEnsamble(numEsc, cantPosE, auxO, auxT, auxX);
		return obs;
	}
	
	
	
	/**
	 * Construye una nueva ObsEnsamble a partir de this, reducida a los postes de la etapa
	 * serieX se carga con ceros
	 * 
	 * @param posteIni
	 * @param posteFin
	 * @return
	 */
	public ObsEnsamble reduceAEtapa(int etapa) {
		int posteIni = datGen.getPosIniEtapa()[etapa];
		int posteFin = datGen.getPosFinEtapa()[etapa];
		return reduceAEtapa(posteIni, posteFin);
	}
	
	
	
	/**
	 * En una ObsEnsamble carga las seriesT a partir de las seriesO
	 * Se supone que ya están creadas las seriesO
	 * @return
	 */
	public void cargaSeriesT() {
		int cantP = seriesO.get(nombresSeriesO[0]).length;
		for(String nt: nombresSeriesT) {
			double[] coefs = alfasTrans.get(nt);
			for(int p=0; p<cantP; p++) {
				double sum = 0.0;
				for(int io=0; io<cantSeriesO; io++) {
					String no = nombresSeriesO[io];
					sum += coefs[io]*seriesO.get(no)[p];
				}
				seriesT.get(nt)[p] = sum;
			}			
		}		
	}
	
	
	
	/**
	 * En una ObsEnsamble carga la serieX a partir de las seriesT
	 * Se supone que ya están creadas las seriesT, reducidas a los valores de la etapa.
	 * 
	 * A partir de la matriz T [cantSeriesT x cantidad de postes] de una observación transformada en una etapa se obtiene el vector x[1×(posFin-posIni+4)], un double[posFin-posIni+4] de una observación, que se forma así:
	 * x[0] tiene el promedio de POT-DISP-HORARIA en la etapa 
	 * x[1] … x[posFin-posIni+1] tienen la monótona creciente de DEMANDA-RESIDUAL en los postes de la etapa
	 * x[posFin-posIni+2] y x[posFin-posIni+3]  son los valores inicial y final de la DEMANDA-RESIDUAL en la etapa.
	 * @return
	 */
	public void cargaSerieX() {
		int cantPos = seriesT.get(nombresSeriesT[0]).length;
		if(serieX==null) serieX = new double[cantPos + POS_ADIC_EN_X];
		double[] potDispHor = seriesT.get(ConCP.POT_DISP_HORARIA);
		serieX[0] = UtilArrays.promedio(potDispHor);
		double[] demRes = seriesT.get(ConCP.DEMANDA_RESIDUAL).clone();
		ArrayList<Double> demResAL = UtilArrays.dameAListDDeArray(demRes);
		Collections.sort(demResAL);
		for(int p=1; p<=cantPos; p++) {
			serieX[p] = demResAL.get(p-1);
		}
		serieX[cantPos+1] = seriesT.get(ConCP.DEMANDA_RESIDUAL)[0];
		serieX[cantPos+2] = seriesT.get(ConCP.DEMANDA_RESIDUAL)[cantPos-1];
	}
	
	
	public static double distX1(ObsEnsamble o1, ObsEnsamble o2) {
		return Math.abs(o1.serieX[0] - o2.serieX[0]);
	}
	
	public static double distX2(ObsEnsamble o1, ObsEnsamble o2) {
		int cantPos = o1.getSeriesT().get(nombresSeriesT[0]).length;
		double result = 0.0;
		for(int p=1; p<= cantPos; p++) {
			double r1 = Math.abs(o1.serieX[p] - o2.serieX[p]);
			if(r1 >result) result = r1;
		}
		return result;
	}
	
	public static double distX3(ObsEnsamble o1, ObsEnsamble o2) {	
		int cantPos = o1.getSeriesT().get(nombresSeriesT[0]).length;
		return Math.abs(o1.serieX[cantPos+1] - o2.serieX[cantPos+1]) + Math.abs(o1.serieX[cantPos+2] - o2.serieX[cantPos+2]);
	}
	
	/**
	 * Requiere que ya estén cargadas las observaciones de las series originales
	 * @param observaciones
	 *  clave: número de observación empezando en 0
	 */
	public static void inicializaParaDistX(Hashtable<Integer, ObsEnsamble> observaciones) {
		for(ObsEnsamble ob: observaciones.values()) {
			ob.cargaSeriesT();
			ob.cargaSerieX();
		}
//		for(ObsEnsamble o1: observaciones.values()) {
//			for(ObsEnsamble o2: observaciones.values()) {
//				Par par12 = new Par(o1.getNumEsc(), o2.getNumEsc());
//				Par par21 = new Par(o2.getNumEsc(), o2.getNumEsc());
//				distancias1.put(par12, distX1(o1, o2));
//				distancias1.put(par21, distX1(o1, o2));
//				distancias2.put(par12, distX2(o1, o2));
//				distancias2.put(par21, distX2(o1, o2));
//				distancias3.put(par12, distX3(o1, o2));
//				distancias3.put(par21, distX3(o1, o2));
//			}
//		}
		dist1Med = 0;
		dist2Med = 0;
		dist3Med = 0;
		for(ObsEnsamble o1: observaciones.values()) {
			for(ObsEnsamble o2: observaciones.values()) {
				dist1Med += distX1(o1, o2)/2;
				dist2Med += distX2(o1, o2)/2;
				dist3Med += distX3(o1, o2)/2;
			}
		}	
	}
	
	
	/**
	 * Calcula la distancia distX
	 */
	public double distX(Observable o1, Observable o2) {
		ObsEnsamble oe1 = (ObsEnsamble)o1;
		ObsEnsamble oe2 = (ObsEnsamble)o2;
		double result = distX1(oe1, oe2)/dist1Med;
		result += distX2(oe1, oe2)/dist2Med;
		result += distX3(oe1, oe2)/dist3Med;
		return result;		
	}

	/**
	 * Calcula la distancia DISTRANS
	 */
	public double distTrans(Observable o1, Observable o2) {
		ObsEnsamble oe1 = (ObsEnsamble)o1;
		ObsEnsamble oe2 = (ObsEnsamble)o2;
		ArrayList<double[]> al1 = new ArrayList<double[]>();
		ArrayList<double[]> al2 = new ArrayList<double[]>();
		int is = 0;
		for(String s: nombresSeriesT) {
			double[] aux1 = UtilArrays.prodNumero(oe1.getSeriesT().get(s), pesosTrans[is]);
			double[] aux2 = UtilArrays.prodNumero(oe2.getSeriesT().get(s), pesosTrans[is]);
			al1.add(aux1);
			al2.add(aux2);
			is++;
		}
		double[] v1 = UtilArrays.yuxtad(al1);
		double[] v2 = UtilArrays.yuxtad(al2);
		double result = UtilArrays.distEuclid(v1, v2);
		return result;
	}
	

	
	
	@Override
	public double distancia(Observable otra) {
		if(tipoDeDistancia.equalsIgnoreCase(DISTRANS)) {
			return distTrans(this, otra);
		}else {
			return distX(this, otra);
		}
	}
	
	

	@Override
	public Observable baricentro(ArrayList<Observable> alObs) {
		int cantPos = ((ObsEnsamble)alObs.get(0)).getCantPos();
		int cantObs = alObs.size();
		double inv = 1/cantObs;
		ObsEnsamble bar = new ObsEnsamble(9999, cantPos);
		for(Observable ob: alObs) {
			ObsEnsamble oe = (ObsEnsamble)ob;
			for(String ns: nombresSeriesO) {
				double[] aux = bar.getSeriesO().get(ns);
				bar.getSeriesO().remove(ns);
				bar.getSeriesO().put(ns, UtilArrays.suma2Arrays(aux, UtilArrays.prodNumero(oe.getSeriesO().get(ns), inv)));
			}
		}
		return bar;
	}

	@Override
	public int indiceCentroMasCercano(ArrayList<Observable> centros) {
		double minDist = Double.MAX_VALUE;
		int indMin = 0;
		int i = 0;
		for(Observable c: centros) {
			ObsEnsamble ce = (ObsEnsamble)c;
			if(this.distancia(ce)<minDist) indMin = i;
			i++;
		}
		return i;
	}

	public static String getTipoDeDistancia() {
		return tipoDeDistancia;
	}

	public static void setTipoDeDistancia(String tipoDeDistancia) {
		ObsEnsamble.tipoDeDistancia = tipoDeDistancia;
	}

	public int getNumEsc() {
		return numEsc;
	}

	public void setNumEsc(int numEsc) {
		this.numEsc = numEsc;
	}

	public Hashtable<String, double[]> getSeriesO() {
		return seriesO;
	}

	public void setSeriesO(Hashtable<String, double[]> seriesO) {
		this.seriesO = seriesO;
	}

	public Hashtable<String, double[]> getSeriesT() {
		return seriesT;
	}

	public void setSeriesT(Hashtable<String, double[]> seriesT) {
		this.seriesT = seriesT;
	}

	public double[] getSerieX() {
		return serieX;
	}

	public void setSerieX(double[] serieX) {
		this.serieX = serieX;
	}

	public static DatosGeneralesCP getDatGen() {
		return datGen;
	}

	public static void setDatGen(DatosGeneralesCP datGen) {
		ObsEnsamble.datGen = datGen;
	}

	public static int getCantSeriesO() {
		return cantSeriesO;
	}

	public static void setCantSeriesO(int cantSeriesO) {
		ObsEnsamble.cantSeriesO = cantSeriesO;
	}

	public static int getCantSeriesT() {
		return cantSeriesT;
	}

	public static void setCantSeriesT(int cantSeriesT) {
		ObsEnsamble.cantSeriesT = cantSeriesT;
	}

	public static String[] getNombresSeriesO() {
		return nombresSeriesO;
	}

	public static void setNombresSeriesO(String[] nombresSeriesO) {
		ObsEnsamble.nombresSeriesO = nombresSeriesO;
	}

	public static String[] getNombresSeriesT() {
		return nombresSeriesT;
	}

	public static void setNombresSeriesT(String[] nombresSeriesT) {
		ObsEnsamble.nombresSeriesT = nombresSeriesT;
	}

	public static Hashtable<String, double[]> getAlfasTrans() {
		return alfasTrans;
	}

	public static void setAlfasTrans(Hashtable<String, double[]> alfasTrans) {
		ObsEnsamble.alfasTrans = alfasTrans;
	}

	public static GrafoEscenarios getGe() {
		return ge;
	}

	public static void setGe(GrafoEscenarios ge) {
		ObsEnsamble.ge = ge;
	}



	public static double[] getPesosTrans() {
		return pesosTrans;
	}

	public static void setPesosTrans(double[] pesosTrans) {
		ObsEnsamble.pesosTrans = pesosTrans;
	}

	public static String getDistrans() {
		return DISTRANS;
	}

	public static String getDistx() {
		return DISTX;
	}

	public static int getPosAdicEnX() {
		return POS_ADIC_EN_X;
	}

	public int getCantPos() {
		return cantPos;
	}

	public void setCantPos(int cantPos) {
		this.cantPos = cantPos;
	}

	public static Hashtable<Par, Double> getDistancias1() {
		return distancias1;
	}

	public static void setDistancias1(Hashtable<Par, Double> distancias1) {
		ObsEnsamble.distancias1 = distancias1;
	}

	public static Hashtable<Par, Double> getDistancias2() {
		return distancias2;
	}

	public static void setDistancias2(Hashtable<Par, Double> distancias2) {
		ObsEnsamble.distancias2 = distancias2;
	}

	public static Hashtable<Par, Double> getDistancias3() {
		return distancias3;
	}

	public static void setDistancias3(Hashtable<Par, Double> distancias3) {
		ObsEnsamble.distancias3 = distancias3;
	}

	public static double getDist1Med() {
		return dist1Med;
	}

	public static void setDist1Med(double dist1Med) {
		ObsEnsamble.dist1Med = dist1Med;
	}

	public static double getDist2Med() {
		return dist2Med;
	}

	public static void setDist2Med(double dist2Med) {
		ObsEnsamble.dist2Med = dist2Med;
	}

	public static double getDist3Med() {
		return dist3Med;
	}

	public static void setDist3Med(double dist3Med) {
		ObsEnsamble.dist3Med = dist3Med;
	}

	@Override
	public double distACentroMasCercano(ArrayList<Observable> centros) {
		// TODO Auto-generated method stub
		return 0;
	}



	
	
	
	
	
	
	

}
