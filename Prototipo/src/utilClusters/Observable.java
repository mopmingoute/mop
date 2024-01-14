package utilClusters;

import java.util.ArrayList;

/**
 * Implementa los métodos que debe tener una observación de cualquier tipo
 * para que un conjunto de esas observaciones se pueda clasificar en clusters
 */
public interface Observable {
	
	/**
	 * Devuelve la energía entre la observación this
	 * y la Observable otra
	 * @param otra
	 * @return
	 */
	public double distancia(Observable otra);
	
	public static double distancia(Observable o1, Observable o2) {
		return o1.distancia(o2);
	}
	
	/**
	 * Devuelve el vector baricentro, que no está asociado a una observación
	 * promediando las coordenadas de las observaciones de this con las de alObs
	 * @param alObs
	 * @return
	 */
	public Observable baricentro(ArrayList<Observable> alObs);
	
	
	/**
	 * Devuelve un entero entre 0 y la cantidad de centros menos 1, 
	 * que es el ordinal del centro más cercano a this.
	 * 
	 * @param centros
	 * @return
	 */
	public int indiceCentroMasCercano(ArrayList<Observable> centros);
	
	
	
	
	
	/**
	 * Devuelve la distancia entre this y el centro más cercano de la lista centros
	 * @param centros
	 * @return
	 */
	public double distACentroMasCercano(ArrayList<Observable> centros);

}
