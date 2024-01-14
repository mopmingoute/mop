/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosHiperplano is part of MOP.
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

package datatypesResOptim;

public class DatosHiperplano {

	private int numeroId; //

	private int paso;

	// Generación en la que se creó el hiperplano
	private int generacion;

	// Coordenadas en las variables de estado continuas del punto que originó el
	// hiperplano
	// El orden de las variables de estado continuas es determinado por el
	// ResOptimHiperplanos
	private double[] punto;

	// Valor de Bellman en el punto punto, segón el hiperplano
	private double vBellman;

	// Coeficientes de las variables de estado continuas en el hiperplano
	// Ejemplo: si la variable de estado es un volumen de embalse es un coeficiente
	// negativo
	private double[] coefs;

	// Tórmino independiente del hiperplano
	private double tind;

	// Variable dual en el problema lineal si se está sacando la salida
	private double vdual;
	
	
	public DatosHiperplano() {
		
	}
	
	


	public DatosHiperplano(int numeroId, int paso, int generacion, double[] punto, double vBellman, double[] coefs) {
		super();
		this.numeroId = numeroId;
		this.paso = paso;
		this.generacion = generacion;
		this.punto = punto;
		this.vBellman = vBellman;
		this.coefs = coefs;
		double tind = vBellman;
		for(int j=0; j<coefs.length; j++) {
			tind -= coefs[j]*punto[j];
		}		
	}




	public int getNumeroId() {
		return numeroId;
	}

	public void setNumeroId(int numeroId) {
		this.numeroId = numeroId;
	}

	public int getPaso() {
		return paso;
	}

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public int getGeneracion() {
		return generacion;
	}

	public void setGeneracion(int generacion) {
		this.generacion = generacion;
	}

	public double[] getPunto() {
		return punto;
	}

	public void setPunto(double[] punto) {
		this.punto = punto;
	}

	public double getvBellman() {
		return vBellman;
	}

	public void setvBellman(double vBellman) {
		this.vBellman = vBellman;
	}

	public double[] getCoefs() {
		return coefs;
	}

	public void setCoefs(double[] coefs) {
		this.coefs = coefs;
	}

	public double getTind() {
		return tind;
	}

	public void setTind(double tind) {
		this.tind = tind;
	}

	public double getVdual() {
		return vdual;
	}

	public void setVdual(double vdual) {
		this.vdual = vdual;
	}

}