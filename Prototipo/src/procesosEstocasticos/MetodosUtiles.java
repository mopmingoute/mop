/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MetodosUtiles is part of MOP.
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
import utilitarios.AsistenteLectorTextos;

public class MetodosUtiles {
	
	
	
	
	/**
	 * Dado un archivo de texto, separado por blancos, con un formato con las líneas
	 * 
	 * // Una o más líneas de comentarios
	 * DURACION_PASO  (SEMANA, DIA, HORA)
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
		
		AsistenteLectorTextos lec = new AsistenteLectorTextos(dat, dirArchivo);
		int i=0;
		String nombreDur = lec.cargaPalabraDeLista(i, "DURACION_PASO", utilitarios.Constantes.NOMBRESPASOS);
		i++;
		int largoSeries = dat.size()- 2;
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
		}
		ConjuntoDeSeries conj = new ConjuntoDeSeries(dirArchivo, nombresSeries, series, nombreDur, cantSeries);
		return conj;
		
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
			if(is==1){
				largo = series.get(is).getDatos().length;
			}else{				
				if(series.get(is).getDatos().length!=largo){				
					System.out.println("Error en largo de serie " + series.get(is).getNombre());
					if (CorridaHandler.getInstance().isParalelo()){
						//PizarronRedis pp = new PizarronRedis();
						//pp.matarServidores();
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
				if(is==1){
					anio = series.get(is).getAnio()[t];
					paso = series.get(is).getPaso()[t];
				}else{
					if(anio != series.get(is).getAnio()[t] && paso != series.get(is).getPaso()[t]){
						System.out.println("Error en fechas de serie " + series.get(is).getNombre());
						if (CorridaHandler.getInstance().isParalelo()){
							//PizarronRedis pp = new PizarronRedis();
						//	pp.matarServidores();
						}
						System.exit(1);
					}
				}	
				suma += series.get(is).getDatos()[t]*coefs.get(is);				
			}			
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
	 * Calcula la media de la serie s1 en los casos en que s2 vale valor,
	 * para cada paso del año 
	 */
	public static double[] mediaDadoOtra(Serie s1, Serie s2, double valor){
		String nombrePaso = s1.getNombrePaso();
		int cantPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
		int cantDatos = s2.getDatos().length;
		double[] mediaD = new double[cantPasos];
		int[] cuentaD = new int[cantPasos];
		if(cantDatos != s2.getDatos().length){
			System.out.println("Las series tienen distinto largo: " + s1.getNombre() + " " + s2.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
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
	

