/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosHidroCP is part of MOP.
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

package cp_datatypesEntradas;

public class DatosHidroCP extends DatosPartCP{
	
	private String nomVAAp;   // nombre de la variable aleatoria de aportes en el grafo de escenarios
	private double[] qeroAbaH; // el índice i contiene el caudal erogado por la central i+1 horas anteriores al poste inicial del horizonte, en la central en m3/s.
	private double[] qeroAbaP; // el índice i contiene el caudal erogado por la central i+1 postes anteriores al poste inicial del horizonte, en la central en m3/s.
	private double cotaIni; // cota en metros al inicio del horizonte de estudio de CP
	private double valAgua; // valor del agua al final del horizonte. Se emplea si el contructor de hiperplanos tiene USO = NO
	private String modoPot;  // modo de calcular la potencia. COEF_FIJO se toma un coeficiente constante ; PQ se toman las funciones potencia caudal del MOP.
	private String modoVerMax; // modo de calcular el vertimiento máximo.  FIJO_COTA se toma el vertimiento para cota la fija inicial, AL se toma una aproximación lineal por tramos
	
	public DatosHidroCP(String nombrePart, String tipoPart, String nomVAAp, double[] qeroAbaH, double cotaIni, double valAgua, String modoPot, String modoVerMax) {
		super(nombrePart, tipoPart);
		this.nomVAAp = nomVAAp;
		this.qeroAbaH = qeroAbaH;
		this.cotaIni = cotaIni;
		this.valAgua = valAgua;
		this.modoPot = modoPot;
		this.modoVerMax = modoVerMax;
	}
	
	
	public double[] getQeroAbaH() {
		return qeroAbaH;
	}


	public void setQeroAbaH(double[] qeroAbaH) {
		this.qeroAbaH = qeroAbaH;
	}


	public double getCotaIni() {
		return cotaIni;
	}
	
	public void setCotaIni(double cotaIni) {
		this.cotaIni = cotaIni;
	}
		

	public double getValAgua() {
		return valAgua;
	}


	public void setValAgua(double valAgua) {
		this.valAgua = valAgua;
	}


	public String getNomVAAp() {
		return nomVAAp;
	}

	public void setNomVAAp(String nomVAAp) {
		this.nomVAAp = nomVAAp;
	}


	public double[] getQeroAbaP() {
		return qeroAbaP;
	}


	public void setQeroAbaP(double[] qeroAbaP) {
		this.qeroAbaP = qeroAbaP;
	}


	public String getModoPot() {
		return modoPot;
	}


	public void setModoPot(String modoPot) {
		this.modoPot = modoPot;
	}


	public String getModoVerMax() {
		return modoVerMax;
	}


	public void setModoVerMax(String modoVerMax) {
		this.modoVerMax = modoVerMax;
	}




	


}
