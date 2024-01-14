package AuxiliaresRed;

import java.util.ArrayList;

/**
 * Clase para repartir valores de demanda total en demandas por barra, o generación total de un palier entre generadores por barra.
 * @author UT469262
 *
 */
public abstract class DesagregadorPorBarras {
	
	public String nombre;
	
	public ArrayList<String> nombresBarras;
	
	/**
	 * Devuelve la fracción de la demanda total de un sector que representa la barra nombreBarra
	 * @param nombreBarra
	 * @param instante
	 * @return
	 */
	public abstract double devuelveFracBarra(String nombreBarra, long instante);
	
	/**
	 * Devuelve la fracción de la demanda total de un sector que representa cada una de las barras
	 * de la lista nombresBarras, en el mismo orden
	 * @param nombreBarra
	 * @param instante
	 * @return
	 */
	
	public abstract double[] devuelveFracsBarras(long instante);
	
	/**
	 * Le pasa a la clase los nombres de las barras que se usarán
	 * @param nombresBarras
	 */
	public void cargaNombresBarras(ArrayList<String> nombresBarras) {
		this.nombresBarras = nombresBarras;
	}
	
	public ArrayList<String> devuelveNombreBarras(){
		return nombresBarras;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	

}
