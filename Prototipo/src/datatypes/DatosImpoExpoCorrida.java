/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosImpoExpoCorrida is part of MOP.
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
import logica.CorridaHandler;
import tiempo.Evolucion;
import utilitarios.Constantes;

public class DatosImpoExpoCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;									/**Nombre del generador eólico*/
	private String propietario;
	private String barra;
	private String pais;  
	private String tipoImpoExpo;
	private String operacionCompraVenta;
	private Evolucion<Double> costoFijo;
	private int cantBloques; 
	private boolean salDetallada;
	/**
	 * Atributos si tipoImpoExpo = IEEVOL
	 * primer óndice bloque
	 * segundo óndice poste; solo hay mós de un valor si hay valpostización externa
	 * La estacionalidad estó resuelta por la Evolucion
	 * Este tipo cubre las funciones del tipo ESTAC del EDF
	 */
	private ArrayList<Evolucion<ArrayList<Double>>>  potEvol;   // evoluciones de potencias
	private ArrayList<Evolucion<ArrayList<Double>>>  preEvol;   // evoluciones de precios
	private ArrayList<Evolucion<ArrayList<Double>>>  dispEvol;   // evoluciones de precios
	
	/** 
	 * Atributos si tipoImpoExpo = IEALEATPRPOT
	 * Se definen la potencia y el precio mediante variables aleatorias. La disponibilidad queda tenida en cuenta en la potencia.
	 *	El óndice de los ArrayList es en los bloques.
	 *	Para cada una de estas variables se hace el mismo tratamiento respecto a la postización interna o externa que para la demanda o el eólico y solar.
	 *  Si la postización es interna, se toma la realización de la VA en los instantes de muestreo.
	 *  Si la postización es externa, se espera que con el nombre de la respectiva VA y el sufijo nómero de poste, existan variables aleatorias en el proceso proc, una para cada poste.
	 * Con esto se puede hacer lo que hace el tipo FIJO del EDF.	
	 */ 	
	private ArrayList<DatosVariableAleatoria> datPrecio;
	private ArrayList<DatosVariableAleatoria> datPotencia;
	
	/**
	 * Atributos si tipoImpoExpo = IEALEATFORMUL
	 * Hay una variable aleatoria vAleat.
	 * Si la postización es interna, para cada bloque hay Polinomios que a partir del valor de la VA
	 * proporcionan la potencia y el precio instante a instante. La variable aleatoria deberóa ser el costo marginal o precio del paós.
	 * 
	 * Si la postización es externa para cada bloque y cada poste externo hay una variable aleatoria
	 * con cuyo nombre es nombreVA + i, donde i es el poste externo a partir de cero, igual que en la postización
	 * externa de la demanda o el eólico y solar. La variable aleatoria es directamente el 
	 */	
	private ArrayList<Evolucion <DatosPolinomio>> poliPot;
	private ArrayList<Evolucion <DatosPolinomio>> poliPre;
	private ArrayList<Evolucion <DatosPolinomio>> poliDisp;
	private DatosVariableAleatoria datCMg;
	private Evolucion<Double> factorEscalamiento;
	private DatosVariableAleatoria datUniforme;
	private boolean hayMinTec;
	private Evolucion<Double> minTec;
	private Evolucion<Integer> cantModInst;
	
	public DatosImpoExpoCorrida(String nombre, String propietario, Evolucion<Integer> cantModInst, String barra, String pais, String tipoImpoExpo, String opCompVent, Evolucion<Double> costoFijo, int cantBloques, boolean salDet,
			boolean hayMinTec, Evolucion<Double> minTec, DatosVariableAleatoria datCMg, Evolucion<Double> factorEscalamiento, DatosVariableAleatoria datUniforme ){
		this.nombre = nombre;
		this.propietario = propietario;
		this.setCantModInst(cantModInst);
		this.barra = barra;
		this.pais = pais;
		this.tipoImpoExpo = tipoImpoExpo;
		this.operacionCompraVenta = opCompVent;
		this.costoFijo = costoFijo;
		this.cantBloques = cantBloques;
		this.salDetallada = salDet;
		this.potEvol = new ArrayList<Evolucion<ArrayList<Double>>>();
		this.preEvol = new ArrayList<Evolucion<ArrayList<Double>>>();
		this.dispEvol = new ArrayList<Evolucion<ArrayList<Double>>>();
		this.datPrecio = new ArrayList<DatosVariableAleatoria>();
		this.datPotencia = new ArrayList<DatosVariableAleatoria>();
		this.poliPot = new ArrayList<Evolucion<DatosPolinomio>>();
		this.poliPre = new ArrayList<Evolucion<DatosPolinomio>>();
		this.poliDisp = new ArrayList<Evolucion<DatosPolinomio>>();
		this.datCMg = datCMg;
		this.factorEscalamiento = factorEscalamiento;
		this.datUniforme= datUniforme;
		this.hayMinTec = hayMinTec;
		this.minTec = minTec;
			
		
	}
	
    public void cargarBloque(DatosVariableAleatoria datPre, DatosVariableAleatoria datPot, Evolucion<DatosPolinomio> poliPre, Evolucion<DatosPolinomio> poliPot, Evolucion<DatosPolinomio> poliDisp,
    		Evolucion<ArrayList<Double>> potEv, Evolucion<ArrayList<Double>> preEv, Evolucion<ArrayList<Double>> dispEv ){
    	this.datPrecio.add(datPre);
    	this.datPotencia.add(datPot);
    	this.poliPre.add(poliPre);
    	this.poliPot.add(poliPot);
    	this.poliDisp.add(poliDisp);
    	this.potEvol.add(potEv);
    	this.preEvol.add(preEv);
    	this.dispEvol.add(dispEv);
    }
	
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getBarra() {
		return barra;
	}
	public void setBarra(String barra) {
		this.barra = barra;
	}
	public String getPais() {
		return pais;
	}
	public void setPais(String pais) {
		this.pais = pais;
	}
	public String getTipoImpoExpo() {
		return tipoImpoExpo;
	}
	public void setTipoImpoExpo(String tipoImpoExpo) {
		this.tipoImpoExpo = tipoImpoExpo;
	}
	public String getOperacionCompraVenta() {
		return operacionCompraVenta;
	}
	public void setOperacionCompraVenta(String operacionCompraVenta) {
		this.operacionCompraVenta = operacionCompraVenta;
	}
	public int getCantBloques() {
		return cantBloques;
	}
	public void setCantBloques(int cantBloques) {
		this.cantBloques = cantBloques;
	}

	public ArrayList<Evolucion<ArrayList<Double>>> getPotEvol() {
		return potEvol;
	}

	public void setPotEvol(ArrayList<Evolucion<ArrayList<Double>>> potEvol) {
		this.potEvol = potEvol;
	}

	public ArrayList<Evolucion<ArrayList<Double>>> getPreEvol() {
		return preEvol;
	}

	public void setPreEvol(ArrayList<Evolucion<ArrayList<Double>>> preEvol) {
		this.preEvol = preEvol;
	}

	public ArrayList<Evolucion<ArrayList<Double>>> getDispEvol() {
		return dispEvol;
	}

	public void setDispEvol(ArrayList<Evolucion<ArrayList<Double>>> dispEvol) {
		this.dispEvol = dispEvol;
	}

	public ArrayList<DatosVariableAleatoria> getDatPrecio() {
		return datPrecio;
	}
	public void setDatPrecio(ArrayList<DatosVariableAleatoria> datPrecio) {
		this.datPrecio = datPrecio;
	}
	public ArrayList<DatosVariableAleatoria> getDatPotencia() {
		return datPotencia;
	}
	public void setDatPotencia(ArrayList<DatosVariableAleatoria> datPotencia) {
		this.datPotencia = datPotencia;
	}
	public DatosVariableAleatoria getDatCMg() {
		return datCMg;
	}
	public void setDatCMg(DatosVariableAleatoria datCMg) {
		this.datCMg = datCMg;
	}
	
	

	public Evolucion<Double> getFactorEscalamiento() {
		return factorEscalamiento;
	}

	public void setFactorEscalamiento(Evolucion<Double> factorEscalamiento) {
		this.factorEscalamiento = factorEscalamiento;
	}

	public ArrayList<Evolucion<DatosPolinomio>> getPoliPot() {
		return poliPot;
	}

	public void setPoliPot(ArrayList<Evolucion<DatosPolinomio>> poliPot) {
		this.poliPot = poliPot;
	}

	public ArrayList<Evolucion<DatosPolinomio>> getPoliPre() {
		return poliPre;
	}

	public void setPoliPre(ArrayList<Evolucion<DatosPolinomio>> poliPre) {
		this.poliPre = poliPre;
	}

	public ArrayList<Evolucion<DatosPolinomio>> getPoliDisp() {
		return poliDisp;
	}

	public void setPoliDisp(ArrayList<Evolucion<DatosPolinomio>> poliDisp) {
		this.poliDisp = poliDisp;
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public DatosVariableAleatoria getDatUniforme() {
		return datUniforme;
	}

	public void setDatUniforme(DatosVariableAleatoria datUniforme) {
		this.datUniforme = datUniforme;
	}

	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}

	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}

	public boolean isHayMinTec() {
		return hayMinTec;
	}

	public void setHayMinTec(boolean hayMinTec) {
		this.hayMinTec = hayMinTec;
	}

	public Evolucion<Double> getMinTec() {
		return minTec;
	}

	public void setMinTec(Evolucion<Double> minTec) {
		this.minTec = minTec;
	}

	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}

	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}		
	
	

	public ArrayList<String> controlDatosCompletos() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) errores.add("ImpoExpo: Nombre vacío.");
		if(barra == null) errores.add("ImpoExpo: Barra vacío.");
		if(pais.trim().equals("")) errores.add("ImpoExpo: pais vacío.");
		if(tipoImpoExpo.trim().equals("")) errores.add("ImpoExpo: tipoImpoExpo vacío.");
		if(operacionCompraVenta.trim().equals("")) errores.add("ImpoExpo: operacionCompraVenta vacío.");
		if(cantModInst == null ) errores.add("ImpoExpo: cantModInst vacío.");
		if(costoFijo == null ) errores.add("ImpoExpo: costoFijo vacío.");
		if(isHayMinTec()){
			if( minTec == null) { errores.add("ImpoExpo: Mínimo técnico es vacío"); }
		}
		if(cantBloques == 0) errores.add("ImpoExpo: cantBloques vacío.");

		if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {
			if(datCMg.controlDatosCompletos().size()>0) { errores.add("ImpoExpo: Cmg vacío."); }
			if (factorEscalamiento == null){ errores.add("ImpoExpo: factor escalamiento vacío."); }
			else if (factorEscalamiento.controlDatosCompletos().size() > 0 ) { errores.add("ImpoExpo: factor escalamiento vacío."); }
		}

		/*if(datUniforme != null)  { errores.add("ImpoExpo: dat Uniforme vacío."); }
		else if (datUniforme.controlDatosCompletos().size() > 0 ) { errores.add("ImpoExpo: dat Uniforme vacío."); }*/

		if(tipoImpoExpo.equals(Constantes.IEALEATPRPOT)) {

			if(datPrecio.size() != cantBloques) { errores.add("Bloque ImpoExpo: datPrecio vacío."); }
			else{
				datPrecio.forEach((n) -> { if(n == null || n.controlDatosCompletos().size()>0) errores.add("ImpoExpo: datPrecio vacío."); }  );
			}
			if(datPotencia.size() != cantBloques) { errores.add("Bloque ImpoExpo: datPotencia vacío."); }
			else{
				datPotencia.forEach((n) -> { if(n == null || n.controlDatosCompletos().size()>0) errores.add("ImpoExpo: datPotencia vacío."); }  );
			}
		}
		if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {


			if(poliPot.size() != cantBloques) { errores.add("Bloque ImpoExpo: poliPot vacío."); }
			else{
				poliPot.forEach((n) -> { if(n == null || n.getValor(instanteActual).controlDatosCompletos().size() >0 ) errores.add("ImpoExpo: poliPot vacío."); }  );
			}

			if(poliPre.size() != cantBloques) { errores.add("Bloque ImpoExpo: poliPre vacío."); }
			else{
				poliPre.forEach((n) -> { if(n == null || n.getValor(instanteActual).controlDatosCompletos().size() >0 ) errores.add("ImpoExpo: poliPre vacío."); }  );
			}

			if(poliDisp.size() != cantBloques) { errores.add("Bloque ImpoExpo: poliDisp vacío."); }
			else{
				poliDisp.forEach((n) -> { if(n == null || n.getValor(instanteActual).controlDatosCompletos().size() >0 ) errores.add("ImpoExpo: poliDisp vacío."); }  );
			}

		}
		if(tipoImpoExpo.equals(Constantes.IEEVOL)) {

			if(potEvol.size() != cantBloques) { errores.add("Bloque ImpoExpo: potEvol vacío."); }
			else{
				potEvol.forEach((n) -> { if(n == null || n.controlDatosCompletos().size() > 0 ) errores.add("ImpoExpo: potEvol vacío."); }  );
			}

			if(preEvol.size() != cantBloques) { errores.add("Bloque ImpoExpo: preEvol vacío."); }
			else{
				preEvol.forEach((n) -> { if(n == null || n.controlDatosCompletos().size() > 0 ) errores.add("ImpoExpo: preEvol vacío."); }  );
			}

			if(dispEvol.size() != cantBloques) { errores.add("Bloque ImpoExpo: dispEvol vacío."); }
			else{
				dispEvol.forEach((n) -> { if(n == null || n.controlDatosCompletos().size() > 0 ) errores.add("ImpoExpo: dispEvol vacío."); }  );
			}

		}

		return errores;
	}
}
