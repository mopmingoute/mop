/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPolinomio is part of MOP.
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

package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class DatosPolinomio implements Serializable{
	private static final long serialVersionUID = 1L;
	String tipo;
	double[] coefs;
	Double xmin;
	Double xmax;
	Double valmin;
	Double valmax;
	
	private ArrayList<Pair<Double,Double>> rangos;
	private ArrayList<Pair<Double,Double>> segmentos;
	private ArrayList<DatosPolinomio> polsrangos;
	private DatosPolinomio fueraRango;
	private Hashtable<String,DatosPolinomio> pols;



	private boolean editoValores;
	
	public DatosPolinomio() {
		setPols(new Hashtable<String,DatosPolinomio>());
		setRangos(new ArrayList<Pair<Double,Double>>());
	}

	public DatosPolinomio(String tipo, double[] coefs, Double xmin, Double xmax, Double valmin, Double valmax) {
		super();
		setPols(new Hashtable<String,DatosPolinomio>());
		this.tipo = tipo;
		this.coefs = coefs;
		this.xmin = xmin;
		this.xmax = xmax;
		this.valmin = valmin;
		this.valmax = valmax;
	}
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public double[] getCoefs() {
		return coefs;
	}
	public void setCoefs(double[] coefs) {
		this.coefs = coefs;
	}
	public Double getXmin() {
		return xmin;
	}
	public void setXmin(Double xmin) {
		this.xmin = xmin;
	}
	public Double getXmax() {
		return xmax;
	}
	public void setXmax(Double xmax) {
		this.xmax = xmax;
	}
	public Double getValmin() {
		return valmin;
	}
	public void setValmin(Double valmin) {
		this.valmin = valmin;
	}
	public Double getValmax() {
		return valmax;
	}
	public void setValmax(Double valmax) {
		this.valmax = valmax;
	}
	public Hashtable<String,DatosPolinomio> getPols() {
		return pols;
	}
	public void setPols(Hashtable<String,DatosPolinomio> pols) {
		this.pols = pols;
	}
	
	public ArrayList<Pair<Double, Double>> getRangos() {
		return rangos;
	}
	public void setRangos(ArrayList<Pair<Double, Double>> rangos) {
		this.rangos = rangos;
	}
	public ArrayList<DatosPolinomio> getPolsrangos() {
		return polsrangos;
	}
	public void setPolsrangos(ArrayList<DatosPolinomio> polsrangos) {
		this.polsrangos = polsrangos;
	}
	public DatosPolinomio getFueraRango() {
		return fueraRango;
	}
	public void setFueraRango(DatosPolinomio fueraRango) {
		this.fueraRango = fueraRango;
	}
	public boolean isEditoValores() {		return editoValores;	}
	public void setEditoValores(boolean editoValores) {	this.editoValores = editoValores;	}





	@Override
	public String toString() {
		String resultado = "<funcion tipo="+"\""+tipo+"\">\n";

		if (tipo.equalsIgnoreCase("poliConCotas")) {
			resultado+="\t<xmin>"+this.getXmin().toString()+"</xmin>\n";
			resultado+="\t<xmax>"+this.getXmax().toString()+"</xmax>\n";
			resultado+="\t<valmin>"+this.getValmin().toString()+"</valmin>\n";
			resultado+="\t<valmax>"+this.getValmax().toString()+"</valmax>\n";
			String coef = Arrays.toString(this.getCoefs());
			resultado+="\t<coefs>"+coef.substring(1, coef.length()-1)+"</coefs>\n";

		} else if (tipo.equalsIgnoreCase("poli")) {
			String coef = Arrays.toString(this.getCoefs());
			resultado+="\t<coefs>"+coef.substring(1, coef.length()-1)+"</coefs>\n";

		} else if (tipo.equalsIgnoreCase("poliMulti")) {


			String erogado = "";
			if(this.pols.get("QErogado") != null) {
				erogado = this.pols.get("QErogado").toString();
			}
			erogado = erogado.replaceAll("\n", "");
			if(erogado.length() > 21){
			resultado += "\t\t\t" + erogado.substring(0,20) + " var=\"QErogado\" >"+ erogado.substring(21) + "\n"; }

			String cotaAA = "";
			if(this.pols.get("CotaAguasAbajo") != null) {
				cotaAA = this.pols.get("CotaAguasAbajo").toString();
			}
			if(cotaAA.length() > 21){
			cotaAA = cotaAA.replaceAll("\n", "");
			resultado += "\t\t\t" + cotaAA.substring(0,20) + " var=\"CotaAguasAbajo\" >"+ cotaAA.substring(21) + "\n\t\t";    }


		} else if (tipo.equalsIgnoreCase("porRangos")) {
			resultado += "\t<fueraRango>\n \t\t" + this.fueraRango.toString() + "</fueraRango> \n";
			resultado += "\t<rangos>" + this.rangos.toString() + "</rangos> \n";
			for (DatosPolinomio p : this.polsrangos){
					resultado += "\t\t" + p.toString();
			}
		}
		
		resultado += "</funcion> \n";
		return resultado;
		
	}

	public Element toXML(Document doc, String var){
//		cotaAguasArribaGenAguasAbajo
		Element res = doc.createElement("funcion");
		if (tipo.equalsIgnoreCase("poliConCotas")) {
			res.setAttribute("tipo", "poliConCotas");
			Element xmin = doc.createElement("xmin");
			xmin.appendChild(doc.createTextNode(getXmin().toString()));
			res.appendChild(xmin);
			Element xmax = doc.createElement("xmax");
			xmax.appendChild(doc.createTextNode(getXmax().toString()));
			res.appendChild(xmax);
			Element valmin = doc.createElement("valmin");
			valmin.appendChild(doc.createTextNode(getValmin().toString()));
			res.appendChild(valmin);
			Element valmax = doc.createElement("valmax");
			valmax.appendChild(doc.createTextNode(getValmax().toString()));
			res.appendChild(valmax);
			String coef = Arrays.toString(this.getCoefs());
			Element coefs = doc.createElement("coefs");
			coefs.appendChild(doc.createTextNode(coef.substring(1, coef.length()-1)));
			res.appendChild(coefs);
		} else if (tipo.equalsIgnoreCase("poli")) {
			res.setAttribute("tipo", "poli");
			if(var != null){
				res.setAttribute("var", var);
			}
			String coef = Arrays.toString(this.getCoefs());
			res.appendChild(doc.createTextNode(coef.substring(1, coef.length()-1)));
		} else if (tipo.equalsIgnoreCase("poliMulti")) {
			res.setAttribute("tipo", "poliMulti");
			for(String varNom : getPols().keySet()){
				res.appendChild(getPols().get(varNom).toXML(doc, varNom));
			}
		} else if (tipo.equalsIgnoreCase("porRangos")) {
			res.setAttribute("tipo", "porRangos");
			//fueraRango
			Element fueraRangoElem = doc.createElement("fueraRango");
			fueraRangoElem.appendChild(fueraRango.toXML(doc, null));
			res.appendChild(fueraRangoElem);
			//rangos
			Element rangosElem = doc.createElement("rangos");
			StringBuilder rangosStr = new StringBuilder();
			for(Pair<Double,Double> par : rangos){
				rangosStr.append("(").append(par.first).append(";").append(par.second).append(")").append(",");
			}
			rangosElem.appendChild(doc.createTextNode(rangosStr.toString().substring(0,rangosStr.toString().length()-1)));
			res.appendChild(rangosElem);
			//funcs
			for(DatosPolinomio dP : polsrangos){
				res.appendChild(dP.toXML(doc, null));
			}

		}
		return res;
	}

	public ArrayList<Pair<Double,Double>> getSegmentos() {
		return segmentos;
	}

	public void setSegmentos(ArrayList<Pair<Double,Double>> segmentos) {
		this.segmentos = segmentos;
	}


	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(tipo.trim().equals("")) { errores.add(" Tipo vacio"); }
		else {
			if (tipo.equalsIgnoreCase("poliConCotas")) {
				if( this.getXmin() == null) {errores.add(" Xmin vacio"); }
				if( this.getXmax() == null) {errores.add(" Xmax vacio"); }
				if( this.getValmin() == null) {errores.add(" Valmin vacio"); }
				if( this.getValmax() == null) {errores.add(" Valmax vacio"); }
				if( this.getCoefs() == null) {errores.add(" Coef vacio"); }


			} else if (tipo.equalsIgnoreCase("poli")) {
				if( this.getCoefs() == null) {errores.add(" Coef vacio"); }

			} else if (tipo.equalsIgnoreCase("poliMulti")) {

				if( this.pols.get("QErogado").controlDatosCompletos().size() >0 ) {errores.add(" QErogado vacio"); }
				if( this.pols.get("CotaAguasAbajo").controlDatosCompletos().size() >0 ) {errores.add(" CotaAguasAbajo vacio"); }

			} else if (tipo.equalsIgnoreCase("porRangos")) {
				if( this.fueraRango.controlDatosCompletos().size() >0 ) {errores.add(" FueraRango vacio"); }
				rangos.forEach((r)-> {
					if(r.first == null) {errores.add(" Rango vacio"); }
					if(r.second == null) {errores.add(" Rango vacio"); }
				});

				polsrangos.forEach((r)-> {
					if(r.controlDatosCompletos().size() >0) {errores.add(" Polinomio Rango vacio"); }

				});
			}
		}
		return errores;
	}
	
}

