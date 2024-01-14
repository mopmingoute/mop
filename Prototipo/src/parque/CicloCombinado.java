/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CicloCombinado is part of MOP.
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

package parque;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.BarraCompDesp;
import compdespacho.CicloCombCompDesp;
import compdespacho.TermicoCompDesp; 
import compgeneral.CicloCombComp;
import compgeneral.TermicoComp;
import compsimulacion.CicloCombCompSim;
import compsimulacion.TermicoCompSim;
import control.VariableControlDE;
import cp_compdespProgEst.AcumuladorCompDespPE;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import datatypes.DatosCicloCombinadoCorrida;
import datatypes.DatosTermicoCorrida; 
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosCicloCombSP;
import datatypesSalida.DatosSalidaPaso;
import datatypesSalida.DatosTermicoSP;
import datatypesSalida.DatosTermicoSPAuxComb;
import estado.VariableEstado;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.Evolucion;
import utilitarios.Constantes;

public class CicloCombinado extends Generador implements AportanteEstado {
	// TODO TENDRA QUE IMPLEMENTAR APORTANTE ESTADO DESPUES

	/**
	 * 
	 * Los datos de rendimiento del atributo tVs CORRESPONDEN A LOS DEL CICLO COMBINADO TOTAL (a plena carga y en el mínimo).
	 * Los datos de potencia del atributo tVs CORRESPONDEN A LOS DE CADA CICLO DE VAPOR.
	 * 
	 * La potencia de una TG combinada se obtiene por:
	 * 
	 * potMax1CC = (potMax1TG * cantidad de TG instaladas + potMaxqTV*ninstTV)/ cantida de TGs instaladas
	 * 
	 * La potencia mínima
	 * 
	 * Los datos de mantenimiento y cantidad de módulos de tGs y tVs son los de los respectivos atributos
	 * 
	 * Los datos de costo variable del ciclo de vapor (USD/MWh) generado con el ciclo de vapor son los
	 * del atributo tVs
	 */
	
	private ArrayList<String> listaCombustibles;  // Lista de nombres de los combustibles
	private Hashtable<String,Combustible> combustibles;						/**Lista de combustibles asociados al generador tórmico*/					
	private Hashtable<String,BarraCombustible> barrasCombustible;			/**Barras correspondientes a cada uno de los combustibles de la lista anterior*/
	
	private GeneradorTermico tGs;	// las turbinas del CC
	private GeneradorTermico cCs;	// los ciclos de vapor del CC  a veces en el programa se los llama CVs
	
	private CicloCombCompDesp compD;
	private CicloCombCompSim compS;
	private CicloCombComp compG;
	
	private Evolucion<Double> potMax1CV;
	
	/**
	 * relpot es la relación de potencia entre el CC a plena carga y la suma de las TGs a plena carga, cuando todas las unidades están disponibles.
	 * Es mayor que 1 por lo tanto.
	 * Ese valor así calculado se emplea también para potencias parciales y con cualquier número de TGs y CVs.
	 * 
	 * pot TGCC = potencia del ciclo combinado. 
	 * relPot = pot TGCC / pot TGs ciclo abierto    (todo a plena carga en condiciones de diseño de la central)
	 * relPot = (pot TGs + pot CVs) / potTGs
	 * potCVs = (relPot - 1) potTGs
	 * potTGs = pot TGCC/ relPot
	 * 
	 */
	 
	
	private double costoArranque1TG_CA_USD; // costo de arranque de cada TG en ciclo abierto en dólares, no se usa en el comportamiento TGSMEJORES 
	private double costoArranque1TG_CC_USD; // costo de arranque del ciclo por cada TG arrancada en ciclo combinado en dólares, no se usa en el comportamiento TGSMEJORES

	private Evolucion<Double> costoArranque1TGCicloAbierto;  // en USD
	private Evolucion<Double> costoArranque1TGCicloCombinado; // en USD
	private VariableEstadoPar cantTGCombIni;
	private VariableEstadoPar cantTGCombIniOpt;

	private static ArrayList<String> atributosDetallados;	
	
	/**
	 * relacionTGCCInst  cantidad de TGs máxima para cada CV disponible.
	 * relacionTGCCInst = entero por exceso (cantTGinstaladas/cantCVinstalados) de diseño
	 */
	private double relacionTGCCInst;

	
	/**
	 * COMPORTAMIENTOS GENERALES POSIBLES
	 *
	 * COMPDESP = "compDesp";  nombre de la variable de comportamiento general, cuyos valores pueden ser:
	 * 
	 * TGSMEJORES = "tgsmejores":  operan como un GeneradorTermico con el rendimiento del CC, sin otras restricciones (lo que se hace hasta 2022).
	 * Pueden tener mínimo técnico por paso o por poste.
	 * 
	 * CCSINESTADO = "ccsinestado";  cada TV tiene una variable entera que indica si está combinada en el paso (sus gases de escape van a los ciclos de vapor) 
	 * si hay alguna TV disponible o si por el contrario opera como TG en ciclo abierto.
	 * Si hay TVs indisponibles la cantidad de TGs que pueden combinarse se reduce al entero por defecto. Ejemplo 5 TGs de 100 c/u y 2 TVs de 100 c/u, si hay una TV
	 * indisponible 100/200 = 0.5, sólo pueden combinarse 2 de las 5 TGs. Las TGs combinadas lo están durante todo el paso, al menos con mínimo técnico del CC.
	 * Las TGs tienen un costo de arranque en ciclo abierto y otro cuando se combinan.
	 * El comportamiento de mínimo técnico operando como TG es por poste.
	 * El comportamiento de mínimo técnico operando como ciclo combinado es por paso.
	 * 
	 * 
	 * CCCONESTADO = "ccconestado"; Además de las características del comportamiento CCSINESTADO:
	 * Cada TG tiene una variable de estado binaria que indica si está combinada al fin del paso de tiempo anterior. Si
	 * no está combinada para arrancarla combinada en el paso corriente hay una rampa de arranque.
	 * Para cada TG combinada son posibles distintos modos de arranque y uso combinado, especificados por el usuario, cada uno de ellss
	 * representado por una variable binaria en el despacho. Por ejemplo son modos de uso:
	 * -Empieza la rampa a las 8 de la mañana y apaga a las 22 hs.
	 * -Empieza la rampa a las 12 de la mañana y no apaga al final del paso
	 * Cada TG puede tomar un solo modo de uso en el paso.
	 * 
	 * PODRIA HABE UN COMPORTAMIENTO CCCONESTADO_EN_SIMULACION
	 *
	 */
	
	
	
	public CicloCombinado(DatosCicloCombinadoCorrida dcc, Barra barra, Hashtable<String,Combustible> combs, Hashtable<String,BarraCombustible> barrasCombs) {
		
		tGs = new GeneradorTermico(dcc.getDatosTGs(), barra, combs, barrasCombs);
		cCs = new GeneradorTermico(dcc.getDatosCCs(), barra, null, null);
		this.setNombre(dcc.getNombre());
		this.setBarra(barra);
		this.setPotMax1CV(dcc.getPotMax1CV());

		this.combustibles =combs;
		this.listaCombustibles = dcc.getListaCombustibles();
		this.barrasCombustible = barrasCombs; 
		
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		int modTG = tGs.getCantModInst().getValor(instanteActual);
		int modCV = cCs.getCantModInst().getValor(instanteActual);
		this.setPropietario(dcc.getPropietario());
		if(modCV!=0) {
			relacionTGCCInst = Math.ceil(modTG/modCV);
		}else {
			relacionTGCCInst = 0;
		}
		tGs.setListaCombustibles(listaCombustibles);
		cCs.setListaCombustibles(listaCombustibles);
		tGs.setRendsPotMin(dcc.getDatosTGs().getRendimientosPotMin());
		tGs.setRendsPotMax(dcc.getDatosTGs().getRendimientosPotMax());
		cCs.setRendsPotMin(dcc.getDatosCCs().getRendimientosPotMin());
		cCs.setRendsPotMax(dcc.getDatosCCs().getRendimientosPotMax());
		compD = new CicloCombCompDesp(this);
		compG = new CicloCombComp(this);
		compS = new CicloCombCompSim(this, compD, compG);		
		
		compS.setCompgeneral(compG);
		compS.setCompdespacho(compD);
		compG.setCompSimulacion(compS);
		compG.setCompDespacho(compD);
		this.setCompDesp(compD);
		this.setCompGeneral(compG);
		this.setCompSimulacion(compS);
		compD.setParticipante(this);
		compS.setParticipante(this);
		compG.setParticipante(this);
		
		compG.setEvolucionComportamientos(dcc.getValoresComportamientos());
		
	}
	
	/**
	 * Potencia máxima del ciclo
	 */
	@Override
	public double potInstaladaTotal(long instante) {
		double potTG = tGs.getCantModInst().getValor(instante)*tGs.getPotenciaMaxima().getValor(instante);
		double potCV = cCs.getCantModInst().getValor(instante)*cCs.getPotenciaMaxima().getValor(instante);
		return potTG + potCV;
	}
	
	@Override
	public void asignaVAOptim() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void asignaVASimul() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		double [] potencia = new double [this.getCantPostes()];
		double[] potTGs = new double [this.getCantPostes()]; // potencia de las TG (los alternadores de las TG) 
		double[] potTVs = new double [this.getCantPostes()]; // potencia de los ciclos de vapor
		double[] potAb = new double [this.getCantPostes()];
		double[] potComb = new double [this.getCantPostes()];
		int[] cantModTGAbDesp = new int[this.getCantPostes()];
		int[] cantModTGCombDesp = new int[this.getCantPostes()];
		
		double relpot = cCs.getPotenciaMaxima().getValor(instante)/tGs.getPotenciaMaxima().getValor(instante);
		Hashtable<String, Evolucion<Double>> rendsMaxTGs = this.getTGs().getRendsPotMax();
		Hashtable<String, Evolucion<Double>> rendsMinTGs = this.getTGs().getRendsPotMin();
		Hashtable<String, Evolucion<Double>> rendsMaxCCs = this.getCCs().getRendsPotMax();
		Hashtable<String, Evolucion<Double>> rendsMinCCs = this.getCCs().getRendsPotMin();
		
	
		
		double[] costosMarginales = new double[this.getCantPostes()];
		Barra b = this.getBarra();
		boolean uninodal = this.getBarra().isRedUninodal(instante);		
		if (uninodal) {
			b = this.getBarra().dameBarraUnica();
		}
		BarraCompDesp bcd = (BarraCompDesp)b.getCompDesp();
		for (int p = 0; p < this.getCantPostes(); p++) {
			String nombreRest = bcd.nombreRestPoste(p);
			// costo marginal por poste en USD/MWh
			costosMarginales[p] = salidaUltimaIter.getDuales().get(nombreRest)*Constantes.SEGUNDOSXHORA/this.getDuracionPostes(p);
		}
		int cantPostes = this.getCantPostes();
//				double[] valor1MWAlMarginal = new double[cantPostes]; 
		for (int p = 0; p < cantPostes; p++) {
			potencia[p] = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("pot", Integer.toString(p)));
			potTGs[p] = salidaUltimaIter.getSolucion().get(this.getCompD().getNpotTGp()[p]);
			// atención: la variable contenida en npotCCp es la potencia de las TGs combinadas INCLUSO EL CICLO DE VAPOR
			potTGs[p] += salidaUltimaIter.getSolucion().get(this.getCompD().getNpotCCp()[p])/relpot;
			potTVs[p] = salidaUltimaIter.getSolucion().get(this.getCompD().getNpotCCp()[p])*(relpot-1)/relpot;
			potAb[p] = salidaUltimaIter.getSolucion().get(this.getCompD().getNpotTGp()[p]);
			potComb[p] = salidaUltimaIter.getSolucion().get(this.getCompD().getNpotCCp()[p]);
			cantModTGAbDesp[p] = salidaUltimaIter.getSolucion().get(this.getCompD().getNnmodTGp()[p]).intValue();
			cantModTGCombDesp[p] = salidaUltimaIter.getSolucion().get(this.getCompD().getNnmodCC()).intValue();
		}
		
		String nombarra = this.getBarra().getNombre();
		int [] cantModDesp = new int[this.getCantPostes()];	
		
		double [][] enerTC = new double[this.getCantPostes()][this.getCombustibles().size()];
		double [] costoPC = new double[this.getCombustibles().size()];
		costoPC = this.compS.calculaCostoDeCombustiblesPaso(salidaUltimaIter, instante);
		DatosTermicoSPAuxComb dcomb = this.compS.calculaResultadosDeCombustiblesPaso(salidaUltimaIter, instante);
		String clave;
		String nombre;
		
		double costoVarMintec = -1;
		int cantComb = listaCombustibles.size();
//		double[] valoresComb = new double[cantComb];
		double[] costoVarPropTGAb = new double[cantComb]; // costo variable proporcional USD/MWh de TG ciclo abierto por encima del mónimo tócnico con cada combustible
		double[] costoVarPropCC = new double[cantComb]; // lo mismo para el CC

		//		double[] costoVarMax =  new double[cantComb]; // costo variable medio USD/MWh a plena carga con cada combustible
		int ic = 0;
//		double cvMin = Double.MAX_VALUE;
		double[] costoVarMaxTGAb = new double[cantComb];
		double[] costoVarMaxTGCC = new double[cantComb];
		double potEspMaxTGAb, potEspMaxTGCC;
		for(String sc: listaCombustibles){
			BarraCombustible bc = barrasCombustible.get(sc);
			RedCombustible redc = bc.getRedAsociada();
			if(redc.isUninodal()) bc = redc.getBarraunica();
			Combustible comb = combustibles.get(sc);
			// Se usa cualquier entero para el poste porque es irrelevante
			double valorComb = bc.devuelveVarDualBalance(salidaUltimaIter, -1);
			double pci = comb.getPci(); // PCI en MWh por unidad
			double potEspEncimaMinTG = compD.getPotTerPropTG();  // MW tórmicos por MW elóctrico por encima del mínimo
			double potEspEncimaMinCC = compD.getPotTerPropCC();			
			potEspMaxTGAb = 1/this.getTGs().getRendsPotMax().get(sc).getValor(instante);  // MW tórmicos por MW eléctrico a plena carga
			potEspMaxTGCC = 1/this.getCCs().getRendsPotMax().get(sc).getValor(instante);  // MW tórmicos por MW eléctrico a plena carga
			//			costoVarProp[ic] = valorComb/pci*potEspEncimaMin;  // costo proporcional de comb. por encima el mínimo tec en USD/MWh
			costoVarMaxTGAb[ic] = valorComb/pci*potEspMaxTGAb;   // costo variable medio de combustible a potencia móxima en USD/MWh
			costoVarMaxTGCC[ic] = valorComb/pci*potEspMaxTGCC;
			costoVarPropTGAb[ic] = valorComb/pci*potEspEncimaMinTG; 
			costoVarPropCC[ic] = valorComb/pci*potEspEncimaMinCC; 
			ic++;
		}		

		double costoTotPaso = 0;
		for (int p = 0; p < this.getCantPostes(); p++) {
			ic = 0;
			cantModDesp[p] = salidaUltimaIter.getSolucion().get(this.compD.getNnmodCC()).intValue() +
					salidaUltimaIter.getSolucion().get(this.compD.getNnmodTGp()[p]).intValue();		
		}
		
		
		int cantFuncUltimoIM = 0;
		if(this.getCompG().getEvolucionComportamientos().get(utilitarios.Constantes.COMPCC).getValor(instante).equalsIgnoreCase(utilitarios.Constantes.CCSINESTADO)) {
			cantFuncUltimoIM = salidaUltimaIter.getSolucion().get(this.compD.getNnmodCC()).intValue();
		}else {
			System.out.println("No se ha definido cantFuncUltimoIM para algún comportamiento del ciclo combinado");
			System.exit(1);
		}
		
		
		Integer cantModini;
	
		for (int i = 0; i < dcomb.getCostoC().length; i++) {			
				costoTotPaso += dcomb.getCostoC()[i];
		}
		
		costoTotPaso += this.compS.calculaCostoPaso(salidaUltimaIter); // Se agrega costo O&M
		int cantModTGDisp = this.getTGs().getCantModDisp().getValor().intValue();
		int cantModTVDisp = this.getCCs().getCantModDisp().getValor().intValue();			
		double dispTG = this.getTGs().getDispMedia().getValor(instante);
		double dispCV = this.getCCs().getDispMedia().getValor(instante);
		
		double cvNoCombTG = this.getTGs().getCostoVariable().getValor(instante); // costo variable no combus. en USD/MWh de las TG
		double cvNoCombCV = this.getCCs().getCostoVariable().getValor(instante); // costo variable no combus. en USD/MWh de los CV
		double cvNoCombTGComb =  cvNoCombTG*(1/relpot) +  cvNoCombCV*(1-1/relpot); // costo variable no comb de una TG combinada
		
		
		// Cálculo del gradiente de gestión para disponibilidad media en USD/MW
		double[] gradGestion = new double[cantComb];
		double alfa = 0.0;
		double beta = 0.0;
		for(int ic2=0; ic2<cantComb; ic2++){
			int ntgcomb = salidaUltimaIter.getSolucion().get(this.compD.getNnmodCC()).intValue();
			for (int p = 0; p < this.getCantPostes(); p++) {
				int ntgab = salidaUltimaIter.getSolucion().get(this.compD.getNnmodTGp()[p]).intValue();
				if(ntgab==0) {
					alfa = 1.0;
					beta = 1.0;
				}else {
					alfa = ntgcomb/(ntgab+ntgcomb);
					beta = ntgab/(ntgab+ntgcomb);
				}
				gradGestion[ic2] += alfa*Math.max(0, costosMarginales[p] - costoVarMaxTGCC[ic2] - cvNoCombTGComb)*(this.getDuracionPostes(p)/utilitarios.Constantes.SEGUNDOSXHORA)*dispTG*dispCV
						+ beta*Math.max(0, costosMarginales[p] - costoVarMaxTGAb[ic2] - cvNoCombTG)*(this.getDuracionPostes(p)/utilitarios.Constantes.SEGUNDOSXHORA)*dispTG;

			}
		}
		
		DatosCicloCombSP dcc = new DatosCicloCombSP(this.getNombre(),nombarra, this.getCCs().getPotenciaMaxima().getValor(instante),this.getCCs().getMinimoTecnico().getValor(instante), 
				this.getTGs().getPotenciaMaxima().getValor(instante), this.getTGs().getMinimoTecnico().getValor(instante), cantModTGDisp, cantModTVDisp, cantFuncUltimoIM, cantModTGAbDesp, cantModTGCombDesp, 
				potencia, rendsMaxTGs, rendsMinTGs, rendsMaxCCs, rendsMinCCs, this.getListaCombustibles(),
				dcomb.getVolC(), dcomb.getEnerTC(), dcomb.getEnerTPC(),dcomb.getEnerEC(), dcomb.getCostoC(), dcomb.getVolPC(), dcomb.getPotEPC(), 
				costoVarMintec, costoVarPropTGAb, costoVarPropCC, costoTotPaso, potTGs, potTVs, potAb, potComb, gradGestion);	
		

		
		for (DatosBarraSP dbsp: resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre()) || this.getBarra().getRedAsociada().getCompD().isUninodal()){
				dbsp.agregarCicloCombinado(dcc);
				break;
			}
		}		
	}
	

	@Override

	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();
		ret.add(this.getTGs().getCantModDisp().getPe());
		ret.add(this.getCCs().getCantModDisp().getPe());
		return ret;
	}
	
	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public ArrayList<String> getListaCombustibles() {
		return listaCombustibles;
	}


	public void setListaCombustibles(ArrayList<String> listaCombustibles) {
		this.listaCombustibles = listaCombustibles;
	}


	public Hashtable<String, Combustible> getCombustibles() {
		return combustibles;
	}


	public void setCombustibles(Hashtable<String, Combustible> combustibles) {
		this.combustibles = combustibles;
	}


	public Hashtable<String, BarraCombustible> getBarrasCombustible() {
		return barrasCombustible;
	}


	public void setBarrasCombustible(Hashtable<String, BarraCombustible> barrasCombustible) {
		this.barrasCombustible = barrasCombustible;
	}



	public GeneradorTermico getTGs() {
		return tGs;
	}


	public void setTGs(GeneradorTermico tGs) {
		this.tGs = tGs;
	}


	public GeneradorTermico getCCs() {
		return cCs;
	}


	public void setCCs(GeneradorTermico cCs) {
		this.cCs = cCs;
	}


	public double getCostoArranque1TG_CA_USD() {
		return costoArranque1TG_CA_USD;
	}


	public void setCostoArranque1TG_CA_USD(double costoArranque1TG_CA_USD) {
		this.costoArranque1TG_CA_USD = costoArranque1TG_CA_USD;
	}


	public double getCostoArranque1TG_CC_USD() {
		return costoArranque1TG_CC_USD;
	}


	public void setCostoArranque1TG_CC_USD(double costoArranque1TG_CC_USD) {
		this.costoArranque1TG_CC_USD = costoArranque1TG_CC_USD;
	}


	public Evolucion<Double> getPotMax1CV() {
		return potMax1CV;
	}


	public void setPotMax1CV(Evolucion<Double> potMax1CV) {
		this.potMax1CV = potMax1CV;
	}


	public double getRelacionTGCCInst() {
		return relacionTGCCInst;
	}


	public void setRelacionTGCCInst(double relacionTGCCInst) {
		this.relacionTGCCInst = relacionTGCCInst;
	}


	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}


	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		CicloCombinado.atributosDetallados = atributosDetallados;
	}




	public Evolucion<Double> getCostoArranque1TGCicloAbierto() {
		return costoArranque1TGCicloAbierto;
	}


	public Evolucion<Double> getCostoArranque1TGCicloCombinado() {
		return costoArranque1TGCicloCombinado;
	}


	public String getNEnerT(int p, String nombre) {
		CicloCombCompDesp tc = (CicloCombCompDesp)this.getCompDesp();		
		return tc.getNenerTpc(p,nombre);
	}
	
//	public Evolucion<Double> getPotMax1CC() {
//		return potMax1CC;
//	}
//
//
//	public Evolucion<Double> getPotMin1CC() {
//		return potMin1CC;
//	}
//
//
//	public Evolucion<Double> getRendPotMaxCC() {
//		return rendPotMaxCC;
//	}
//
//
//	public Evolucion<Double> getRendPotMinCC() {
//		return rendPotMinCC;
//	}




	public void setCostoArranque1TGCicloAbierto(Evolucion<Double> costoArranque1TGCicloAbierto) {
		this.costoArranque1TGCicloAbierto = costoArranque1TGCicloAbierto;
	}


	public void setCostoArranque1TGCicloCombinado(Evolucion<Double> costoArranque1TGCicloCombinado) {
		this.costoArranque1TGCicloCombinado = costoArranque1TGCicloCombinado;
	}


//	public void setPotMax1CC(Evolucion<Double> potMax1CC) {
//		this.potMax1CC = potMax1CC;
//	}
//
//
//	public void setPotMin1CC(Evolucion<Double> potMin1CC) {
//		this.potMin1CC = potMin1CC;
//	}
//
//
//	public void setRendPotMaxCC(Evolucion<Double> rendPotMaxCC) {
//		this.rendPotMaxCC = rendPotMaxCC;
//	}
//
//
//	public void setRendPotMinCC(Evolucion<Double> rendPotMinCC) {
//		this.rendPotMinCC = rendPotMinCC;
//	}
	

	/**
	 * METODOS QUE SOBREESCRIBEN LOS DE CLASES PADRES
	 */
	

	@Override
	public void actualizarVarsEstadoSimulacion() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actualizarVarsEstadoOptimizacion() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void contribuirAS0fint() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void contribuirAS0fintOptim() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void cargarValVEOptimizacion() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cargarValRecursoVESimulacion() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public ArrayList<VariableEstado> getVarsEstado() {
		// TODO Auto-generated method stub
		return null;
	}




	public CicloCombCompDesp getCompD() {
		return compD;
	}


	public CicloCombCompSim getCompS() {
		return compS;
	}


	public CicloCombComp getCompG() {
		return compG;
	}


	public void setCompD(CicloCombCompDesp compD) {
		this.compD = compD;
	}


	public void setCompS(CicloCombCompSim compS) {
		this.compS = compS;
	}


	public void setCompG(CicloCombComp compG) {
		this.compG = compG;
	}


	public void setCantTGCombIni(VariableEstadoPar cantTGCombIni) {
		this.cantTGCombIni = cantTGCombIni;
	}


	public void setCantTGCombIniOpt(VariableEstadoPar cantTGCombIniOpt) {
		this.cantTGCombIniOpt = cantTGCombIniOpt;
	}


	public VariableEstadoPar getCantTGCombIni() {
		return cantTGCombIni;
	}


	public VariableEstadoPar getCantTGCombIniOpt() {
		return cantTGCombIniOpt;
	}
	

	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public void crearCompDespPE() {
		CicloCombCompDespPE compDespPE = new CicloCombCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
	
	@Override
	public GeneradorCompDespPE devuelveCompDespPE() {
		return (CicloCombCompDespPE)getCompDespPE();
	}

}
