/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BaseTermino is part of MOP.
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

import cp_compdespProgEst.CompDespPE;

/**
 * @author ut469262
 * 
 * Permite generar términos de restricciones para todos los escenarios de una etapa
 *
 */
public class BaseTermino implements Comparable{

	
	private BaseVar bV; 	 
	 
	private double coef; // Valor del coeficiente fijo
	
	/**
	 * Si es !=null, nombre de la variable aleatoria de la que se obtiene un valor 
	 * A MULTIPLICAR por el fijo anterior
	 */
	private String nomVAcoef;  
	
	private static GrafoEscenarios ge;
	
	
	public BaseTermino() {};
	
	public BaseTermino(String nombreVar, String nombrePar, int poste, double coef, String nomVAcoef, GrafoEscenarios ge) {
		this.bV = CompDespPE.dameBaseVar(nombreVar, nombrePar, poste);
		this.coef = coef;	
		this.nomVAcoef = nomVAcoef;
	}
	
	
	public int dameEtapa() {
		if(bV==null) {
			int pp = 0;
		}
		return bV.getEtapa();
	}
	
	public int damePosteODia() {
		return bV.getEntero();
	}
	
	
	
	public String getNomVAcoef() {
		return nomVAcoef;
	}

	public void setNomVAcoef(String nomVAcoef) {
		this.nomVAcoef = nomVAcoef;
	}

	/**
	 * Devuelve el nombreBaseVar de la BaseVar del BaseTermino,
	 * que incluye nombre de la variable, nombre del participante y entero (poste o día) 
	 * @return
	 */
	public String dameNomBaseVar() {
		return bV.getNomBaseVar();
	}
	
//	public double[] getCoef() {
//		return coef;
//	}
//	public void setCoef(double[] coef) {
//		this.coef = coef;
//	}
	
	
	
	public static GrafoEscenarios getGe() {
		return ge;
	}
	public double getCoef() {
		return coef;
	}
	public void setCoef(double coef) {
		this.coef = coef;
	}
	public static void setGe(GrafoEscenarios ge) {
		BaseTermino.ge = ge;
	}

	public BaseVar getbV() {
		return bV;
	}

	public void setbV(BaseVar bV) {
		this.bV = bV;
	}
	
	
	public String toString() {
		String result="";
		if(coef==0) {
			return "";
		}else {
			if(coef==1) {
				result = "+";
			}else if(coef==-1) {
				result = "-" ;
			}else if(coef>0){
				result += "+" + coef;				
			}else result += coef;
			if(nomVAcoef!=null) result += "[" + nomVAcoef + "]";
			result += bV.getNomBaseVar();
		}
		return result;		
	}
	
	@Override
	public int compareTo(Object bt) {
		BaseTermino b = (BaseTermino)bt;
		String nomObj = b.getbV().getNomBaseVar();
		return this.getbV().getNomBaseVar().compareTo(nomObj);
	}




}
