/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AproximadorLinealPE is part of MOP.
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

import cp_compdespProgEst.CompDespPE;
import utilitarios.Constantes;

/**
 * Clase que permite crear un conjunto de BaseVar y de BaseRest que representan una función
 * lineal por tramos entre un conjunto de parejas (v0,f0),(v1,v1),...(vn,fn), donde los vi son 
 * los argumentos y los fi los valores respectivos de la función.
 * Al agregar al problema lineal los BaseVar y BaseRest creados por esta clase, la variable
 * de nombre nomVarF queda limitada a ser una función lineal por tramos de la variable de nombre nomVarV
 * @author UT469262
 * 
 * Al crear las BaseVar del participante al que pertenece la función se debe invocar
 * this.contruyeBasesVar
 * Al crear las BaseRest del participante al que pertenece la función se debe invocar
 * this.contruyeBasesRest
 *
 */
public class AproximadorLinealPE {
	private CompDespPE c;
	private String nombreAp; // nombre del aproximador
	private String nompar; // nombre del participante para el que se genera el aproximador
	private int n;  // cantidad de parejas de valores
	private ArrayList<Double>  vi;  // valores del argumento
	private ArrayList<Double>  fi;  // valores respectivos de la función
	private String nomVarV;  // nombre de la variable argumento
	private String nomVarF;  // nombre de la variable que sigue la función lineal por tramos
	
	private Integer entero;
	private boolean esPoste;
	private ArrayList<BaseRest>  basesRest; // lista de los BaseRest creados
	
	private static final Double cero = 0.0;
	private static final Double uno = 1.0;
	
	/**
	 * @param nombre
	 * @param vi
	 * @param fi
	 * @param nomVarV  nombre de una variable con BaseVar preexistente que será el argumento 
	 * de la función a crear, por ejemplo VOLINI.
	 * @param nomVarF  nombre de una variable con BaseVar preexistente que seguirá la función a crear,
	 * por ejemplo QVERMAX
	 */
	public AproximadorLinealPE(CompDespPE c, String nompar, String nomVarV, String nomVarF, boolean esPoste, ArrayList<Double> vi, ArrayList<Double> fi) {
		super();
		this.c = c;
		this.nompar = nompar;
		this.esPoste = esPoste;
		this.vi = vi;
		this.fi = fi;
		if(vi.size()!=fi.size()) {
			System.out.println("Error en las dimensiones de aproximador lineal");
			System.out.println(vi);
			System.out.println(fi);
		}else {
			n = vi.size();
		}
		this.nomVarV = nomVarV;
		this.nomVarF = nomVarF;
		basesRest = new ArrayList<BaseRest>();
	}






    /** 
     * Los dos métodos siguientes construyen las BaseVar y BaseRest para las restricciones para el poste (o día) del argumento entero
	 * Debe invocarse para cada poste o día
	 * 
	 * 
	 * nomVarV - [ alfa-0 * v-0 + ........alfa-n-1 * v-n-1 ] = 0	(R1)
	 * nomVarF - [ alfa-0 * f-0 + ........alfa-n-1 * f-n-1 ] = 0	(R2)
	 * 
	 * x-0, ..., x-n-1   variables binarias
	 * alfa-0,...,alfa-n-1 todas menores o iguales a 1.
	 * 
	 * suma(x-0 + ...+ x-n-1) = 2				(R3)
	 * suma(alfa-0 + ...+ alfa-n-1) = 1			(R4)
	 * 
	 * alfa-i - x-i <= 0  para i=0,...n-1			(R5-i)
	 * 
	 * Para todo i=0,..,n-2						(R6-i-j)
	 * 		para todo j = i+2,...,n-1
	 * 			x-i + x-j <= 1
	 * 
	 * 
	 * Las variables x y alfa tienen como prefijo el nombre del aproximador, 
	 * que no se escribió en las ecuaciones anteriores
	 * 
	 * 
	 */
	public void construyeBasesVar(Integer entero) {
		BaseVar bv;
		for(int i=0; i<n; i++) {
			// Construye las alfa-i
			bv = new BaseVar(nomVarF + "_alfa_" + i, nompar, entero, esPoste, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false,
					cero, null, uno, null, true);
			c.cargaBaseVar(bv, nomVarF + "_alfa_" + i, nompar , entero);
			// Construye las x-i
			bv = new BaseVar(nomVarF + "_x_" + i, nompar, entero, esPoste, Constantes.VCBINARIA, Constantes.VCPOSITIVA, false,
					null, null, null, null, true);
			c.cargaBaseVar(bv, nomVarF + "_x_" + i, nompar , entero);	
		}
	}
		
	public void construyeBasesRest(Integer entero) {
		int i;
		// Construye R1
		BaseRest br = new BaseRest("Aproximador_" + nomVarF + "_R1" , nompar, entero, 
				cero, null, null, null, entero, Constantes.RESTIGUAL);			
		for(i=0; i<n; i++) {
			br.agSumVar(nomVarF + "_alfa_" + i, nompar, entero, -fi.get(i), null);	
		}
		c.agrega1BR(CompDespPE.generaNomBRest("Aproximador_" + nomVarF + "_R1" , nompar, entero), br);	
		
		// Construye R2
		br = new BaseRest("Aproximador_" + nomVarF + "_R2" , nompar, entero, 
				cero, null, null, null, entero, Constantes.RESTIGUAL);			
		for(i=0; i<n; i++) {
			br.agSumVar(nomVarF + "_alfa_" + i, nompar, entero, -vi.get(i), null);	
		}
		c.agrega1BR(CompDespPE.generaNomBRest("Aproximador_" + nomVarF + "_R2" , nompar, entero), br);			
		
		// Construye R3
		br = new BaseRest("Aproximador_" + nomVarF + "_R3" , nompar, entero, 
				2.0, null, null, null, entero, Constantes.RESTIGUAL);			
		for(i=0; i<n; i++) {
			br.agSumVar(nomVarF + "_x_" + i, nompar, entero, 1.0, null);	
		}
		c.agrega1BR(CompDespPE.generaNomBRest("Aproximador_" + nomVarF + "_R3" , nompar, entero), br);		
		
		// Construye R4
		br = new BaseRest("Aproximador_" + nomVarF + "_R4" , nompar, entero, 
				uno, null, null, null, entero, Constantes.RESTIGUAL);			
		for(i=0; i<n; i++) {
			br.agSumVar(nomVarF + "_alfa_" + i, nompar, entero, 1.0, null);	
		}
		c.agrega1BR(CompDespPE.generaNomBRest("Aproximador_" + nomVarF + "_R4" , nompar, entero), br);	
		
		// Construye R5-i
		for(i=0; i<n; i++) {
			br = new BaseRest("Aproximador_" + nomVarF + "_R5_" + i, nompar, entero, 
					cero, null, null, null, entero, Constantes.RESTMENOROIGUAL);			
			br.agSumVar(nomVarF + "_alfa_" + i, nompar, entero, 1.0, null);	
			br.agSumVar(nomVarF + "_x_" + i, nompar, entero, -1.0, null);	
			c.agrega1BR(CompDespPE.generaNomBRest("Aproximador_" + nomVarF + "_R5_" + i, nompar, entero), br);	
		}
		
		// Construye R6-i-j
		for(i=0; i<n-1; i++) {
			for(int j=i+2; j<n; j++) {
				br = new BaseRest("Aproximador_" + nomVarF + "_R6_" + i + "_" + j, nompar, entero, 
						uno, null, null, null, entero, Constantes.RESTMENOROIGUAL);			
				br.agSumVar(nomVarF + "_x_" + i, nompar, entero, 1.0, null);	
				br.agSumVar(nomVarF + "_x_" + j, nompar, entero, -1.0, null);	
				c.agrega1BR(CompDespPE.generaNomBRest("Aproximador_" + nomVarF + "_R6_" + i + "_" + j, nompar, entero), br);									
			}
		}
	}
	
	
	
	
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}

	public ArrayList<Double> getVi() {
		return vi;
	}


	public void setVi(ArrayList<Double> vi) {
		this.vi = vi;
	}


	public Integer getEntero() {
		return entero;
	}

	public void setEntero(Integer entero) {
		this.entero = entero;
	}


	public ArrayList<Double> getFi() {
		return fi;
	}
	public void setFi(ArrayList<Double> fi) {
		this.fi = fi;
	}
	public String getNomVarV() {
		return nomVarV;
	}
	public void setNomVarV(String nomVarV) {
		this.nomVarV = nomVarV;
	}
	public String getNomVarF() {
		return nomVarF;
	}
	public void setNomVarF(String nomVarF) {
		this.nomVarF = nomVarF;
	}

	public ArrayList<BaseRest> getBasesRest() {
		return basesRest;
	}
	public void setBasesRest(ArrayList<BaseRest> basesRest) {
		this.basesRest = basesRest;
	}
	
	
	

}
