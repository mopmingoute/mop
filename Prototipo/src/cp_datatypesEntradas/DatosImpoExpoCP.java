/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosImpoExpoCP is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;

public class DatosImpoExpoCP extends DatosPartCP{
	

	/**
	 *  Nombres de las ofertas 
	 */
	private ArrayList<String> listaOf; 

	/**
	 * Atributos generales de todas las ofertas
	 */
	private Hashtable<String, String> operacion; // valor:COMPRA O VENTA
	
	private Hashtable<String, String> tipoOf; // valor: tipo de oferta según constantes en la clase ConCP: BASE, POTMEDIA, ALEAT, etc.
	
	/**
	 * clave: nombre de la oferta
	 * valor: atributo de ofertas no aleatorias
	 */	
	
	private Hashtable<String, Boolean> yaConv; // true si la oferta ya está convocada
	
	private Hashtable<String, Double> precio; // valor: precio de la energia en USD/MWh
	
	private Hashtable<String, Double> potMax; // valor: potencia máxima
	
	private Hashtable<String, Double> potMed; // valor: potencia media mínima requerida
	
	private Hashtable<String, Double> multa;  // si es mayor que 0.0 las restricciones duras de potencia media mínima y despacho en la base se eliminan y aparece una multa por incumplimiento
	
	private Hashtable<String, Boolean> hayMulta; // si es true no hay restricción dura de potencia media sino una multa en el objetivo
	
	private Hashtable<String, Integer> durD; // duración en días
	
	private Hashtable<String, Integer> anticipH; // anticipación en horas con que se debe pedir la oferta respecto al primer día de validez
	
	private Hashtable<String, Integer> anticipP; // anticipación en postes
	
	private Hashtable<String, Integer> primD; // primer dia en que puede entrar
	
	private Hashtable<String, Integer> ultD; // ultimo dia en que puede entrar
		
	private Hashtable<String, Integer> diasYa; // días ya transcurridos de la oferta
	
	private Hashtable<String, Double> enerYaGWh; // valor: energía ya comprada en GWh
	
	/**
	 * clave: nombre de la oferta
	 * valor: atributo de ofertas aleatorias
	 */	
	private Hashtable<String, String> nomVAPot; // valor: nombre de la VA de potencia disponible en el grafo de escenarios
	
	private Hashtable<String, String> nomVAPre; // valor: nombre de la VA de precio de compra o venta en el grafo de escenarios
	
	public DatosImpoExpoCP(String nomP, String tipoP, ArrayList<String> listaOf, Hashtable<String, String> operacion, Hashtable<String, String> tipoOf,
			Hashtable<String, Boolean> yaConv, Hashtable<String, Double> precio, Hashtable<String, Double> potMax, Hashtable<String, Double> potMed, Hashtable<String, Double> multa,
			Hashtable<String, Integer> durD, Hashtable<String, Integer> anticipH, Hashtable<String, Integer> primerDia, Hashtable<String, Integer> ultimoDia, Hashtable<String, Integer> diasYa,
			Hashtable<String, Double> enerYaGWh, Hashtable<String, String> nomVAPot, Hashtable<String, String> nomVAPre) {
		
		super(nomP, tipoP);

		this.listaOf = listaOf;
		this.operacion = operacion;
		this.tipoOf = tipoOf;
		this.yaConv = yaConv;
		this.precio = precio;
		this.potMax = potMax;
		this.potMed = potMed;
		this.multa = multa;
		Hashtable<String, Boolean> hayMulta = new Hashtable<String, Boolean>(); 
		for(String s: multa.keySet()) {
			if(multa.get(s) >0.0) {
				hayMulta.put(s,true);
			}else {
				hayMulta.put(s,false);
			}
		}
		this.hayMulta = hayMulta;
		this.durD = durD;
		this.anticipH = anticipH;
		this.primD = primerDia;
		this.ultD = ultimoDia;
		this.diasYa = diasYa;
		this.enerYaGWh = enerYaGWh;
		
		this.nomVAPot = nomVAPot;
		this.nomVAPre = nomVAPre;
	}
	
	
	

	public ArrayList<String> getListaOf() {
		return listaOf;
	}

	public void setListaOf(ArrayList<String> listaOf) {
		this.listaOf = listaOf;
	}

	public Hashtable<String, String> getTipoOf() {
		return tipoOf;
	}

	public void setTipoOf(Hashtable<String, String> tipoOf) {
		this.tipoOf = tipoOf;
	}

	public Hashtable<String, Double> getPrecio() {
		return precio;
	}

	public void setPrecio(Hashtable<String, Double> precio) {
		this.precio = precio;
	}

	public Hashtable<String, Double> getPotMax() {
		return potMax;
	}

	public void setPotMax(Hashtable<String, Double> potMax) {
		this.potMax = potMax;
	}

	public Hashtable<String, Double> getPotMed() {
		return potMed;
	}

	public void setPotMed(Hashtable<String, Double> potMed) {
		this.potMed = potMed;
	}

	public Hashtable<String, Double> getMulta() {
		return multa;
	}


	public void setMulta(Hashtable<String, Double> multa) {
		this.multa = multa;
	}


	public Hashtable<String, Boolean> getYaConv() {
		return yaConv;
	}

	public void setYaConv(Hashtable<String, Boolean> yaConv) {
		this.yaConv = yaConv;
	}

	public Hashtable<String, Integer> getDurD() {
		return durD;
	}

	public void setDurD(Hashtable<String, Integer> durD) {
		this.durD = durD;
	}

	public Hashtable<String, Integer> getPrimD() {
		return primD;
	}

	public void setPrimD(Hashtable<String, Integer> primD) {
		this.primD = primD;
	}

	public Hashtable<String, Integer> getUltD() {
		return ultD;
	}

	public void setUltD(Hashtable<String, Integer> ultD) {
		this.ultD = ultD;
	}

	public Hashtable<String, Integer> getDiasYa() {
		return diasYa;
	}

	public void setDiasYa(Hashtable<String, Integer> diasYa) {
		this.diasYa = diasYa;
	}

	public Hashtable<String, Double> getEnerYaGWh() {
		return enerYaGWh;
	}

	public void setEnerYaGWh(Hashtable<String, Double> enerYaGWh) {
		this.enerYaGWh = enerYaGWh;
	}

	public Hashtable<String, Integer> getAnticipH() {
		return anticipH;
	}

	public void setAnticipH(Hashtable<String, Integer> anticipH) {
		this.anticipH = anticipH;
	}

	public Hashtable<String, Integer> getAnticipP() {
		return anticipP;
	}

	public void setAnticipP(Hashtable<String, Integer> anticipP) {
		this.anticipP = anticipP;
	}




	public Hashtable<String, String> getOperacion() {
		return operacion;
	}




	public void setOperacion(Hashtable<String, String> operacion) {
		this.operacion = operacion;
	}




	public Hashtable<String, String> getNomVAPot() {
		return nomVAPot;
	}




	public void setNomVAPot(Hashtable<String, String> nomVAPot) {
		this.nomVAPot = nomVAPot;
	}




	public Hashtable<String, String> getNomVAPre() {
		return nomVAPre;
	}




	public void setNomVAPre(Hashtable<String, String> nomVAPre) {
		this.nomVAPre = nomVAPre;
	}




	public Hashtable<String, Boolean> getHayMulta() {
		return hayMulta;
	}




	public void setHayMulta(Hashtable<String, Boolean> hayMulta) {
		this.hayMulta = hayMulta;
	}

	

		
	
	

}
