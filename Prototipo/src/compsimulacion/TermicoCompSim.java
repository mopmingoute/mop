/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TermicoCompSim is part of MOP.
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

package compsimulacion;

import java.util.*;

import parque.Combustible;
import parque.ContratoCombustible;
import parque.ContratoCombustibleCanio;
import parque.GeneradorTermico;
import parque.RedCombustible;
import pizarron.PizarronRedis;
import compdespacho.TermicoCompDesp;
import compgeneral.TermicoComp;
import tiempo.Evolucion;
import utilitarios.Constantes;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosEPPUnEscenario;
import datatypesSalida.DatosTermicoSPAuxComb;
import estado.VariableEstadoPar;
import logica.CorridaHandler;

public class TermicoCompSim extends CompSimulacion {
	private GeneradorTermico gt;
	private TermicoCompDesp tcd;
	private TermicoComp compG;

	public TermicoCompSim() {
		super();
		this.gt = (GeneradorTermico) this.getParticipante();
		tcd = (TermicoCompDesp) this.getCompdespacho();

	}

	public TermicoCompSim(GeneradorTermico generadorTermico, TermicoCompDesp tcd, TermicoComp tcg) {
		this.setParticipante(generadorTermico);
		setCompG(tcg);
		this.tcd = tcd;
		this.gt = generadorTermico;

	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		String valComp = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valComp.equalsIgnoreCase(Constantes.TERDOSPASOS)) {
			if (iter == 1) {
				this.getCompdespacho().getParametros().put(Constantes.COMPMINTEC, Constantes.TERSINMINTEC);
			} else {
				this.getCompdespacho().getParametros().put(Constantes.COMPMINTEC, Constantes.TERMINTECFORZADO);
			}
		} else {
			if (iter == 1)
				this.getCompdespacho().getParametros().put(Constantes.COMPMINTEC, valComp);
		}
		GeneradorTermico gt = (GeneradorTermico) this.getParticipante();
		this.getCompdespacho().getParametros().put(Constantes.COMPFLEXIBILIDAD, gt.getFlexibilidad());
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		tcd.setNnmodp(new String[gt.getCantPostes()]);
		tcd.setCantModForzadop(new int[gt.getCantPostes()]);
		tcd.setNenerTpc(new String[gt.getCantPostes()][]);
		tcd.setNpotp(new String[gt.getCantPostes()]);

		// DATOS QUE VIENEN DE EVOLUCIONES
		Double minTec = gt.getMinimoTecnico().getValor(instante);
		Double potMax = gt.getPotenciaMaxima().getValor(instante);
		String combustibleBase = gt.getRendsPotMax().keys().nextElement();
		Double rendMaxBase = gt.getRendsPotMax().get(combustibleBase).getValor(instante);
		Double rendMinBase = gt.getRendsPotMin().get(combustibleBase).getValor(instante);
		Hashtable<String,Double> coefsEnerTer = new Hashtable<String,Double>();

		for (Map.Entry<String, Evolucion<Double>> c : gt.getRendsPotMax().entrySet())  {
			coefsEnerTer.put(c.getKey(), c.getValue().getValor(instante)/rendMaxBase);
		}

		tcd.setCoefsEnerTer(coefsEnerTer);
		tcd.setMinTec(minTec);
		tcd.setPotMax(potMax);
		tcd.setPotEspTerProp(tcd.calcPotEspTerProp(minTec, potMax, rendMinBase, rendMaxBase));
		tcd.setPotEspTerPropMax(tcd.calcPotEspTerPropMax(rendMaxBase));
		tcd.setPotTerMinTec(tcd.calcPotTerMinTec(minTec, rendMinBase));

		// DATOS QUE VIENEN DE VARIABLES DE ESTADO
		String valCompGen = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			tcd.setCantModIni(gt.getCantModIni().getEstadoDespuesDeCDE().intValue());
		}

		// DATOS QUE VIENEN DE VARIABLES ALEATORIAS
	
	
		
		Integer cantModDisp = gt.getCantModDisp().getValor().intValue();
//		System.out.println("NOMBRE: " + gt.getNombre() + "CANTMOD: " + cantModDisp);
		tcd.setCantModDisp(cantModDisp);

		// TODO: RECORDAR CARGAR COSARRANQUE Y COSPARADA PARA SIGUIENTES
		// VERSIONES PARA EL CASO TERVARENTERASYVARESTADO CUANDO EL COMPGLOBAL
		// ES POR INCREMENTOS

		// TODO: OBTENER INCREMENTOS DEL RESOPTIM PARA EL CASO
		// TERVARENTERASYVARESTADO CUANDO EL COMPGLOBAL ES POR INCREMENTOS
		for (int i = 0; i < gt.getCantPostes(); i++) {
			tcd.getCantModForzadop()[i] = 0;
		}

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		String valComp = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valComp.equalsIgnoreCase(Constantes.TERDOSPASOS)) {
			if (iter == 2) {
				int cantPos = gt.getCantPostes();
				double[] potDespachada = new double[cantPos];
				double potDespMax = 0;

				for (int i = 0; i < cantPos; i++) {
					potDespachada[i] = salidaIter.getSolucion().get(tcd.getNpotp()[i]);
					if (potDespachada[i] > potDespMax)
						potDespMax = potDespachada[i];
				}

				int cantModForzado;
				cantModForzado = (int) Math
						.ceil(potDespMax / gt.getPotenciaMaxima().getValor(instanteActual) - Constantes.EPSILONCOEF);
				for (int i = 0; i < cantPos; i++) {
					if (tcd.getParametros().get(Constantes.COMPFLEXIBILIDAD)
							.equalsIgnoreCase(Constantes.TERFLEXHORARIO)) {
						cantModForzado = (int) Math
								.ceil(potDespachada[i] / gt.getPotenciaMaxima().getValor(instanteActual) - Constantes.EPSILONCOEF);
					}
					tcd.getCantModForzadop()[i] = cantModForzado;
				}
			}
		}

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		/** TODO, ACA HAY QUE HACER LAS ROTURAS **/

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		String valComp = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		boolean res = true;
		if ((valComp.equalsIgnoreCase(Constantes.TERDOSPASOS) && iter == 1))
			res = false;
		return res;

	}

	@Override
	public void inicializarParaEscenario() {
		GeneradorTermico participante = (GeneradorTermico) this.getParticipante();

		String valCompGen = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			participante.getCantModIni().cargarValorInicial();
		}
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);

	}

	/**
	 * En la heuróstica se mantiene la cantidad de módulos iniciales conectada.
	 * Si la cantidad de módulos a arrancar y parar son VC discretas exhaustivas
	 * no se requiere esta heuróstica porque se hacen las pruebas exhaustivas
	 */
	public void contribuirAS0fint() {

		GeneradorTermico gt = (GeneradorTermico) this.getParticipante();
		String valCompGen = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			VariableEstadoPar cantModini = gt.getCantModIni();
			cantModini.setEstadoS0fint(cantModini.getEstadoDespuesDeCDE());
		}
	}

	/**
	 * TODO: ATENCION 25 ENERO Cambia la VE del anterior por la VE de la
	 * optimización del generador
	 */
	public void contribuirAS0fintOptim() {
		GeneradorTermico gt = (GeneradorTermico) this.getParticipante();
		String valCompGen = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			VariableEstadoPar cantModiniOpt = gt.getCantModIniOpt();
			cantModiniOpt.setEstadoS0fint(cantModiniOpt.getEstadoDespuesDeCDE());
		}
	}

	public TermicoComp getCompG() {
		return compG;
	}

	public void setCompG(TermicoComp compG) {
		this.compG = compG;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN
	//////////////////////// ////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// TODO Auto-generated method stub

	}

	
	@Override
	/**
	 * Calcula el costo variable de OyM no incluye combustible
	 * @param salidaUltimaIter
	 * @return
	 */
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		/**
		 * El costo del combustible del tórmico se atribuye a los contratos de su combustible.
		 * Acó va sólo el variable
		 */
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		Double costo = 0.0;
		DatosEPPUnEscenario des = this.gt.getSimPaso().getDatosEscenario();
		//ArrayList<Double>  = des.getCurvOfertas().get(gt.getSimPaso().getPaso()).getVariables().get(this.gt.getNombre());
		
		for (int i = 0; i < gt.getCantPostes(); i++) {
			costo += salidaUltimaIter.getSolucion().get(tcd.getNpotp()[i]) * gt.getDuracionPostes(i) / Constantes.SEGUNDOSXHORA
					* gt.getCostoVariable().getValor(instanteActual);  // el costo variable estó en USD/MWh 
			
		}
	//	System.out.println("costo paso " + this.gt.getNombre() + " : " + costo);
		return costo;

	}


	
	/**
	 * Este mótodo se usa sólo para las salidas, para estimar un costo de cada combustible para el tórmico
	 * @param salidaUltimaIter
	 * @param instante instante del paso para valor de Evoluciones
	 * @return un costo para cada combustible
	 * Atribuye el costo total de combustible en proporción del consumo de
	 * cada central
	 */
	public double[] calculaCostoDeCombustiblesPaso(DatosSalidaProblemaLineal salidaUltimaIter, long instante) {
		double [] resultado = new double[gt.getCombustibles().size()];  
		Set<String> keyset = gt.getCombustibles().keySet();
		Iterator<String> it = keyset.iterator();
		int indCombus = 0;
		// Calcula el costo total y el caudal total de cada combustible de todos los tórmicos
		while (it.hasNext()) {
			double volumenTotalCombustible = 0;
			double costoTotalCombustible = 0;
			double volumenTotalEsteTer = 0;
			String nombComb = it.next();			
			RedCombustible redComb = gt.getSimPaso().getCorrida().getRedesCombustible().get(nombComb); 
			Combustible comb = redComb.getCombustible();
			Collection<ContratoCombustible> contratos = redComb.getContratos().values();
			
			Iterator<ContratoCombustible> itCont = contratos.iterator();
					
			while (itCont.hasNext()) {
				ContratoCombustible contrato = itCont.next();
				String nombVarControlCaudal = "";
				if(contrato instanceof ContratoCombustibleCanio){
					ContratoCombustibleCanio contratoCanio = (ContratoCombustibleCanio)contrato;				
					nombVarControlCaudal = contratoCanio.getCompD().getNCaudalComb();
					//resultado[indCombus] += salidaUltimaIter.getSolucion().get(nombVarControlCaudal) * contrato.getPrecio().getValor();
					if (salidaUltimaIter.getSolucion().get(nombVarControlCaudal)==null) {
						System.out.println("No se entontró variable del caudal de combustible" + nombVarControlCaudal);
						if (CorridaHandler.getInstance().isParalelo()){
						//	//PizarronRedis pp = new PizarronRedis();
							//pp.matarServidores();
						}
						System.exit(1);
					}
				}else{
					System.out.println("Error en clase de contrato " + contrato.getNombre());
					if (CorridaHandler.getInstance().isParalelo()){
						////PizarronRedis pp = new PizarronRedis();
						//pp.matarServidores();
					}
					System.exit(1);					
				}
				volumenTotalCombustible += salidaUltimaIter.getSolucion().get(nombVarControlCaudal) *gt.getDuracionPaso()/Constantes.SEGUNDOSXHORA;
				costoTotalCombustible += volumenTotalCombustible * contrato.costoMedio(instante);				
			}
			
			for (int ip= 0; ip < gt.getCantPostes(); ip++) {
				String nombreEnerTpc = tcd.getNenerTpc(ip, nombComb);
				volumenTotalEsteTer += salidaUltimaIter.getSolucion().get(nombreEnerTpc)/comb.getPci(); 				
			}
			if(volumenTotalCombustible==0){
				resultado[indCombus] = 0.0;
			}else{
				resultado[indCombus] = volumenTotalEsteTer/volumenTotalCombustible*costoTotalCombustible;
			}			
			indCombus++;			
		}

		return resultado;

	}
	
	
	/**
	 * Este mótodo se usa sólo para las salidas, para estimar para un térmico
	 * double[] volC;   // volumen de combustible usado en el paso para cada combustible en unidades de combustible
	 * double[] enerTC;  // energía térmica usada en el paso para cada combustible (MWh)
	 * double[][] enerTPC; // energía térmica por poste y por combustile, primer índice combustible (MWh)
	 * double[] enerEC;    // energía eléctrica generada en el paso para cada combustible (MWh)
	 * double[] costoC;   // costo variable en el paso para cada combustible (USD/MWh)
	 * double[][] volPC;   // volumen de combustible usado en el paso para cada poste para cada combustible, primer índice poste segundo índice combustible (unidades de comb.)
	 * double[][] potEPC;    // energía eléctrica generada en cada poste del paso para cada combustible, primer índice poste, segundo índice combustible (MWh)
	 *
	 * @param salidaUltimaIter
	 * @param instante instante del paso para valor de Evoluciones
	 * @return DatosTermicoSPAuxComb que junta los resultados de esos arrays
	 * 
	 * Se atribuye el costo total de combustible en proporción del consumo de
	 * cada central
	 */
	public DatosTermicoSPAuxComb calculaResultadosDeCombustiblesPaso(DatosSalidaProblemaLineal salidaUltimaIter, long instante) {
		ArrayList<String> listaComb = gt.getListaCombustibles();
		int cantP = gt.getCantPostes();
		int cantC = listaComb.size();
		double[] volC = new double[cantC];   
		double[] enerTC = new double[cantC];   
		double[][] enerTPC = new double[cantP][cantC];
		double[] enerEC = new double[cantC];       
		double[] costoC = new double[cantC];   ;  
		double[][] volPC = new double[cantP][cantC];   
		double[][] potEPC= new double[cantP][cantC];  
	
		double[] enerTP = new double[cantP];   // energía térmica total del poste
		int indCombus = 0;
		// Calcula el costo total y el caudal total de cada combustible de todos los tórmicos
		for(String nombComb: listaComb) {
			double volumenTotalCombustible = 0;
			double costoTotalCombustible = 0;
			double volumenTotalEsteTer = 0;	
			RedCombustible redComb = gt.getSimPaso().getCorrida().getRedesCombustible().get(nombComb); 
			Combustible comb = redComb.getCombustible();
			Collection<ContratoCombustible> contratos = redComb.getContratos().values();
			
			Iterator<ContratoCombustible> itCont = contratos.iterator();
					
			while (itCont.hasNext()) {
				ContratoCombustible contrato = itCont.next();
				String nombVarControlCaudal = "";
				if(contrato instanceof ContratoCombustibleCanio){
					ContratoCombustibleCanio contratoCanio = (ContratoCombustibleCanio)contrato;				
					nombVarControlCaudal = contratoCanio.getCompD().getNCaudalComb();
					//resultado[indCombus] += salidaUltimaIter.getSolucion().get(nombVarControlCaudal) * contrato.getPrecio().getValor();
					if (salidaUltimaIter.getSolucion().get(nombVarControlCaudal)==null) {
						System.out.println("No se entontró variable del caudal de combustible" + nombVarControlCaudal);
						if (CorridaHandler.getInstance().isParalelo()){
							////PizarronRedis pp = new PizarronRedis();
							//pp.matarServidores();
						}
						System.exit(1);
					}
				}else{
					System.out.println("Error en clase de contrato " + contrato.getNombre());
					if (CorridaHandler.getInstance().isParalelo()){
						////PizarronRedis pp = new PizarronRedis();
						//pp.matarServidores();
					}
					System.exit(1);					
				}
				volumenTotalCombustible += salidaUltimaIter.getSolucion().get(nombVarControlCaudal) *gt.getDuracionPaso()/Constantes.SEGUNDOSXHORA;
				costoTotalCombustible += volumenTotalCombustible * contrato.costoMedio(instante);				
			}						
			for (int ip= 0; ip < gt.getCantPostes(); ip++) {
				String nombreEnerTpc = tcd.getNenerTpc(ip, nombComb);
				double ener = salidaUltimaIter.getSolucion().get(nombreEnerTpc);
				enerTPC[ip][indCombus] = ener;
				volPC[ip][indCombus] = ener/comb.getPci();
				volumenTotalEsteTer += ener/comb.getPci();
				enerTC[indCombus] += ener;
				enerTP[ip] += ener;
			}
			if(volumenTotalCombustible==0){
				costoC[indCombus] =0.0;
				volC[indCombus] = 0.0;
			}else{
				volC[indCombus] = volumenTotalEsteTer;
				costoC[indCombus] = (volumenTotalEsteTer/volumenTotalCombustible)*costoTotalCombustible;
			}				
			indCombus++;			
		}
		
		indCombus = 0;
		for(String nombComb: listaComb) {
			for (int ip= 0; ip < gt.getCantPostes(); ip++) {
				String nombreVarPot = tcd.generarNombre("pot", Integer.toString(ip));
				double potp = salidaUltimaIter.getSolucion().get(nombreVarPot);
				if(enerTP[ip]==0.0){
					potEPC[ip][indCombus]=0.0;
				}else{
					potEPC[ip][indCombus] = potp*(enerTPC[ip][indCombus]/enerTP[ip]);
					enerEC[indCombus] += potp*(enerTPC[ip][indCombus]/enerTP[ip])*gt.getDuracionPostes(ip)/utilitarios.Constantes.SEGUNDOSXHORA;					
				}
			}
			indCombus++;
		}
		DatosTermicoSPAuxComb resultado = new DatosTermicoSPAuxComb(volC, enerTC, enerTPC, enerEC, costoC, volPC, potEPC);
		return resultado;
	}


	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		// TODO: OJOJOJOJO VER ACA NO ENTEND
		cargarDatosCompDespacho(instante);
//		System.out.println("Generador: "+ this.gt.getNombre() + " CantModDisp:" + this.gt.getCantModDisp().getValor() );
	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub
		
	}

	////////////////////////////////////////////////////////////////////////////////

}
