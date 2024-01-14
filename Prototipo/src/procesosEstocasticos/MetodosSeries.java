/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MetodosSeries is part of MOP.
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

package procesosEstocasticos;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procEstocUtils.EstimadorTransBoxCox;
import tiempo.LineaTiempo;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ParReales;
import utilitarios.UtilArrays;

public class MetodosSeries {
	
	
	/**
	 * Dado un archivo de texto, separado por blancos, con un formato con las líneas
	 * 
	 * // Una o más líneas de comentarios
	 * DURACION_PASO  (MES, SEMANA, DIA, HORA)
	 * CANT_CRON  nn
	 * ANIO   PASO_DEL_ANIO  NOMBRE_SERIE_1 ...  NOMBRE_SERIE_N   
	 * 1909    1              54.0          ...  599.0
	 * 
	 * Crea las series con los nombres y datos leídos.
	 * Todas las series deben tener el mismo largo y duración del paso 
	 * y no debe haber datos en blanco
	 * El valor del primer paso del año es 1.
	 */
	public static ConjuntoDeSeries leeConjuntoDeSeries(String dirArchivo){
		
		Hashtable<String, Serie> series = new Hashtable<String, Serie>();
		ArrayList<ArrayList<String>> dat = utilitarios.LeerDatosArchivo.getDatos(dirArchivo);
		
		AsistenteLectorEscritorTextos lec = new AsistenteLectorEscritorTextos(dat, dirArchivo);
		int i=0;
		String nombreDur = lec.cargaPalabraDeLista(i, "DURACION_PASO", utilitarios.Constantes.NOMBRESPASOS);
		i++;
		int cantCron = lec.cargaEntero(i,"CANT_CRON");
		i++;		
		int largoSeries = dat.size()- 3;
		int cantSeries = dat.get(i).size()-2;
		String[] nombresSeries = new String[cantSeries];
		for(int j=2; j<dat.get(i).size(); j++){
			Serie sj = new Serie(nombreDur);
			String nombre = dat.get(i).get(j);
			nombresSeries[j-2] = nombre;
			sj.setNombre(dat.get(i).get(j));
			sj.setNombrePaso(nombreDur);
			series.put(nombre, sj);
		}
		i++;
		double[][] matrizDatos = new double[cantSeries][largoSeries];
		int[] pasos = new int[largoSeries];
		int[] anios = new int[largoSeries];
		for(int il=0; il<largoSeries; il++){
			for(int j=0; j<cantSeries; j++){
				matrizDatos[j][il] = Double.parseDouble(dat.get(i).get(2+j));
			}
			anios[il]=Integer.parseInt(dat.get(i).get(0));
			pasos[il]=Integer.parseInt(dat.get(i).get(1));
			i++;
		}
		
		for(int is=0; is<cantSeries; is++){
			Serie s = series.get(nombresSeries[is]);
			s.setAnio(anios);
			s.setPaso(pasos);
			s.setDatos(matrizDatos[is]);	
			s.setCantDatos(pasos.length);
		}
		ConjuntoDeSeries conj = new ConjuntoDeSeries(dirArchivo, nombresSeries, series, nombreDur, cantCron);
		return conj;
		
	}
	
	
	
	/**
	 * Escribe estadísticos de la lista de series nombresSeries, que se extaen
	 * del hashtable series, en el archivo "estadisticos.xlt" del directorio dirsal
	 * 
	 * TODAS LAS SERIES DEBEN TENER EL MISMO nombrePaso.
	 */
	public static void imprimeEstadisticosSeries(String[] nombresSeries, Hashtable<String,Serie> series, String dirSal){
		String nombrePaso = series.get(nombresSeries[0]).getNombrePaso();
		for(int is=1; is<nombresSeries.length; is++){
			String np = series.get(nombresSeries[is]).getNombrePaso();
			if(!np.equalsIgnoreCase(nombrePaso)){
				System.out.println("ERROR: las series " + nombresSeries[0] +
						" y " + nombresSeries[is] + "tienen distinta cantidad de pasos por año");
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
			//		pp.matarServidores();
				}
				System.exit(1);
			}
		}
		int cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
		StringBuilder sb = new StringBuilder();
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
		// calcula medias por paso de las series leídas
		System.out.println("IMPRIME MEDIAS");
		sb.append("MEDIAS POR PASO DE LAS SERIES LEIDAS\n");
		
		for(int p=1; p<= cantMaxPasos; p++){
			sb.append(" "+p);
		}
		sb.append(" PROM-ANUAL");
		sb.append("\n");
		for(String s: nombresSeries){
			sb.append(s +"\t");
			sb.append(ale.escribeListaReales(MetodosSeries.mediaPorPaso(series.get(s)), "\t"));
			sb.append("\t" + series.get(s).media());
			sb.append("\n");
		}
		// calcula covarianzas y coeficientes de correlación por paso de las series leidas
		System.out.println("IMPRIME COVARIANZAS");
		sb.append("\n");
		sb.append("COVARIANZAS POR PASO DE LAS SERIES LEIDAS");
		ArrayList<Serie> ls = new ArrayList<Serie>();	
		for(String ns: nombresSeries){
			ls.add(series.get(ns));
		}
		int cantSL = ls.size();
		double[][][] cov = MetodosSeries.covarianzaPPaso(ls);		
		for(int i1=0; i1<cantSL; i1++){
			for(int i2=0; i2<cantSL; i2++){
				double[] aux = new double[cantMaxPasos];
				for(int ip=0; ip<cantMaxPasos; ip++){
					aux[ip] = cov[ip][i1][i2];
				}	
				sb.append(ale.escribeEtiqYListaReales(nombresSeries[i1]+
						"-"+nombresSeries[i2], aux, "\t"));
				sb.append("\n");
			}
		}
		sb.append("\n");
		System.out.println("IMPRIME CORRELACIONES CRUZADAS");
		sb.append("COEF. DE CORRELACIÓN POR PASO ENTRE LAS SERIES LEIDAS\n");
		double[][][] corr = MetodosSeries.corrPPaso((ls));		
		for(int i1=0; i1<cantSL; i1++){
			for(int i2=0; i2<cantSL; i2++){
				double[] aux = new double[cantMaxPasos];
				for(int ip=0; ip<cantMaxPasos; ip++){
					aux[ip] = corr[ip][i1][i2];
				}	
				sb.append(ale.escribeEtiqYListaReales(nombresSeries[i1]+
						"-"+nombresSeries[i2], aux, "\t"));
				sb.append("\n");
			}
		}	
		sb.append("\n");
		int cantLags = 52;
		System.out.println("IMPRIME AUTOCORRELACIONES");
		sb.append("AUTOCORRELOGRAMA POR PASO DE LAS SERIES LEIDAS\n");
		for(int il=0; il<=cantLags;il++){
			sb.append(il+"\t");
		}
		sb.append("\n");
		for(String ns: nombresSeries){
			sb.append(ns);
			sb.append("\n");
			double[][] auto = MetodosSeries.autoCorrelogramaPPaso(series.get(ns), cantLags);
			sb.append(ale.escribeMatrizReal(auto, "\t"));
		}		
				
		// Escribe
		
		String nombreArch = dirSal + "/estadisticos.xlt";
		DirectoriosYArchivos.eliminaArchivo(nombreArch);
		DirectoriosYArchivos.agregaTexto(nombreArch, sb.toString());
	}
	
	
	/**
	 * Dada una serie de datos, calcula la media por
	 * paso de tiempo entre los años de las serie.
	 * Si el paso es diario junta los días 365 y 366 o las horas del día 366 al del 365
	 * Los números de paso empiezan en 1.
	 * @return medias devuelve las medias, en medias[0] está la media del paso 1. 
	 */
	public static double[] mediaPorPaso(Serie x){
		int cantPasosMax = 0;
		int bisiesto = 0;
		int cantDatos = x.getDatos().length;
		String nombrePaso = x.getNombrePaso();
		if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOSEMANA)){
			cantPasosMax = 52;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASODIA)){
			cantPasosMax = 365;
			bisiesto = 1;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOHORA)){
			cantPasosMax = 8760;
			bisiesto = 24;
		}
		double[] medias = new double[cantPasosMax];
		int[] cuenta = new int[cantPasosMax];
		for(int i=0; i<cantDatos; i++){
			int npaso = x.getPaso()[i];
			if(npaso>cantPasosMax) npaso -= bisiesto;
			cuenta[npaso-1] = cuenta[npaso-1] + 1;
			medias[npaso-1] = medias[npaso-1] + x.getDatos()[i]; 
		}
		for(int p=0; p<cantPasosMax; p++){
			medias[p] = medias[p]/cuenta[p];
		}
		return medias;		
		
	}


	
	/**
	 * Dada una serie de datos, calcula el máximo por
	 * paso de tiempo entre los años de las serie.
	 * Si el paso es diario junta los días 365 y 366 o las horas del día 366 al del 365
	 * Los números de paso empiezan en 1.
	 * @return max devuelve el máximo, en max[0] está la media del paso 1. 
	 */
	public static double[] maxPorPaso(Serie x){
		int cantPasosMax = 0;
		int bisiesto = 0;
		int cantDatos = x.getDatos().length;
		String nombrePaso = x.getNombrePaso();
		if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOSEMANA)){
			cantPasosMax = 52;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASODIA)){
			cantPasosMax = 365;
			bisiesto = 1;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOHORA)){
			cantPasosMax = 8760;
			bisiesto = 24;
		}
		double[] max = new double[cantPasosMax];
		for(int p=0; p<cantPasosMax; p++){
			max[p]=Double.MIN_VALUE;
		}
		int[] cuenta = new int[cantPasosMax];
		for(int i=0; i<cantDatos; i++){
			int npaso = x.getPaso()[i];
			if(npaso>cantPasosMax) npaso -= bisiesto;
			if(x.getDatos()[i]>max[npaso-1]) max[npaso-1] = x.getDatos()[i]; 
		}
		return max;				
	}	
	
	
	/**
	 * Dada una serie de datos, calcula el mínimo por
	 * paso de tiempo entre los años de las serie.
	 * Si el paso es diario junta los días 365 y 366 o las horas del día 366 al del 365
	 * Los números de paso empiezan en 1.
	 * @return max devuelve el máximo, en max[0] está la media del paso 1. 
	 */
	public static double[] minPorPaso(Serie x){
		int cantPasosMax = 0;
		int bisiesto = 0;
		int cantDatos = x.getDatos().length;
		String nombrePaso = x.getNombrePaso();
		if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOSEMANA)){
			cantPasosMax = 52;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASODIA)){
			cantPasosMax = 365;
			bisiesto = 1;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOHORA)){
			cantPasosMax = 8760;
			bisiesto = 24;
		}
		double[] min = new double[cantPasosMax];
		for(int p=0; p<cantPasosMax; p++){
			min[p]=Double.MAX_VALUE;
		}
		int[] cuenta = new int[cantPasosMax];
		for(int i=0; i<cantDatos; i++){
			int npaso = x.getPaso()[i];
			if(npaso>cantPasosMax) npaso -= bisiesto;
			if(x.getDatos()[i]<min[npaso-1]) min[npaso-1] = x.getDatos()[i]; 
		}
		return min;				
	}		
	
	
	/**
	 * Devuelve una serie con la misma longitud que la original pero con los valores estandarizados
	 * en el rango [epsi,1] con la fórmula
	 * xe(t) = [ (x(t)-xmin(s(t)) - epsi] /  [ xmax(s(t)) - xmin(s(t)) - epsi]
	 * donde s(t) es el paso del año del intervalo t y  
	 * xmin(s(t)) y xmax(s(t)) son el mínimo y el máximo de los valores en la serie
	 * que pertenecen al paso s(t)
	 * @param original
	 * @return
	 */
	public static Serie estandariza01(Serie original, double epsi){
		int cantDatos = original.getDatos().length;
		Serie nueva = new Serie(original.getNombrePaso());
		nueva.setAnio(original.getAnio());
		nueva.setPaso(original.getPaso());
		nueva.setDatos(new double[cantDatos]);
		double[] max = maxPorPaso(original);
		double[] min = minPorPaso(original);
		int maxPasos = utilitarios.Constantes.CANTMAXPASOS.get(original.getNombrePaso());
		for(int t = 0; t<cantDatos; t++){
			int paso = nueva.getPaso()[t];
			nueva.getDatos()[t] = (original.getDatos()[t] - min[paso-1] - epsi)/(max[paso-1] - min[paso-1] - epsi);
		}
		return nueva;		
	}
	
	
	/**
	 * Crea una serie que es la combinacion lineal de otras.
	 * Las series deben tener el mismo largo.
	 * @return
	 */
	public static Serie combilin(ArrayList<Serie> series , ArrayList<Double> coefs){
		Serie nueva = new Serie(series.get(0).getNombrePaso());
		int largo = 0;
		int cantSeries = series.size();
		for(int is = 0; is<cantSeries; is++){
			if(is==0){
				largo = series.get(is).getDatos().length;
			}else{				
				if(series.get(is).getDatos().length!=largo){				
					System.out.println("Error en largo de serie en MetodosSeries.combilin " + series.get(is).getNombre());
					if (CorridaHandler.getInstance().isParalelo()){
						//PizarronRedis pp = new PizarronRedis();
					//	pp.matarServidores();
					}
					System.exit(1);	
				}
			}
		}
		nueva.setAnio(series.get(0).getAnio());
		nueva.setPaso(series.get(0).getPaso());
		nueva.setDatos(new double[largo]);
		for(int t = 0; t<largo; t++){
			int anio =-1; 
			int paso =-1;
			double suma = 0.0;
			for(int is = 0; is<cantSeries; is++){
				if(is==0){
					anio = series.get(is).getAnio()[t];
					paso = series.get(is).getPaso()[t];
				}else{
					if(anio != series.get(is).getAnio()[t] && paso != series.get(is).getPaso()[t]){
						System.out.println("Error en fechas de serie " + series.get(is).getNombre());
						if (CorridaHandler.getInstance().isParalelo()){
							//PizarronRedis pp = new PizarronRedis();
							//pp.matarServidores();
						}
						System.exit(1);
					}
				}	
				suma += series.get(is).getDatos()[t]*coefs.get(is);				
			}
			nueva.getDatos()[t]=suma;
		}		
		return nueva;		
	}
	
	
	/**
	 * Crea una nueva serie que es la combinacion lineal de otras dos.
	 * Las series deben tener el mismo largo.
	 * @return
	 */
	public static Serie combilin2S(Serie s1, Serie s2, double d1, double d2){
		Serie nueva = new Serie(s1.getNombrePaso());
		if(s1.getDatos().length!= s2.getDatos().length){
			System.out.println("Distinto largo de series " + s1.getNombre() + " " + s2.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);	
		}
		if(s1.getNombrePaso() != s2.getNombrePaso()){
			System.out.println("Distinto paso de series " + s1.getNombre() + " " + s2.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(1);	
		}
		int largo = s1.getDatos().length;
		nueva.setAnio(s1.getAnio());
		nueva.setPaso(s1.getPaso());
		nueva.setDatos(new double[largo]);
		for(int t = 0; t<largo; t++){
			nueva.getDatos()[t] = s1.getDatos()[t]*d1 + s2.getDatos()[t]*d2;			
		}		
		return nueva;		
	}	
	
	
	/**
	 * Aplica una transformación lineal a una lista de series, creando otra 
	 * lista de series.
	 * La matriz tlin premultiplica a las series de lista para cada tiempo t
	 * @param tlin es la matriz de la aplicación lineal
	 * @param nombres es la lista de nombres de las series que se crean
	 * @param lista es la lista de Series originales
	 * 
	 */
	public static ArrayList<Serie> transLineal(double[][] tlin, String[] nombres, ArrayList<Serie> lista){
		int nfilt = tlin.length;
		int ncolt =tlin[0].length;
		Serie s0 = lista.get(0);
		int cantDatos = lista.get(0).getDatos().length;
		if( ncolt != lista.size()){
			StringBuilder sb = new StringBuilder("SERIES ");
			for(Serie s: lista){
				sb.append(s.getNombre() + " ");
			}
			System.out.println("ERROR DE DIMENSION AL APLICAR METODO translineal " + sb.toString());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		ArrayList<Serie> result = new ArrayList<Serie>();
		for(int ifil = 0; ifil<nfilt; ifil++){
			result.add(new Serie(nombres[ifil], s0.getNombrePaso(),
					new double[cantDatos], s0.getAnio(), s0.getPaso()));
		}
		for(int t=0; t<cantDatos; t++){
			for(int ifil = 0; ifil<nfilt; ifil++){
				double suma = 0.0;
				for(int icol = 0; icol<ncolt; icol++){
					suma += tlin[ifil][icol]*lista.get(icol).getDatos()[t];
				}
				result.get(ifil).getDatos()[t] = suma;
			}
		}		
		return result;		
	}
	
	
	/**
	 * Crea una nueva serie que es tiene en cada tiempo el mínimo de otras dos.
	 * Las series deben tener el mismo largo.
	 * @return
	 */
	public static Serie min2S(Serie s1, Serie s2){
		Serie nueva = new Serie(s1.getNombrePaso());
		if(s1.getDatos().length!= s2.getDatos().length){
			System.out.println("Distinto largo de series " + s1.getNombre() + " " + s2.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);	
		}
		if(s1.getNombrePaso() != s2.getNombrePaso()){
			System.out.println("Distinto paso de series " + s1.getNombre() + " " + s2.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(1);	
		}
		int largo = s1.getDatos().length;
		nueva.setAnio(s1.getAnio());
		nueva.setPaso(s1.getPaso());
		nueva.setDatos(new double[largo]);
		for(int t = 0; t<largo; t++){
			nueva.getDatos()[t] = Math.min(s1.getDatos()[t], s2.getDatos()[t]);			
		}		
		return nueva;		
	}		
	
	/**
	 * Para cada paso de tiempo del año construye la lista de valores que separan los cuantiles
	 * Ejemplo si cuantiles[i] = 0.5 devuelve el valor mediano (si hay una cantidad impar de valores del paso)
	 * o la semisuma de los dos más próximos (si hay una cantidad par)
	 * a ser medianos  
	 * @param serie
	 * @param cuantiles primer índice paso del año, segundo índice probabilidad del cuantil
	 * @return
	 */
	public static double[][] calculaCuantiles(Serie serie, double[] probCuantiles){
		int maxPasos = utilitarios.Constantes.CANTMAXPASOS.get(serie.getNombrePaso());
		int cantCuantiles = probCuantiles.length;
		double[][] cuantiles = new double[maxPasos][cantCuantiles];
		int largo = serie.getDatos().length;
		
		ArrayList<ArrayList<Double>> obs = new ArrayList<ArrayList<Double>>();
		for(int i=1; i<=maxPasos; i++){
			obs.add(new ArrayList<Double>());
		}
		for(int t=0; t<largo; t++){
			obs.get(serie.getPaso()[t]-1).add(serie.getDatos()[t]);
		}
		for(int paso=0; paso<maxPasos; paso++){
			int cantObs = obs.get(paso).size();
			Collections.sort(obs.get(paso));
			int ic=0;
			for(double prob: probCuantiles){
				int ordc = indiceCuantilRound(prob, cantObs);
				cuantiles[paso][ic] = obs.get(paso).get(ordc);				
				ic++;
			}
		}
		return cuantiles;
	}
	
	
	/**
	 * Media de una serie dado el valor de otra
	 * Calcula la media en cada paso de la serie s1 en los casos en que s2 vale valor,
	 * para cada paso del año £
	 */
	public static double[] mediaPorPasoDadoOtra(Serie s1, Serie s2, double valor){
		String nombrePaso = s1.getNombrePaso();
		int cantPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
		int cantDatos = s2.getDatos().length;
		double[] mediaD = new double[cantPasos];
		int[] cuentaD = new int[cantPasos];
		if(cantDatos != s2.getDatos().length){
			System.out.println("Las series tienen distinto largo: " + s1.getNombre() + " " + s2.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
		//		pp.matarServidores();
			}
			System.exit(1);
		}
		
		for(int t = 0; t<cantDatos; t++){
			if(s2.getDatos()[t] == valor){
				int paso = s2.getPaso()[t];
				mediaD[paso-1] += s1.getDatos()[t];
				cuentaD[paso-1] ++;
			}			
		}
		for(int ip=0; ip<cantPasos; ip++){
			mediaD[ip] = mediaD[ip]/cuentaD[ip];
		}
		return mediaD;
	}

	
	/**
	 * Dada una cantidad cantTotal de valores ordenados X(0), X(1).... X(cantTotal-1)
	 * de una variable X, devuelve el índice i1, empezando en 0
	 * tal que 
	 * Prob(X<=X(i1)) <= prob
	 * Prob(X<=X(i1+1)) > prob
	 * 
	 * 
	 * Ejemplos
	 * si hay 9 valores numerados del 0 al 8 y prob=0.5 devuelve 4
	 * si hay 8 valores numerados del 0 al 7 y prob=0.5 devuelve 3
	 * @param prob
	 * @param cantTotal
	 * @return
	 */
	public static int indiceCuantilRound(double prob, int cantTotal){
		return ((int)Math.round((cantTotal)*prob) - 1);		
	}
	

	/**
	 * A partir de una serie s1 devuelve una serie normalizada sn con el mismo nombre y paso,
	 * empleando el tipo de transformacion tipoTrans y la definicion de poblaciones dp
	 * @param s1
	 * @param tipoTrans
	 * @param dp
	 * @return
	 */
	public static SerieTransformadaYTransformaciones normalizar(Serie s1, String tipoTrans, DefPoblacionSerie dpob){
		int cantDatos = s1.getDatos().length;
		int cantPasosMax = utilitarios.Constantes.CANTMAXPASOS.get(s1.getNombrePaso());
		double[] datosSn = new double[cantDatos];
		Serie sn = new Serie(s1.getNombre(), s1.getNombrePaso(), datosSn, s1.getAnio(), s1.getPaso());
		ArrayList<ArrayList<Double>> poblaciones = s1.devuelvePoblacionesPasos(dpob);
		ArrayList<TransformacionVA> transformaciones = new ArrayList<TransformacionVA>();
		TransformacionVA trans1Paso = null;
		for(int ip=1; ip<=cantPasosMax; ip++){
			ArrayList<Double> aux = poblaciones.get(ip-1);
			double[] auxArray = new double[aux.size()];			
			for(int i=0; i<aux.size(); i++){
				auxArray[i]=aux.get(i);
			}
			
			if(tipoTrans.equalsIgnoreCase(utilitarios.Constantes.BOXCOX)){
				EstimadorTransBoxCox eTBC = new EstimadorTransBoxCox(auxArray);
				trans1Paso = eTBC.estimaBoxCox();
			}else if(tipoTrans.equalsIgnoreCase(utilitarios.Constantes.NQT)){
				trans1Paso = new TransNQT(auxArray);				
			}else{
				System.out.println("Error en tipo de transformación " + tipoTrans + " en serie " + s1.getNombre());
			}
			transformaciones.add(trans1Paso);
		}
		for(int t=0; t<cantDatos; t++){
			int paso = s1.getPaso()[t];
			datosSn[t] = transformaciones.get(paso-1).transformar(s1.getDatos()[t]);
		}
		SerieTransformadaYTransformaciones styt = new SerieTransformadaYTransformaciones(sn, transformaciones);
		return styt;
	}
	
	
	/**
	 * DEVUELVE LA MATRIZ DE COVARIANZA DE SERIES ESTACIONARIAS
	 * Dada una lista de series supuestas todas estacionarias y de media nula,
	 * y del mismo largo, calcula la matriz de covarianzas
	 * @param series
	 * @return
	 */
	public static double[][] covarianza(ArrayList<Serie> series){
		int cantS = series.size();
		ArrayList<Serie> series0 = new ArrayList<Serie>();  // tiene las series restada la media
		for(Serie s: series){
			series0.add(restaMediasPorPaso(s));
		}
		double[][] cov = new double[cantS][cantS];
		int cantDatos = series0.get(0).getDatos().length;
		for(int i=0; i<cantS; i++){
			for(int j=0; j<cantS; j++){
				double suma = 0;
				for(int t=0; t<cantDatos; t++){
					suma += series0.get(i).getDatos()[t]*series0.get(j).getDatos()[t];
				}
				suma = suma/cantDatos;
				cov[i][j] = suma;
			}
		}
		return cov;	
	}
	
	
	/**
	 * Calcula la covarianza por cada paso del año de un conjunto de series
	 * supuestas estacionarias y con el mismo nombrePaso
	 * @param series
	 * @return cov double[][][] con las matrices de covarianzas
	 * primer índice paso del año
	 * segundo y tercer índice, la matriz de covarianzas entre series del paso dado
	 */
	public static double[][][] covarianzaPPaso(ArrayList<Serie> series){
		int cantSeries = series.size();
		ArrayList<Serie> seriesMediaCero = new ArrayList<Serie>();
		int cantPasosMax = utilitarios.Constantes.CANTMAXPASOS.get(series.get(0).getNombrePaso());
		for(Serie s: series){
			Serie sn = MetodosSeries.restaMediasPorPaso(s);
			seriesMediaCero.add(sn);
		}
		int cantDatos = series.get(0).getDatos().length;
		double[][][] cov = new double[cantPasosMax][cantSeries][cantSeries];
		ArrayList<double[][]> seriesMedCeroPPaso = new ArrayList<double[][]>();
		for(int i=0; i<cantSeries; i++){
			seriesMedCeroPPaso.add(seriesMediaCero.get(i).datosPorPaso());
		}
		for(int ip=0; ip<cantPasosMax; ip++){
			for(int i=0; i<cantSeries; i++){
				for(int j=0; j<cantSeries; j++){
					if(seriesMedCeroPPaso.get(i)[ip].length!=seriesMedCeroPPaso.get(j)[ip].length){
						System.out.println("");
					}
					cov[ip][i][j] = 
							utilitarios.UtilArrays.prodEscalar(seriesMedCeroPPaso.get(i)[ip],
															seriesMedCeroPPaso.get(j)[ip]);
				}
			}
		}
		return cov;
	}
	
	
	/**
	 * Calcula la varianza por cada paso del año de una serie
	 * @param serie
	 * @return var double[] con las varianzas por paso del año
	 * 
	 */
	public static double[] varianzaPPaso(Serie s){
		int cantPasosMax = utilitarios.Constantes.CANTMAXPASOS.get(s.getNombrePaso());
		Serie s0 = restaMediasPorPaso(s);
		double[][] datPas = s0.datosPorPaso();
		double[] var = new double[cantPasosMax];
		for(int ip=0; ip<cantPasosMax; ip++){
			var[ip]=utilitarios.UtilArrays.prodEscalar(datPas[ip], datPas[ip]);
		}
		return var;
	}
		
	
	/**
	 * Calcula el coeficiente de correlación por cada paso del año de un conjunto de series
	 * supuestas estacionarias y con el mismo nombrePaso
	 * @param series
	 * @return corr double[][][] con las matrices de coeficientes de correlación
	 * primer índice paso del año
	 * segundo y tercer índice, la matriz de correlaciones entres series del paso dado
	 */
	public static double[][][] corrPPaso(ArrayList<Serie> series){
		double[][][] cov = covarianzaPPaso(series);
		int cantPasos = cov.length;
		int cantSeries = series.size();		
		double[][][] corr = new double[cantPasos][cantSeries][cantSeries];
		for(int ip=0; ip<cantPasos; ip++){
			for(int i=0; i<cantSeries; i++){
				for(int j=0; j<cantSeries; j++){
					corr[ip][i][j] = cov[ip][i][j]/Math.sqrt(cov[ip][i][i]*cov[ip][j][j]);
				}
			}		
		}
		return corr;		
	}
	
	
	
	
	/**
	 * AUTOCORRELOGRAMA POR PASO DEL AÑO DE UNA SERIE CON ESTACIONALIDAD ANUAL
	 * Devuelve un double[][] con las autocorrelaciones de orden 0,1,...lagMax
	 * para cada paso del año de la serie s
	 * @param s
	 * @param n cantidad máxima de lags tomados
	 * @return autocorr primer índice paso del año segundo índice lag empezando en 0, será siempre un 1
	 */
	public static double[][] autoCorrelogramaPPaso(Serie s, int n){
		Serie s0 = restaMediasPorPaso(s);
		double[][] autocorr = new double[s.getCantMaxPasos()][n+1];
		for(int il=0; il<=n; il++){
			ArrayList<Serie> ls = new ArrayList<Serie>();
			ls.add(s0.truncaNIniciales(il));
			ls.add(s0.rezagaL(il));
			double[][][] cov = corrPPaso(ls);
			for(int ip=0; ip<s.getCantMaxPasos(); ip++){		
				autocorr[ip][il]=cov[ip][0][1];
			}
		}
		return autocorr;
	}
	
	
	
	/**
	 * AUTOCORRELOGRAMA DE UNA SERIE ESTACIONARIA
	 * Devuelve un double[] con las autocorrelaciones de orden 0,1,...lagMax
	 * @param s
	 * @param n cantidad máxima de lags tomados
	 * @return autocorr primer índice paso del año segundo índice lag empezando en 0, será siempre un 1
	 */
	public static double[] autoCorrelograma(Serie s, int n){		
		Serie s0 = restaMediasPorPaso(s);
		double[] autocorr = new double[n+1];
		for(int il=0; il<=n; il++){
			ArrayList<Serie> ls = new ArrayList<Serie>();
			ls.add(s0);
			ls.add(s0.rezagaL(il));
			int cant0 = s.getDatos().length-il;
			autocorr[il] = covarianza(ls)[0][1];
		}
		return autocorr;
	}
		
	
	/**
	 * Devuelve una nueva serie que es la original restando las 
	 * media por paso en cada paso. Los pasos del año se numeran a partir de 1,2,...
	 * @param s1
	 * @return
	 */
	public static Serie restaMediasPorPaso(Serie s1){
		int cantDatos =  s1.getDatos().length;
		Serie result = new Serie(s1.getNombre(), s1.getNombrePaso(),
				new double[cantDatos], s1.getAnio(), s1.getPaso());
		double[] medias = mediaPorPaso(s1);
		for(int t=0; t<cantDatos; t++){
			int pasoDB = s1.pasoDepB(s1.getPaso()[t]);
//			System.out.println("t " + t + "  s1.getPaso()[t] " + s1.getPaso()[t] + " pasoDB " + pasoDB);
			result.getDatos()[t] = s1.getDatos()[t] - medias[pasoDB-1];
		}
		return result;
	}
	
	
	
    /**
     * Devuelve la duración y suma de faltante para cada racha
     * de valores de la serie por debajo de su media paso por paso.
     * @param s
     * @return
     */
    public static ArrayList<ParReales> devuelveRachasBajoMedia(Serie s){
    	Serie s0 = restaMediasPorPaso(s);
    	ArrayList<ParReales> rachas = new ArrayList<ParReales>();
    	double sumaNeg = 0.0;  // acumula la suma de las rachas negativas , es decir es menor que cero
    	double dur = 0.0; // duración de la racha, en realidad es un entero siempre.
    	for(int t=0; t<s.getCantDatos(); t++){
    		double valt = s0.getDatos()[t];
    		if(sumaNeg<0.0 && valt<0.0){
    			// continua una racha negativa
    			sumaNeg = sumaNeg + valt;
    			dur = dur + 1;
    		}else if(sumaNeg<0.0 && valt>0.0){
    			// termina una racha negativa
    			ParReales par = new ParReales(dur, -sumaNeg);
    			rachas.add(par);
    			sumaNeg = valt;
    			dur = 0.0;   			
    		}else if(sumaNeg>0.0 && valt<0.0){
    			// termina una racha positiva y empieza una negativa
    			sumaNeg = valt;
    			dur = 1.0;
    		}  		
    	}
    	return rachas;
    }

    /**
     * Devuelve todas las sumas de la serie en periodos definidos por una clave,
     * @param clave
     * = 1A sumas de 1 año calendario
     * = nA sumas de n años calendario sucesivos (ventana de n años móvil)
     * = 1T sumas de primer trimestre (13 semanas, 91 dias o 92 para el ultimo trimestre)
     * = nT sumas de n esimo trimestre del año
     * 
     * Si modo = "P"  devuelve promedios y si = "S" devuelve sumas.
     * @return
     */
    public static ArrayList<Double> devuelvePromOSumaPorPeriodo(String clave, Serie s, String modo){
    	ArrayList<Double> result = new ArrayList<Double>();
    	int[] anios = s.getAnio();
    	int[] paso = s.getPaso();
    	double[] datos = s.getDatos();
    	int cantDatos = datos.length;
    	int cantMaxPasos = s.getCantMaxPasos();
    	int cantAnios = cantDatos/s.getCantMaxPasos();
    	int pini = 1; // paso inicial del año para contabilizar empezando en 1
    	
    	int pfin = cantMaxPasos;
    	if(paso[0]!=1 || paso[cantDatos-1]!=s.getCantMaxPasos()){
    		System.out.println("La serie " + s.getNombre() + " no tiene años completos");
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
    		System.exit(1);
    	}
    	String letra = clave.substring(0,1);
    	ArrayList<Double> valAnios = new ArrayList<Double>();
    	int horizAnios = 0;
    	int ntrim = 0;
    	if(letra.equalsIgnoreCase("A")){   		
    		horizAnios = Integer.parseInt(clave.substring(1,2));
    	}else if(letra.equalsIgnoreCase("T")){
    		horizAnios = 1;
    		ntrim = Integer.parseInt(clave.substring(1,2)); // trimestre del 1 al 4
    		pini = (ntrim-1)*(cantMaxPasos/4)+1; // pini y pfin contados a partir de 1
    		pfin = pini + (cantMaxPasos/4-1);
    	}
		int t=0;
		for(int ia=0; ia<cantAnios; ia++){
			double suma =0.0;
    		if(s.getNombrePaso().equalsIgnoreCase(utilitarios.Constantes.PASO_DIARIO) && ntrim==4){
    			pfin = 365;
    			if(LineaTiempo.bisiesto(s.getAnio()[t])) pfin = 366;
    		}
			for(int ip=1; ip<pini;ip++){
				suma += s.getDatos()[t];
				t++;
			}			
			for(int ip=pini; ip<=pfin;ip++){
				suma += s.getDatos()[t];
				t++;
			}
			for(int ip=pfin+1; ip<=cantMaxPasos;ip++){
				suma += s.getDatos()[t];
				t++;
			}	
			if(modo=="P") suma = suma/(pfin-pini);
			valAnios.add(suma);
		}    		
		
		double prom2;
		for(int ian=horizAnios-1; ian<cantAnios-1; ian++){
			prom2 = 0.0;
			for(int a=0; a<horizAnios; a++){
				prom2 += valAnios.get(ian-a);
			}
			result.add(prom2/horizAnios);			  		
    	}
    	return result;   	   	
    }
    
    
    /**
     * Produce un texto con los cálculos de promedios anuales y trimestrales
     * Se escriben promedios o sumas anuales en ventanas móviles de uno, dos,...hanios
     * y promedios de trimestres, cada uno ordenado en orden creciente. 
     * Si modo = "P"  devuelve promedios y si = "S" devuelve sumas.
     * 
     * @param s
     * @param archSal
     * @return
     */
    public static String escribePromOSumaAyT(Serie s, int hanios, String modo){
    	if(hanios<1){
    		System.out.println("Se pidió promedio con ventana de " + hanios + " años, de serie " + s.getNombre());
    	}
    	StringBuilder sb = new StringBuilder();
    	AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
    	for(int na=1; na<= hanios; na++){
    		String etiq = "ventana " + na + " año(s)\t";
    		String clave = "A" + na;
    		ArrayList<Double> aux = devuelvePromOSumaPorPeriodo(clave, s, "S");
    		Collections.sort(aux);
    		sb.append(ale.escribeEtiqYListaReales(etiq, UtilArrays.dameArrayD(aux), "\t"));
    	}
    	for(int nt=1; nt<= 4; nt++){
    		String etiq = "Promedios trimestre " + nt +"\t";
    		String clave = "T" + nt;
    		ArrayList<Double> aux = devuelvePromOSumaPorPeriodo(clave, s, "S");
    		Collections.sort(aux);
    		sb.append(ale.escribeEtiqYListaReales(etiq, UtilArrays.dameArrayD(aux), "\t"));
    	}    	
    	return sb.toString();   	
    }
	
//	public static void main(String[] args) {              
//		String dirArchivo = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\PRUEBAS-MARKOV-AMPLIADO\\datos.prn";
//		Hashtable<String, Serie> series = leeSeries(dirArchivo);
//		Serie bon = series.get("APORTE-BONETE");
//		double[] mediaBon = mediaPorPaso(bon);
//		double[] maxBon = maxPorPaso(bon);
//		for(int i=0; i<mediaBon.length; i++){
//			System.out.println(mediaBon[i]);
//		}	
//		System.out.println("\n\n");
//		for(int i=0; i<mediaBon.length; i++){
//			System.out.println(maxBon[i]);
//		}		
//	}

	public static void main(String[] args) {    
		System.out.println("0.5  8   " + indiceCuantilRound(0.5, 8));
		System.out.println("0.5  9   " + indiceCuantilRound(0.5, 9));
		System.out.println("0.2  100  " + indiceCuantilRound(0.2, 100));
		System.out.println("0.2  101  " + indiceCuantilRound(0.2, 101));
		System.out.println("0.2  103  " + indiceCuantilRound(0.2, 103));
	}

}
	

