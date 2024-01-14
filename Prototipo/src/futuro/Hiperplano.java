/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Hiperplano is part of MOP.
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

package futuro;

import java.io.Serializable;
import java.util.ArrayList;

import datatypesResOptim.DatosHiperplano;
import datatypesResOptim.DatosTablaHiperplanos;
import estado.VariableEstado;
import utilitarios.Constantes;

public class Hiperplano implements Serializable{
	
	
	private int numeroId; //
	
	private int paso;
	
	// Generación en la que se creó el hiperplano
	private int generacion; 
	
	// Coordenadas en las variables de estado continuas del punto que originó el hiperplano
	// El orden de las variables de estado continuas es determinado por el ResOptimHiperplanos
	private double[] punto;
	
	
	// Valor de Bellman en el punto punto, segón el hiperplano
	private double vBellman;
	
	
	// Coeficientes de las variables de estado continuas en el hiperplano
	// Ejemplo: si la variable de estado es un volumen de embalse es un coeficiente negativo
	private double[] coefs;
	
	// Tórmino independiente del hiperplano
	private double tind;
	
	/**
	 * El hiperplano es de la forma  valor de Bellman = suma en ivc { coef[ivc]* x[ivc] } + tind 
	 * donde ivc es el óndice de la variable continua y x[ivc] es la variable continua ivc-ósima.
	 * 
	 * Se cumple:
	 * vBellman = suma en ivc { coef[ivc]* punto[ivc] } + tind
	 * punto[] y tind son los atributos del hiperplano.
	 * 
	 */


	public Hiperplano(int cantVar, int paso, int numeroId){	
		this.setPaso(paso);
		this.setNumeroId(numeroId);
		generacion = 0;
		vBellman = 0.0;
		coefs = new double[cantVar];
		punto = new double[cantVar];
		tind = 0.0;		
	}
	
	
	public Hiperplano(DatosHiperplano dh){
		this.setNumeroId(dh.getNumeroId());
		this.setPaso(dh.getPaso());
		this.setCoefs(dh.getCoefs());
		this.setGeneracion(dh.getGeneracion());
		this.setPunto(dh.getPunto());
		this.setTind(dh.getTind());
		this.setvBellman(dh.getvBellman());
	}
	
	
	/**
	 * Suma al hiperplano this un nuevo hiperplano hip multiplicado por el escalar alfa
	 * El hiperplano this queda modificado
	 * El punto no se altera. Se supone que se opera con hiperplanos por el mismo punto
	 * @param hip
	 * @para alfa
	 */
	public void sumaHiperplanoPorEscalar(Hiperplano hip, double alfa){
		int cantVar = hip.getCoefs().length;
		for(int i=0; i<cantVar; i++){
			this.getCoefs()[i] += alfa*hip.getCoefs()[i];
		}
		this.setTind(this.getTind()+hip.getTind()*alfa);
		this.setvBellman(this.getvBellman()+hip.getvBellman()*alfa);
	}
	
	
	/**
	 * Evalóa el valor del hiperplano en el punto x
	 * @param x valores de las variables del hiperplano en el orden de los coeficientes
	 * @return valor
	 */
	public double valor(double[] x){
		double valor = this.getTind();
		int cantVar = this.coefs.length;
		for(int i=0; i<cantVar; i++){

			valor += x[i]*this.getCoefs()[i]*Constantes.M3XHM3;
		}
		return valor;
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
	
	
	public DatosHiperplano creaDataType(){
		DatosHiperplano dh = new DatosHiperplano();
		dh.setCoefs(this.getCoefs());
		dh.setGeneracion(this.getGeneracion());
		dh.setPunto(this.getPunto());
		dh.setTind(this.getTind());
		dh.setvBellman(this.getvBellman());	
		dh.setNumeroId(this.getNumeroId());
		return dh;
	}
	
	
	/**
	 * Devuelve los rótulos para imprimir una sucesión de hiperplanos
	 * @return
	 */
	public String imprimeTitulosHiperplanos(ArrayList<VariableEstado> veTotal, ArrayList<VariableEstado> veCont){
		
		int cantVC = coefs.length;
		StringBuilder sb = new StringBuilder("Hiperplano nómero \t" + "Generacion \t" );
		
		sb.append("ClaveVEDiscretas \t");
		
		sb.append("Punto \t");
		for(int i=0; i<cantVC; i++){
			sb.append(veCont.get(i).getNombre());
			sb.append("\t");
		}
		sb.append("Coeficientes \t");
		for(int i=0; i<cantVC; i++){
			sb.append(veCont.get(i).getNombre());
			sb.append("\t");
		}
		sb.append("Tórm.indep \t");
		sb.append("Valor de Bellman \n");
		return sb.toString();				
		
	}
	
	
	/**
	 * 
	 */
	public String imprimeHiperplano(ArrayList<VariableEstado> veTotal, ArrayList<VariableEstado> veCont) {
		
		int cantVC = coefs.length;
		StringBuilder sb = new StringBuilder(numeroId + "\t" + generacion + "\t");
		
		sb.append("\t");
		
		sb.append("Punto \t");
		for(int i=0; i<cantVC; i++){
			sb.append(veCont.get(i).getEstado());
			sb.append("\t");
		}
		sb.append("Coeficientes \t");
		for(int i=0; i<cantVC; i++){
			sb.append(coefs[i]);
			sb.append("\t");
		}
		sb.append(tind + "\t");
		sb.append(vBellman + "\n");
		return sb.toString();			
		
		
	}
	


	
	
}
