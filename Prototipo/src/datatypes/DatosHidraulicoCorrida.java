/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosHidraulicoCorrida is part of MOP.
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
import java.util.Hashtable;

import logica.CorridaHandler;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.Recta;


/**
 * Datatype que representa los datos asociados a un generador hidróulico
 * @author ut602614
 *
 */
public class DatosHidraulicoCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;
	private String propietario;
	private String barra;
	private String rutaPQ;
	private Hashtable<Pair<Double,Double> , ArrayList<Recta>> funcionesPQ;  // Para una cota del Lago y una cota del Lago AAbajo hay un conjunto de rectas
	private Evolucion<Integer> cantModInst; 
	private Evolucion<Double> factorCompartir;
	private ArrayList<String> hidraulicosAguasArriba;
	private String hidraulicoAguasAbajo;
	private Evolucion<Double> potMin;
	private Evolucion<Double> potMax;
	private Evolucion<Double> rendPotMin;
	private Evolucion<Double> rendPotMax;
	private DatosPolinomio fRendimiento;
	private Evolucion<Double> qTur1Max;
	private DatosVariableAleatoria aporte; 
	private Integer cantModIni;
	private Evolucion<Double> volFijo;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private Evolucion<DatosPolinomio> fCoefEnerg;
	private Evolucion<DatosPolinomio> fCoAA;	
	private Double saltoMin;
	private Double cotaInundacionAguasAbajo;
	private Double cotaInundacionAguasArriba;
	private Evolucion<DatosPolinomio> fQEroMin;
	private Evolucion<DatosPolinomio> fCoVo;
	private Evolucion<DatosPolinomio> fVoCo;
	private Evolucion<DatosPolinomio> fEvaporacion;
	private Evolucion<Double> coefEvaporacion;
	private Evolucion<DatosPolinomio> fFiltracion;
	private Evolucion<DatosPolinomio> fQVerM;	
	private Hashtable<String,Evolucion<String>> valoresComportamientos;
	private Hashtable<String,DatosVariableEstado> varsEstado;
	private Double epsilonCaudalErogadoIteracion;
	private boolean salDetallada;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;
	private Evolucion<Double> costoVariable;	
	private Evolucion<Double> volReservaEstrategica; // volumen en hm3 debajo del cual se revaloriza el agua del embalse
	private Evolucion<Double> valorMinReserva; // valor en USD/hm3 mónimo por debajo del volumen anterior. Se aplica como cota inferior al valor del agua
	private boolean valorAplicaOptim; // si es true se usa tamnbión en la optimización, de lo contrario solo en simulación.
	private boolean hayReservaEstrategica; // 
	private boolean vertimientoConstante; // en el modelo PQ se condiciona el vertimiento constante en el paso (true)
	private boolean hayVolObjVert;
	private Evolucion<Double> volObjVert;
	private boolean hayControldeCotasMinimas;
	private Evolucion<Double> volumenControlMinimo;
	private Evolucion<Double> penalidadControlMinimo;
	private boolean hayControldeCotasMaximas;
	private Evolucion<Double> volumenControlMaximo;
	private Evolucion<Double> penalidadControlMaximo; 
	
	
	public DatosHidraulicoCorrida(String nombre, String propietario, String barra, String rutaPQ,
			Evolucion<Integer> cantModInst, Evolucion<Double> factorCompartir,
			ArrayList<String> hidraulicosAguasArriba,
			String hidraulicoAguasAbajo, Evolucion<Double> potMin,
			Evolucion<Double> potMax, Evolucion<Double> rendPotMin,
			Evolucion<Double> rendPotMax,
			Evolucion<Double> volFijo, Evolucion<Double> qTur1Max,
			DatosVariableAleatoria aporte, Integer cantModIni,
			Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo, 
			Evolucion<DatosPolinomio> fCoefEnerg,Evolucion<DatosPolinomio> fCoAA,
			Double saltoMin, Double cotaInundacionAguasAbajo,Double cotaInundacionAguasArriba,
			Evolucion<DatosPolinomio> fQEroMin,
			Evolucion<DatosPolinomio> fCoVo, Evolucion<DatosPolinomio> fVoCo,
			Evolucion<DatosPolinomio> fEvaporacion,Evolucion<Double> coefEvaporacion,
			Evolucion<DatosPolinomio> fFiltracion, Evolucion<DatosPolinomio> fQVerM,
			Hashtable<String, DatosVariableEstado> varsEstado, Double epsilonCaudalErogadoIteracion,
			boolean salDetallada, Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo, Evolucion<Double> costoVariable,
			Evolucion<Double> volReservaEstrategica, Evolucion<Double> valorMinReserva, boolean valorAplicaOptim,
			boolean hayReservaEstrategica, boolean vertimientoConstante, 
			boolean hayVolObjVert, Evolucion<Double> volObjVert, boolean hayControldeCotasMinimas, Evolucion<Double> volumenControlMinimo, Evolucion<Double> penalidadControlMinimo, 
			boolean hayControldeCotasMaximas, Evolucion<Double> volumenControlMaximo, Evolucion<Double> penalidadControlMaximo ) {
		super();
		this.nombre = nombre;
		this.propietario = propietario;
		this.barra = barra;
		this.rutaPQ = rutaPQ;
		this.cantModInst = cantModInst;
		this.factorCompartir = factorCompartir;
		this.hidraulicosAguasArriba = hidraulicosAguasArriba;
		this.hidraulicoAguasAbajo = hidraulicoAguasAbajo;
		this.potMin = potMin;
		this.potMax = potMax;
		this.rendPotMin = rendPotMin;
		this.rendPotMax = rendPotMax;
		this.volFijo = volFijo;
		this.qTur1Max = qTur1Max;
		this.aporte = aporte;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.fCoefEnerg = fCoefEnerg;
		this.setfCoAA(fCoAA);
		this.setSaltoMin(saltoMin);
		this.setCotaInundacionAguasAbajo(cotaInundacionAguasAbajo);
		this.setCotaInundacionAguasArriba(cotaInundacionAguasArriba);
		this.fQEroMin = fQEroMin;
		this.fCoVo = fCoVo;
		this.fVoCo = fVoCo;
		this.fEvaporacion = fEvaporacion;
		this.coefEvaporacion = coefEvaporacion;
		this.fFiltracion = fFiltracion;
		this.setfQVerM(fQVerM);
		this.valoresComportamientos = new Hashtable<String, Evolucion<String>>();
		this.varsEstado = varsEstado;
		this.setEpsilonCaudalErogadoIteracion(epsilonCaudalErogadoIteracion);
		this.setSalDetallada(salDetallada);
		this.setMantProgramado(mantProgramado);
		this.costoFijo = costoFijo;
		this.costoVariable = costoVariable;
		this.funcionesPQ = new Hashtable<Pair<Double,Double>, ArrayList<Recta>>();
		this.volReservaEstrategica = volReservaEstrategica;
		this.valorMinReserva =valorMinReserva;
		this.valorAplicaOptim = valorAplicaOptim;
		this.hayReservaEstrategica = hayReservaEstrategica;
		this.vertimientoConstante = vertimientoConstante;
		this.hayVolObjVert = hayVolObjVert;
		this.volObjVert = volObjVert;
		this.hayControldeCotasMinimas=hayControldeCotasMinimas;
		this.volumenControlMinimo=volumenControlMinimo;
		this.penalidadControlMinimo=penalidadControlMinimo;
		this.hayControldeCotasMaximas=hayControldeCotasMaximas;
		this.volumenControlMaximo=volumenControlMaximo;
		this.penalidadControlMaximo=penalidadControlMaximo; 
	}
	
	public DatosHidraulicoCorrida() {
		// TODO Auto-generated constructor stub
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
	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}
	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}
	public ArrayList<String> getHidraulicosAguasArriba() {
		return hidraulicosAguasArriba;
	}
	public void setHidraulicosAguasArriba(ArrayList<String> hidraulicosAguasArriba) {
		this.hidraulicosAguasArriba = hidraulicosAguasArriba;
	}
	public String getHidraulicoAguasAbajo() {
		return hidraulicoAguasAbajo;
	}
	public void setHidraulicoAguasAbajo(String hidraulicoAguasAbajo) {
		this.hidraulicoAguasAbajo = hidraulicoAguasAbajo;
	}
	public Evolucion<Double> getPotMin() {
		return potMin;
	}
	public void setPotMin(Evolucion<Double> potMin) {
		this.potMin = potMin;
	}
	public Evolucion<Double> getPotMax() {
		return potMax;
	}
	public void setPotMax(Evolucion<Double> potMax) {
		this.potMax = potMax;
	}
	public Evolucion<Double> getRendPotMin() {
		return rendPotMin;
	}
	public void setRendPotMin(Evolucion<Double> rendPotMin) {
		this.rendPotMin = rendPotMin;
	}
	public Evolucion<Double> getRendPotMax() {
		return rendPotMax;
	}
	public void setRendPotMax(Evolucion<Double> rendPotMax) {
		this.rendPotMax = rendPotMax;
	}
	public Evolucion<Double> getqTur1Max() {
		return qTur1Max;
	}
	public void setqTur1Max(Evolucion<Double> qTur1Max) {
		this.qTur1Max = qTur1Max;
	}
	public DatosVariableAleatoria getAporte() {
		return aporte;
	}
	public void setAporte(DatosVariableAleatoria aporte) {
		this.aporte = aporte;
	}
	public Integer getCantModIni() {
		return cantModIni;
	}
	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
	}
	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}
	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}
	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}
	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}
	public Evolucion<DatosPolinomio> getfQEroMin() {
		return fQEroMin;
	}
	public void setfQEroMin(Evolucion<DatosPolinomio> fQEroMin) {
		this.fQEroMin = fQEroMin;
	}
	public Evolucion<DatosPolinomio> getfCoVo() {
		return fCoVo;
	}
	public void setfCoVo(Evolucion<DatosPolinomio> fCoVo) {
		this.fCoVo = fCoVo;
	}
	public Evolucion<DatosPolinomio> getfVoCo() {
		return fVoCo;
	}
	public void setfVoCo(Evolucion<DatosPolinomio> fVoCo) {
		this.fVoCo = fVoCo;
	}
	public Evolucion<DatosPolinomio> getfEvaporacion() {
		return fEvaporacion;
	}
	public void setfEvaporacion(Evolucion<DatosPolinomio> fEvaporacion) {
		this.fEvaporacion = fEvaporacion;
	}
	
	public Evolucion<Double> getCoefEvaporacion() {
		return coefEvaporacion;
	}
	public void setCoefEvaporacion(Evolucion<Double> coefEvaporacion) {
		this.coefEvaporacion = coefEvaporacion;
	}
	public Evolucion<DatosPolinomio> getfFiltracion() {
		return fFiltracion;
	}
	public void setfFiltracion(Evolucion<DatosPolinomio> fFiltracion) {
		this.fFiltracion = fFiltracion;
	}
	public Hashtable<String, Evolucion<String>> getValoresComportamientos() {
		return valoresComportamientos;
	}
	public void setValoresComportamientos(
			Hashtable<String, Evolucion<String>> valoresComportamientos) {
		this.valoresComportamientos = valoresComportamientos;
	}
	public Hashtable<String, DatosVariableEstado> getVarsEstado() {
		return varsEstado;
	}
	public void setVarsEstado(Hashtable<String, DatosVariableEstado> varsEstado) {
		this.varsEstado = varsEstado;
	}
	public Evolucion<DatosPolinomio> getfCoefEnerg() {
		return fCoefEnerg;
	}
	public void setfCoefEnerg(Evolucion<DatosPolinomio> fCoefEnerg) {
		this.fCoefEnerg = fCoefEnerg;
	}
	public Evolucion<Double> getVolFijo() {
		
		return this.volFijo;
	}
	public Evolucion<DatosPolinomio> getfCoAA() {
		return fCoAA;
	}
	public void setfCoAA(Evolucion<DatosPolinomio> fCoAA) {
		this.fCoAA = fCoAA;
	}
	public Evolucion<DatosPolinomio> getfQVerM() {
		return fQVerM;
	}
	public void setfQVerM(Evolucion<DatosPolinomio> fQVerM) {
		this.fQVerM = fQVerM;
	}
	public Double getSaltoMin() {
		return saltoMin;
	}
	public void setSaltoMin(Double saltoMin) {
		this.saltoMin = saltoMin;
	}
	public Double getCotaInundacionAguasAbajo() {
		return cotaInundacionAguasAbajo;
	}
	public void setCotaInundacionAguasAbajo(Double cotaInundacionAguasAbajo) {
		this.cotaInundacionAguasAbajo = cotaInundacionAguasAbajo;
	}
	public Double getCotaInundacionAguasArriba() {
		return cotaInundacionAguasArriba;
	}
	public void setCotaInundacionAguasArriba(Double cotaInundacionAguasArriba) {
		this.cotaInundacionAguasArriba = cotaInundacionAguasArriba;
	}
	public Double getEpsilonCaudalErogadoIteracion() {
		return epsilonCaudalErogadoIteracion;
	}
	public void setEpsilonCaudalErogadoIteracion(Double epsilonCaudalErogadoIteracion) {
		this.epsilonCaudalErogadoIteracion = epsilonCaudalErogadoIteracion;
	}
	public Evolucion<Double> getFactorCompartir() {
		return factorCompartir;
	}
	public void setFactorCompartir(Evolucion<Double> factorCompartir) {
		this.factorCompartir = factorCompartir;
	}
	public boolean isSalDetallada() {
		return salDetallada;
	}
	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}
	public DatosPolinomio getfRendimiento() {
		return fRendimiento;
	}
	public void setfRendimiento(DatosPolinomio fRendimiento) {
		this.fRendimiento = fRendimiento;
	}
	public Evolucion<Integer> getMantProgramado() {
		return mantProgramado;
	}
	public void setMantProgramado(Evolucion<Integer> mantProgramado) {
		this.mantProgramado = mantProgramado;
	}
	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}
	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}
	public Evolucion<Double> getCostoVariable() {
		return costoVariable;
	}
	public void setCostoVariable(Evolucion<Double> costoVariable) {
		this.costoVariable = costoVariable;
	}
	public String getRutaPQ() {
		return rutaPQ;
	}
	public void setRutaPQ(String rutaPQ) {
		this.rutaPQ = rutaPQ;
	}
	public Hashtable<Pair<Double,Double>, ArrayList<Recta>> getFuncionesPQ() {
		return funcionesPQ;
	}
	public void setFuncionesPQ(Hashtable<Pair<Double,Double>, ArrayList<Recta>> funcionesPQ) {
		this.funcionesPQ = funcionesPQ;
	}

	public void setVolFijo(Evolucion<Double> volFijo) {
		this.volFijo = volFijo;
	}

	public Evolucion<Double> getVolReservaEstrategica() {
		return volReservaEstrategica;
	}

	public void setVolReservaEstrategica(Evolucion<Double> volReservaEstrategica) {
		this.volReservaEstrategica = volReservaEstrategica;
	}

	public Evolucion<Double> getValorMinReserva() {
		return valorMinReserva;
	}

	public void setValorMinReserva(Evolucion<Double> valorMinReserva) {
		this.valorMinReserva = valorMinReserva;
	}

	public boolean isValorAplicaOptim() {
		return valorAplicaOptim;
	}

	public void setValorAplicaOptim(boolean valorAplicaOptim) {
		this.valorAplicaOptim = valorAplicaOptim;
	}

	public boolean isHayReservaEstrategica() {
		return hayReservaEstrategica;
	}

	public void setHayReservaEstrategica(boolean hayReservaEstrategica) {
		this.hayReservaEstrategica = hayReservaEstrategica;
	}

	public boolean isVertimientoConstante() {
		return vertimientoConstante;
	}

	public void setVertimientoConstante(boolean vertimientoConstante) {
		this.vertimientoConstante = vertimientoConstante;
	}

	public boolean isHayVolObjVert() {
		return hayVolObjVert;
	}

	public void setHayVolObjVert(boolean hayVolObjVert) {
		this.hayVolObjVert = hayVolObjVert;
	}

	public Evolucion<Double> getVolObjVert() {
		return volObjVert;
	}

	public void setVolObjVert(Evolucion<Double> volObjVert) {
		this.volObjVert = volObjVert;
	}


	
	public boolean isHayControldeCotasMinimas() {
		return hayControldeCotasMinimas;
	}

	public void setHayControldeCotasMinimas(boolean hayControldeCotasMinimas) {
		this.hayControldeCotasMinimas = hayControldeCotasMinimas;
	}

	public Evolucion<Double> getVolumenControlMinimo() {
		return volumenControlMinimo;
	}

	public void setVolumenControlMinimo(Evolucion<Double> volumenControlMinimo) {
		this.volumenControlMinimo = volumenControlMinimo;
	}

	public Evolucion<Double> getPenalidadControlMinimo() {
		return penalidadControlMinimo;
	}

	public void setPenalidadControlMinimo(Evolucion<Double> penalidadControlMinimo) {
		this.penalidadControlMinimo = penalidadControlMinimo;
	}

	public boolean isHayControldeCotasMaximas() {
		return hayControldeCotasMaximas;
	}

	public void setHayControldeCotasMaximas(boolean hayControldeCotasMaximas) {
		this.hayControldeCotasMaximas = hayControldeCotasMaximas;
	}

	public Evolucion<Double> getVolumenControlMaximo() {
		return volumenControlMaximo;
	}

	public void setVolumenControlMaximo(Evolucion<Double> volumenControlMaximo) {
		this.volumenControlMaximo = volumenControlMaximo;
	}

	public Evolucion<Double> getPenalidadControlMaximo() {
		return penalidadControlMaximo;
	}

	public void setPenalidadControlMaximo(Evolucion<Double> penalidadControlMaximo) {
		this.penalidadControlMaximo = penalidadControlMaximo;
	}

	public ArrayList<String>  controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		if (valoresComportamientos == null) { errores.add("Hidraulico "+ nombre +": valoresComportamientos vacío."); }
		else {
			valoresComportamientos.forEach((k,v) -> { if(v.controlDatosCompletos().size() > 0) errores.add("Hidraulico "+ nombre +": valoresComportamientos: " + k +" vacío."); } );
			if(valoresComportamientos.get(Constantes.COMPLAGO).getValor(instanteActual) != null && !valoresComportamientos.get(Constantes.COMPLAGO).getValor(instanteActual).equals(Constantes.HIDROSINLAGO))
			{
				if(varsEstado == null) { errores.add("Hidraulico "+ nombre +": Variables de Estado vacío."); }
				else {
					varsEstado.forEach((k,v) -> {
						if(v.controlDatosCompletos().size() > 0 ) errores.add("Hidraulico "+ nombre +": Variables de Estado vacío.");
					});
				}
			}
		}

		if(nombre.trim().equals("")) errores.add("Hidraulico: Nombre vacío.");
		if(barra == null) errores.add("Hidraulico "+ nombre +": Barra vacío.");
		if(cantModInst == null ) { errores.add("Hidraulico "+ nombre +": cantModInst vacío."); }
		else if (cantModInst.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": cantModInst vacío."); }

		if(factorCompartir == null ) errores.add("Hidraulico "+ nombre +": factorCompartir vacío.");
		else if (factorCompartir.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": factorCompartir vacío."); }

		if(hidraulicosAguasArriba == null){  errores.add("Hidraulico "+ nombre +": hidraulicosAguasArriba vacío.");}
		else { hidraulicosAguasArriba.forEach((v)-> { if(v.trim().equals("")){ errores.add("Hidraulico "+ nombre +": hidraulicosAguasArriba vacío."); } });}

		//if(hidraulicoAguasAbajo.trim().equals("")){  errores.add("Hidraulico: hidraulicoAguasAbajo vacío.");}

		if(potMax == null ) { errores.add("Hidraulico "+ nombre +": potMax vacío."); }
		else if (potMax.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": potMax vacío."); }
		if(potMin == null ) { errores.add("Hidraulico "+ nombre +": potMin vacío."); }
		else if (potMin.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": potMin vacío."); }
		if(rendPotMin == null ) { errores.add("Hidraulico "+ nombre +": rendPotMin vacío."); }
		else if (rendPotMin.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": rendPotMin vacío."); }
		if(rendPotMax == null ) { errores.add("Hidraulico "+ nombre +": rendPotMax vacío."); }
		else if (rendPotMax.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": rendPotMax vacío."); }

		if(cantModIni == null ) errores.add("Hidraulico "+ nombre +": cantModIni vacío.");
		if(dispMedia == null ) { errores.add("Hidraulico "+ nombre +": dispMedia vacío."); }
		else if (dispMedia.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": dispMedia vacío."); }
		if(tMedioArreglo == null ) { errores.add("Hidraulico "+ nombre +": dispMedia vacío."); }
		else if (tMedioArreglo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": tMedioArreglo vacío."); }
		if(mantProgramado == null ) { errores.add("Hidraulico "+ nombre +": mantProgramado vacío."); }
		else if (mantProgramado.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": mantProgramado vacío."); }
		if(costoFijo == null ) { errores.add("Hidraulico "+ nombre +": costoFijo vacío."); }
		else if (costoFijo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": costoFijo vacío."); }
		if(costoVariable == null ) { errores.add("Hidraulico "+ nombre +": costoVariable vacío."); }
		else if (costoVariable.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": costoVariable vacío."); }

		if(epsilonCaudalErogadoIteracion == null || epsilonCaudalErogadoIteracion == 0) errores.add("Hidraulico "+ nombre +": Epsilon Caudal Erogado vacío.");
		if(rutaPQ.trim().equals("")) errores.add("Hidraulico: rutaPQ vacío.");

		if (fCoAA == null) { errores.add("Hidraulico "+ nombre +": Funcion CotaAA vacío."); }
		else if(fCoAA.getValor(instanteActual).controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion CotaAA vacío."); }

		if(volFijo == null ) errores.add("Hidraulico "+ nombre +": volFijo vacío.");
		else if(volFijo.controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion volFijo vacío."); }

		if(qTur1Max == null ) errores.add("Hidraulico "+ nombre +": qTur1Max vacío.");
		else if(qTur1Max.controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion qTur1Max vacío."); }

		if(saltoMin == null || saltoMin == 0) errores.add("Hidraulico "+ nombre +": salto Minimo vacío.");

		if (fCoVo == null) errores.add("Hidraulico: Funcion Cota Volumen vacío.");
		else if(fCoVo.getValor(instanteActual).controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion Cota Volumen vacío.");
		}

		if (fVoCo == null) errores.add("Hidraulico "+ nombre +": Funcion Volumen Cota vacío.");
		else if (fVoCo.getValor(instanteActual).controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion Volumen Cota vacío.");
		}

		if(coefEvaporacion == null ) errores.add("Hidraulico "+ nombre +": Coef Evaporacion vacío.");

		if (fEvaporacion == null) errores.add("Hidraulico: Funcion Evaporacion vacío.");
		else if(fEvaporacion.getValor(instanteActual).controlDatosCompletos().size() > 0)  { errores.add("Hidraulico "+ nombre +": Funcion Evaporacion vacío.");
		}

		if (fFiltracion == null) errores.add("Hidraulico "+ nombre +": Funcion Filtracion vacío.");
		else if(fFiltracion.getValor(instanteActual).controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion Filtracion vacío.");
		}


		if(cotaInundacionAguasAbajo == null || saltoMin == 0) errores.add("Hidraulico "+ nombre +": Cota Inundacion Aguas Abajo vacío.");
		if(cotaInundacionAguasArriba == null || saltoMin == 0) errores.add("Hidraulico "+ nombre +": Cota Inundacion Aguas Arriba vacío.");

		if (fQEroMin == null) errores.add("Hidraulico "+ nombre +": Funcion QErogado Minimo vacío.");
		else if(fQEroMin.getValor(instanteActual).controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion QErogado Minimo vacío.");
		}

		if (fQVerM == null) errores.add("Hidraulico "+ nombre +": Funcion QErogado Vertimiento Maximo vacío.");
		else if(fQVerM.getValor(instanteActual).controlDatosCompletos().size() > 0) { errores.add("Hidraulico "+ nombre +": Funcion QErogado Vertimiento Maximo vacío.");
		}

		if(aporte == null){ errores.add("Hidraulico "+ nombre +": aporte vacío."); }
		else if(aporte.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": aporte vacío."); }

		if(volReservaEstrategica == null ) errores.add("Hidraulico "+ nombre +": volReservaEstrategica vacío.");
		else if(volReservaEstrategica.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": volReservaEstrategica vacío."); }
		if(valorMinReserva == null ) errores.add("Hidraulico "+ nombre +": valorMinReserva vacío.");
		else if(valorMinReserva.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": valorMinReserva vacío."); }


		if(volObjVert == null ) errores.add("Hidraulico "+ nombre +": volumen Objetivo Vertimento vacío.");
		else if(volObjVert.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": volObjVert vacío."); }
		if(volumenControlMinimo == null ) errores.add("Hidraulico "+ nombre +": volumenControlMinimo vacío.");
		else if(volumenControlMinimo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": volumenControlMinimo vacío."); }
		if(penalidadControlMinimo == null ) errores.add("Hidraulico "+ nombre +": penalidadControlMinimo vacío.");
		else if(penalidadControlMinimo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": penalidadControlMinimo vacío."); }
		if(volumenControlMaximo == null ) errores.add("Hidraulico "+ nombre +": volumenControlMaximo vacío.");
		else if(volumenControlMaximo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": volumenControlMaximo vacío."); }
		if(penalidadControlMaximo == null ) errores.add("Hidraulico "+ nombre +": penalidadControlMaximo vacío.");
		else if(penalidadControlMaximo.controlDatosCompletos().size() >0 ) { errores.add("Hidraulico "+ nombre +": penalidadControlMaximo vacío."); }

		return errores;
    }

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}

}

