package cp_despacho;

import java.util.ArrayList;
import java.util.Hashtable;

import cp_datatypesEntradas.DatosGeneralesCP;

public class ConstructorGrafoEsc {
			
	private DatosGeneralesCP datGen;
	
	private static String[] nombresSeriesO;  // nombres de las series originales de datos
	private static String[] nombresSeriesT; // idem de series transformadas
	
	static {
		nombresSeriesT[0] = ConCP.DEMANDA_RESIDUAL;
		nombresSeriesT[1] = ConCP.POT_DISP_HORARIA;
	}
	
	
	/**
	* clave: número de escenario
	* valor: tabla que para cada serie original da los valores por poste 
	*/
	private Hashtable<Integer, Hashtable<String, double[]>> seriesO;
	
	private int cantSeriesOrig;
	private int cantSeriesTrans;
	
	/**
	* clave: número de escenario
	* valor: tabla que para cada serie transformada da los valores por poste 
	*/
	private Hashtable<Integer, Hashtable<String, double[]>> seriesT;
	
	/**
	* clave: número de escenario
	* valor: vector X con el que se calculan las distancias entre observaciones 
	 */
	private Hashtable<Integer, double[]> seriesX;

	
	/**
	* Las combinaciones lineales de las series transformadas se definen con los coeficientes de alfasTrans
	* clave: nombre de la serie transformada
	* valor: coeficiente de la serie original en la combinación, en el orden determinado por seriesOrig
	*/
	private Hashtable<String, double[]>  alfasTrans;
	
	
	private int[] posIniEtapa; // poste inicial de cada etapa
	private int[] posFinEtapa; // poste final
	private int[] cantPostesEtapas;  // cantidad de postes de cada etapa

	// Para cada etapa, los cantEsc escenarios dan lugar a cantEsc observaciones que deben clasificarse en clusters.
	/**
	* la lista recorre las etapas
	* en cada tabla
	* -clave: número de escenario de la observación
	*-valor: un vector de características de la observación
	*/
	private ArrayList<Hashtable<Integer, double[]> > observaciones;




}
