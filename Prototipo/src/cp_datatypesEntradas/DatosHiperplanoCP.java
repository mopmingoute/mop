package cp_datatypesEntradas;

import java.util.Hashtable;

public class DatosHiperplanoCP {
	
	private String[] nombresVE;  // dimensión igual a la cantidad cantVE de variables de estado continuas	
	private double[] puntoSoporte;  // dimensión igual a la cantidad cantVE de variables de estado continuas
	private double[] coeficientes;  // dimensión cantVE + 1. El primer coeficiente es el término independiente
	
	
	/**
	 * Clave nombre base 
	 */
	private Hashtable<String, String> nombresVEParticipantes;  
	
	
	public DatosHiperplanoCP(String[] nombresVE, double[] puntoSoporte, double[] coeficientes) {
		super();
		this.nombresVE = nombresVE;
		this.puntoSoporte = puntoSoporte;
		this.coeficientes = coeficientes;
	}


	public String[] getNombresVE() {
		return nombresVE;
	}


	public void setNombresVE(String[] nombresVE) {
		this.nombresVE = nombresVE;
	}


	public double[] getPuntoSoporte() {
		return puntoSoporte;
	}


	public void setPuntoSoporte(double[] puntoSoporte) {
		this.puntoSoporte = puntoSoporte;
	}


	public double[] getCoeficientes() {
		return coeficientes;
	}


	public void setCoeficientes(double[] coeficientes) {
		this.coeficientes = coeficientes;
	}



	
	
	
}
 