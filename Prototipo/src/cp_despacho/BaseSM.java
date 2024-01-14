/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BaseSM is part of MOP.
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

package cp_despacho;

import java.util.ArrayList;

/**
 * Contiene la información del segundo miembro de las restricciones y sobre como expandirlos cuando se expande la restricción
 * @author ut469262
 *
 */
public class BaseSM {
	

	private static GrafoEscenarios ge;

	private double fijo;   
	
	/**
	 * Si es !null es el nombre de las VA cuya combinación lineal SE SUMA AL FIJO en el segundo miembro, 	
	 */
	private ArrayList<String> nomsVASMSum;
	
	/**
	 * Si es nomsVASMSum !null valores de los coeficientes de la combinación lineal.
	 */
	private ArrayList<Double> coefsVASMSum;
	
	/**
	 * Si es !null es el nombre de una VA cuyo valor se multiplica a la suma de fijo más las VAs de nomsVASM
	 */
	private String nomVASMProd;
	
	/**
	 *
	 * El poste en el que se evalúa la VA en SMALEA, 
	 * empezando en cero en el origen. Normalmente es igual al atributo ent del BaseRest al que pertenece el segundo miembro.
	 */
	private Integer posteSM;
	
	public BaseSM(double fijo, ArrayList<String> nomsVASMSum, ArrayList<Double> coefsVASMSum, String nomVASMProd, Integer posteSM) {
		this.fijo = fijo;
		if(nomsVASMSum!=null) {
			this.nomsVASMSum = nomsVASMSum;
		}else {
			this.nomsVASMSum = new ArrayList<String>();
		}
		if(nomsVASMSum!=null) {
			this.coefsVASMSum = coefsVASMSum;
		}else {
			this.coefsVASMSum = new ArrayList<Double>();
		}
		this.nomVASMProd = nomVASMProd;
		this.posteSM = posteSM;
	}
	
	
	/**
	 * Devuelve el valor del segundo miembro para el poste y en el 
	 * escenario esc.
	 * Para eso suma el fijo de this más el valor de la combinación lineal de variables
	 * de nomsVASMSum en el poste y para el escenario esc
	 * @param poste
	 * @param esc
	 * @return
	 */
	public double valorSM(int esc) {
		double result = fijo;
		if(nomsVASMSum!=null) {
			int iva = 0;
			for(String nva: nomsVASMSum) {
				result += ge.valorVA(nva, posteSM, esc)*coefsVASMSum.get(iva);
				iva++;
			}
		}
		if(nomVASMProd!=null) {
			result = result*ge.valorVA(nomVASMProd, posteSM, esc);
		}
		return result;
	}
	
	
	

	public static GrafoEscenarios getGe() {
		return ge;
	}

	public static void setGe(GrafoEscenarios ge) {
		BaseSM.ge = ge;
	}


	public double getFijo() {
		return fijo;
	}

	public void setFijo(double fijo) {
		this.fijo = fijo;
	}



	public ArrayList<String> getNomsVASMSum() {
		return nomsVASMSum;
	}


	public void setNomsVASMSum(ArrayList<String> nomsVASMSum) {
		this.nomsVASMSum = nomsVASMSum;
	}

	

	public ArrayList<Double> getCoefsVASMSum() {
		return coefsVASMSum;
	}


	public void setCoefsVASMSum(ArrayList<Double> coefsVASMSum) {
		this.coefsVASMSum = coefsVASMSum;
	}


	public String getNomVASMProd() {
		return nomVASMProd;
	}


	public void setNomVASMProd(String nomVASMProd) {
		this.nomVASMProd = nomVASMProd;
	}


	public void setPosteSM(Integer posteSM) {
		this.posteSM = posteSM;
	}


	public Integer getPosteSM() {
		return posteSM;
	}


	
	
	
	



	
	
	
	
	
	
	
	

}
